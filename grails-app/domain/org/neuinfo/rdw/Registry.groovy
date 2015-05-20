package org.neuinfo.rdw

import groovy.transform.ToString;

@ToString
class Registry {

	String uuid
	String nifId
	String resourceName
	String abbrev
	String availability
	String description
	String url
	String parentOrganization
	String parentOrganizationId
	String supportingAgency
	String supportingAgencyId
	String resourceType
	String resourceTypeIds
	String keyword
	String nifPmidLink
	String publicationlink
	Integer resourceUpdated
	String grants
	String synonym
	String logo
	String comment
	String licenseUrl
	String licenseText
	Integer dateCreated
	Integer dateUpdated
	String curationStatus
	Integer indexTime
	String superCategory

	static mapping = {
		// id generator: "assigned"
		version false
		abbrev type: 'text'
		availability type: 'text'
		description type: 'text'
		url type: 'text'
		parentOrganization type: 'text'
		parentOrganizationId type: 'text'
		supportingAgency type: 'text'
		supportingAgencyId type: 'text'
		resourceType type: 'text'
		resourceTypeIds type: 'text'
		keyword type: 'text'
		nifPmidLink type: 'text'
		publicationlink type: 'text'
		grants type: 'text'
		synonym type: 'text'
		logo type: 'text'
		comment type: 'text'
		licenseUrl type: 'text'
		licenseText type: 'text'
		curationStatus type: 'text'
		uuid type: 'text'
		nifId type: 'text'
		resourceName type: 'text'
		superCategory column:'supercategory' 
	}

	static constraints = {
		uuid nullable: true
		nifId nullable: true
		resourceName nullable: true
		abbrev nullable: true
		availability nullable: true
		description nullable: true, maxSize: 65535
		url nullable: true, maxSize: 65535
		parentOrganization nullable: true, maxSize: 65535
		parentOrganizationId nullable: true, maxSize: 65535
		supportingAgency nullable: true, maxSize: 65535
		supportingAgencyId nullable: true, maxSize: 65535
		resourceType nullable: true, maxSize: 65535
		resourceTypeIds nullable: true, maxSize: 65535
		keyword nullable: true, maxSize: 65535
		nifPmidLink nullable: true, maxSize: 65535
		publicationlink nullable: true, maxSize: 65535
		resourceUpdated nullable: true
		grants nullable: true, maxSize: 65535
		synonym nullable: true, maxSize: 65535
		logo nullable: true, maxSize: 65535
		comment nullable: true, maxSize: 65535
		licenseUrl nullable: true, maxSize: 65535
		licenseText nullable: true, maxSize: 65535
		dateCreated nullable: true
		dateUpdated nullable: true
		curationStatus nullable: true, maxSize: 65535
		indexTime nullable: true
		superCategory nullable:true
	}
}
