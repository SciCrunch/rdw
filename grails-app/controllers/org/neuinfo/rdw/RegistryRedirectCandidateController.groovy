package org.neuinfo.rdw

class RegistryRedirectCandidateController {
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
		int totCount = 0
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			totCount = registryUpdateService.countRedirectCandidates()
		}
		println "totCount:$totCount"
		def rcList = registryUpdateService.getRedirectCandidates(params.max, params.offset)

		render(view:'list', model:[rcList:rcList, totCount:totCount])
	}

	def getOrigContent() {
		int registryId = params.registryId as int
		def map = registryUpdateService.getOrigSiteContent(registryId)
		render(contentType:'text/json') { map }
	}

	def saveUserAnnot() {
		String rcId = params.urId
		String label = params.label
		String notes = null
		println "rcId: $rcId label:$label"
		String modUser = session.user.loginId
		if (params.notes) {
			notes = params.notes
		}
		registryUpdateService.saveRedirectCandidateAnnotInfo(rcId as int, label, modUser, notes)
		render(status:200)
	}

	def getUserNote() {
		String rcId = params.rcId
		RegistryRedirectAnnotInfo rrai = RegistryRedirectAnnotInfo.get(rcId as int)
		String redirectUrl = ''
		String notes = ''
		if (rrai.notes) {
			notes = rrai.notes
		}
		if (rrai.redirectUrl) {
			redirectUrl = rrai.redirectUrl
		}
		render(contentType:'text/json') { result(notes:notes, redirectUrl: redirectUrl) }
	}

}
