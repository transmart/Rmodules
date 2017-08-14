<h2>Heatmap</h2>

<p>
    <div class="plot_hint">
        Click on the heatmap image to open it in a new window as this may increase readability.
        <br><br>
    </div>

    <g:each var="location" in="${imageLocations}">
        <a onclick="window.open('${resource(file: location, dir: "images")}', '_blank')">
            <g:img file="${location}" class="img-result-size"></g:img>
        </a>
    </g:each>

    <g:if test="${zipLink}">
        <a class='AnalysisLink' class='downloadLink' href="${resource(file: zipLink)}">Download raw R data</a>
        <g:if test="${grailsApplication.config.org.transmartproject.helpUrls.heatMapFiles}">
            &nbsp;
            <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.heatMapFiles}">
                <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
            </a>
        </g:if>
    </g:if>
</p>

