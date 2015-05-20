package org.neuinfo.rdw

import org.neuinfo.rdw.ResourceFilterService.Filter;

import grails.converters.JSON

import java.text.SimpleDateFormat

class ResourceFilterController {
	def resourceFilterService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}

	def show() {
		def batchIds = resourceFilterService.getBatchIds()
		// render(view:'show', model:[batchId:'201310'])
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMM')
		batchIds.sort {String a, String b -> sdf.parse(a).compareTo( sdf.parse(b)) }
		render(view:'show', model:[batchIds:batchIds])
	}

	def index() {
		redirect(action:'show')
	}

	def search() {
		String batchId = params.batchId
		String opMode = params.opMode
		
		String filterType = params.filterTypeChooser ?: ''
		String value = params.filterInput ?: ''
		if (!filterType) {
			filterType = params.filterType ?: ''
		}

		println "params:$params"
		params.max = Math.min(params.max ? params.int('max') : 10, 100);
		params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
		println "params2:$params"

		Filter filter = null
		if (value) {		
			filter = new Filter(field: filterType, value: value)
		}

		def urList = null
		if (opMode == 'learning' || opMode == 'active learning') {
			boolean activeLearning = opMode == 'active learning'
			urList = resourceFilterService.getUrlsWithDescriptionPageML(batchId, params.max, params.offset,
					activeLearning, filter)
		} else if (opMode == 'popularity') {
			urList = resourceFilterService.getUrlsWithDescriptionPageMLHostLinkSorted(batchId,
					params.max, params.offset, filter)
		} else {
			urList = resourceFilterService.getUrlsWithDescriptionPage(batchId, 5, params.max,
					params.offset, filter)
		}
		int totCount = 0;
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			if (opMode == 'learning' || opMode == 'active learning') {
				totCount = resourceFilterService.getCountForUrlsWithDescriptionML(batchId, filter);
			} else if (opMode == 'popularity') {
				totCount = resourceFilterService.getCountForUrlsWithDescriptionMLHostLinkSorted(batchId, filter);
			} else {
				totCount = resourceFilterService.getCountForUrlsWithDescription(batchId, 5, filter)
			}
		}

		def map =  resourceFilterService.getURLAnnotInfoForUrls(urList)
		println "resources:" + map['resources']
		render(view: 'list', model:[urList: urList, totCount: totCount, batchId:batchId, opMode:opMode,
			labels:map['labels'], noteList:map['notes'], resourceTypes: map['resourceTypes'],
			resourceList:map['resources'],
			filterType:filterType, filterInput:value  ])
	}

	def getResourceTypes() {
		String term = params.term
		println "term:$term"
		return Urls.executeQuery(
		"select distinct(u.resourceType) from Urls u where u.resourceType is not null and u.resourceType like :term",
		[term:"%$term%"])
	}

	def saveUserAnnot() {
		String urId = params.urId
		String label = params.label
		String notes = null
		String resourceType = null
		println "urId: $urId label:$label"
		String modUser = session.user.loginId
		if (params.notes) {
			notes = params.notes
		}
		if (params.resourceType && params.resourceType != 'null') {
			resourceType = params.resourceType
		}
		resourceFilterService.saveURLAnnotInfo(urId as int, label, modUser, notes, resourceType)
		render(status:200)
	}

	def getUserNote() {
		String urId = params.urId
		String notes = resourceFilterService.getUserNote(urId as int)
		if (!notes) {
			notes = ''
		}
		render(contentType:'text/json') { uai(notes:notes) }
	}

	def saveDeduplicationInfo() {
		String urId = params.urId
		String resourceName = params.resourceName
		String modUser = session.user.loginId

		def uai = resourceFilterService.assocURLAnnotInfoWithResouce(urId as int, resourceName, modUser)
		def map = [rc: (uai ? resourceName : '')]

		render map as JSON
	}

	def getResourceNames() {
		String term = params.term
		println "term:$term"
		def resourceNames = resourceFilterService.getResourceNames(term)
		render resourceNames as JSON
	}

	def getResourceName() {
		String urId = params.urId
		String resourceName =  resourceFilterService.getResourceName(urId as int)
		if (!resourceName) {
			resourceName = ''
		}
		render(contentType:'text/json') { ['resourceName' : resourceName] }
	}
}
