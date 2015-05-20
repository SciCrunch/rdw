package org.neuinfo.rdw

class PaperReference {

	String publisherDocId
	String pubmedId
	Publisher publisher
	Registry registry
	String genre
	String publicationDate
	String publicationName
	String title
	Long queryLogId
	int flags
	Double cScore
	String description
	String authors
	String meshHeadings

	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_paper_reference'
		registry column: 'registry_id'
		publisher column: 'publisher_id'
		description type: 'text'
		authors type: 'text'
		meshHeadings type: 'text'
	}

	static constraints = {
		publisherDocId nullable: true, maxSize: 100
		pubmedId nullable: true
		publisher nullable: true
		registry nullable: true
		genre nullable: true, maxSize: 40
		publicationDate nullable: true, maxSize: 40
		publicationName nullable: true, maxSize: 1000
		title nullable: true, maxSize: 1000
		queryLogId nullable: true
		cScore nullable:true
		description nullable:true
		authors nullable:true
		meshHeadings nullable:true
	}
}
