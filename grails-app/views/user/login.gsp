<%@ page import="org.neuinfo.rdw.User" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="User Login" />
		<title>Resource Disambiguator Web - User Login</title>
	</head>
	<body>
		<a href="#create-user" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<g:if test="${session?.user}">
				  <li><g:link class="list" action="logout"><g:message code="Logout" args="[entityName]" /></g:link></li>
				</g:if>
			</ul>
		</div>
		<div id="create-user" class="content scaffold-create" role="main">
			<h1>User Login</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${userInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${userInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form action="authenticate" >
			 <fieldset class="form">
                    <div class="fieldcontain ${hasErrors(bean: userInstance, field: 'loginId', 'error')} ">
                        <label for="userName">
                            <g:message code="user.loginId.label" default="User Name" />
                            
                        </label>
                        <g:textField name="loginId" value="${userInstance?.loginId}"/>
                    </div>
                    
                    <div class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} ">
                        <label for="password">
                            <g:message code="user.password.label" default="Password" />
                            
                        </label>
                        <g:field type="password" name="password" value="${userInstance?.password}"/>
                    </div>
                </fieldset>
                <fieldset class="buttons">
                    <g:submitButton name="login" class="save" value="Login" />
                </fieldset>
			
			</g:form>
		</div>
	</body>
</html>
