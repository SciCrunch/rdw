package org.neuinfo.rdw

import grails.converters.JSON

class NerResourceRefController {
	def nerResourceRefService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}
	def index() {
		redirect(action:'search')
	}
	
	def search() {
		println "params:$params"
		params.max = Math.min(params.max ? params.int('max') : 10, 100);
		params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
		String opType = params.opType
		String selResource = params.selectedResource
		println "params2:$params"

		def rrList = null
		int totCount = 0
		String filterType = params.filterTypeChooser ?: ''
		String value = params.filterInput ?: ''
		if (!filterType) {
			filterType = params.filterType ?: ''
		}
		
		if (opType == 'learning' || opType == 'active learning') {
			boolean activeLearning = opType == 'active learning'
			rrList = nerResourceRefService.getResourceRecsWithRegistryML(params.max, params.offset, filterType,
					value, activeLearning)
		} else {
			rrList = nerResourceRefService.getResourceRecsWithRegistry(params.max, params.offset,
					filterType, value)
		}
		if (params.totCount) {
			totCount = params.totCount as int
		} else {
			if (opType == 'learning' || opType == 'active learning') {
				boolean activeLearning = opType == 'active learning'
				totCount = nerResourceRefService.getCountResourceRecsWithRegistryML(filterType, value, activeLearning)
			} else {
				totCount = nerResourceRefService.getCountResourceRecsWithRegistry(filterType, value)
			}
		}
		def lnMap = nerResourceRefService.getNerAnnotInfoForResourceRecs(rrList)
		println "lnMap[labels]:" + lnMap['labels']
		
		def popList = nerResourceRefService.getMostPopularResources(100)
		// println "popList:${popList}"

		render(view: 'list', model:[rrList: rrList, totCount: totCount, filterType:filterType,
			filterInput:value, labels:lnMap['labels'], noteList:lnMap['notes'], opType:opType, 
			popList:popList, selectedResource: selResource])
	}

	
	def saveUserAnnot() {
		String rrId = params.urId
		String label = params.label
		String notes = null
		println "urId: $rrId label:$label"
		String modUser = session.user.loginId
		if (params.notes) {
			notes = params.notes
		}
		nerResourceRefService.saveRRAnnotInfo(rrId as int, label, modUser, notes)
		render(status:200)
	}

	def getUserNote() {
		String rrId = params.urId
		String notes = nerResourceRefService.getUserNote(rrId as int)
		notes = notes ?: ''
		render(contentType:'text/json') { uai(notes:notes) }
	}

	def getResourceNames() {
		String term = params.term
		println "term:$term"
		println "params:$params"
		String filterType = params.filterType ?: 'name'
		def resourceNames = nerResourceRefService.getResourceNames(term, filterType)
		render resourceNames as JSON
	}
}
