package org.neuinfo.rdw

import grails.transaction.Transactional

@Transactional
class AcronymService {

	def getAcronymsWithExpansions(int max, int offset, String value = null) {
		String hql = "from Acronym a order by a.frequency desc"
		def list = null
		if (!value || value == '*') {
			list = Acronym.executeQuery(hql, [max:max, offset:offset])
		} else {
			list = Acronym.executeQuery("from Acronym a where a.acronym = :value order by a.frequency desc",
			[max:max, offset:offset, value:value])
		}
		return list
	}

	def getAcronymCount(String value = null) {
		def list = null
		if (!value || value == '*') {
			list = Acronym.executeQuery("select count(a.id) from Acronym a")
		} else {
		   return 1
		}
		return list[0] as int
	}

	def findPapersForExpansion(String acronym, String expansion, int max, int offset) {
		def list = PaperAcronyms.executeQuery("select a.pmid, p.title, p.journalTitle from PaperAcronyms a, Paper p where a.acronym = :acronym " +
		"and a.expansion = :expansion and a.pmid = p.pubmedId", [acronym:acronym, expansion:expansion, max:max, offset:offset])

		def paperList = []
		list.each {
			row ->
			PaperInfo pi = new PaperInfo(pmid:row[0], title: row[1], journal: row[2])
			paperList << pi
		}
		return paperList
	}

	def getPapersForExpansionSize(String acronym, String expansion) {
		def list = PaperAcronyms.executeQuery("select count(p.id) from PaperAcronyms a, Paper p where a.acronym = :acronym " +
		"and a.expansion = :expansion and a.pmid = p.pubmedId", [acronym:acronym, expansion:expansion])
		return list[0] as int
	}

	static class PaperInfo {
		String title
		String journal
		String pmid
	}
}
