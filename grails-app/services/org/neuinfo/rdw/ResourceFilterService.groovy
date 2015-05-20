package org.neuinfo.rdw

import groovy.transform.ToString;

class ResourceFilterService {

    def getBatchIds() {
        return JobLog.executeQuery("select distinct(batchId) from JobLog where status = :status and operation = :op",
                [status: 'finished', op: 'pmc_resource_ref'])
    }

    def getUrlsWithDescription(batchIds, int scoreThreshold, Filter filter) {
        String hql = "from Urls u where u.batchId in (:batchIds) " +
                "and u.description is not null and u.score >= :score"
        def urList = null
        if (filter) {
            hql += filter.queryPart()
            urList = Urls.executeQuery(hql,
                    [batchIds: batchIds, score: scoreThreshold as double, fv: filter.value])
        } else {
            urList = Urls.executeQuery(hql,
                    [batchIds: batchIds, score: scoreThreshold as double])
        }
        return urList
    }

    def getUrlsWithDescriptionPage(String batchId, int scoreThreshold, int max, int offset, Filter filter) {
        String suffix = " order by u.score desc"
        String hql = "from Urls u where u.batchId = :batchId " +
                "and u.description is not null and u.rank <> -1"
        // "and u.description is not null and u.score >= :score and u.rank <> -1"
        def urList = null
        if (filter) {
            hql += filter.queryPart()
            println hql + suffix
            println "-" * 80
            println "batchId: $batchId"
            println filter
            hql += suffix
            urList = Urls.executeQuery(hql,
                    [batchId: batchId, /*score: scoreThreshold as double,*/ max: max, offset: offset,
                     fv     : filter.value])
        } else {
            urList = Urls.executeQuery(hql + suffix,
                    [batchId: batchId, /* score: scoreThreshold as double,*/ max: max, offset: offset])
        }
        return urList
    }

    def getUrlsWithDescriptionPageML(String batchId, int max, int offset, boolean activeLearning, Filter filter) {
        String suffix = activeLearning ? ' order by abs(u.cScore) asc' : ' order by u.cScore desc'
        String hql = "from Urls u where u.batchId = :batchId " +
                "and u.description is not null and u.cScore is not null and u.rank <> -1 "
        def urList = null
        if (filter) {
            hql += filter.queryPart()
            urList = Urls.executeQuery(hql + suffix,
                    [batchId: batchId, max: max, offset: offset, fv: filter.value])
        } else {
            urList = Urls.executeQuery(hql + suffix,
                    [batchId: batchId, max: max, offset: offset])
        }
        return urList
    }

    @ToString
    static class Filter {
        String field;
        String value

        def String queryPart() {
            if (field == 'resourceType') {
                return " and  u.resourceType = :fv "
            }
            return ''
        }
    }

    def getUrlsWithDescriptionPageMLHostLinkSorted(String batchId, int max, int offset, Filter filter) {
        String suffix = " order by  u.hostLinkSize desc, u.cScore desc"
        String hql = "from Urls u, UrlStatus s where u.batchId = :batchId " +
                "and u.description is not null and u.registry is null and u.cScore > 0 and u.rank <> -1 and u.id = s.urlID " +
                "and s.alive = true and s.type <> 1 and s.type <> 2 "
        def list = null
        if (filter) {
            hql += filter.queryPart()
            list = Urls.executeQuery(hql + suffix,
                    [batchId: batchId, max: max, offset: offset, fv: filter.value])
        } else {
            list = Urls.executeQuery(hql + suffix,
                    [batchId: batchId, max: max, offset: offset])
        }
        def urList = []
        list.each { r ->
            urList << r[0]
        }
        return urList
    }

    def getCountForUrlsWithDescriptionMLHostLinkSorted(String batchId, Filter filter) {
        String hql = "select count(u.id) from Urls u, UrlStatus s where u.batchId = :batchId " +
                "and u.description is not null and u.registry is null and u.cScore > 0 and u.rank <> -1 and u.id = s.urlID " +
                "and s.alive = true and s.type <> 1 and s.type <> 2"
        def list = null
        if (filter) {
            hql += filter.queryPart()
            list = Urls.executeQuery(hql, [batchId: batchId, fv: filter.value])
        } else {
            list = Urls.executeQuery(hql, [batchId: batchId])
        }
        return list[0] as int
    }

    def getCountForUrlsWithDescription(String batchId, int scoreThreshold, Filter filter) {
        String hql = "select count(u.id) from Urls u where u.batchId = :batchId " +
                "and u.description is not null and u.score >= :score and u.rank <> -1"
        def list = null
        if (filter) {
            hql += filter.queryPart()
            list = Urls.executeQuery(hql, [batchId: batchId, score: scoreThreshold as double, fv: filter.value])
        } else {
            list = Urls.executeQuery(hql, [batchId: batchId, score: scoreThreshold as double])
        }
        println list
        return list[0] as int
    }


    def getCountForUrlsWithDescriptionML(String batchId, Filter filter) {
        String hql = "select count(u.id) from Urls u where u.batchId = :batchId " +
                "and u.description is not null and u.cScore is not null and u.rank <> -1"
        def list = null
        if (filter) {
            hql += filter.queryPart()
            list = Urls.executeQuery(hql, [batchId: batchId, fv: filter.value])
        } else {
            list = Urls.executeQuery(hql, [batchId: batchId])
        }
        return list[0] as int
    }


