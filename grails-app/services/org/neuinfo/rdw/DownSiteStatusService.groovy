package org.neuinfo.rdw

import grails.transaction.Transactional

@Transactional
class DownSiteStatusService {

    def getDownSites(int max, int offset) {
		def dssList = null
		dssList = DownSiteStatus.executeQuery("from DownSiteStatus d order by d.id", [max:max, offset:offset])
		
		dssList
    }
	
	def getDownSiteCount() {
		def list = null
		list = DownSiteStatus.executeQuery("select count(d.id) from DownSiteStatus d")
		return list[0] as int
	}
	
	def saveDSSAnnotInfo(int dssId, String label, String modUser) {
		DownSiteStatus dss = DownSiteStatus.get(dssId)
		if (dss) {
			dss.modifiedBy = modUser
			dss.modTime = new Date()
			if (label == 'nosel') {
				dss.label = null
			} else {
			   dss.label = label
			}
			dss.save(failOnError:true, flush:true)
		}
	}
}
