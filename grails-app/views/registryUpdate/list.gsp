<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Registry Site Update Information</title>
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<g:javascript>
  $(function() {
     $('.menu').fixedMenu();
     var showContentsUrl = "${g.createLink(controller:'registryUpdate', action:'getSiteContents')}";
     var rdw = nif.rdwModule;
     
     rdw.prepSiteDiffButton('div.show-diff-but', showContentsUrl);
   });
</g:javascript>
<style>
div.paginateButtons {
	font-size: 1.3em;
	line-height: 150%;
	padding: 5px;
}

.paginateButtons a.step,a.nextLink,a.prevLink,span.currentStep {
	padding-left: 5px;
}

a.more,a.less {
	padding-left: 5px;
	font-weight: bold;
}

.has-notes,.has-resource {
	border: 2px solid #2E8B57;
}

.ui-autocomplete-loading {
	background: white url('../images/ui-anim_basic_16x16.gif') right center
		no-repeat;
}

.hilight {
	color: maroon;
	font-weight: bold;
}

.journalTitle {
	font-style: italic;
	padding-top: 4px;
}

#diff-panel {
	border: 1px solid #cccccc;
	padding: 5px;
	background-color: white;
	margin: 5px;
	font-size: 12px;
}

.diff-left {
	border: 1px solid #cccccc;
	padding: 5px;
	background-color: white;
	margin: 5px;
	float: left
}

.diff-right {
	border: 1px solid #cccccc;
	padding: 5px;
	background-color: white;
	margin: 5px;
	float: right;
}
</style>
</head>
<body>
	<g:render template="/common_menu" />

	<div id="page-body" class="content" role="main">
		<div id="dp"></div>
		<div id="filterDiv">
			<g:form action="search">
				<fieldSet>					
					<label
						for="opType" style="font-weight: bold">Sort By:</label>
						 <g:select name="opType"  id="opType" value = "${opType}"
						 from="${['default':'Jaccard Similarity','cosSim':'Cosine Similarity','semantic':'Semantic Similarity']}"
						 optionKey="key" optionValue="value" />
												
					<input type="submit" value="Filter">
				</fieldSet>
			</g:form>
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<th style="width: 40%">Registry</th>
				<th>Similarity</th>
				<th>Containment</th>
				<th>Last CheckedDate</th>
				<th style="width: 50px;">&nbsp;</th>
			</thead>
			<tbody>
				<g:each in="${rusList}" status="i" var="rus">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 40%; word-wrap: break-word;">
							${rus.registry.resourceName}<br> <a href="${rus.registry.url}">
								${rus.registry.url}
						</a> <g:if test="${redirectUrls[i] }">
								<br />
								<span style="font-weight: bold;">Redirect URL:</span>
								<a href="${redirectUrls[i]}">
									${redirectUrls[i]}
								</a>
							</g:if>
						</td>
						<td><g:formatNumber number="${rus.similarity}" format="#.###" />
							(<g:formatNumber number="${rus.cosSimilarity}" format="#.###" /> / <g:formatNumber number="${rus.semSimilarity}" format="#.###" />)
						</td>
						<td><g:formatNumber number="${rus.containment}"
								format="#.###" /></td>
						<td>
							${rus.lastCheckedTime}
						</td>
						<td>
							<div>
								<div id="${'show_' + rus.registry.id}" class="show-diff-but"
									style="height: 32px; width: 32px; display: inline-block;">
								</div>
							</div>
						</td>

					</tr>
				</g:each>
			</tbody>
			<tfoot>
				<div class="paginateButtons">
					<g:paginate action="search" total="${totCount}"
						params="${[totCount:totCount, opType: opType]}" />
				</div>
			</tfoot>
		</table>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount, opType: opType]}" />
		</div>
	</div>

	<div id='dialog-diff' title='Site Content Difference'
		style='font-size: 12px; display: none'>
		<form>
			<h3 id="dlg-header"></h3>
			<table>
				<thead>
					<th>Original</th>
					<th>Latest</th>
				</thead>
				<tbody>
					<tr>
						<td id="original" style="width: 50%"></td>
						<td id="latest"></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</body>
</html>
