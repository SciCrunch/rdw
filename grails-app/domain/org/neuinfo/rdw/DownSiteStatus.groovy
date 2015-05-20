package org.neuinfo.rdw

class DownSiteStatus {
	String resourceName
	String nifId
	String message
	String batchId
	String url
	String label
	Integer numOfConsecutiveChecks
	Date lastCheckedTime
	Date modTime
	String modifiedBy

	static mapping = {
		version false
		table 'rd_down_site_status'
	}
	static constraints = {
		lastCheckedTime nullable: false
		message nullable: true
		label nullable: true, maxSize: 5
		nifId maxSize: 50
		modTime nullable: true
		modifiedBy nullable:false, maxSize:40
		batchId nullable: false, maxSize:8
	}
}
