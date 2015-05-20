<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main" />
<title>Resource Candidates Searcher</title>
<g:javascript library="jquery" />
<r:require module="jquery-ui" />

<g:javascript>
	         $(function() {
	          $('.menu').fixedMenu();
	          var saveUserAnotUrl = "${g.createLink(controller:'resourceFilter', action:'saveUserAnnot')}";
	          var getUserNoteUrl = "${g.createLink(controller:'resourceFilter', action:'getUserNote')}";
	          var getResourcesUrl = "${g.createLink(controller:'resourceFilter', action:'getResourceNames')}";
	          var saveDedupInfoUrl = "${g.createLink(controller:'resourceFilter', action:'saveDeduplicationInfo')}";
	          var getResourceNameUrl = "${g.createLink(controller:'resourceFilter', action:'getResourceName')}";
	          var getResourceTypesUrl =  "${g.createLink(controller:'resourceFilter', action:'getResourceTypes')}";
	          var rdw = nif.rdwModule;
	          
	          rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
	          rdw.toggleTextExpansion('td.description', 300);
	          rdw.prepNotesButton('div.user-notes-but', saveUserAnotUrl, getUserNoteUrl);
	          rdw.prepResourceTypesCB('.user-rt-cb', saveUserAnotUrl);
	          
	          rdw.prepToolsButton('div.user-tools-but', getResourcesUrl, saveDedupInfoUrl, getResourceNameUrl);
	          // rdw.handleResourceFilter($('#filterDiv'), getResourceTypesUrl);
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

.classifier {
  background-color: #fafacd;
}
</style>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<h1>Resource Candidates from PMC Open Access</h1>
		<div id="filterDiv">
			<g:form action="search">
				<fieldSet>
					<input type="hidden" name="batchId" value="${batchId}"> <label
						for="filterTypeChooser" style="font-weight: bold">Filter
						By:</label> <select name="filterTypeChooser" id="filterTypeChooser">
						<option value="resourceType">Resource Type</option>
					</select> <input id="filterInput" name="filterInput" value="${filterInput}">
					<input type="submit" value="Filter">
				</fieldSet>
			</g:form>
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<tr>
					<th style="width: 30%;">URL</th>
					<th style="width: 50%;">Description</th>
					<th>Annotation</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${urList}" status="i" var="ur">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 30%; word-wrap: break-word;">
							<a href="${ur.url}">${ur.url}</a> (${ur.score}/${ur.hostLinkSize})
						</td>
						<td class="description" style="width: 50%;">
							${ur.description}
						</td>
						<td>
							<div id="${'annot_' + ur.id}" class="user-annot-but ${labels[i]}"
								style="height: 32px; width: 32px; display: inline-block;"></div>
							<div id="${'notes_' + ur.id}"
								class="user-notes-but ${noteList[i]}"
								style="height: 32px; width: 32px; display: inline-block;"></div>
							<div id="${'tools_' + ur.id}"
								class="user-tools-but ${resourceList[i]}"
								style="height: 32px; width: 32px; display: inline-block;"></div>
							<div style="display: inline-block;">
								<g:if test="${ur.resourceType == null || resourceTypes[i]}">
									<g:select id="${'rt_' + ur.id}" name="${'rt_' + ur.id}"
										from="${['Database','Ontology','Software','Core Facility','Tissue Bank']}"
										value="${resourceTypes[i]}"
										noSelection="${['null':'Select a category']}"
										class="user-rt-cb" />
								</g:if>
								<g:if test="${ur.resourceType && !resourceTypes[i]}">
									<g:select id="${'rt_' + ur.id}" name="${'rt_' + ur.id}"
										from="${['Database','Ontology','Software','Core Facility','Tissue Bank']}"
										value="${ur.resourceType}"
										noSelection="${['null':'Select a category']}"
										class="user-rt-cb classifier" />*
								</g:if>
							</div>
						</td>
					</tr>
				</g:each>

			</tbody>
			<tfoot>
				<div class="paginateButtons">
					<g:paginate action="search" total="${totCount}"
						params="${[totCount:totCount, batchId:batchId, filterType:filterType, filterInput:filterInput, opMode:opMode]}" />
				</div>
			</tfoot>

		</table>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount, batchId:batchId, filterType:filterType, filterInput:filterInput,opMode:opMode]}" />
		</div>
	</div>

	<div id='dialog-notes' title='Notes'
		style='font-size: 12px; display: none'>
		<form>
			<fieldSet>
				<label for="notesText" style="font-weight: bold;">Notes:</label>
				<textarea id='notesText' name='notesText' rows="20" cols="80"
					style="width: 500px; height: 200px;"></textarea>

			</fieldSet>
		</form>
	</div>

	<div id="dialog-tools" title="Associate with a Registry Item"
		style="font-size: 12px; display: none;">
		<form>
			<fieldSet>
				<label for="resourceName" style="font-weight: bold">Resource:</label>
				<input id="resourceName" name="resourceName">
			</fieldSet>
		</form>

	</div>
</body>
</html>
