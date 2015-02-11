<h2>Correlation Table (p-values on top right half, correlation coefficient on bottom left)</h2>

<p>
    <p>
        <g:renderAsTable file="${correlationFile}"
                         cls="AnalysisResults"
                         separator="\t"
                         columnIndexesNumber="1"
                         rowIndexesNumber="1"/>
    </p>

    <g:each var="location" in="${imageLocations}">
        <g:img file="${location}"/>
    </g:each>

    <div>
        <a href="${resource(file: zipLink)}" class="downloadLink">Download raw R data</a>
    </div>
</p>
