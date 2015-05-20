package org.neuinfo.rdw

class RegUpdateStatus {
	Double containment
	Double similarity
	String updateYear
	String updateLine
	Registry registry
	Date lastCheckedTime
	String batchId
	Double semSimilarity
	Double cosSimilarity
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_reg_update_status'
		updateLine type: 'text'	
		registry column: 'registry_id'
		semSimilarity column: 'sem_similarity'
		cosSimilarity column: 'cos_similarity'
	}
	
    static constraints = {
		containment nullable:true
		similarity nullable:true
		updateYear nullable:true, maxSize:4
		updateLine nullable:true
		lastCheckedTime nullable:true
		batchId maxSize:8
		semSimilarity nullable:true
		cosSimilarity nullable:true
    }
}
