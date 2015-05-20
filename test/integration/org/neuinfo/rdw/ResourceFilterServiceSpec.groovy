package org.neuinfo.rdw

import spock.lang.Ignore
import grails.test.spock.IntegrationSpec
import org.neuinfo.rdw.ResourceFilterService
import org.neuinfo.rdw.classification.text.Config;
import org.neuinfo.rdw.classification.text.Prediction;
import org.neuinfo.rdw.classification.text.RCTypeConfig;
import org.neuinfo.rdw.classification.text.ResourceCandidateClassifier;
import org.neuinfo.rdw.classification.text.ResourceCandidateTypeClassifier;
import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

class ResourceFilterServiceSpec extends IntegrationSpec {
	def resourceFilterService
	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

	def setup() {
	}

	def cleanup() {
	}

	@Ignore
	void "test getUrlsWithDescription"() {
		String batchId = null
		def urList = []
		given: "a batch id and score threshold"
		batchId = '201310'
		int scoreTh = 5
		when: "getUrlsWithDescription is called"
		urList = resourceFilterService.getUrlsWithDescription(batchId, scoreTh)
		urList.each { println it }
		then: "you should get some results"
		urList != null
	}

	@Ignore
	void "test fixUrlsWithDuplicateDescriptions"() {
		String batchId = null
		def urList = []
		given: "a batch id and score threshold"
		batchId = '201310'
		int scoreTh = 5
		when: "fixUrlsWithDuplicateDescriptions is called"
		urList = resourceFilterService.fixUrlsWithDuplicateDescriptions(batchId, scoreTh)
		urList.each { println it }
		then: "it should work"
		urList != null
	}

	@Ignore
	void "test ResourceCandidateClassifier"() {
		String batchId = null
		def uaiList = []
		List<ResourceCandidateInfo> rciList = new ArrayList<ResourceCandidateInfo>()
		given: "a batch id"
		batchId = '201310'
		when: "training features are generated"
		uaiList =  resourceFilterService.getURLWithAnnotInfo(batchId)
		uaiList.each { uai ->
			ResourceCandidateInfo rci = new ResourceCandidateInfo(uai.url.id as int)

			rci.label = uai.label
			rci.description = uai.url.description
			rci.score = uai.url.score as float
			rci.url = uai.url.url
			rciList.add(rci)
		}

		File workDir = new File("/tmp/rc_filter")
		workDir.mkdir()
		Config config = new Config(workDir.absolutePath)
		ResourceCandidateClassifier rcc = new ResourceCandidateClassifier(config)

		rcc.prepTrainingFeatures(rciList)
		rcc.train()

		def urList = resourceFilterService.getUrlsWithDescription(batchId, 10, null);
		List<ResourceCandidateInfo> testRciList = new ArrayList<ResourceCandidateInfo>(urList.size())
		urList.each { Urls ur ->
			ResourceCandidateInfo rci = new ResourceCandidateInfo(ur.id as int)
			rci.description = ur.description
			rci.score = ur.score
			rci.url = ur.url
			testRciList.add(rci)
		}
		rcc.prepTestingFeatures(testRciList)
		List<Prediction> predList = rcc.runClassifier(testRciList, -1, null)
		Collections.sort(predList)
		int count = 0
		for(Prediction pred : predList) {
			println "${pred.rci.url}  [${pred.score}]"
			Urls.executeUpdate('update Urls u set u.cScore = :score where u.id = :id',
					[score:pred.score as double, id:pred.rci.id as long])
			count++;
			if (count % 100 == 0) {
				def session = sessionFactory.currentSession
				session.flush()
				session.clear()
				propertyInstanceMap.get().clear()
			}
			/*
			 Urls ur = Urls.get(pred.rci.id)
			 if (ur) {
			 ur.cScore = pred.score
			 ur.save(failOnError:true, flush:true)
			 }
			 */
		}
		then:"it should work"
		!rciList.isEmpty()
	}

