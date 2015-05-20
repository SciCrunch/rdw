package org.neuinfo.rdw

class RegistryUpdateController {
	def registryUpdateService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}
	def index() {
		redirect(action:'search')
	}

	def search() {
		params.max = Math.min(params.max ? params.int('max') : 10, 100);
		params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
		params.opType = params.opType ?: 'cosSim'
		println "params2:$params"
		int totCount = 0
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			totCount = registryUpdateService.countRegUpdateInfos(params.opType)
		}
		println "totCount:$totCount"
		def rusList = registryUpdateService.getRegUpdateInfos(params.max, params.offset, params.opType)
		def redirectUrls = registryUpdateService.getRedirectUrlsForRus(rusList)

                /*
		def opTypes = [
			new KVPair(key:'default',value:'Jaccard Similarity'),
			new KVPair(key:'cosSim',value:'Cosine Similarity'),
			new KVPair(key:'semantic', value:'Semantic Similarity')
		];
                */
		render(view:'list', model:[rusList:rusList, totCount:totCount, redirectUrls:redirectUrls, opType:params.opType])
	}

	static class KVPair {
		String key
		String value
	}

	def getSiteContents() {
		int registryId = params.registryId as int
		def map = registryUpdateService.getSiteContents(registryId)

		render(contentType:'text/json') { map }
	}
}
