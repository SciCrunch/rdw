package org.neuinfo.rdw

class PubSearchAnnotationInfo {
	String label
	String notes
	Date modTime
	PaperReference paperRef
	String opType = 'ps_filter'
	String modifiedBy
	Registry registry
	
    static constraints = {
		modTime nullable: true
		label nullable: true, maxSize: 5
		notes nullable: true, maxSize:1024
		opType nullable:false, maxSize: 30
		modifiedBy nullable:false, maxSize:40
		registry nullable:true
    }
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_ps_annot_info'
		// notes type: 'text'
		registry column: 'registry_id'
		paperRef column: 'pr_id'
	}
}
