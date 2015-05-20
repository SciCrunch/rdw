package org.neuinfo.rdw

import org.neuinfo.rdw.classification.text.NERResourceClassifier;
import org.neuinfo.rdw.classification.text.NERResourceConfig;
import org.neuinfo.rdw.classification.text.NERResourcePrediction;
import org.neuinfo.rdw.data.model.NERResourceInfo;

import grails.test.spock.IntegrationSpec

class NerResourceRefServiceSpec extends IntegrationSpec {
    def nerResourceRefService
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	
	def setup() {
	}

	def cleanup() {
	}

	void "test NERResourceClassifier"() {
		def naiList = null
		List<NERResourceInfo> nriList = new ArrayList<NERResourceInfo>()
		when: "training features are generated"
		naiList = nerResourceRefService.getNERAnnotInfo()
		naiList.each { NERAnnotationInfo nai ->
			NERResourceInfo nri = new NERResourceInfo(nai.resourceRec.id)
			nri.label = nai.label
			nri.context = nai.resourceRec.context
			nri.entity = nai.resourceRec.entity
			nri.resourceName = nai.resourceRec.registry.resourceName
			nriList.add(nri)	
		}
	
		File workDir = new File("/tmp/nr_filter")
		workDir.mkdir()
		NERResourceConfig config = new NERResourceConfig(workDir.absolutePath)
		NERResourceClassifier nrc = new NERResourceClassifier(config)
		
		nrc.prepTrainingFeatures(nriList)
		nrc.train()
		def rrList = nerResourceRefService.getResourceRecsWithContext()
		List<NERResourceInfo> testNriList = new ArrayList<NERResourceInfo>( rrList.size())
		rrList.each { ResourceRec rr ->
			NERResourceInfo nri = new NERResourceInfo(rr.id as int)
			nri.context = rr.context
			nri.entity = rr.entity
			testNriList.add(nri)
		}
		nrc.prepTestingFeatures(testNriList)
		List<NERResourcePrediction> predList = nrc.runClassifier(testNriList, -1, null)
		Collections.sort(predList)
		int count = 0
		for(NERResourcePrediction pred : predList) {
			println "${pred.nri.context} [${pred.score}]"
			ResourceRec.executeUpdate("update ResourceRec r set r.cScore = :score where r.id = :id",
				[score:pred.score as double, id: pred.nri.id as long])
			count++;
			if (count % 100 == 0) {
				cleanUpGORM()
				println "# of predictions so far:$count"
			}
		}
	
		then:"it should work"
		!nriList.isEmpty()
	}
	
	private void cleanUpGORM() {
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
		propertyInstanceMap.get().clear()
	}
}