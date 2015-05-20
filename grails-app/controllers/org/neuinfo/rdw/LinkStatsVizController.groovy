package org.neuinfo.rdw

import grails.converters.*

class LinkStatsVizController {
	def linkStatsVizService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}
	
    def index() { 
		println "in index"
		redirect(action:'show')
	}
	
	def show() {
		println "in show"
		String selRegId = params.selRegId
		println "selRegId:$selRegId"
		def fList = linkStatsVizService.getMostCitedResources(100)
		def nmcrData = fList as JSON
		int registryId = fList[0].id
		if (selRegId) {
			registryId = selRegId as int
		}
		def cList = linkStatsVizService.getLinksByYearForResource(registryId)
		def lbyData = cList as JSON
		
		def lssList = linkStatsVizService.getLinkAliveStatus()
		// println "lssList:" + lssList
		def lssData = lssList as JSON
		
		render(view:'show', model:[nmcrData:nmcrData, lbyData:lbyData, nmcrList:fList, selRegId:registryId, lssData:lssData])		
	}
}
