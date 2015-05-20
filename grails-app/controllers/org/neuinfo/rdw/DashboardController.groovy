package org.neuinfo.rdw

import grails.converters.*

class DashboardController {
	def dashboardService
	def beforeInterceptor = [action:this.&auth]
	
	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}
    def index() { 
		println "in index"
		def fList = dashboardService.getNMostCitedResources(30)
		def nmcrData = fList as JSON
		fList = dashboardService.getResourceMentionsPerBatch()
		def rmpbData = fList as JSON
		
		fList = dashboardService.getLatestRegistrySiteValidation()
		def lrsvData = fList as JSON
		
		fList = dashboardService.getMostCitedResourcesFromPublishers(30)
		def mcprData = fList as JSON
		
		fList = dashboardService.getResourceMentionsByPublisher()
		def rmpubData = fList as JSON
		
		fList = dashboardService.getMostCitedResourcesFromNER(30)
		def mcnerData = fList as JSON
		
		render(view:'show', model:[nmcrData:nmcrData, rmpbData: rmpbData, lrsvData: lrsvData, 
			mcprData: mcprData, rmpubData : rmpubData, mcnerData: mcnerData])
	}
	
	def show() {
		println "in show"
		redirect(action:'index')
	}
	
	
}
