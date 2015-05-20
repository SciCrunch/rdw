package org.neuinfo.rdw

class PublisherQueryLog {
	Date execTime
	String queryStr
	Long publisherId
	Long registryId

	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_publisher_query_log'
	}

	static constraints = {
		execTime nullable: true
		queryStr nullable: true, maxSize: 1000
		publisherId nullable: true
		registryId nullable: true
	}
}
