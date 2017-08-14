
	<form>
		<br />
		<br />
		
		<table>
			<tr>
				<td>
					${countData}		
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>				
			<tr>
				<td>
					${statisticsData}		
				</td>
			</tr>
			<tr>
				<td>
					&nbsp;
				</td>
			</tr>			
			<tr>
				<td>
					<g:if test="${zipLink}">
						<a class='AnalysisLink' class='downloadLink' href="${resource(file: zipLink)}">Download raw R data</a>
						<g:if test="${grailsApplication.config.org.transmartproject.helpUrls.tableWithFisherFiles}">
							&nbsp;
							<a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.tableWithFisherFiles}">
								<img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
							</a>
						</g:if>
					</g:if>
				</td>
			</tr>			
		</table>
	</form>
