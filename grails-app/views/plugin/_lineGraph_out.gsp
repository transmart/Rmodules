<h2>Line Graph</h2>

<p>
    <g:each var="location" in="${imageLocations}">
        <g:img file="${location}" class="img-result-size"></g:img> <br/>
    </g:each>

    <g:if test="${zipLink}">
        <a class='AnalysisLink' class='downloadLink' href="${resource(file: zipLink)}">Download raw R data</a>
        <g:if test="${grailsApplication.config.org.transmartproject.helpUrls.lineGraphFiles}">
            &nbsp;
            <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.lineGraphFiles}">
                <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
            </a>
        </g:if>
    </g:if>
</p>