	@Ignore
	void "test ResourceCandidateTypeClassifying"() {
		def batchIds = null
		int totalCount = 0
		given: "a batch id"
		batchIds = [
			'201310',
			'201312',
			'201401',
			'201402',
			'201403'
		]
		when: "urls with registry matching from all batches are classified"
		File workDir = new File("/tmp/rctype_filter")
		workDir.mkdir()
		RCTypeConfig config = new RCTypeConfig(workDir.absolutePath)
		ResourceCandidateTypeClassifier rcct = new ResourceCandidateTypeClassifier(config)
		for(String batchId : batchIds) {
			println "batchId:$batchId"
			def urList = resourceFilterService.getUrlsWithDescription(batchId, 10, null);
			List<ResourceCandidateInfo> testRciList = new ArrayList<ResourceCandidateInfo>(urList.size())
			urList.each { Urls ur ->
				ResourceCandidateInfo rci = new ResourceCandidateInfo(ur.id as int)
				rci.description = ur.description
				rci.score = ur.score
				rci.url = ur.url
				testRciList.add(rci)
			}
			rcct.prepTestingFeatures(testRciList)
			List<Prediction> predList = rcct.runClassifier(testRciList)
			Collections.sort(predList)
			Collections.reverse(predList)
			int count = 0
			for(Prediction pred : predList) {
				if (pred.score > 0.8) {
					println "${pred.rci.url}  [${pred.rci.resourceType} / ${pred.score}]"
					Urls.executeUpdate('update Urls u set u.resourceTypeSource = 2, u.resourceType =:rt where u.id = :id',
							[rt: pred.rci.resourceType, id:pred.rci.id as long])
					count++;
					totalCount++;
				}
				if (count % 100 == 0) {
					def session = sessionFactory.currentSession
					session.flush()
					session.clear()
					propertyInstanceMap.get().clear()
				}
			}
		}
		then:"it should work"
		totalCount > 0
	}

	void "test ResourceCandidateTypeClassifier"() {
		// String batchId = null
		int totalCount = 0
		def batchIds = [
			'201310',
			'201312',
			'201401',
			'201402',
			'201403'
		]
		def uaiList = []
		List<ResourceCandidateInfo> rciList = new ArrayList<ResourceCandidateInfo>()
		given: "a list of batch ids"
		//batchId = '201310'
		when: "training features are generated"
		uaiList =  resourceFilterService.getUrlWithAnnotInfoAndType()
		uaiList.each { URLAnnotationInfo uai ->
			try {
				ResourceCandidateInfo rci = new ResourceCandidateInfo(uai.url.id as int)

				rci.label = uai.label
				rci.description = uai.url.description
				rci.score = uai.url.score as float
				rci.url = uai.url.url
				rci.resourceType = uai.resourceType
				rciList.add(rci)
			} catch(Throwable t) {
				t.printStackTrace()
			}
		}

		File workDir = new File("/tmp/rctype_filter")
		workDir.mkdir()
		RCTypeConfig config = new RCTypeConfig(workDir.absolutePath)
		ResourceCandidateTypeClassifier rcct = new ResourceCandidateTypeClassifier(config)

		rcct.prepTrainingFeatures(rciList)
		rcct.train()
		// remove any previous resource type predictions
		Urls.executeUpdate('update Urls u set u.resourceType = null, u.resourceTypeSource = 0 where u.resourceTypeSource = 2');
		for(String batchId : batchIds) {
			println "batchId:$batchId"
			def urList = resourceFilterService.getUrlsWithDescription(batchId, 10, null);
			List<ResourceCandidateInfo> testRciList = new ArrayList<ResourceCandidateInfo>(urList.size())
			urList.each { Urls ur ->
				ResourceCandidateInfo rci = new ResourceCandidateInfo(ur.id as int)
				rci.description = ur.description
				rci.score = ur.score
				rci.url = ur.url
				testRciList.add(rci)
			}
			rcct.prepTestingFeatures(testRciList)
			List<Prediction> predList = rcct.runClassifier(testRciList)
			Collections.sort(predList)
			Collections.reverse(predList)
			int count = 0
			for(Prediction pred : predList) {
				if (pred.score > 0.8) {
					println "${pred.rci.url}  [${pred.rci.resourceType} / ${pred.score}]"
					Urls.executeUpdate('update Urls u set u.resourceTypeSource = 2, u.resourceType =:rt where u.id = :id',
							[rt: pred.rci.resourceType, id:pred.rci.id as long])
					count++;
					totalCount++
				}
				if (count % 100 == 0) {
					def session = sessionFactory.currentSession
					session.flush()
					session.clear()
					propertyInstanceMap.get().clear()
				}
			}
		}
		then:"it should work"
		!rciList.isEmpty()
		totalCount > 0
	}
}
