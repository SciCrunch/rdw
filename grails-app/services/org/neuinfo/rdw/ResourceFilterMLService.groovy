package org.neuinfo.rdw

import org.hibernate.ScrollMode;
import org.neuinfo.rdw.classification.text.Config;
import org.neuinfo.rdw.classification.text.NERResourceClassifier;
import org.neuinfo.rdw.classification.text.NERResourceConfig;
import org.neuinfo.rdw.classification.text.NERResourcePrediction;
import org.neuinfo.rdw.classification.text.Prediction;
import org.neuinfo.rdw.classification.text.PublisherResourceClassifier;
import org.neuinfo.rdw.classification.text.PublisherResourceConfig;
import org.neuinfo.rdw.classification.text.PublisherResourcePrediction;
import org.neuinfo.rdw.classification.text.ResourceCandidateClassifier;
import org.neuinfo.rdw.data.model.NERResourceInfo;
import org.neuinfo.rdw.data.model.PublisherResourceInfo;
import org.neuinfo.rdw.data.model.ResourceCandidateInfo;

class ResourceFilterMLService {
    def resourceFilterService
    def nerResourceRefService
    def publisherResourceRefService
    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def getBatchIds() {
        return Urls.executeQuery("select distinct(batchId) from Urls")
    }

    def trainResourceCandidateClassifier(batchIds) {
        def uaiList = []
        List<ResourceCandidateInfo> rciList = new ArrayList<ResourceCandidateInfo>()
        uaiList = resourceFilterService.getURLWithAnnotInfo(batchIds)
        uaiList.each { uai ->
            ResourceCandidateInfo rci = new ResourceCandidateInfo(uai.url.id as int)

            rci.label = uai.label
            rci.description = uai.url.description

            rci.score = uai.url.score != null ? uai.url.score as float : 0f
            rci.url = uai.url.url
            rciList.add(rci)
        }
        File workDir = new File("/tmp/rc_filter")
        workDir.mkdir()
        Config config = new Config(workDir.absolutePath)
        ResourceCandidateClassifier rcc = new ResourceCandidateClassifier(config)

        rcc.prepTrainingFeatures(rciList)
        rcc.train()
        saveJobLog(batchIds.join(','), "rc_train")
    }

    def trainNERResourceClassifier() {
        List<NERResourceInfo> nriList = new ArrayList<NERResourceInfo>()
        def naiList = nerResourceRefService.getNERAnnotInfo()
        naiList.each { NERAnnotationInfo nai ->
            NERResourceInfo nri = new NERResourceInfo(nai.resourceRec.id as int)
            nri.label = nai.label
            nri.context = nai.resourceRec.context
            nri.entity = nai.resourceRec.entity
            nri.resourceName = nai.resourceRec.registry.resourceName
            nriList.add(nri)
        }

        File workDir = new File("/tmp/nr_filter")
        workDir.mkdir()
        println "nriList:" + nriList
        NERResourceConfig config = new NERResourceConfig(workDir.absolutePath)
        NERResourceClassifier nrc = new NERResourceClassifier(config)

        nrc.prepTrainingFeatures(nriList)
        nrc.train()
        saveJobLog('all', 'ner_train')
    }

    def trainPublisherResourceClassifier() {
        List<PublisherResourceInfo> priList = new ArrayList<PublisherResourceInfo>()
        def psaiList = publisherResourceRefService.getPubSearchAnnotationInfos()

        psaiList.each { PubSearchAnnotationInfo psai ->
            PaperReference pp = psai.paperRef
            PublisherResourceInfo pri = new PublisherResourceInfo(pp.id as int)
            pri.label = psai.label
            pri.genre = pp.genre
            pri.title = pp.title
            pri.publicationName = pp.publicationName
            pri.resourceName = pp.registry.resourceName
            pri.description = pp.paperRef.description
            pri.meshHeadings = pp.paperRef.meshHeadings
            priList.add(pri)
        }
        File workDir = new File("/tmp/pr_filter")
        workDir.mkdir()
        new File(workDir, 'svm').mkdir()
        PublisherResourceConfig config = new PublisherResourceConfig(workDir.absolutePath)
        PublisherResourceClassifier prc = new PublisherResourceClassifier(config)

        prc.prepTrainingFeatures(priList)
        prc.train()
        saveJobLog('all', 'pr_train')
    }

    private def saveJobLog(String batchId, String op) {
        JobLog jl = new JobLog(batchId: batchId, operation: op, modTime: new Date(),
                status: "finished", modifiedBy: 'ResourceFilterMLService')
        jl.save(failOnError: true, flush: true)
    }

