package org.neuinfo.rdw

class ResourceCandidate {
	String description
	Date modTime
	String resourceType
	Double score
	Long urlId
	String title
	String batchId
	String status

	static mapping = {
		//id generator: "assigned"
		version false
		table 'rd_resource_candidate'
		description type: 'text'
		//title type: 'text'
	}

	static constraints = {
		description nullable: true
		modTime nullable: true
		resourceType nullable: true
		score nullable: true
		urlId nullable: true
		title nullable: true, maxSize:512
		batchId nullable: true, maxSize: 6
		status nullable: true, maxSize: 20
	}
}
