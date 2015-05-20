package org.neuinfo.rdw

import groovy.json.JsonBuilder
import org.springframework.cache.annotation.Cacheable;
import groovy.sql.Sql
import groovy.transform.ToString;

class CooccurrenceVizService {
    def dataSource

    @Cacheable(value = "rdwco", key = '#root.methodName.concat(#root.args)')
    def preparePairs4Resource(long registryId) {
        def sql = new Sql(dataSource)
        def map = [:]
        sql.eachRow('select r.id, r.resource_name, p.pubmed_id, r.abbrev  from rd_urls u, rd_paper p, registry r where u.doc_id = p.id and u.registry_id = r.id and u.registry_id is not null and u.doc_id is not null order by u.registry_id') { r ->
            long id = r[0] as long
            String name = r[1]
            String pmid = r[2]
            String acronym = r[3]
            Resource resource = map[(id)]
            if (!resource) {
                resource = new Resource(id: id, name: name, acronym: acronym)
                map[(id)] = resource
            }
            resource.pmidSet.add(pmid)
        }
        sql.close()
        def pairs = []
        def resources = map.values().toList()
        Resource refResource = resources.find { Resource r -> r.id == registryId }
        assert refResource
        int len = resources.size()
        for (j in 0..<len) {
            Resource r2 = resources[j]
            if (r2 != refResource) {
                def commonSet = refResource.pmidSet.intersect(r2.pmidSet)
                if (commonSet.size() > 0) {
                    Pair pair = new Pair(item1: refResource, item2: r2, count: commonSet.size())
                    if (refResource.maxPair < commonSet.size()) {
                        refResource.maxPair = commonSet.size()
                    }
                    if (r2.maxPair < commonSet.size()) {
                        r2.maxPair = commonSet.size()
                    }
                    pairs << pair
                }
            }
        }
        Set<Resource> nodeSet = new HashSet<Resource>()
        pairs.each { Pair p ->
            if (!nodeSet.contains(p.item1)) {
                nodeSet.add((Resource) p.item1)
            }
            if (!nodeSet.contains(p.item2)) {
                nodeSet.add((Resource) p.item2)
            }
        }
        if (nodeSet.size() > 50) {
            List<Resource> rList = new ArrayList<>(nodeSet)
            rList.sort { Resource a, b -> b.maxPair <=> a.maxPair }
            nodeSet.clear()
            nodeSet.addAll(rList[0..50])
        }

        pairs = pairs.grep { Pair p -> p.item1 in nodeSet && p.item2 in nodeSet }
        def nodes = nodeSet.toList().sort { Resource a, b -> a.name <=> b.name }
        def resourceIdxMap = [:]
        int i = 0
        int maxPairs = -1
        nodes.each { Resource r ->
            resourceIdxMap[(r.id)] = i++
            maxPairs = Math.max(maxPairs, r.maxPair)
        }

        nodeSet = null
        map = null
        def coMap = [nodes   : nodes.collect { Resource r -> [name: r.acronym ? r.acronym : r.name, group: r.id] },
                     links   : pairs.collect { Pair p ->
                         [source: resourceIdxMap[(p.item1.id)],
                          target: resourceIdxMap[(p.item2.id)], value: p.count]
                     },
                     maxPairs: maxPairs
        ]
        def json = new JsonBuilder(coMap)
        println "nodes: ${nodes.size()}"
        return json.toString()
    }

    @Cacheable(value = "rdwco", key = '#root.methodName.concat(#root.args)')
    def prepareMostUsedResourcePairs(int max) {
        def sql = new Sql(dataSource)
        def map = [:]
        sql.eachRow('select r.id, r.resource_name, p.pubmed_id, r.abbrev  from rd_urls u, rd_paper p, registry r where u.doc_id = p.id and u.registry_id = r.id and u.registry_id is not null and u.doc_id is not null order by u.registry_id') { r ->
            long id = r[0] as long
            String name = r[1]
            String pmid = r[2]
            String acronym = r[3]
            Resource resource = map[(id)]
            if (!resource) {
                resource = new Resource(id: id, name: name, acronym: acronym)
                map[(id)] = resource
            }
            resource.pmidSet.add(pmid)
        }
        sql.close()
        def pairs = []
        def resources = map.values().toList()
        resources = resources.grep { Resource r -> r.pmidSet.size() >= 100 }
        int len = resources.size()
        println "resources ($len)"
        for (i in 0..<len) {
            Resource r = resources[i]
            for (j in i + 1..<len) {
                Resource r2 = resources[j]
                def commonSet = r.pmidSet.intersect(r2.pmidSet)
                if (commonSet.size() > 0) {
                    Pair pair = new Pair(item1: r, item2: r2, count: commonSet.size())
                    if (r.maxPair < commonSet.size()) {
                        r.maxPair = commonSet.size()
                    }
                    if (r2.maxPair < commonSet.size()) {
                        r2.maxPair = commonSet.size()
                    }
                    pairs << pair
                }
            }
        }
        Set<Resource> nodeSet = new HashSet<Resource>()
        pairs.each { Pair p ->
            if (!nodeSet.contains(p.item1)) {
                nodeSet.add((Resource) p.item1)
            }
            if (!nodeSet.contains(p.item2)) {
                nodeSet.add((Resource) p.item2)
            }
        }
        nodeSet = nodeSet.grep { Resource r -> r.maxPair > 80 }
        pairs = pairs.grep { Pair p -> p.item1 in nodeSet && p.item2 in nodeSet }
        def nodes = nodeSet.toList().sort { Resource a, b -> a.name <=> b.name }
        def resourceIdxMap = [:]
        int i = 0
        int maxPairs = -1
        nodes.each { Resource r ->
            resourceIdxMap[(r.id)] = i++
            maxPairs = Math.max(maxPairs, r.maxPair)
        }

        nodeSet = null
        map = null
        def coMap = [nodes   : nodes.collect { Resource r -> [name: r.acronym ? r.acronym : r.name, group: r.id] },
                     links   : pairs.collect { Pair p ->
                         [source: resourceIdxMap[(p.item1.id)],
                          target: resourceIdxMap[(p.item2.id)], value: p.count]
                     },
                     maxPairs: maxPairs
        ]
        def json = new JsonBuilder(coMap)
        // println json.toPrettyString()
        new File('/tmp/resources.json').withWriter { it << json.toPrettyString() }
        println "nodes: ${nodes.size()}"
        return json.toString()
    }

    class Resource {
        long id
        String name
        String acronym
        Set<String> pmidSet = new HashSet<String>()
        int maxPair = -1
    }

    class Pair {
        def item1
        def item2
        int count
    }
}
