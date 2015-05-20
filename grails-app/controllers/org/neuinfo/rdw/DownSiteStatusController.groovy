package org.neuinfo.rdw

class DownSiteStatusController {
	def downSiteStatusService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
	}

	
	def index() {
		redirect(action:'show')
	}

	def show() {
		params.max = Math.min(params.max ? params.int('max') : 10, 100);
		params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
		int totCount = 0
		def dssList = null
		dssList = downSiteStatusService.getDownSites(params.max, params.offset)
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			totCount = downSiteStatusService.getDownSiteCount()
		}
		def labels = []
		dssList.each { DownSiteStatus dss ->
			labels << (dss.label ? dss.label : 'nosel')
		}
		print "labels:" + labels
		render(view:'list', model:[dssList:dssList, labels:labels, totCount:totCount])
	}

	def saveUserAnnot() {
		String dssId = params.urId
		String label = params.label
		String modUser = session.user.loginId
		downSiteStatusService.saveDSSAnnotInfo(dssId as int, label, modUser)
		render(status:200)
	}
}
