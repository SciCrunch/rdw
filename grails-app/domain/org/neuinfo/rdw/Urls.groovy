package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class Urls {
	String description
	Integer lineNumberInFile
	Integer rank
	String resourceType
	Double score
	String updateInfo
	String url
	Paper paper
	Registry registry
	String batchId
	Double cScore
	String context
	int flags = 0
	Integer hostLinkSize
	Integer resourceTypeSource

	static mapping = {
		version false
		table 'rd_urls'
		registry column: 'registry_id'
		paper column: 'doc_id'
		description type: 'text'
		//updateInfo type: 'text'
		context type:'text'
		hostLinkSize column: 'host_link_size'
		resourceTypeSource column: 'resource_type_src'
	}

	static constraints = {
		id generator: "assigned"
		description nullable: true
		lineNumberInFile nullable: true
		rank nullable: true
		resourceType nullable: true
		score nullable: true
		updateInfo nullable: true, maxSize:2048
		batchId maxSize: 6
		cScore nullable:true
		registry nullable:true
		context nullable:true
		hostLinkSize nullable:true
		resourceTypeSource nullable:true
	}
}
