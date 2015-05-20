package org.neuinfo.rdw

class NERAnnotationInfo {
	String label
	String notes
	Date modTime
	ResourceRec resourceRec
	String opType
	String modifiedBy
	Registry registry
	
    static constraints = {
		modTime nullable: true
		label nullable: true, maxSize: 5
		notes nullable: true
		opType nullable:false, maxSize: 30
		modifiedBy nullable:false, maxSize:40
		registry nullable:true
    }
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_ner_annot_info'
		notes type: 'text'
		registry column: 'registry_id'
		resourceRec column: 'rr_id'
	}
}
