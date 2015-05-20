package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class UrlStatus {
    Long urlID
	Boolean alive
	int flags = 0
	int type = 0
	Date lastModifiedTime
	
	static mapping = {
	//	id generator: "assigned"
	  version false
	  table 'rd_url_status'
	  urlID column: 'url_id'
	  lastModifiedTime column: 'last_mod_time'
	}
	
    static constraints = {
		alive nullable:true
		lastModifiedTime nullable:true
    }
}
