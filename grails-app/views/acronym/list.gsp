<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="main" />
<title>Acronyms</title>

<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'jquery.treetable.css')}"
	type="text/css">
<link rel="stylesheet"
	href="${resource(dir: 'css', file: 'jquery.treetable.theme.default.css')}"
	type="text/css">
<g:javascript library="jquery" />
<r:require module="jquery-ui" />
<r:require module="treetable" />
<g:javascript>
  $(function() {
	 $('.menu').fixedMenu();
	 $('table.expansion').treetable({expandable:true,
	   onInitialized: function(tree) {
	      // console.log(this);
	       $(this.table).show();
	   }
	 });
	 var rdw = nif.rdwModule;
	 var getAcronymsUrl =  "${g.createLink(controller:'acronym', action:'getAcronymNames')}";
	 rdw.handleResourceFilter($('#filterDiv'), getAcronymsUrl, 1);
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

.hilight {
	color: maroon;
	font-weight: bold;
}
</style>
</head>
<body>
	<g:render template="/common_menu" />
	<div id="page-body" class="content" role="main">
		<h1>Acronyms detected in PMC papers</h1>
		<div id="filterDiv">
			<g:form action="search">
				<fieldSet style="padding: 5px; border: 1px solid #ccc;">
					<legend style="font-weight: bold">Filter</legend>
					<dl>
						<dd>
							<input id="filterInput" name="filterInput" value="${filterInput}">
							<input type="submit" value="Filter">
						</dd>
					</dl>
				</fieldSet>
			</g:form>
		</div>
		<div class="paginateButtons">
			<g:paginate action="search" total="${totCount}"
				params="${[totCount:totCount]}" />
		</div>
		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<tr>
					<th style="width: 15%">Acronym</th>
					<th style="width: 8%">Frequency</th>
					<th>Expansion</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${acrList}" status="i" var="acr">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 15%">
							${acr.acronym}
						</td>
						<td style="width: 8%">
							${acr.frequency}
						</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td colspan="3">
							<table style="display: none; width: 100%; table-layout: fixed;"
								id="${'table_' + i}" class="expansion">
								<tr data-tt-id="0">
									<td style="width: 15%">Expansions</td>
									<td style="width: 8%">&nbsp;</td>
									<td>&nbsp;</td>
								</tr>
								<g:each in="${acr.clusters}" status="j" var="cluster">
									<g:if test="${cluster.canonicalFreq != -1}">
										<tr data-tt-id="${10 + j}" data-tt-parent-id="0">
											<td>
												${cluster.frequency}
											</td>
											<td>
												${cluster.canonicalFreq}
											</td>
											<td><g:link class="list" controller="acronymPaper"
													action="search"
													params="${[acronym:acr.acronym,expansion:cluster.expansion]}">
													${cluster.expansion}
												</g:link></td>
										</tr>
									</g:if>
									<g:else>
										<tr data-tt-id="${10 + j}" data-tt-parent-id="0">
											<td>
												${cluster.expansion}
											</td>
											<td>&nbsp;</td>
											<td>&nbsp;</td>
										</tr>
									</g:else>
									<g:each in="${cluster.variants}" status="k" var="ae">
										<tr data-tt-id="${1000 + 1000 *j + k}"
											data-tt-parent-id="${10 + j}">
											<td>&nbsp;</td>
											<td>
												${ae.frequency}
											</td>
											<td>
												${ae.expansion}
											</td>
										</tr>
									</g:each>

								</g:each>
							</table>
					</td>
					</tr>

				</g:each>
			</tbody>
			<tfoot>
				<tr>
					<td>
						<div class="paginateButtons">
							<g:paginate action="search" total="${totCount}"
								params="${[totCount:totCount]}" />
						</div>
					</td>
				</tr>
			</tfoot>
		</table>

	</div>

</body>
</html>
