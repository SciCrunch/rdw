package org.neuinfo.rdw

class CombinedResourceRef {
	String pubmedId
	String nifId
	String src
	Integer registryId
	Double confidence
	
	static mapping = {
	    // id generator: "assigned"
		version false
		table 'rd_comb_resource_ref'
	}
	
    static constraints = {
		confidence nullable:true
		nifId maxSize: 30
		src maxSize: 10
    }
}
