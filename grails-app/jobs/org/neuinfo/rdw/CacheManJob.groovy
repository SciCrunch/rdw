package org.neuinfo.rdw

import org.springframework.cache.Cache

class CacheManJob {
	def grailsCacheManager
	def dashboardService
	def linkStatsVizService
	
    static triggers = {
		// simple repeatInterval:60000l
		 cron name: 'rdwTrigger', cronExpression: "0 1 23 * * ?"
    }
	def group = "rdwGroup"

    def execute() {
		println "starting cache eviction"
		Cache cache = grailsCacheManager.getCache('rdw')
		cache.clear()
		int max = 30
		dashboardService.getNMostCitedResources(max)
		dashboardService.getResourceMentionsPerBatch()
		dashboardService.getLatestRegistrySiteValidation()
		dashboardService.getMostCitedResourcesFromNER(max)
		dashboardService.getMostCitedResourcesFromPublishers(max)
		dashboardService.getResourceMentionsByPublisher()
		println "finished dashboard cache refresh"
		linkStatsVizService.getMostCitedResources(100)
		linkStatsVizService.getLinkAliveStatus()
		println "finished linkStatsVizService cache refresh"
    }
}
