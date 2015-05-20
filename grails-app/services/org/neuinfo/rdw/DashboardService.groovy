package org.neuinfo.rdw

import org.springframework.cache.annotation.Cacheable;

import groovy.transform.ToString;


class DashboardService {
	@Cacheable(value="rdw", key='#root.methodName.concat(#root.args)')
	def getNMostCitedResources(int max) {
		def rows = Urls.executeQuery(
				"select r.resourceName, count(u.id) as cnt from Urls u inner join u.registry r group by r.resourceName " +
				"order by count(u.id) desc", [max:max, offset:0])
		def list = []
		rows.each { r ->
			list << new Frequency(label: r[0], count: r[1] as int)
		}
		return list
	}

	@Cacheable(value="rdw", key='#root.methodName.concat(#root.args)')
	def getResourceMentionsPerBatch() {
		def rows = Urls.executeQuery("select  u.batchId, count(u.id) as cnt from Urls u " +
				"where u.registry.id is not null group by u.batchId")
		def list = []
		rows.each { r ->
			list << new Frequency(label: r[0], count:r[1] as int )
		}
		return list
	}

	@Cacheable(value='rdw', key='#root.methodName.concat(#root.args)')
	def getLatestRegistrySiteValidation() {
		def rows = ValidationStatus.executeQuery(
				"select v.registryId, v.isUp, v.lastCheckedTime from ValidationStatus v " +
				"order by v.registryId, v.lastCheckedTime desc")
		int upCount = 0, downCount = 0
		def curRegId = null
		rows.each { r ->
			if (!curRegId || r[0] != curRegId) {
				curRegId = r[0]
				boolean up = r[1] as boolean
				if (up) {
					upCount++
				} else {
					downCount++
				}
			}
		}
		def list = []
		list << upCount; list << downCount
		return list
	}

	@Cacheable(value='rdw', key='#root.methodName.concat(#root.args)')
	def getMostCitedResourcesFromNER(int max) {
		def rows = ResourceRec.executeQuery(
			"select r.resourceName, count(a.registry.id) from ResourceRec a inner join a.registry r " +
			"where a.flags = 0 group by r.resourceName order by count(a.registry.id) desc",
			[max:max, offset:0])
		def list = []
		rows.each { r ->
			list << new Frequency(label: r[0], count: r[1] as int)
		}
		return list
	}
	
	@Cacheable(value='rdw', key='#root.methodName.concat(#root.args)')
	def getMostCitedResourcesFromPublishers(int max) {
		def rows = PaperReference.executeQuery(
				"select r.resourceName, count(p.registry.id)  from PaperReference p inner join p.registry r " +
				"where p.flags = 1  group by r.resourceName order by count(p.registry.id) desc",
				[max:max, offset:0])
		def list = []
		rows.each { r ->
			list << new Frequency(label: r[0], count: r[1] as int)
		}
		return list
	}

	@Cacheable(value='rdw', key='#root.methodName.concat(#root.args)')
	def getResourceMentionsByPublisher() {
		def rows = PaperReference.executeQuery(
				"select a.publisherName, count(p.publisher.id)  from PaperReference p inner join p.publisher a " +
				"where p.flags = 1 group by a.publisherName")
		def list = []
		rows.each { r ->
			list << new Frequency(label: r[0], count: r[1] as int)
		}
		return list
	}


	@ToString
	static class Frequency {
		String label
		int count
	}
}
