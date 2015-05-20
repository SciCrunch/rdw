package org.neuinfo.rdw

class RegistrySiteContent {
	String content
	String title
	Date lastModTime
	String redirectUrl
	Long registryId
	int flags = 1
	
	static mapping = {
		version false
		table 'rd_registry_site_content'
		content type: 'text'
		title type: 'text'	
		//redirectUrl type: 'text'	
	}
	
    static constraints = {
		content nullable:true
		title nullable:true
		registryId nullable:true
		lastModTime nullable:true
		redirectUrl nullable:true, maxSize:500
    }
}
