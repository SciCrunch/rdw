package org.neuinfo.rdw

import spock.lang.Ignore;
import grails.test.spock.IntegrationSpec

class DashboardServiceSpec extends IntegrationSpec {
	def dashboardService

	def setup() {
	}

	def cleanup() {
	}

	@Ignore
	void "test getNMostCitedResources"() {
		int max
		def fList = []
		given: "a max "
		max = 20
		when: "getNMostCitedResources is called"
		fList = dashboardService.getNMostCitedResources(max)
		fList.each { println it }
		then: "you should get some results"
		fList != null
		fList.size() == max
	}
	
	@Ignore
	void "test getResourceMentionsPerBatch"() {
		def fList = []
		when: "getResourceMentionsPerBatch is called"
		fList = dashboardService.getResourceMentionsPerBatch()
		fList.each { println it }
		then: "you should get some results"
		fList != null
	}
	
	@Ignore
	void "test getLatestRegistrySiteValidation"() {
		def fList = null
		when: "getLatestRegistrySiteValidation is called"
		fList = dashboardService.getLatestRegistrySiteValidation()
		println fList
		then: "you should get some results"
		fList != null
	}
	
	void "test getResourceMentionsByPublisher"() {
		def flist = []
		when: "getResourceMentionsByPublisher is called"
		flist = dashboardService.getResourceMentionsByPublisher()
		flist.each { println it }
		then: "you should get some results"
		flist != null
	}
	
	void "test getMostCitedResourcesFromPublishers"() {
		int max
		def fList = []
		given: "a max "
		max = 20
		when: "getMostCitedResourcesFromPublishers is called"
		fList = dashboardService.getMostCitedResourcesFromPublishers(max)
		fList.each { println it }
		then: "you should get some results"
		fList != null
		fList.size() == max
	}
}