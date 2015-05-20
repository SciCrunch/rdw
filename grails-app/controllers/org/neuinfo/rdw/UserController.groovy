package org.neuinfo.rdw

class UserController {
	def beforeInterceptor = [action:this.&auth, except:[
			'authenticate',
			'login',
			'home']
	]

	def auth() {
		if (!session.user) {
			redirect(controller:"User", action:'home')
			return false;
		}
	}

	def home() {
		if (!session.user) {
			redirect(controller:"User", action:'login')
			return false;
		}
		if (session.user.role == 'resource') {
			redirect(controller:'ResourceRef', action:'show')
		} else {
			redirect(controller:'ResourceFilter', action:'show')
		}
	}

	def authenticate() {
		def userInstance = User.findByLoginIdAndPassword(params.loginId, params.password)
		if (userInstance) {
			userInstance.password = null
			session.user = userInstance
			flash.message = "Hello ${userInstance.loginId}!"
			redirect(action:"home")
		} else {
			flash.message = "Could not authenticate '${params.loginId}'. Please Try again"
			redirect(action:'login')
		}
	}

	def logout() {
		flash.message = "Goodbye ${session.user.loginId}!"
		session.user = null
		redirect(action:"home")
	}

	def login() {

	}
}
