<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="main"/>
<title>Publisher Resource Filter Classifier Manager</title>
<g:javascript library="jquery" />
<g:javascript>
  $(function() { 
       $('.menu').fixedMenu();
   });
</g:javascript>
</head>
<body>
  <g:render template="/common_menu"></g:render>
  <div id="page-body" class="content" role="main">
		<h1>Publisher Resource Filter Classifier Management</h1>
		<p style="margin:15px;">
		   Trains a classifier using the curator supplied labels for the resources mentions 
		   retrieved from publisher/Neuinfo searches.
		</p>
		<g:if test="${flash.message}">
			<div class="message" style="display: block">
				${flash.message}
			</div>
		</g:if>
		<g:form>
			<fieldset class="buttons">
				<g:actionSubmit controller="resourceFilterClassifier"
					id="submit-but" action="trainPublisher" value="Train" />
				<g:actionSubmit controller="resourceFilterClassifier"
					id="submit-but" action="testPublisher" value="Test" />
			</fieldset>
		</g:form>
	</div>

</body>
</html>