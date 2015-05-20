<%@ page contentType="text/html;charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Create New User</title>
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<g:javascript>
     $(function() {
         $('.menu').fixedMenu();
         $('#resourceName').autocomplete({
            source:  "${g.createLink(controller:'userAdmin', action:'getResourceNames')}",
            minLength: 2,
            select: function(event, ui) {
                if (ui.item) {
                   console.log(ui.item);
                }
            }
         }); 
     });
  </g:javascript>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="create-user" class="content" role="main">
		<h1>Create User</h1>
		<g:if test="${flash.message}">
			<div class="message" role="status">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${userInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${userInstance}" var="error">
					<li
						<g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>>
						<g:message error="${error}" />
					</li>
				</g:eachError>
			</ul>
		</g:hasErrors>
		<g:form action="">
			<fieldset class="form">
				<g:render template="form" />

				<div class="fieldcontain">
					<label for="resourceName">Resource</label>
					<g:textField name="resourceName" id="resourceName" />
					<g:actionSubmit action="addResource" class="save" value="Add" />
				</div>
			</fieldset>
			<g:if test="${userInstance.resources}">
				<table>
					<thead>
						<th>Resource</th>
						<th>&nbsp;</th>
					</thead>
					<tbody>
						<g:each in="${userInstance.resources}" status="i" var="reg">
							<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
								<td>
									${reg.resourceName}
								</td>
								<td><g:link action="removeResource"
										params="[userId:userInstance.id, resourceName:reg.resourceName]"
										class="delete">Remove</g:link></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</g:if>
			<fieldset class="buttons">
				<g:actionSubmit action="create" class="save" value="Save" />
			</fieldset>
		</g:form>
	</div>
</body>
</html>