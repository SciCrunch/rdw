package org.neuinfo.rdw

class RegistryRedirectAnnotInfo {
	String redirectUrl
	String label
	Double classiferScore
	String notes
	Registry registry
	Date modificationTime
	String modifiedBy
	Integer status = 1
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_reg_redirect_annot_info'
		registry column: 'registry_id'
		classiferScore column: 'c_score'
		modificationTime column: 'mod_time'
		// notes type: 'text'
		
	}
    static constraints = {
	    redirectUrl nullable:true
		label maxSize:5
		notes nullable:true, maxSize:1024
		modifiedBy maxSize:40	
    }
}
