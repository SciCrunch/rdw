package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class PaperAcronyms {
	String acronym
	String expansion
	String pmid

	static mapping = {
		version false
		table 'rd_paper_acronyms'
		pmid column: 'pubmed_id'
	}
	static constraints = {
		id generator: "assigned"
		acronym maxSize:100
		pmid maxSize:20
	}
}
