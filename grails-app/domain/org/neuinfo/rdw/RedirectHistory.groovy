package org.neuinfo.rdw

class RedirectHistory {
	String redirectUrl
	Registry registry
	Date modificationTime
	String modifiedBy
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_redirect_history'
		registry column: 'registry_id'
		modificationTime column: 'mod_time'
	}
    static constraints = {
		modifiedBy maxSize:40
    }
}
