package org.neuinfo.rdw

class Publisher {
	String apiKey
	Integer connectionsAllowed
	String publisherName

	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_publisher'
	}

	static constraints = {
		apiKey nullable: true
		connectionsAllowed nullable: true
		publisherName nullable: true
	}
}
