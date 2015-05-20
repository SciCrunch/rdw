package org.neuinfo.rdw

import spock.lang.Ignore;
import grails.test.spock.IntegrationSpec

class ResourceRefServiceSpec extends IntegrationSpec {
	def resourceRefService

	def setup() {
	}

	def cleanup() {
	}

	@Ignore
	void "test addURLAnnotInfoWS"() {
		String pmid = '23795294'
		String nifId = 'nif-0000-30076'
		when: "addURLAnnotInfoWS is called"
		resourceRefService.addURLAnnotInfoWS(pmid, nifId, 'good')
		then: "you should not get an error"
	}
	
	void "test addPaperWS"() {
		String pmid = '99999'
		String nifId = 'nif-0000-30076'
		when: "addPaperWS is called"
		resourceRefService.addPaperWS(pmid, nifId, 'Some title')
		then: "you should not get an error"
	}
		
}