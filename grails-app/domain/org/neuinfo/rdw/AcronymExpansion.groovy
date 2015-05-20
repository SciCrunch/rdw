package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class AcronymExpansion {
	String expansion
	Integer frequency
	Integer clusterId
	Acronym acronym

	static mapping = {
		version false
		table 'rd_acronym_expansion'
		acronym column: 'acr_id'
	}
	static constraints = { id generator: "assigned" }
}
