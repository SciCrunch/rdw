package org.neuinfo.rdw

class PublisherResourceRefService {

	def getResourceRecsWithRegistry(int max, int offset, String filterType = null, String value = null) {
		def prList = null
		if (!filterType || value == '*') {
			prList = PaperReference.executeQuery("from PaperReference p where p.registry is not null and p.flags = 1 order by p.registry.id",
					[max:max, offset:offset]);
		} else {
			if (filterType == 'name') {
				prList =  PaperReference.executeQuery("from PaperReference p where p.registry.resourceName = :name and p.flags = 1" ,
						[name:value, max:max, offset:offset]);
			} else {
				prList =  PaperReference.executeQuery("from PaperReference p where p.registry.nifId = :nifId and p.flags = 1",
						[nifId:value, max:max, offset:offset]);
			}
		}
		return prList
	}

	def getResourceRecsWithRegistryML(int max, int offset, String filterType = null, String value = null) {
		def prList = null
		if (!filterType || value == '*' || value == '') {
			prList = PaperReference.executeQuery("from PaperReference p where p.registry is not null and p.flags = 1 and " +
					"p.cScore > 0 order by p.cScore desc",
					[max:max, offset:offset]);
		} else {
			if (filterType == 'name') {
				prList =  PaperReference.executeQuery("from PaperReference p where p.registry.resourceName = :name and p.flags = 1 " +
						"and p.cScore > 0 order by p.cScore desc",
						[name:value, max:max, offset:offset]);
			} else {
				prList =  PaperReference.executeQuery("from PaperReference p where p.registry.nifId = :nifId and p.flags = 1 " +
						"and p.cScore > 0 order by p.cScore desc",
						[nifId:value, max:max, offset:offset]);
			}
		}
		return prList
	}

	def getCountResourceRecsWithRegistry(String filterType = null, String value = null) {
		def list = null
		if (!filterType || value == '*') {
			list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry is not null and p.flags = 1")
		} else {
			if (filterType == 'name') {
				list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry.resourceName = :name and p.flags = 1",
						[name:value])
			} else {
				list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry.nifId = :nifId and p.flags = 1",
						[nifId:value])
			}
		}
		return list[0] as int
	}

	def getCountResourceRecsWithRegistryML(String filterType = null, String value = null) {
		def list = null
		if (!filterType || value == '*' || value == '') {
			list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry is not null and p.flags = 1 and p.cScore > 0")
		} else {
			if (filterType == 'name') {
				list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry.resourceName = :name and p.flags = 1 and p.cScore > 0",
						[name:value])
			} else {
				list = PaperReference.executeQuery("select count(p.id) from PaperReference p where p.registry.nifId = :nifId and p.flags = 1 and p.cScore > 0",
						[nifId:value])
			}
		}
		return list[0] as int
	}

	def getMostPopularResources(int max) {
		def list = PaperReference.executeQuery("select p.registry.id, count(p.registry.id) as count from PaperReference p " +
				"where p.registry.id is not null group by p.registry.id order by count(p.registry.id) desc", [max:max])
		def ids = []
		list.each { ids << it[0] }
		def resourceNames = Registry.executeQuery("select r.id, r.resourceName from Registry r where r.id in (:ids)",
				[ids:ids])
		def map = [:]
		resourceNames.each { map[(it[0])] = it[1] }
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
	
	def getPubSearchAnnotInfoForPaperRefs(prList) {
		def labels = []
		def notes = []
		if (!prList) {
			return [labels:labels, notes:notes]
		}
		def ids = []
		def idMap = [:]
		prList.eachWithIndex { pr, i ->
			ids << pr.id; idMap[pr.id] = i
		}
		def list = PubSearchAnnotationInfo.executeQuery(
				"from PubSearchAnnotationInfo p where p.opType = :opType and p.paperRef.id in (:ids)",
				[opType:'ps_filter', ids:ids])
		prList.each { labels << 'nosel' }
		list.each { PubSearchAnnotationInfo psai ->
			labels[ idMap[psai.paperRef.id] ] = psai.label
			notes[ idMap[psai.paperRef.id] ] = psai.notes ? 'has-notes' : ''
		}

		return [labels:labels, notes:notes]
	}

	def getUserNote(int prId) {
		def pr = PaperReference.get(prId)
		if (pr) {
			def psai = PubSearchAnnotationInfo.findByPaperRef(pr)
			return psai?.notes
		}
		return null
	}

	def savePSAnnotInfo(int prId, String label, String modUser, String notes = null) {
		def pr = PaperReference.get(prId)
		if (!pr) {
			return
		}

		def psai = PubSearchAnnotationInfo.findByPaperRef(pr)
		if (psai) {
			if (label == 'nosel') {
				if (notes) {
					psai.label = label
					psai.modTime = new Date()
					psai.modifiedBy = modUser
					psai.notes = notes
					psai.save(failOnError:true, flush:true)
				} else {
					psai.delete(failOnError:true, flush:true)
				}
			} else {
				psai.label = label
				psai.modTime = new Date()
				psai.modifiedBy = modUser
				psai.notes = notes ?: null
				psai.save(failOnError:true, flush:true)
			}
		} else {
			// new rec
			psai = new PubSearchAnnotationInfo(paperRef: pr, modTime: new Date(), label:label, modifiedBy: modUser,
			opType: 'ps_filter')
			psai.notes = notes ?: null
			psai.save(failOnError:true, flush:true)
		}
	}

	def getResourceNames(String term, filterType = 'name') {
		if (filterType == 'name') {
			return Registry.executeQuery('select resourceName from Registry where resourceName like :term', [term:"%$term%"])
		} else {
			return Registry.executeQuery('select nifId from Registry where nifId like :term', [term:"%$term%"])
		}
	}

	def getPubSearchAnnotationInfos() {
		def list = PubSearchAnnotationInfo.executeQuery("from PubSearchAnnotationInfo a  where a.label <> 'nosel'")
		list.each { PubSearchAnnotationInfo psai ->
			println "id:${psai.id}"
			println psai.paperRef.id
		}
		return list
	}

	def getPaperRefsForClassification() {
		def prList = PaperReference.executeQuery("from PaperReference p where p.flags = 1 and p.registry is not null and p.title is not null")
		return priList
	}
}
