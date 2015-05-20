<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>NER Tagged Resources from PMC Open Access</title>
<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css">
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<r:require module="colorbox" />

<g:javascript>
  $(function() {
   $('.menu').fixedMenu();
   var saveUserAnotUrl = "${g.createLink(controller:'nerResourceRef', action:'saveUserAnnot')}";
   var getUserNoteUrl =  "${g.createLink(controller:'nerResourceRef', action:'getUserNote')}";
   var getResourcesUrl =  "${g.createLink(controller:'nerResourceRef', action:'getResourceNames')}";
   
   var rdw = nif.rdwModule;
  
   rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
   rdw.prepNotesButton('div.user-notes-but', saveUserAnotUrl, getUserNoteUrl);
   rdw.handleResourceFilter($('#filterDiv'), getResourcesUrl);
  
   $('td.description').each(function() { 
      var selTxt = $('td:first-child', $(this).parents('tr')).text().trim();
      //console.log('>> ' + selTxt);
      rdw.hilight(this, selTxt);
   });
   $('.paperLink').click(function() {
       var pmid = $.trim($(this).text());
      $.ajax({
            url: "${g.createLink(controller:'resourceRef', action:'getPaperInfo')}",
            data: { pmid:pmid},
            success:function(data) {
                // console.log(data);
                var frm$ = $('#articleTemplate').clone();
                $('.articleTitle', frm$).text(data.title);
                $('.journalTitle', frm$).text(data.journalTitle);
                $('.pmidLabel',frm$).text('('+ pmid + ')');
                var html = frm$.html();
                // console.log(html);
                $.colorbox({html: html, width:'50%'});
             },
             error: function(request, status, error) {
                  alert(error);
             } 
        });
   });
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
	<a href="#create-user" class="skip" tabindex="-1"><g:message
			code="default.link.skip.label" default="Skip to content&hellip;" /></a>

	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<h1>PMC Registry Mentions detected by NER</h1>
		<div id="filterDiv">
			<g:form action="search">
				<fieldSet style="padding: 5px; border: 1px solid #ccc;">
					<legend style="font-weight: bold">Filter</legend>
					<dl>
						<dd>
							<%-- 		
					<label for="filterTypeChooser" style="font-weight: bold">Filter
						By:</label> 
				--%>
							<select name="filterTypeChooser" id="filterTypeChooser">
								<option value="name">Resource Name</option>
								<option value="nifId">Resource NIF ID</option>
							</select> <input id="filterInput" name="filterInput"
								value="${filterInput}">
							<g:select name="opType"
								from="${['default','learning','active learning']}"
								value="${opType}" />

							<input type="submit" value="Filter">
						</dd>
						<dd style="margin-top:5px;">
							<g:select id="selectedResource" name="selectedResource" from="${popList}"
								optionKey="value" optionValue="label" value="${selectedResource}"/>
							<input id="selResourceApplyBut" type="submit" value="Select">
						</dd>
					</dl>
				</fieldSet>
			</g:form>
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<th style="width: 20%">Entity</th>
				<th style="width: 40%;">Context</th>
				<th>PMID</th>
				<th>Registry</th>
				<th style="width: 85px;">Annotation</th>
			</thead>
			<tbody>
				<g:each in="${rrList}" status="i" var="rr">
				    
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 20%; word-wrap: break-word;">
							${rr.entity}
						</td>
						<td class="description" style="width: 40%;">
							${rr.context}
						</td>
						<td><a class="paperLink" href="#"> ${rr.paper.pubmedId}</a></td>
						<td>
							${rr.registry.resourceName}
						</td>
						<td style="width: 85px;">
							<div>
								<div id="${'annot_' + rr.id}"
									class="user-annot-but ${labels[i]}"
									style="height: 32px; width: 32px; display: inline-block;">
								</div>
								<div id="${'notes_' + rr.id}"
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
	<div id="articleTemplate" style="display: none">
		<div style="background-color: #ccc; padding: 5px;">
			<h3>
				Paper Information <span class="pmidLabel"></span>
			</h3>
			<div class="articleTitle"></div>
			<div class="journalTitle"></div>
		</div>
	</div>



</body>
</html>
