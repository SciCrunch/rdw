<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main">
<title>Resource Disambiguator Web - Users</title>
<g:javascript library="jquery" />

<g:javascript>
   $(function(){
       $('.menu').fixedMenu();
   });
</g:javascript>
</head>
<body>
	<a href="#create-user" class="skip" tabindex="-1"><g:message
			code="default.link.skip.label" default="Skip to content&hellip;" /></a>
	
	<g:render template="/common_menu" />
	<div id="page-body" role="main">
		<table width="90%">
			<thead>
				<th>User Login</th>
				<th>Role</th>
				<th>Email</th>
				<th>Resources</th>
				<th>&nbsp;</th>
			</thead>
			<tbody>
				<g:each in="${users}" status="i" var="user">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td>
							${user.loginId}
						</td>
						<td>
							${user.role}
						</td>
						<td>
							${user.email}
						</td>
						<td>&nbsp;</td>
						<td><g:link action="editUser" params="[userId:user.id]"
								class="edit">Edit</g:link> <g:if test="${user.role != 'admin'}">
						     &nbsp;
						     <g:link action="removeUser" params="[userId:user.id]"
									class="delete">Remove</g:link>
							</g:if></td>
					</tr>
				</g:each>
			</tbody>
		</table>
		<g:form action="create">
			<fieldSet class="buttons">
				<g:submitButton name="addUser" class="save" value="Add User" />
			</fieldSet>
		</g:form>
	</div>
</body>
</html>