    def classifyResourceCandidates(batchIds, int scoreTh) {
        File workDir = new File("/tmp/rc_filter")
        assert workDir.isDirectory()
        Config config = new Config(workDir.absolutePath)
        ResourceCandidateClassifier rcc = new ResourceCandidateClassifier(config)

        def urList = resourceFilterService.getUrlsWithDescription(batchIds, scoreTh, null);
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
        int count = 0;
        for (Prediction pred : predList) {
            println "${pred.rci.url}  [${pred.score}]"
            Urls.executeUpdate('update Urls u set u.cScore = :score where u.id = :id',
                    [score: pred.score as double, id: pred.rci.id as long])
            count++;
            if (count % 100 == 0) {
                cleanUpGORM()
            }
        }
        saveJobLog(batchIds.join(','), "rc_classify")
    }

    def classifyNERResources() {
        File workDir = new File("/tmp/nr_filter")
        assert workDir.isDirectory()
        NERResourceConfig config = new NERResourceConfig(workDir.absolutePath)
        NERResourceClassifier nrc = new NERResourceClassifier(config)

        def rrList = nerResourceRefService.getResourceRecsWithContext()
        List<NERResourceInfo> testNriList = new ArrayList<NERResourceInfo>(rrList.size())
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
        for (NERResourcePrediction pred : predList) {
            println "${pred.nri.context} [${pred.score}]"
            ResourceRec.executeUpdate("update ResourceRec r set r.cScore = :score where r.id = :id",
                    [score: pred.score as double, id: pred.nri.id as long])
            count++;
            if (count % 100 == 0) {
                cleanUpGORM()
            }
        }
        saveJobLog('all', 'ner_classify')
    }

    def classifyPublisherResources() {
        File workDir = new File("/tmp/pr_filter")
        assert workDir.isDirectory()
        PublisherResourceConfig config = new PublisherResourceConfig(workDir.absolutePath)
        PublisherResourceClassifier prc = new PublisherResourceClassifier(config)
        // def prList = publisherResourceRefService.getPaperRefsForClassification()
        List<PublisherResourceInfo> testPriList = new ArrayList<PublisherResourceInfo>()

        PaperReference.withSession { session ->
            def rs = session.createQuery(
                    "select p.id, p.genre, p.title, p.publicationName, p.registry.resourceName, p.description, p.meshHeadings from PaperReference p where p.flags = 1 and p.registry is not null and p.title is not null")
                    .scroll(ScrollMode.FORWARD_ONLY)
            int count = 0
            while (rs.next()) {
                println rs.get(0)
                int id = rs.get(0) as int

                PublisherResourceInfo pri = new PublisherResourceInfo(id)
                pri.genre = rs.get(1) ?: 'unknown'
                pri.title = rs.get(2) //pr.title
                pri.publicationName = rs.get(3) // pr.publicationName
                pri.resourceName = rs.get(4) // pr.registry.resourceName
                pri.description = rs.get(5)
                pri.meshHeadings = rs.get(6)

                testPriList.add(pri)
                count++
                if (count % 1000 == 0) {
                    println "read so far: $count"
                    handlePRClassification(prc, testPriList)
                    testPriList.clear()
                }
            }
        }
        /*
         prc.prepTestingFeatures(testPriList)
         List<PublisherResourcePrediction> predList = prc.runClassifier(testPriList, -1, null)
         Collections.sort(predList)
         int count = 0
         for(PublisherResourcePrediction pred: predList) {
         println "${pred.pri.title} [${pred.score}]"
         PaperReference.executeUpdate("update PaperReference p set p.cScore = :score where p.id = :id",
         [score:pred.score as double, id: pred.pri.id as long])
         count++
         if (count % 100 == 0) {
         cleanUpGORM()
         println "# of predictions so far:$count"
         }
         }
         */
        // saveJobLog('all','pr_classify')
    }

    private def handlePRClassification(PublisherResourceClassifier prc, List<PublisherResourceInfo> testPriList) {
        prc.prepTestingFeatures(testPriList)
        List<PublisherResourcePrediction> predList = prc.runClassifier(testPriList, -1, null)
        Collections.sort(predList)
        int count = 0
        for (PublisherResourcePrediction pred : predList) {
            println "${pred.pri.title} [${pred.score}]"
            PaperReference.executeUpdate("update PaperReference p set p.cScore = :score where p.id = :id",
                    [score: pred.score as double, id: pred.pri.id as long])
            count++
            if (count % 100 == 0) {
                cleanUpGORM()
                println "# of predictions so far:$count"
            }
        }
    }


    private void cleanUpGORM() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }
}
