package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class URLAnnotationInfo {
	String label
	String resourceType
	String notes
	Date modTime
	Urls url
	String opType
	String modifiedBy
	Registry registry
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_url_annot_info'
		notes type: 'text'
		registry column: 'registry_id'
	}
	
    static constraints = {
		modTime nullable: true
		resourceType nullable: true
		label nullable: true, maxSize: 5
		notes nullable: true
		opType nullable:false
		modifiedBy nullable:false, maxSize:40
		registry nullable:true
    }
}
