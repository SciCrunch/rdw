<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Resource Mentions from Publishers</title>
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<g:javascript>
  $(function() {
     $('.menu').fixedMenu();
     var saveUserAnotUrl = "${g.createLink(controller:'publisherResourceRef', action:'saveUserAnnot')}";
     var getUserNoteUrl =  "${g.createLink(controller:'publisherResourceRef', action:'getUserNote')}";
     var getResourcesUrl =  "${g.createLink(controller:'publisherResourceRef', action:'getResourceNames')}";
     var rdw = nif.rdwModule;
    
     rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
     rdw.prepNotesButton('div.user-notes-but', saveUserAnotUrl, getUserNoteUrl);
     rdw.handleResourceFilter($('#filterDiv'), getResourcesUrl);  
     
     $('#selResourceApplyBut').click(function(event) {
         event.preventDefault();
         var selResourceName = $(':selected',$('#selectedResource')).val();
         if (selResourceName) {
            $('#filterInput').val(selResourceName);
         }
      });
  });
</g:javascript>
<style>
div.paginateButtons {
	font-size: 1.3em;
	line-height: 150%;
	padding: 5px;
}

.paginateButtons a.step, a.nextLink, a.prevLink, span.currentStep {
	padding-left: 5px;
}

a.more, a.less {
	padding-left: 5px;
	font-weight: bold;
}

.has-notes, .has-resource {
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
</style>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<h1>Resource Mentions from Publisher/Neuinfo Search</h1>
		<div id="filterDiv">
			<g:form action="search">
				<fieldSet style="padding: 5px; border: 1px solid #ccc;">
					<legend style="font-weight: bold">Filter</legend>
					<dl>

						<%-- <label for="filterTypeChooser" style="font-weight: bold">Filter By:</label> 
						--%>
						<dd>
							<select name="filterTypeChooser" id="filterTypeChooser">
								<option value="name">Resource Name</option>
								<option value="nifId">Resource NIF ID</option>
							</select> <input id="filterInput" name="filterInput"
								value="${filterInput}">
							<g:select name="opType" from="${['default','learning']}"
								value="${opType}" />
							<input type="submit" value="Filter">
						</dd>
						<dd style="margin-top: 5px;">
							<g:select id="selectedResource" name="selectedResource"
								from="${popList}" optionKey="value" optionValue="label"
								value="${selectedResource}" />
							<input id="selResourceApplyBut" type="submit" value="Select">
						</dd>
					</dl>
				</fieldSet>
			</g:form>
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<th style="width: 30%">Title</th>
				<th style="width: 20%;">Journal</th>
				<th>PMID</th>
				<th>Registry</th>
				<th>Date</th>
				<th>Source</th>
				<th style="width: 85px;">Annotation</th>
			</thead>
			<tbody>
				<g:each in="${prList}" status="i" var="pr">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 30%; word-wrap: break-word;">
							${pr.title}
						</td>
						<td style="width: 20%;">
							${pr.publicationName}
						</td>
						<td><a
							href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=${pr.pubmedId}&dopt=Abstract"
							target="_blank"> ${pr.pubmedId}</a></td>
						<td>
							${pr.registry.resourceName}
						</td>
						<td>
							${pr.publicationDate}
						</td>
						<td>
							${pr.publisher.publisherName}
						</td>
						<td style="width: 85px;">
							<div>
								<div id="${'annot_' + pr.id}"
									class="user-annot-but ${labels[i]}"
									style="height: 32px; width: 32px; display: inline-block;">
								</div>
								<div id="${'notes_' + pr.id}"
									class="user-notes-but ${noteList[i]}"
									style="height: 32px; width: 32px; display: inline-block;"></div>
							</div>
						</td>
					</tr>
				</g:each>
			</tbody>
			<tfoot>
				<div class="paginateButtons">
					<g:paginate action="search" total="${totCount}"
						params="${[totCount:totCount, filterType:filterType, filterInput:filterInput, opType:opType]}" />
				</div>
			</tfoot>
		</table>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount, filterType:filterType, filterInput:filterInput, opType:opType]}" />
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
</body>
</html>