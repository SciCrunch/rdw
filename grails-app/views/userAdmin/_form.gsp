<%@ page import="org.neuinfo.rdw.User"%>

<div
	class="fieldcontain ${hasErrors(bean: userInstance, field: 'loginId', 'error')} required">
	<label for="loginId"> Login ID <span class="required-indicator">*</span>
	</label>
	<g:textField name="loginId" required=""
		value="${userInstance?.loginId}" />
</div>
<div
	class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} required">
	<label for="password"> <g:message code="user.password.label"
			default="Password" />
			<span class="required-indicator">*</span>
	</label>
	<g:field type="password" name="password"
		value="${userInstance?.password}" />
</div>

<div class="fieldcontain">
	<label for="role">Role</label>
	<g:select name="role" from="${['curator','admin','resource']}" value="${userInstance?.role}" />
</div>

<div class="fieldcontain">
	<label for="email"> Email </label>
	<g:textField name="email" maxlength="40" value="${userInstance?.email}" />
</div>