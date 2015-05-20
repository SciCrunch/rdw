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
     var saveUserAnotUrl = "${g.createLink(controller:'downSiteStatus', action:'saveUserAnnot')}";
     var rdw = nif.rdwModule;
     
     rdw.prepUserOpButton('div.user-annot-but', saveUserAnotUrl);
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
		<h1>Sites that are down more than three consecutive times</h1>
		<div class="paginateButtons">
			<g:paginate action="show" total="${totCount}"
				params="${[totCount:totCount]}" />
		</div>

		<table style="width: 100%; table-layout: fixed;">
			<thead>
				<tr>
					<th style="width: 30%">Resource Name</th>
					<th style="width: 20%;">Message</th>
					<th>NIF ID</th>
					<th>Last Checked</th>
					<th style="width: 60px;">Annotation</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${dssList}" status="i" var="dss">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd' }">
						<td style="width: 30%; word-wrap: break-word;"><a
							href="${dss.url}" target="_blank"> ${dss.resourceName}
						</a></td>
						<td>
							${dss.message}
						</td>
						<td><a href="http://neurolex.org/wiki/${dss.nifId}"
							target="_blank"> ${dss.nifId}
						</a></td>
						<td>
							${dss.lastCheckedTime}
						</td>
						<td style="width: 85px;">
							<div>
								<div id="${'annot_' + dss.id}"
									class="user-annot-but ${labels[i]}"
									style="height: 32px; width: 32px; display: inline-block;">
								</div>
							</div>
						</td>
					</tr>
				</g:each>
			</tbody>
			<tfoot>
				<tr>
					<td>
						<div class="paginateButtons">
							<g:paginate action="show" total="${totCount}"
								params="${[totCount:totCount]}" />
						</div>
					</td>
				</tr>
			</tfoot>

		</table>

	</div>
</body>
</html>
