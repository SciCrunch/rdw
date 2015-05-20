package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class Acronym {
	String acronym
	Integer frequency
	static hasMany = [expansions: AcronymExpansion]

	static mapping = {
		version false
		table 'rd_acronym'
		expansions column: 'acr_id'
	}
    static constraints = {
		id generator: "assigned"
		acronym maxSize:100
    }
}
