package org.neuinfo.rdw

import grails.test.spock.IntegrationSpec

import org.neuinfo.rdw.classification.text.PublisherResourceClassifier
import org.neuinfo.rdw.classification.text.PublisherResourceConfig
import org.neuinfo.rdw.classification.text.PublisherResourcePrediction
import org.neuinfo.rdw.data.model.PublisherResourceInfo

class PublisherResourceRefServiceSpec extends IntegrationSpec {
	def publisherResourceRefService
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

	def setup() {
	}

	def cleanup() {
	}

	void "test PublisherResourceClassifier"() {
		def psaiList = null
		List<PublisherResourceInfo> priList = new ArrayList<PublisherResourceInfo>()
		when: "training features are generated"
		psaiList = publisherResourceRefService.getPubSearchAnnotationInfos()
		psaiList.each { PubSearchAnnotationInfo psai ->
			PaperReference pp = psai.paperRef
			PublisherResourceInfo pri = new PublisherResourceInfo(pp.id)
			pri.label = psai.label
			pri.genre = pp.genre
			pri.title = pp.title
			pri.publicationName = pp.publicationName
			pri.resourceName = pp.registry.resourceName
			priList.add(pri)
		}
		File workDir = new File("/tmp/pr_filter")
		workDir.mkdir()
		PublisherResourceConfig config = new PublisherResourceConfig(workDir.absolutePath)
		PublisherResourceClassifier prc = new PublisherResourceClassifier(config)

		prc.prepTrainingFeatures(priList)
		prc.train()

		def prList = publisherResourceRefService.getPaperRefsForClassification()
		List<PublisherResourceInfo> testPriList = new ArrayList<PublisherResourceInfo>( prList.size())
		prList.each { PaperReference pr ->
			PublisherResourceInfo pri = new PublisherResourceInfo(pr.id as int)
			pri.genre = pr.genre
			pri.title = pr.title
			pri.publicationName = pr.publicationName
			pri.resourceName = pr.registry.resourceName
			testPriList.add(pri)
		}
		prc.prepTestingFeatures(testPriList)
		List<PublisherResourcePrediction> predList = prc.runClassifier(testPriList, -1, null)
		Collections.sort(predList)
		int count = 0
		for(PublisherResourcePrediction pred: predList) {
			println "${pred.pri.title} [${pred.score}]"
			PaperReference.executeUpdate("update PaperReference p set p.cScore = :score where p.id = :id",
					[score:pred.score as double, id: pred.pri.id as long])
			count++
			if (count % 100 ==0) {
				cleanUpGORM()
				println "# of predictions so far:$count"
			}
		}

		then:"it should work"
		!priList.isEmpty()
	}
	
	private void cleanUpGORM() {
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
		propertyInstanceMap.get().clear()
	}
}