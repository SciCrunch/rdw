package org.neuinfo.rdw

import org.springframework.cache.annotation.Cacheable;

import groovy.sql.Sql
import groovy.transform.ToString;

class LinkStatsVizService {
	def sessionFactory

	@Cacheable(value="rdw", key='#root.methodName.concat(#root.args)')
	def getMostCitedResources(int max) {
		def rows = Urls.executeQuery(
				"select r.id, r.resourceName, count(u.id) as cnt from Urls u inner join u.registry r group by r.resourceName, r.id " +
				"order by count(u.id) desc", [max:max, offset:0])
		def list = []
		rows.each { r ->
			list << new Frequency(id: r[0] as int, label: r[1], count: r[2] as int)
		}
		return list
	}

	@Cacheable(value="rdw", key='#root.methodName.concat(#root.args)')
	def getLinksByYearForResource(int registryId) {
		def rows = Urls.executeQuery("select p.publicationDate from Urls u inner join u.paper p  where " +
				"p.publicationDate is not null and u.registry.id = :rid", [rid:registryId as long])
		def map = [:]
		rows.each { r ->
			String year = r.substring(0,4)
			int key = year as int
			Frequency f = map[(key)]
			if (!f) {
				f = new Frequency(label: year)
				map[(key)] = f
			}
			f.count++
		}
		map = map.sort { it.key }

		def list = []
		map.values().each { list << it }
		return list
	}
	
	@Cacheable(value="rdw", key='#root.methodName.concat(#root.args)')
	def getLinkAliveStatus() {
		def sql = new Sql(sessionFactory.currentSession.connection())
		def map = [:]
		sql.eachRow(
				"""select substr(p.pubdate, 1,4) as year, count(u.id) from rd_urls u, rd_paper p, rd_url_status s  
where u.doc_id = p.id and p.pubdate is not null and u.id = s.url_id 
and s.flags = 1 and s.alive = true group by substr(p.pubdate,1,4)""") { r ->
					int year = r[0] as int
					map[(year)] = new LinkStatusStat(alive: r[1], year:year)
				}
		sql.eachRow(
				"""select substr(p.pubdate, 1,4) as year, count(u.id) from rd_urls u, rd_paper p, rd_url_status s
where u.doc_id = p.id and p.pubdate is not null and u.id = s.url_id 
and s.flags = 1 and s.alive = false group by substr(p.pubdate,1,4)""") { r ->
					int year = r[0] as int
					def lss = map[(year)]
					if (lss) {
						lss.dead = r[1]
					}
				}
         map = map.sort { it.key }
		 def list = []
		 map.values().each { lss ->
			 // println lss
			 if (lss.year > 2000) {
				 list << lss
			 }
		 }
		 return list
	}

	@ToString
	static class Frequency {
		int id
		String label
		int count
	}

	@ToString
	static class LinkStatusStat {
		int year
		int alive
		int dead
	}
}
