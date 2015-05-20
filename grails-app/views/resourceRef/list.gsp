<%--<!DOCTYPE html>--%>
<html>
<head>
<meta name="layout" content="main" />
<title>Resource References</title>
<style>
.hilight {
	color: maroon;
	font-weight: bold;
}
</style>
<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css">
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<r:require module="colorbox" />

<g:javascript>
      $(function() {
            $('.menu').fixedMenu();
            var saveUserAnotUrl = "${g.createLink(controller:'resourceRef', action:'saveUserAnnot')}";
            var getUserNoteUrl =  "${g.createLink(controller:'resourceRef', action:'getUserNote')}";
            var getResourcesUrl =  "${g.createLink(controller:'resourceRef', action:'getResourceNames')}";
            var rdw = nif.rdwModule;
                        
            rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
            rdw.prepNotesButton('div.user-notes-but', saveUserAnotUrl, getUserNoteUrl);
            rdw.handleResourceFilter($('#filterDiv'), getResourcesUrl);
             
            $('td.description').each(function(){
               var selTxt = $('td:first-child', $(this).parents('tr')).text().trim();
               // console.log('>> ' + selTxt);
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
                })
            })
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

.has-notes {
	border: 2px solid #2E8B57;
}

.journalTitle {
   font-style:italic;
   padding-top:4px;
}
</style>
</head>
<body>
	<a href="#create-user" class="skip" tabindex="-1"><g:message
			code="default.link.skip.label" default="Skip to content&hellip;" /></a>

    <g:render template="/common_menu" /> 
	<div id="page-body" class="content" role="main">
	<h1>PMC Registry Mentions detected by URL</h1>
		<div id="filterDiv">
			<g:form action="search">
			    <input type="hidden" name="batchId" value="${batchId}"></input>
				<fieldSet>
					<label for="filterTypeChooser" style="font-weight: bold">Filter
						By:</label> 						
						<select name="filterTypeChooser" id="filterTypeChooser">
						  <option value="name">Resource Name</option>
						  <option value="nifId">Resource NIF ID</option>
					    </select> 
					    <input id="filterInput" name="filterInput" value="${filterInput}"> <input
						type="submit" value="Filter">
				</fieldSet>
			</g:form>
		</div>
		<table style="width:100%; table-layout:fixed;">
			<thead>
				<tr>
					<th style="width:25%;">URL</th>
					<th style="width:45%;">Context</th>
					<th>PMID</th>
					<th>Registry</th>
					<th style="width: 85px;">Annotation</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${urList}" status="i" var="ur">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width:25%; word-wrap:break-word;">
							${ur.url}
						</td>
						<td class="description" style="width:45%;">
							${ur.context}
						</td>
						<td><a class="paperLink" href="#">
								${ur.paper.pubmedId}
						</a></td>
						<td>
							${ur.registry.resourceName}
						</td>
						<td style="width: 85px;">
							<div>
								<div id="${'annot_' + ur.id}"
									class="user-annot-but ${labels[i]}"
									style="height: 32px; width: 32px; display: inline-block;"></div>
								<div id="${'notes_' + ur.id}"
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
						params="${[totCount:totCount, batchId:batchId, filterType:filterType, filterInput:filterInput]}" />
				</div>
			</tfoot>

		</table>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount, batchId:batchId, filterType:filterType, filterInput:filterInput]}" />
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
		<div style="background-color:#ccc; padding:5px;">
		   <h3>Paper Information <span class="pmidLabel"></span></h3>
			<div class="articleTitle"></div>
			<div class="journalTitle"></div>
		</div>
	</div>

</body>
</html>
