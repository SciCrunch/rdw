<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main" />
<title>Papers where the acronym is defined</title>

<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'jquery.treetable.css')}"
	type="text/css">
<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'jquery.treetable.theme.default.css')}"
	type="text/css">
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<g:javascript>
  $(function() {
	 $('.menu').fixedMenu();
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

.ui-autocomplete-loading {
	background: white url('../images/ui-anim_basic_16x16.gif') right center
		no-repeat;
}
</style>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<h1>PMC papers for acronym</h1>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount, acronym:acronym, expansion:expansion]}" />
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<tr>
					<th style="width: 70%">Paper Title</th>
					<th style="width: 22%">Journal</th>
					<th style="width: 8%">PMID</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${paperList}" status="i" var="paper">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td>
							${paper.title}
						</td>
						<td>
							${paper.journal}
						</td>
						<td><a
							href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=${paper.pmid}&dopt=Abstract"
							target="_blank">
								${paper.pmid}
						</a></td>
					</tr>
				</g:each>
			</tbody>
			<tfoot>
				<tr>
					<td colspan=3">
						<div class="paginateButtons">
							<g:paginate action="search" total="${totCount}"
								params="${[totCount:totCount, acronym:acronym, expansion:expansion]}" />
						</div>
					</td>
				</tr>
			</tfoot>
		</table>

	</div>
</body>
</html>