    def fixUrlsWithDuplicateDescriptions(String batchId, int scoreThreshold) {
        def urList = Urls.executeQuery("from Urls u where u.batchId = :batchId " +
                "and u.description is not null and u.score >= :score",
                [batchId: batchId, score: scoreThreshold as double])
        Set<String> descrSet = new HashSet<String>()
        def updateList = []
        urList.each { Urls ur ->
            if (descrSet.contains(ur.description)) {
                updateList << ur
            } else {
                descrSet.add(ur.description)
            }
        }

        updateList.each { Urls ur ->
            ur.rank = -1
            ur.save(failOnError: true, flush: true)
        }

        return urList
    }

    def getURLAnnotInfoForUrls(urList) {
        def ids = []
        def idMap = [:]
        urList.eachWithIndex { ur, i ->
            ids << ur.id; idMap[ur.id] = i
        }
        def list = []
        if (urList) {
            list = URLAnnotationInfo.executeQuery(
                    "from URLAnnotationInfo a where a.opType = :opType and a.url.id in (:ids)",
                    [opType: 'candidate_filter', ids: ids])
        }
        def labels = []
        def notes = []
        def resourceTypes = []
        def resources = []
        urList.each { labels << 'nosel' }
        list.each { uai ->
            labels[idMap[uai.url.id]] = uai.label
            notes[idMap[uai.url.id]] = uai.notes ? 'has-notes' : ''
            resourceTypes[idMap[uai.url.id]] = uai.resourceType
            resources[idMap[uai.url.id]] = uai.registry ? 'has-resource' : ''
        }

        return [labels: labels, notes: notes, resourceTypes: resourceTypes, resources: resources]
    }

    def getURLWithAnnotInfo(batchIds) {
        def list = URLAnnotationInfo.executeQuery("from URLAnnotationInfo a where a.url.batchId in (:batchIds) and a.opType = :opType",
                [batchIds: batchIds, opType: 'candidate_filter'])
        return list
    }

    def getUrlWithAnnotInfoAndType() {
        //	def list = URLAnnotationInfo.executeQuery(
        //			"from URLAnnotationInfo a where a.opType = :opType and a.resourceType is not null and a.url is not null",
        //			[ opType:'candidate_filter'])
        def list = URLAnnotationInfo.executeQuery(
                "from URLAnnotationInfo a where a.opType = :opType and (a.resourceType is not null or a.label is not null) and a.url is not null",
                [opType: 'candidate_filter'])


        def filteredList = []
        list.each { URLAnnotationInfo uai ->
            if (uai.resourceType) {
                filteredList << uai
            } else if (uai.label && uai.label == 'bad') {
                filteredList << uai
            }
        }

        return filteredList
    }

    def getUserNote(int urId) {
        def url = Urls.get(urId)
        if (url) {
            def uai = URLAnnotationInfo.findByUrl(url)
            return uai?.notes
        }
        return null
    }

    def getResourceName(int urId) {
        def list = URLAnnotationInfo.executeQuery(
                "select r.resourceName from URLAnnotationInfo a JOIN a.registry r where a.url.id = :urId", [urId: urId as long])
        if (list) {
            return list[0]
        }
        return null
    }

    def getResourceNames(String term) {
        term = term.toLowerCase()
        return Registry.executeQuery('select resourceName from Registry where lower(resourceName) like :term', [term: "%$term%"])
    }


    def assocURLAnnotInfoWithResouce(int urId, String resourceName, String modUser) {
        Urls url = Urls.get(urId)
        def reg = Registry.findByResourceName(resourceName)

        if (url && reg) {
            def uai = URLAnnotationInfo.findByUrl(url)
            if (uai) {
                uai.registry = reg
                uai.modifiedBy = modUser

                uai.save(failOnError: true, flush: true)
                return uai
            } else {
                uai = new URLAnnotationInfo(url: url, label: 'nosel', modTime: new Date(), modifiedBy: modUser,
                        opType: 'candidate_filter')
                uai.registry = reg
                uai.save(failOnError: true, flush: true)
                // update the registry of the corresponding url
                url.registry = reg
                url.save(failOnError: true, flush: true);
                return uai
            }
        } else if (url) {
            def uai = URLAnnotationInfo.findByUrl(url)
            if (uai) {
                uai.modifiedBy = modUser
                uai.registry = null
                uai.save(failOnError: true, flush: true)
                // update the registry of the corresponding url
                if (url.registry) {
                    url.registry = null
                    url.save(failOnError: true, flush: true);
                }
                return uai
            }
        }
        return null
    }


    def saveURLAnnotInfo(int urId, String label, String modUser, String notes = null, String resourceType = null) {
        def url = Urls.get(urId)
        if (url) {
            def uai = URLAnnotationInfo.findByUrl(url)
            if (uai) {
                if (label == 'nosel') {
                    if (notes || resourceType) {
                        uai.label = label
                        uai.modTime = new Date()
                        uai.modifiedBy = modUser
                        if (notes) {
                            uai.notes = notes
                        }

                        uai.resourceType = resourceType
                        println "resourceType:$resourceType"

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

                    uai.resourceType = resourceType

                    uai.save(failOnError: true, flush: true)
                }
            } else {
                uai = new URLAnnotationInfo(url: url, label: label, modTime: new Date(), modifiedBy: modUser,
                        opType: 'candidate_filter')
                if (notes) {
                    uai.notes = notes
                }
                if (resourceType) {
                    uai.resourceType = resourceType
                }

                uai.save(failOnError: true, flush: true)
            }
        }
    }
}
