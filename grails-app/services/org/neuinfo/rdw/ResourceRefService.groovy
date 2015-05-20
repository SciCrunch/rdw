package org.neuinfo.rdw

import org.h2.engine.SessionFactory;
import org.hibernate.ScrollMode;

import groovy.xml.MarkupBuilder

class ResourceRefService {
    def sessionFactory

    def getBatchIds() {
        return JobLog.executeQuery("select distinct(batchId) from JobLog where status = :status and operation = :op",
                [status: 'finished', op: 'pmc_resource_ref'])
    }

    def getPMIDs4Resource(String nifId, int max, int offset) {
        //	def rows = Urls.executeQuery("select p.pubmedId from Urls u inner join u.registry as r inner join u.paper as p where r.nifId = :nifId",
        //			[nifId: nifId, max: max, offset:offset])

        def rows = CombinedResourceRef.executeQuery(
                "select c.pubmedId from CombinedResourceRef c where c.nifId = :nifId order by confidence desc, c.pubmedId desc",
                [nifId: nifId, max: max, offset: offset])
        return rows
    }


    int getPMIDCount4Resource(String nifId) {
        //def list =  Urls.executeQuery(
        //		"select count(p.pubmedId) from Urls u inner join u.registry as r inner join u.paper as p where r.nifId = :nifId",
        //		[nifId: nifId]);
        def list = CombinedResourceRef.executeQuery(
                "select count(c.pubmedId) from CombinedResourceRef c where c.nifId = :nifId", [nifId: nifId])
        return list[0] as int
    }

    def getContext4PMID(String pmid, String nifId) {
        def rows = Urls.executeQuery(
                "select u.context, p.title, p.journalTitle from Urls u inner join u.registry as r inner join u.paper as p where r.nifId = :nifId and p.pubmedId =:pmid",
                [nifId: nifId, pmid: pmid])
        def nerRows = []
        def publisherRows = []
        if (!rows) {
            nerRows = ResourceRec.executeQuery("select a.context, p.title, p.journalTitle from ResourceRec a inner join a.registry r inner join a.paper p " +
                    "where r.nifId = :nifId and p.pubmedId =:pmid", [nifId: nifId, pmid: pmid])
        }
        if (!nerRows) {
            println "checking publisher records"
            publisherRows = PaperReference.executeQuery("select a.title, a.publicationName from PaperReference a inner join a.registry r " +
                    "where r.nifId = :nifId and a.pubmedId =:pmid",
                    [nifId: nifId, pmid: pmid])
        }

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.result() {
            for (row in rows) {
                paper(PMID: pmid, nifId: nifId) {
                    context(row[0])
                    title(row[1])
                    journalTitle(row[2])
                }
            }
            for (row in nerRows) {
                paper(PMID: pmid, nifId: nifId) {
                    context(row[0])
                    title(row[1])
                    journalTitle(row[2])
                }
            }
            for (row in publisherRows) {
                paper(PMID: pmid, nifId: nifId) {
                    context()
                    title(row[0])
                    journalTitle(row[1])
                }
            }
        }
        return writer.toString()
    }

    /**
     *
     * @param pmid
     * @param resourceNifId
     * @param good
     * @return
     */
    def addURLAnnotInfoWS(String pmid, String resourceNifId, String label, String modUser = 'admin') {
        def reg = Registry.findByNifId(resourceNifId)
        if (reg == null) {
            return null
        }

        def urList = Urls.executeQuery("from Urls u where u.registry.id = :rid and u.paper.pubmedId = :pmid",
                [rid: reg.id, pmid: pmid])
        if (urList && reg) {
            Urls ur = urList[0]

            def uai = URLAnnotationInfo.findByUrl(ur)
            if (uai) {
                uai.registry = reg
                uai.modifiedBy = modUser
                uai.label = label
                uai.save(failOnError: true, flush: true)
                return uai
            } else {
                uai = new URLAnnotationInfo(url: ur, label: label,
                        modTime: new Date(),
                        modifiedBy: modUser, opType: 'candidate_filter')
                uai.registry = reg
                uai.save(failOnError: true, flush: true)
                return uai
            }
        }
        return null
    }

