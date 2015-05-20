package org.neuinfo.rdw

class NerResourceRefService {

	def getResourceRecsWithContext() {
		def rrList = ResourceRec.executeQuery("from ResourceRec r where r.context is not null and r.registry is not null");
		return rrList
	}

	def getMostPopularResources(int max) {
		def list = ResourceRec.executeQuery("select r.registry.id, count(r.registry.id) as count from ResourceRec r " +
				"where r.registry.id is not null group by r.registry.id order by count(r.registry.id) desc", [max:max])
		def ids = []
		list.each { ids << it[0] }
		def resourceNames = Registry.executeQuery("select r.id, r.resourceName from Registry r where r.id in (:ids)",
				[ids:ids])
		def map = [:]
		resourceNames.each { map[(it[0])] = it[1] }
		// println "map:$map"
		def rciList = []
		list.each {
			String resourceName = map[it[0]]
			rciList << new ResourceCountInfo(label:"$resourceName (${it[1]})", value: resourceName)
		}
		rciList
	}

	
	static class ResourceCountInfo {
		String label
		String value
		
		public String toString() { "[label:$label, value:$value]" }
	}
	
	def getResourceRecsWithRegistry(int max, int offset, String filterType = null, String value = null) {
		def rrList = null
		if (!filterType || value == '*' || !value) {
			rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry is not null order by r.registry.id",
					[max:max, offset:offset])
		} else {
			if (filterType == 'name') {
				rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry.resourceName = :name",
						[name:value, max:max, offset:offset])
			} else {
				rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry.nifId = :nifId",
						[nifId:value, max:max, offset:offset])
			}
		}
		// println rrList
		return rrList
	}

	def getResourceRecsWithRegistryML(int max, int offset, String filterType = null, String value = null,
			boolean activeLearning) {
		String suffix = activeLearning ? ' order by abs(r.cScore) asc, r.registry.id' : ' and r.cScore > 0 order by r.cScore desc, r.registry.id'
		def rrList = null
		if (!filterType || value == '*' || !value ) {
			rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry is not null"
					+ suffix, [max:max, offset:offset])
		} else {
			if (filterType == 'name') {
				rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry.resourceName = :name" + suffix,
						[name:value, max:max, offset:offset])
			} else {
				rrList = ResourceRec.executeQuery("from ResourceRec r where r.registry.nifId = :nifId" + suffix,
						[nifId:value, max:max, offset:offset])
			}
		}
		//println rrList
		rrList
	}

	def getCountResourceRecsWithRegistry(String filterType = null, String value = null) {
		def list = null
		if (!filterType || value == '*' || !value) {
			list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry is not null")
		} else {
			if (filterType == 'name') {
				list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry.resourceName = :name",
						[name:value])
			} else {
				list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry.nifId = :nifId",
						[nifId:value])
			}
		}
		return list[0] as int
	}

	def getCountResourceRecsWithRegistryML(String filterType = null, String value = null, boolean activeLearning) {
		String suffix = activeLearning ? '' : ' and r.cScore > 0'
		def list = null
		if (!filterType || value == '*' || !value) {
			list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry is not null" + suffix)
		} else {
			if (filterType == 'name') {
				list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry.resourceName = :name" + suffix,
						[name:value])
			} else {
				list = ResourceRec.executeQuery("select count(r.id) from ResourceRec r where r.registry.nifId = :nifId" + suffix,
						[nifId:value])
			}
		}
		return list[0] as int
	}

	def getNerAnnotInfoForResourceRecs(rrList) {
		def labels = []
		def notes = []
		if (!rrList) {
			return [labels:labels, notes:notes]
		}
		def ids = []
		def idMap = [:]

		rrList.eachWithIndex { rr, i -> ids << rr.id; idMap[rr.id] = i }
		//println "rrList: $rrList"
		//println "ids:$ids"
		def list = NERAnnotationInfo.executeQuery("from NERAnnotationInfo n where n.opType= :opType and n.resourceRec.id in (:ids)",
				[opType:'ner_filter', ids:ids])
		// println "list:" + list
		rrList.each { labels << 'nosel' }
		list.each { NERAnnotationInfo nai ->
			labels[ idMap[nai.resourceRec.id] ] = nai.label
			notes[ idMap[nai.resourceRec.id] ] = nai.notes ? 'has-notes' : ''
		}
		return [labels:labels, notes:notes]
	}

	def getUserNote(int rrId) {
		def rr = ResourceRec.get(rrId)
		if (rr) {
			def nai = NERAnnotationInfo.findByResourceRec(rr)
			return nai?.notes
		}
		return null
	}

	def saveRRAnnotInfo(int rrId, String label, String modUser, String notes = null) {
		def rr = ResourceRec.get(rrId)
		if (rr) {
			def nai = NERAnnotationInfo.findByResourceRec(rr)
			if (nai) {
				if (label == 'nosel') {
					if (notes) {
						nai.label = label
						nai.modTime = new Date()
						nai.modifiedBy = modUser
						nai.notes = notes
						nai.save(failOnError:true, flush:true)
					} else {
						nai.delete(failOnError:true, flush:true)
					}
				} else {
					nai.label = label
					nai.modTime = new Date()
					nai.modifiedBy = modUser
					nai.notes = notes ?: null
					nai.save(failOnError:true, flush:true)
				}
			} else {
				// new rec
				nai = new NERAnnotationInfo(resourceRec: rr,  modTime: new Date(), label:label, modifiedBy: modUser,
				opType:'ner_filter')
				nai.notes = notes ?: null
				nai.save(failOnError:true, flush:true)
			}
		}
	}

	def getResourceNames(String term, filterType = 'name') {
		if (filterType == 'name') {
			return Registry.executeQuery('select resourceName from Registry where resourceName like :term', [term:"%$term%"])
		} else {
			return Registry.executeQuery('select nifId from Registry where nifId like :term', [term:"%$term%"])
		}
	}

	def getNERAnnotInfo() {
		def list = NERAnnotationInfo.executeQuery("from NERAnnotationInfo where label <> 'nosel'")
		return list
	}
}
