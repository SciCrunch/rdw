<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main" />
<title>Resource Filter Classifier Manager</title>
<g:javascript library="jquery" />
<g:javascript>
  $(function() { 
       $('.menu').fixedMenu();
   });
</g:javascript>
</head>
<body>
	<a href="#create-user" class="skip" tabindex="-1"><g:message
			code="default.link.skip.label" default="Skip to content&hellip;" /></a>
     <g:render template="/common_menu" /> 
	
	<div id="page-body" class="content" role="main">
	   <h1>Resource Filter Classifier Management</h1>
	   <g:if test="${flash.message}">
            <div class="message" style="display: block">${flash.message}</div>
        </g:if>
		<g:form>
			<ol class="property-list user">
				<li class="fieldcontain required">
				 <span id="search-label" class="property-label">Batch ID (yyyymm):</span>&nbsp; 
					<g:select id="batchId" name="batchId" from="${batchIds}" multiple="true" />
				</li>
			</ol>
			<fieldset class="buttons">
				<g:actionSubmit controller="resourceFilterClassifier"
					id="submit-but" action="train" value="Train" />
				<g:actionSubmit controller="resourceFilterClassifier"
					id="submit-but" action="test" value="Test" />

			</fieldset>
		</g:form>

	</div>
</body>
</html>
