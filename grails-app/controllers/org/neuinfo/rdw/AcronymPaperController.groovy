package org.neuinfo.rdw

class AcronymPaperController {
	def acronymService
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
		println "params2:$params"

		def paperList = acronymService.findPapersForExpansion(params.acronym, params.expansion, params.max, params.offset)
		int totCount = 0;
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			totCount = acronymService.getPapersForExpansionSize(params.acronym, params.expansion)
		}
		render(view:'list', model:[paperList: paperList, totCount:totCount, acronym: params.acronym, expansion: params.expansion])
	}
}
