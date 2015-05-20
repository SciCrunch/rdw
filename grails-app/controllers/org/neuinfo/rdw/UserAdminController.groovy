package org.neuinfo.rdw

import grails.converters.JSON

class UserAdminController {
	def resourceFilterService
	def beforeInterceptor = [action:this.&auth]

	def auth() {
		if (!session.user || session.user.role != 'admin') {
			redirect(controller:"User", action:'login')
			return false;
		}
	}

	def index() {
		def users = User.list();
		render(view:'list', model:[users:users])
	}

	def list() {
		index()
	}

	def create() {
		[userInstance: new User(params)]
	}

	def editUser() {
		User theUser = User.get(params.userId)
		assert theUser
		render(view:'create', model:[userInstance: theUser])
	}
	
	def removeUser() {
		User theUser = User.get(params.userId)
		if (theUser) {
			theUser.delete(flush:true)
		}
		index()
	}

	def save() {
		def userInstance = new User(params)
		if (!userInstance.save(flush:true)) {
			render(view:'create', model:[userInstance: userInstance])
			return
		}
		flash.message = message(code: 'default.created.message',
		args:[
			message(code:'user.loginId', default:'User'),
			userInstance.id
		])
		redirect(action:'list')
	}

	def addResource() {
		def userInstance = new User(params)
		User theUser = User.findByLoginId(params.loginId)
		if (!theUser) {
			if (!userInstance.save(flush:true)) {
				render(view:'create', model:[userInstance: userInstance])
				return
			}
			theUser = userInstance
		}
		if (params.resourceName) {
			Registry reg = Registry.findByResourceName(params.resourceName)
			if (reg) {
				theUser.addToResources(reg)
			}
			if (!theUser.save(flush:true)) {
				render(view:'create', model:[theUser: userInstance])
				return
			}
		}
		render(view:'create', model:[userInstance: theUser])
	}

	def removeResource() {
		User theUser = User.get(params.userId)
		if (theUser) {
			String resourceName = params.resourceName
			Registry reg = Registry.findByResourceName(params.resourceName)
			if (reg) {
				theUser.removeFromResources(reg)
				theUser.save(flush:true)
			}
		}
		render(view:'create', model:[userInstance: theUser])
	}

	def getResourceNames() {
		String term = params.term
		println "term:$term"
		def resourceNames = resourceFilterService.getResourceNames(term)
		render resourceNames as JSON
	}
}
