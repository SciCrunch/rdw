<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main" />
<title>Registry Mentions in PubMED Central Open Access Papers</title>
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
		<h1>PMC Registry Mentions Searcher</h1>
		<g:form>
			<ol class="property-list user">
				<li class="fieldcontain required"><span id="search-label"
					class="property-label">Batch ID (yyyymm):</span>&nbsp; <%--    
		         <g:textField id="batchId" name="batchId" value="${batchId}" required="" /> 
		      --%> <g:select id="batchId" name="batchId" from="${batchIds}" />
				</li>

			</ol>
			<fieldset class="buttons">
				<g:actionSubmit controller="resourceFilter" id="submit-but"
					action="search" value="Fetch Mentions" />
			</fieldset>
		</g:form>

	</div>
</body>
</html>


