package org.neuinfo.rdw


import grails.converters.*
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

class ResourceRefController {
    def resourceRefService
    static allowedMethods = [dumpResourceRefs              : "GET", retrieveLatestValidationStatus: "GET",
                             retrieveInvalidResources      : "GET",
                             retrievePMIDs4Resource        : 'GET', addURLAnnotInfoWS: 'POST', addPaperWS: 'POST',
                             removePaperWS                 : 'POST',
                             retrieveRegistryUpdateYearInfo: 'GET', checkHeartBeat: 'GET']

    def beforeInterceptor = [action: this.&auth, except: [
            'dumpResourceRefs',
            'retrieveLatestValidationStatus',
            'retrievePMIDs4Resource',
            'retrieveContext4PMID',
            'addURLAnnotInfoWS',
            'addPaperWS',
            'removePaperWS',
            'retrieveInvalidResources',
            'retrieveRegistryUpdateYearInfo',
            'checkHeartBeat'
    ]]

    def auth() {
        if (!session.user) {
            redirect(controller: "User", action: 'login')
            return false;
        }
    }

    def checkHeartBeat() {
        def list = Publisher.executeQuery("from Publisher")
        if (!list) {
            render(status: 404)
        } else {
            render(status: 200)
        }
    }

    def getResourceNames() {
        String term = params.term
        println "term:$term"
        def resourceNames = resourceRefService.getResourceNames(term)
        render resourceNames as JSON
    }

    def retrievePMIDs4Resource() {
        String nifId = params.nifId
        int max = params.max ? params.max as int : 50
        int offset = params.offset ? params.offset as int : 0
        println "max:$max offset:$offset"
        def pmids = resourceRefService.getPMIDs4Resource(nifId, max, offset)
        int count = -1;
        if (offset == 0) {
            count = resourceRefService.getPMIDCount4Resource(nifId)
        }
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.result() {
            if (count > 0) {
                numRecs(count)
            }
            for (String p : pmids) {
                pmid(p)
            }
        }
        String data = writer.toString()
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }

    def retrieveContext4PMID() {
        String nifId = params.nifId
        String pmid = params.pmid
        String data = resourceRefService.getContext4PMID(pmid, nifId)
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }

    def addURLAnnotInfoWS() {
        String nifId = params.nifId
        String pmid = params.pmid
        if (!nifId || !pmid) {
            render(status: 400, text: 'Both nifId and pmid are required')
            return
        }
        String label = 'bad'
        if (params.label) {
            label = params.label == 'good' ? 'good' : 'bad'
        }

        println "addURLAnnotInfoWS:: params:$params"

        def uai = resourceRefService.addURLAnnotInfoWS(pmid, nifId, label)

        if (uai) {
            render(status: 200)
        } else {
            render(status: 404, text: 'Did not found any record matching nifId and pmid')
        }
    }

    def addPaperWS() {
        String nifId = params.nifId
        String pmid = params.pmid
        if (!nifId || !pmid) {
            render(status: 400, text: 'Both nifId and pmid are required')
            return
        }
        String title = params.title ?: null
        String journalTitle = params.journalTitle ?: null

        println "addPaperWS:: params:$params"


        def ur = resourceRefService.addPaperWS(pmid, nifId, title, journalTitle)
        if (ur) {
            render(status: 200)
        } else {
            render(status: 404, text: 'Did not found any matching registry resource for the given nifId')
        }
    }

    def removePaperWS() {
        String nifId = params.nifId
        String pmid = params.pmid
        String key = params.key
        if (!nifId || !pmid) {
            render(status: 400, text: 'Both nifId and pmid are required')
            return
        }
        if (!key || key != 'zoN3tLv4c8A=') {
            render(status: 401)
            return
        }
        resourceRefService.removePaperWS(pmid, nifId)
        render(status: 200)
    }


    def dumpResourceRefs() {
        int maxCount = -1
        if (params.maxCount) {
            maxCount = params.maxCount as int
        }
        String data = resourceRefService.dumpResourceRefs2(maxCount)
        // println "data:$data"
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }


    def retrieveLatestValidationStatus() {
        String data = resourceRefService.getLatestValidationStatus()
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }

    def retrieveRegistryUpdateYearInfo() {
        String data = resourceRefService.getLatestUpdateInfo()
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }

    def retrieveInvalidResources() {
        String data = resourceRefService.getInvalidResources2()
        render(contentType: 'text/xml', text: data, encoding: 'UTF-8')
    }


    def show() {
        def batchIds = resourceRefService.getBatchIds()
        //render(view:'show', model:[batchId:'201310'])
        SimpleDateFormat sdf = new SimpleDateFormat('yyyyMM')
		batchIds.sort {String a, String b -> sdf.parse(a).compareTo( sdf.parse(b)) }
        render(view: 'show', model: [batchIds: batchIds])
    }

    def index() {
        redirect(action: 'show')
    }

    def search() {
        String batchId = params.batchId

        println "params:$params"
        params.max = Math.min(params.max ? params.int('max') : 10, 100);
        params.offset = Math.max(params.offset ? params.int('offset') : 0, 0);
        println "params2:$params"

        def urList = null
        int totCount = 0
        String filterType = params.filterTypeChooser ?: ''
        String value = params.filterInput ?: ''
        if (!filterType) {
            filterType = params.filterType ?: ''
        }

        if (session.user.resources) {

            def registryIds = []
            session.user.resources.each { Registry r ->
                registryIds << r.id
            }
            urList = resourceRefService.getUrlsWithRegistryFiltered(batchId, registryIds, params.max, params.offset)
            if (params.totCount) {
                totCount = params.int('totCount')
            } else {
                totCount = resourceRefService.getCountForUrlsWithRegistryFiltered(batchId, registryIds)
            }
        } else {
            urList = resourceRefService.getUrlsWithRegistry(batchId, params.max, params.offset, filterType, value)
            if (params.totCount) {
                totCount = params.int('totCount')
            } else {
                totCount = resourceRefService.getCountForUrlsWithRegistry(batchId, filterType, value)
            }
        }
        if (!urList) {
            render(view: 'list', model: [urList  : urList, totCount: totCount, batchId: batchId, labels: [],
                                         noteList: [], resourceList: []])
            return
        }
        def lnMap = resourceRefService.getURLAnnotInfoForUrls(urList)
        println "resources:" + lnMap['resources']
        render(view: 'list', model: [urList     : urList, totCount: totCount, batchId: batchId, filterType: filterType,
                                     filterInput: value, labels: lnMap['labels'],
                                     noteList   : lnMap['notes'], resourceList: lnMap['resources']])
    }

    def getUserNote() {
        String urId = params.urId
        String notes = resourceRefService.getUserNote(urId as int)
        if (!notes) {
            notes = ''
        }
        render(contentType: 'text/json') { uai(notes: notes) }
    }

    def getPaperInfo() {
        String pmid = params.pmid
        Paper paper = resourceRefService.getPaperInfo(pmid)
        String title = paper.title ?: ''
        String journalTitle = paper.journalTitle ?: ''
        render(contentType: 'text/json') { ['title': title, 'journalTitle': journalTitle] }
    }


    def saveUserAnnot() {
        String urId = params.urId
        String label = params.label
        String notes = null
        println "urId: $urId label:$label"
        String modUser = session.user.loginId
        if (params.notes) {
            notes = params.notes
        }
        resourceRefService.saveURLAnnotInfo(urId as int, label, modUser, notes)
        render(status: 200)
    }
}
