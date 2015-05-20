package org.neuinfo.rdw

class ResourceStatus {

	Date lastCheckedTime
	Boolean isValid
	Long docId
	Long registryId

	static mapping = {
		//id generator: "assigned"
		version false
		table 'rd_resource_status'
	}

	static constraints = {
		lastCheckedTime nullable: true
		isValid nullable: true
		docId nullable: true
		registryId nullable: true
	}
}
