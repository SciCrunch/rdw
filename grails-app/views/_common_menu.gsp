<div class="menu">
	<ul>
		<g:if test="${session?.user}">
			<li><a class="home" href="${createLink(uri: '/')}"><g:message
						code="default.home.label" />&nbsp;(${session.user.loginId})</a></li>
		</g:if>

		<li><a href="#">Resources<span class="arrow"></span></a>
			<ul>
				<li><g:link class="list" controller="resourceRef" action="show">Resource References</g:link></li>
				<g:if test="${session.user.role != 'resource'}">
					<li><g:link class="list" controller="resourceFilter"
							action="show">Resource Candidates</g:link></li>
					<li><g:link class="list" controller="nerResourceRef"
							action="index">NER Resources</g:link></li>
					<li><g:link class="list" controller="publisherResourceRef"
							action="index">Publisher Resources</g:link></li>
					<li><g:link class="list" controller="dashboard" action="show">Dashboard</g:link></li>
				</g:if>
			</ul></li>
		<g:if test="${session?.user.role != 'resource'}">
			<li><a href="#">Registry<span class="arrow"></span></a>
				<ul>
					<li><g:link class="list" controller="downSiteStatus"
							action="show">Down Registry Sites</g:link></li>
					<li><g:link class="list" controller="registryUpdate"
							action="search">Registry Update Info</g:link></li>
					<li><g:link class="list"
							controller="registryRedirectCandidate"
								action="search">Registry Redirect Candidates</g:link></li>
					<li><g:link class="list" controller="acronym"
							action="search">Acronyms</g:link></li>
														
				</ul></li>
		</g:if>

		<g:if test="${session?.user.role != 'resource'}">
			<li><a href="#">Visualization<span class="arrow"></span></a>
				<ul>
					<li><g:link class="list" controller="linkStatsViz"
							action="show">Link Stats</g:link></li>
					<li><g:link class="list" controller="cooccurrenceViz"
								action="show">Tool Co-occurrence Stats</g:link></li>
				</ul></li>
		</g:if>

		<g:if test="${session?.user.loginId == 'admin'}">
			<li><a href="#">Admin<span class="arrow"></span></a>
				<ul>
					<li><g:link class="list" controller="resourceFilterClassifier"
							action="show">Resource Candidate Filter ML</g:link></li>
					<li><g:link class="list" controller="resourceFilterClassifier"
							action="showNER">NER Resource Filter ML</g:link></li>
					<li><g:link class="list" controller="resourceFilterClassifier"
							action="showPublisher">Publisher Resource Filter ML</g:link></li>
					<li><g:link class="list" controller="userAdmin" action="list">User Admin</g:link></li>
				</ul></li>
		</g:if>
		<li><g:link class="list" controller="user" action="logout">
				<g:message code="Logout" args="[entityName]" />
			</g:link></li>
	</ul>
</div>
