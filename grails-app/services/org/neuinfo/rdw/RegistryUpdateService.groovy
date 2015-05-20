package org.neuinfo.rdw

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST


class RegistryUpdateService {


	def getRedirectCandidates(int max, int offset) {
		def list = null
		try {
			list = RegistryRedirectAnnotInfo.executeQuery("from RegistryRedirectAnnotInfo a order by a.classiferScore desc", 
				[max:max, offset:offset])
		} catch(Throwable t) {
			t.printStackTrace()
		}
		return list
	}

	def countRedirectCandidates() {
		def list = RegistryRedirectAnnotInfo.executeQuery("select count(*) from RegistryRedirectAnnotInfo a")

		return list[0] as int
	}

	def getRegUpdateInfos(int max, int offset, String opType) {
		def list = null
		try {
			if (opType == 'semantic') {
				list = RegUpdateStatus.executeQuery("from RegUpdateStatus r where r.semSimilarity >= 0 order by r.semSimilarity asc",
						[max:max, offset:offset]);
			} else if (opType == 'cosSim') {
				list = RegUpdateStatus.executeQuery("from RegUpdateStatus r where r.cosSimilarity >= 0 order by r.cosSimilarity asc",
						[max:max, offset:offset]);
			} else {
				list = RegUpdateStatus.executeQuery("from RegUpdateStatus r where r.similarity >= 0 order by r.similarity asc",
						[max:max, offset:offset]);
			}
		} catch(Throwable t) {
			t.printStackTrace()
		}
		return list
	}

	def countRegUpdateInfos(String opType) {
		def list = null
		if (opType == 'semantic') {
			list = RegUpdateStatus.executeQuery("select count(r.id) from RegUpdateStatus r where r.semSimilarity >= 0");
		} else if (opType == 'cosSim') {
			list = RegUpdateStatus.executeQuery("select count(r.id) from RegUpdateStatus r where r.cosSimilarity >= 0");
		} else {
			list = RegUpdateStatus.executeQuery("select count(r.id) from RegUpdateStatus r where r.similarity >= 0");
		}
		return list[0] as int
	}

	def getRedirectUrlsForRus(rusList) {
		def ids = []
		def idMap = [:]
		rusList.eachWithIndex { RegUpdateStatus rus, i ->
			ids << (rus.registry.id as long)
			idMap[rus.registry.id] = i
		}
		def list = RegistrySiteContent.executeQuery("from RegistrySiteContent r where r.registryId in (:ids)",
				[ids:ids])
		def redirectUrls = []
		list.each { RegistrySiteContent rsc ->
			redirectUrls[idMap[(rsc.registryId)]] = rsc.redirectUrl
		}
		return redirectUrls
	}

	def getOrigSiteContent(int registryId) {
		RegistrySiteContent rsc = RegistrySiteContent.findByRegistryIdAndFlags(registryId,1)
		assert rsc
		Registry reg = Registry.get(registryId as long)
		assert reg != null
		def map = [content: rsc.content, resourceName: reg.resourceName]
		return map
	}

	def getSiteContents(int registryId) {
		RegistrySiteContent rsc = RegistrySiteContent.findByRegistryIdAndFlags(registryId,1)
		RegistrySiteContent latestRSC = RegistrySiteContent.findByRegistryIdAndFlags(registryId,2)
		assert rsc
		Registry reg = Registry.get(registryId as long)
		assert reg != null
		/*
		 def http = new HTTPBuilder(reg.url)
		 def curContent = null
		 def resp
		 http.handler.failure = { r ->  resp = r }
		 http.request(GET, TEXT) { req ->
		 response.success = { r, reader ->
		 def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
		 def parser = new XmlSlurper(tagsoupParser)
		 println "parser:$parser"
		 def root = parser.parse(reader)
		 StringBuilder sb = new StringBuilder()
		 root.'**'.findAll { it ->
		 if (it.name != 'script' && it.name != 'style' && it.text()) {
		 sb.append(it.text()).append(' ')
		 }
		 }
		 curContent = sb.toString().trim()
		 // System.out << reader
		 resp = r
		 }
		 }
		 println curContent
		 */

		def map = [origContent: rsc.content, content:latestRSC.content, resourceName: reg.resourceName]
		return map
	}
	
	def saveRedirectCandidateAnnotInfo(int rcId, String label, String modUser, String notes = null, String redirectUrl = null) {
		RegistryRedirectAnnotInfo rrai = RegistryRedirectAnnotInfo.get(rcId)
		assert(rrai)
		rrai.label = label
		rrai.modificationTime = new Date()
		rrai.modifiedBy = modUser
		if (notes) {
			rrai.notes = notes
		}
		if (redirectUrl) {
			rrai.redirectUrl = redirectUrl
		}
		rrai.save(failOnError:true, flush:true)
		if (redirectUrl) {
			// keep a history of redirect urls for the resource
			RedirectHistory rh = new RedirectHistory(redirectUrl: redirectUrl)
		    rh.registry = rrai.registry
			rh.modifiedBy = modUser
			rh.modificationTime = new Date()
			rh.save(failOnError: true, flush:true)
		}
		
	}
}