    def addPaperWS(String pmid, String resourceNifId, String title = null, String journalTitle = null) {
        def reg = Registry.findByNifId(resourceNifId)
        if (reg == null) {
            return null
        }
        def paperList = Paper.executeQuery(
                "from Paper p where p.pubmedId = :pmid", [pmid: pmid])
        if (!paperList && reg) {
            Paper p = new Paper(pubmedId: pmid, filePath: 'none', title: title, journalTitle: journalTitle)
            p.save(failOnError: true, flush: true)
            Urls ur = new Urls(url: 'none', paper: p, registry: reg, batchId: 'none', flags: 4)
            ur.save(failOnError: true, flush: true)
            return ur
        }
        return null
    }

    def removePaperWS(String pmid, String resourceNifId) {
        def reg = Registry.findByNifId(resourceNifId)
        if (reg == null) {
            return null
        }
        def paperList = Paper.executeQuery(
                "from Paper p where p.pubmedId = :pmid", [pmid: pmid])
        if (paperList?.size() == 1 && reg) {
            Paper paper = paperList[0]
            def urList = Urls.executeQuery("from Urls u where u.paper.id = :pid", [pid: paper.id])
            if (urList?.size() == 1) {
                Urls ur = urList[0]
                ur.delete(failOnError: true, flush: true)
            }
            paper.delete(failOnError: true, flush: true)
        }
        return null
    }

    def getUrlsWithRegistry(String batchId, int max, int offset, String filterType = null, String value = null) {
        def urList = null
        if (!filterType || value == '*') {
            urList = Urls.executeQuery("from Urls u where u.batchId = :batchId " +
                    "and u.registry is not null order by u.registry.id",
                    [batchId: batchId, max: max, offset: offset])
        } else {
            if (filterType == 'name') {
                urList = Urls.executeQuery("from Urls u where u.batchId = :batchId " +
                        "and u.registry.resourceName = :name",
                        [batchId: batchId, name: value, max: max, offset: offset])
            } else {
                urList = Urls.executeQuery("from Urls u where u.batchId = :batchId " +
                        "and u.registry.nifId = :nifId",
                        [batchId: batchId, nifId: value, max: max, offset: offset])
            }
        }
        return urList
    }

    def getUrlsWithRegistryFiltered(String batchId, registryIds, int max, int offset) {
        def urList = Urls.executeQuery("from Urls u where u.batchId = :batchId " +
                "and u.registry.id in (:ids) order by u.registry.id",
                [batchId: batchId, ids: registryIds, max: max, offset: offset])
        return urList
    }

    def getCountForUrlsWithRegistryFiltered(String batchId, registryIds) {
        def list = Urls.executeQuery("select count(u.id) from Urls u where u.batchId = :batchId " +
                "and u.registry.id in (:ids)", [batchId: batchId, ids: registryIds])
        return list[0] as int
    }

    def getCountForUrlsWithRegistry(String batchId, String filterType = null, String value = null) {
        def list = null

        if (!filterType || value == '*') {
            list = Urls.executeQuery("select count(u.id) from Urls u where u.batchId = :batchId " +
                    "and u.registry is not null", [batchId: batchId])
        } else {
            if (filterType == 'name') {
                list = Urls.executeQuery("select count(u.id) from Urls u where u.batchId = :batchId " +
                        "and u.registry.resourceName = :name", [batchId: batchId, name: value])
            } else {
                list = Urls.executeQuery("select count(u.id) from Urls u where u.batchId = :batchId " +
                        "and u.registry.nifId = :nifId", [batchId: batchId, nifId: value])
            }
        }
        return list[0] as int
    }

    def dumpResourceRefs(int maxCount) {
        def urList = null
        if (maxCount <= 0) {
            urList = Urls.executeQuery("from Urls u where u.registry is not null")
        } else {
            urList = Urls.executeQuery("from Urls u where u.registry is not null", [max: maxCount])
        }
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.resourceRefs(count: urList.size()) {
            for (Urls ur : urList) {
                row() {
                    url(ur.url)
                    pmid(ur.paper.pubmedId)
                    batchId(ur.batchId)
                    resourceName(ur.registry.resourceName)
                    nifId(ur.registry.nifId)
                }
            }
        }
        return writer.toString()
    }

