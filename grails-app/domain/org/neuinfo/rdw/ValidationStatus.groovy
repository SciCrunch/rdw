package org.neuinfo.rdw

class ValidationStatus {
	Date lastCheckedTime
	Boolean isUp
	Long registryId
	String message

	static mapping = {
		version false
		table 'rd_validation_status'
	}

	static constraints = {
		lastCheckedTime nullable: true
		isUp nullable: true
		registryId nullable: true
	}
}
