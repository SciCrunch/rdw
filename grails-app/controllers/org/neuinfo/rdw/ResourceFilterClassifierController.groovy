package org.neuinfo.rdw

import java.text.SimpleDateFormat

class ResourceFilterClassifierController {
	def resourceFilterMLService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user || session.user.loginId != 'admin') {
			redirect(controller:"User", action:'login')
			return false;
		}
	}

	def show() {
		def batchIds = resourceFilterMLService.getBatchIds()
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMM')
		batchIds.sort {String a, String b -> sdf.parse(a).compareTo( sdf.parse(b)) }
		render(view:'show', model:[batchIds:batchIds])
	}
	
	def index() {
		redirect(action:'show')
	}
	
	def train() {
		def batchIds = params.list('batchId')
		println "train:: batchIds:$batchIds"
		
		resourceFilterMLService.trainResourceCandidateClassifier(batchIds)
		flash.message = "Training complete for batch: " + batchIds.join(',')
		redirect(action:'show')
	}
	
	def test() {
		def batchIds = params.list('batchId')
		println "test:: batchIds:$batchIds"
		resourceFilterMLService.classifyResourceCandidates(batchIds, 10)
		flash.message = "Filtering complete for batch:" + batchIds.join(',')
		redirect(action:'show')
	}
	
	def trainNER() {
		resourceFilterMLService.trainNERResourceClassifier()
		flash.message = "Training complete for NER resource filtering"
		redirect(action:'showNER')
	}
	
	def testNER() {
		resourceFilterMLService.classifyNERResources()
		flash.message = "Filtering complete for NER resources"
		redirect(action:'showNER')
	}
	
	def showNER() {
		render(view:'showNER')
	}
	
	def trainPublisher() {
		resourceFilterMLService.trainPublisherResourceClassifier()
		flash.message = "Training complete for publisher resource filtering"
		redirect(action:'showPublisher')
	}
	
	def testPublisher() {
		resourceFilterMLService.classifyPublisherResources()
		flash.message = "Filtering complete for publisher resources"
		redirect(action:'showPublisher')
	}
	
	def showPublisher() {
		render(view:'showPublisher')
	}
}
