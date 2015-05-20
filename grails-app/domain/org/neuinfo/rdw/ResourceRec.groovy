package org.neuinfo.rdw

class ResourceRec {
	String context
	String entity
	int startIdx
	int endIdx
	Paper paper
	Registry registry
	int flags
	Double cScore
	
	static mapping = {
		version false
		table 'rd_resource_ref'
		context type:'text'
		//entity type:'text'
		paper column: 'doc_id'
	}
	
    static constraints = {
		// id generator: "assigned"
		context nullable:true
		entity nullable:true, maxSize:1000
		startIdx nullable:true
		endIdx nullable:true
		registry nullable:true
		cScore nullable:true
    }
}
