<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Registry Redirect Candidates</title>
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<g:javascript>
 $(function() {
     $('.menu').fixedMenu();
     var showContentsUrl = "${g.createLink(controller:'registryRedirectCandidate', action:'getOrigContent')}";
     var saveUserAnotUrl = "${g.createLink(controller:'registryRedirectCandidate', action:'saveUserAnnot')}";
     var getUserNoteUrl =  "${g.createLink(controller:'registryRedirectCandidate', action:'getUserNote')}";
     var rdw = nif.rdwModule;
     
     rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
     rdw.prepNotesAndRedirectUrlButton('div.user-notes-but', saveUserAnotUrl, getUserNoteUrl);
     
     rdw.prepShowSiteContentButton('div.show-diff-but', showContentsUrl);
     
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

a.more,a.less {
	padding-left: 5px;
	font-weight: bold;
}

.has-notes {
	border: 2px solid #2E8B57;
}

div.field {
   margin-top:5px;
}
</style>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<th style="width: 40%">Registry</th>
				<th>Redirect URL</th>
				<th style="width: 120px;">&nbsp;</th>
			</thead>
			<tbody>
				<g:each in="${rcList}" status="i" var="rc">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 40%; word-wrap: break-word;">
							${rc.registry.resourceName}<br> <a href="${rc.registry.url}"
							target="_blank"> ${rc.registry.url}
						</a>
						</td>
						<td>
							<span id="${'rurl_' + rc.id}">${rc.redirectUrl}</span>
						</td>
						<td>
							<div>
								<div id="${'annot_' + rc.id}" class="user-annot-but ${rc.label}"
									style="height: 32px; width: 32px; display: inline-block;">
								</div>
								<div id="${'notes_' + rc.id}"
									class="user-notes-but ${rc.notes ? 'has-notes' : ''}"
									style="height: 32px; width: 32px; display: inline-block;"></div>
								<div id="${'show_' + rc.registry.id}" class="show-diff-but"
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
						params="${[totCount:totCount]}" />
				</div>
			</tfoot>
		</table>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount]}" />
		</div>
	</div>

	<div id='dialog-content' title='Site Content'
		style='font-size: 12px; display: none'>
		<form>
			<h3 id="dlg-header"></h3>
			<textarea id="contents-area" rows="20" cols="80" readonly="true"
				style="width: 600px; height: 250px;"></textarea>
		</form>
	</div>

	<div id='dialog-notes' title='Notes'
		style='font-size: 12px; display: none'>
		<form>
			<fieldSet>
			    <div class="field">
				<label for="notesText" style="font-weight: bold;">Notes:</label>
				<textarea id='notesText' name='notesText' rows="20" cols="80"
					style="width: 500px; height: 200px;"></textarea>
				</div>
				<div class="field">	
				<label for="redirectUrlText" style="font-weight: bold">Redirect URL:</label>
				<input type="text" id="redirectUrlText" name="redirectUrlText" size="60">
				</div>
			</fieldSet>
		</form>
	</div>
</body>
</html>