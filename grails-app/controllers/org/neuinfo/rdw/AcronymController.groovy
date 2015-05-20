package org.neuinfo.rdw

import grails.converters.JSON
class AcronymController {
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
		String value = params.filterInput ?: ''

		println "params2:$params"
		def acrList = acronymService.getAcronymsWithExpansions(params.max, params.offset, value)

		int totCount = 0;
		if (params.totCount) {
			totCount = params.int('totCount')
		} else {
			totCount = acronymService.getAcronymCount(value)
		}
		def aiList = []
		acrList.each { Acronym a ->
			AcronymInfo ai = new AcronymInfo(acronym: a.acronym, frequency:a.frequency)
			aiList << ai
			int curClusterId = -1
			SenseCluster sc = null
			def expansions = new ArrayList(a.expansions)
			expansions.sort { AcronymExpansion b, c ->  b.clusterId <=> c.clusterId ?: c.frequency <=> b.frequency}
			expansions.each {AcronymExpansion ae ->
				if (curClusterId != ae.clusterId) {
					sc = new SenseCluster(expansion: ae.expansion, canonicalFreq: ae.frequency)
					ai.clusters << sc
					curClusterId = ae.clusterId
				} else {
					AcrExpansionInfo aei = new AcrExpansionInfo(expansion: ae.expansion, frequency: ae.frequency,
					clusterId: ae.clusterId)
					sc.variants << aei
				}
			}
			ai.clusters.each  { SenseCluster asc ->
				asc.variants.sort { AcrExpansionInfo b, c ->  b.clusterId <=> c.clusterId ?: c.frequency <=> b.frequency}
			}
			def collapsedClusters = []
			SenseCluster otherCluster = new SenseCluster(expansion:'Others', canonicalFreq: -1)
			ai.clusters.each  { SenseCluster asc ->
				asc.calcFrequency()
				if (asc.frequency >= 5) {
					collapsedClusters << asc
				} else {
					AcrExpansionInfo aei = new AcrExpansionInfo(expansion:asc.expansion, frequency: asc.canonicalFreq)
					otherCluster.variants << aei
					if (asc.variants) {
						asc.variants.each {AcrExpansionInfo x ->  otherCluster.variants << x }
					}
				}
			}			
			ai.clusters = collapsedClusters
			ai.clusters.sort { SenseCluster b, c ->  c.frequency <=> b.frequency  }
			if (otherCluster.variants) {
				otherCluster.calcFrequency()
				ai.clusters << otherCluster
			}
		}

		render(view:'list', model:[acrList:aiList, totCount:totCount, filterInput:value])
	}

	def getAcronymNames() {
		String term = params.term
		println "term:$term"
		def acronyms =  Acronym.executeQuery('select acronym from Acronym where acronym like :term',[term:"$term%"])
		render acronyms as JSON
	}

	class AcronymInfo {
		String acronym
		int frequency
		def clusters = []
	}

	class SenseCluster {
		String expansion
		int frequency
		int canonicalFreq
		def variants = []

		def calcFrequency() {
			frequency = canonicalFreq
			variants.each { AcrExpansionInfo aei -> frequency += aei.frequency }
		}
	}

	class AcrExpansionInfo {
		String expansion
		int frequency
		int clusterId
	}
}