    def dumpResourceRefs2(int maxCount) {
        def urList = null
        String query = """
               select u.url, u.batchId , p.pubmedId, r.resourceName, r.nifId from Urls 
               u inner join u.registry as r inner join u.paper as p
               """
        if (maxCount <= 0) {
            urList = Urls.executeQuery(query)
        } else {
            urList = Urls.executeQuery(query, [max: maxCount])
        }
        def nerList = null
        String nerQuery = """
              select p.pubmedId, r.resourceName, r.nifId from ResourceRec n
              inner join n.registry as r inner join n.paper as p
              where n.cScore >= 0.5 and n.id not in
              (select na.resourceRec.id from NERAnnotationInfo na where na.label = 'bad')
           """


        if (maxCount <= 0) {
            nerList = ResourceRec.executeQuery(nerQuery)
        } else {
            nerList = ResourceRec.executeQuery(nerQuery, [max: maxCount])
        }
        println "urlList:" + urList.size()
        println "nerList:" + nerList.size()
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        int count = urList.size() + nerList.size()
        println "count:$count"

        xml.resourceRefs(count: count) {
            for (r in urList) {
                row() {
                    url(r[0])
                    pmid(r[2])
                    batchId(r[1])
                    resourceName(r[3])
                    nifId(r[4])
                }
            }
            for(r in nerList) {
                row() {
                    url('')
                    pmid(r[0])
                    batchId('')
                    resourceName(r[1])
                    nifId(r[2])
                }
            }
        }
        return writer.toString()
    }

    def getInvalidResources2() {
        def dssList = DownSiteStatus.executeQuery("from DownSiteStatus d where (d.label is null or d.label <> 'bad') order by d.id")
        println "dssList.size: " + dssList.size()
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.downSites(count: dssList.size()) {
            for (DownSiteStatus dss in dssList) {
                row() {
                    nifId(dss.nifId)
                    resource(dss.resourceName)
                    url(dss.url)
                    lastCheckedTime(dss.lastCheckedTime)
                    message(dss.message)
                    numConsecutiveChecks(dss.numOfConsecutiveChecks)
                }
            }
        }
        return writer.toString()
    }

    /**
     * all resources that were invalid for three or more consecutive checks
     * @return
     */
    def getInvalidResources() {
        def vsList = ValidationStatus.executeQuery("select v.registryId, v.isUp, v.message, v.lastCheckedTime " +
                "from ValidationStatus v order by v.registryId, v.lastCheckedTime desc")
        def regList = Registry.executeQuery("select r.id, r.nifId, r.resourceName, r.url from Registry r")
        def regMap = [:]
        regList.each { r ->
            regMap[r[0] as String] = r
        }
        def vssMap = [:]
        vsList.each { vs ->
            ValidationStatusStat vss = vssMap[(vs[0])]
            if (!vss) {
                vss = new ValidationStatusStat(vs: vs)
                vssMap[(vs[0])] = vss
            }
            vss.upList << vs[1]
        }
        def vssList = []
        vssMap.values().each { ValidationStatusStat vss ->
            if (vss.isInvalid()) {
                String msg = vss.vs[2]
                boolean ok = true
                // if temporary problems or authentication issues assume the site is valid
                if (msg) {
                    msg = msg.toLowerCase()
                    if (msg.indexOf('peer not authenticated') != -1 || msg.indexOf('temporary failure in name resolution') != -1) {
                        ok = false
                    }
                    if (ok) {
                        def m = msg =~ /connection\s+.+\s+refused/
                        if (m) {
                            ok = false
                        }
                    }
                }
                if (ok) {
                    vssList << vss
                }
            }
        }
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.downSites(count: vssList.size()) {
            for (ValidationStatusStat vss in vssList) {
                def key = vss.vs[0]
                def reg = regMap[key as String]
                assert reg
                row() {
                    nifId(reg[1])
                    resource(reg[2])
                    url(reg[3])
                    lastCheckedTime(vss.vs[3])
                    message(vss.vs[2])
                    numConsecutiveChecks(vss.consecutiveCount)
                }
            }
        }
        return writer.toString()
    }

    static class ValidationStatusStat {
        def vs
        def upList = []
        int consecutiveCount = 0

        def isInvalid() {
            if (upList && upList[0]) {
                return false
            }
            int count = 0
            for (int i in 0..<upList.size) {
                if (!upList[i]) {
                    if (i == 0 || !upList[i - 1]) {
                        count++;
                    } else if (i != 0) {
                        break
                    }
                }
            }
            consecutiveCount = count
            return count >= 3;
        }
    }

    class ValStatRec {
        String nifId
        String resource
        String url
        Date lastCheckTime
        String message
    }

    def getLatestUpdateInfo() {
        def rusList = RegUpdateStatus.executeQuery("from RegUpdateStatus r where r.updateYear is not null")
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.siteUpdateInfo(count: rusList.size()) {
            for (RegUpdateStatus rus in rusList) {
                row() {
                    nifId(rus.registry.nifId)
                    updateYear(rus.updateYear)
                    lastCheckedTime(rus.lastCheckedTime)
                }
            }
        }
        writer.toString()
    }

