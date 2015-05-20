package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class Paper {

	String doi
	String filePath
	String pubmedId
	String pmcId
	String title
	String journalTitle
	String publicationDate

	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_paper'
		//title type: 'text'
		//journalTitle type: 'text'
		publicationDate column: 'pubdate'
	}

	static constraints = {		
		doi nullable: true
		filePath nullable: true
		pubmedId nullable: true
		pmcId nullable: true, maxSize: 20
		title nullable:true, maxSize:2000
		journalTitle nullable:true, maxSize: 1000
		publicationDate nullable:true
	}
}
