package org.neuinfo.rdw

import grails.test.spock.IntegrationSpec

class AcronymServiceSpec extends IntegrationSpec {
	def acronymService

	def setup() {
	}

	def cleanup() {
	}

	void "test getAcronymsWithExpansions"() {
		def alist = null
		when: "getAcronymsWithExpansions is called"
		alist = acronymService.getAcronymsWithExpansions(10, 0)
		alist.each { println it }
		then: "you should get some results"
		!alist.isEmpty()
		alist[0].expansions
	}
	
	void "test findPapersForExpansion"() {
		def list = null
		when: "findPapersForExpansion() is called"
		list = acronymService.findPapersForExpansion('PBS','phosphate buffered saline', 10, 0)
		list.each { println it }
		then: "you should get some results"
		!list.isEmpty()
	}
}
