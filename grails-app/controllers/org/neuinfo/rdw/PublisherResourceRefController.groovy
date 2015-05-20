package org.neuinfo.rdw

import grails.converters.JSON

class PublisherResourceRefController {
	def publisherResourceRefService
	def beforeInterceptor = [action: this.&auth]

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
		String opType = params.opType ?: 'default'
		params.max = Math.min(params.max ? params.int('max') : 10, 100);
		params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
		String selResource = params.selectedResource
		println "params2:$params"

		def prList = null
		int totCount = 0
		String filterType = params.filterTypeChooser ?: ''
		String value = params.filterInput ?: ''
		if (!filterType) {
			filterType = params.filterType ?: ''
		}
		if (opType == 'learning') {
			prList = publisherResourceRefService.getResourceRecsWithRegistryML(params.max,
					params.offset, filterType, value)
		} else {
			prList = publisherResourceRefService.getResourceRecsWithRegistry(params.max,
					params.offset, filterType, value)
		}
		if (params.totCount) {
			totCount = params.totCount as int
		} else {
			if (opType == 'learning') {
				totCount = publisherResourceRefService.getCountResourceRecsWithRegistryML(filterType, value)				
			} else {
				totCount = publisherResourceRefService.getCountResourceRecsWithRegistry(filterType, value)
			}
		}

		def lnMap = publisherResourceRefService.getPubSearchAnnotInfoForPaperRefs(prList)
		
		def popList = publisherResourceRefService.getMostPopularResources(200)

		render(view:'list', model:[prList:prList, totCount:totCount, filterType:filterType, filterInput:value,
			labels:lnMap['labels'], noteList:lnMap['notes'], opType:opType,
			popList:popList, selectedResource: selResource])
	}

	def getResourceNames() {
		String term = params.term
		println "term:$term"
		String filterType = params.filterType ?: 'name'
		def resourceNames = publisherResourceRefService.getResourceNames(term, filterType)
		println resourceNames
		render resourceNames as JSON
	}

	def saveUserAnnot() {
		String prId = params.urId
		String label = params.label
		String notes = null
		println "prId: $prId label:$label"
		String modUser = session.user.loginId
		if (params.notes) {
			notes = params.notes
		}
		publisherResourceRefService.savePSAnnotInfo(prId as int, label, modUser, notes)
		render(status:200)
	}

	def getUserNote() {
		String prId = params.urId
		String notes = publisherResourceRefService.getUserNote(prId as int)
		notes = notes ?: ''
		render(contentType:'text/json') { uai(notes:notes) }
	}
}
