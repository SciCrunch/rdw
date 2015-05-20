package org.neuinfo.rdw

class RedirectUrl {
	String redirectUrl
	Long urlId

	static mapping = {
		// id generator: "assigned"
		version false
		table 'rd_redirect_url'
	}

	static constraints = {
		urlId nullable: true
	}
}
