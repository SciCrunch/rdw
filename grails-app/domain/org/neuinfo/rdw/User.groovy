package org.neuinfo.rdw

import java.util.Date;

class User {
	String loginId
	String password
	String role = 'curator'
	String email
	Date dateCreated
	
	static hasMany = [resources : Registry]
	
	static mapping = {
		//id generator: "assigned"
		version false
		table "rd_user"
		resources joinTable: [name: 'rd_user_registry', column: 'registry_id', key: 'user_id'], lazy:false
	}
	static constraints = {
		loginId size:4..20, unique: true
		password size:5..20, password:true
		role inList:['admin','curator','resource']
		email email:true, nullable:true
	}
}