    def getLatestValidationStatus() {
        def regList = Registry.executeQuery("select r.id, r.nifId, r.resourceName, r.url from Registry r")
        def regMap = [:]
        regList.each { r ->
            regMap[r[0] as String] = r
        }
        def recList = []
        ValidationStatus.withSession { session ->
            def rs = session.createQuery("select v.registryId, v.isUp, v.message, v.lastCheckedTime "
                    + "from ValidationStatus v  order " +
                    "by v.lastCheckedTime desc").scroll(ScrollMode.FORWARD_ONLY)
            long curRegId = -1
            Date refDay = null
            while (rs.next()) {
                long regId = rs.getLong(0)
                boolean isUp = rs.get(1) as boolean
                curRegId = regId
                def reg = regMap[regId as String]
                assert reg
                Date lastCheckTime = rs.get(3) as Date
                if (!refDay) {
                    refDay = new java.text.SimpleDateFormat("MM/dd/yyyy").parse(lastCheckTime.format("MM/dd/yyyy"))
                } else if (lastCheckTime.before(refDay)) {
                    break
                }
                if (!isUp) {
                    ValStatRec vsr = new ValStatRec(nifId: reg[1], resource: reg[2], url: reg[3],
                            lastCheckTime: rs.get(3) as Date,
                            message: rs.getString(2))
                    recList << vsr
                }
            }
        }

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.downSites(count: recList.size()) {
            for (ValStatRec vsr in recList) {
                row() {
                    nifId(vsr.nifId)
                    resource(vsr.resource)
                    url(vsr.url)
                    lastCheckedTime(vsr.lastCheckTime)
                    message(vsr.message)
                }
            }
        }

        return writer.toString()
    }

    def getURLAnnotInfoForUrls(urList) {
        def ids = []
        def idMap = [:]
        urList.eachWithIndex { ur, i -> ids << ur.id; idMap[ur.id] = i }
        def list = URLAnnotationInfo.executeQuery("from URLAnnotationInfo a where a.opType = :opType and a.url.id in (:ids)",
                [opType: 'registry_ref', ids: ids])
        def labels = []
        def notes = []
        def resources = []
        urList.each { labels << 'nosel' }
        list.each { URLAnnotationInfo uai ->
            labels[idMap[uai.url.id]] = uai.label
            notes[idMap[uai.url.id]] = uai.notes ? 'has-notes' : ''
            resources[idMap[uai.url.id]] = uai.registry ? 'has-resource' : ''
        }

        return [labels: labels, notes: notes, resources: resources]
    }

    def getUserNote(int urId) {
        def url = Urls.get(urId)
        if (url) {
            def uai = URLAnnotationInfo.findByUrl(url)
            return uai?.notes
        }
        return null
    }

    def getPaperInfo(String pmid) {
        def papers = Paper.findByPubmedId(pmid)
        if (papers) {
            return papers
        }
        return null
    }


    def saveURLAnnotInfo(int urId, String label, String modUser, String notes = null) {
        def url = Urls.get(urId)
        if (url) {
            def uai = URLAnnotationInfo.findByUrl(url)
            if (uai) {
                if (label == 'nosel') {
                    if (notes) {
                        uai.label = label
                        uai.modTime = new Date()
                        uai.modifiedBy = modUser
                        if (notes) {
                            uai.notes = notes
                        }
                        uai.save(failOnError: true, flush: true)
                    } else {
                        uai.delete(failOnError: true, flush: true)
                    }
                } else {
                    uai.label = label
                    uai.modTime = new Date()
                    uai.modifiedBy = modUser
                    if (notes) {
                        uai.notes = notes
                    }

                    uai.save(failOnError: true, flush: true)
                }
            } else {
                uai = new URLAnnotationInfo(url: url, label: label, modTime: new Date(), modifiedBy: modUser,
                        opType: 'registry_ref')
                if (notes) {
                    uai.notes = notes
                }
                uai.save(failOnError: true, flush: true)
            }
        }
    }

    def getResourceNames(String term, filterType = 'name') {
        if (filterType == 'name') {
            return Registry.executeQuery('select resourceName from Registry where resourceName like :term', [term: "%$term%"])
        } else {
            return Registry.executeQuery('select nifId from Registry where nifId like :term', [term: "%$term%"])
        }
    }
}
