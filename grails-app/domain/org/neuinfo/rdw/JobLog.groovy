package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class JobLog {
	String batchId
	Date modTime
	String operation
	String status
	String modifiedBy
	
	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_job_log'
	}
	
    static constraints = {
		batchId maxSize: 1024
		operation maxSize:40
		status maxSize:30
		modifiedBy maxSize:40
    }
}
