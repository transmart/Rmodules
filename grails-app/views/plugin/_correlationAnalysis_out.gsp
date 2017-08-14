<h2>Correlation Table (p-values on top right half, correlation coefficient on bottom left)</h2>

<p>
    <p>${correlationData}</p>

    <g:each var="location" in="${imageLocations}">
        <g:img file="${location}"></g:img>
    </g:each>

    <g:if test="${zipLink}">
        <a class='AnalysisLink' class='downloadLink' href="${resource(file: zipLink)}">Download raw R data</a>
        <g:if test="${grailsApplication.config.org.transmartproject.helpUrls.correlationAnalysisFiles}">
            &nbsp;
            <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.correlationAnalysisFiles}">
                <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
            </a>
        </g:if>
    </g:if>
</p>
