class UrlMappings {

	static mappings = {
		"/service/heartbeat"(controller:"resourceRef") { action="checkHeartBeat"}
		"/service/resourceRefs"(controller:"resourceRef") { action="dumpResourceRefs"}
		"/service/validationStatus"(controller:"resourceRef")  { action="retrieveLatestValidationStatus"}
		"/service/resource/pmids"(controller:"resourceRef") { action="retrievePMIDs4Resource" }
		"/service/resource/pmid/context"(controller:"resourceRef") { action="retrieveContext4PMID" }
		"/service/resource/annot/save"(controller:"resourceRef") { action="addURLAnnotInfoWS" }
		"/service/resource/paper/add"(controller:"resourceRef") { action="addPaperWS" }
		"/service/resource/paper/remove"(controller:"resourceRef") { action="removePaperWS" }
		"/service/invalidResources"(controller:"resourceRef") { action="retrieveInvalidResources" }
		"/service/lastUpdateYearForResources"(controller:"resourceRef") { action="retrieveRegistryUpdateYearInfo" }
	    "/service/cooccurrence/mostPopular"(controller: "cooccurrenceViz") { action="mostPopularToolCoocs"}
		"/service/cooccurrence/$registryName"(controller: "cooccurrenceViz") { action="preparePairs4Resource"}

		"/$controller/$action?/$id?"{ constraints {
				// apply constraints here
			} }

		// "/"(view:"/index")
		"/"(controller:"user") { action="home" }
		"500"(view:'/error')
		// IBO
		"400"(view:'/error')
		"404"(view:'/error')

	}
}
