%{--include js lib for heatmap dynamically--}%
<r:require modules="kclust"/>
<r:layoutResources disposition="defer"/>

<div id="analysisWidget">

    <h2>
        Variable Selection
        <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.kMeansClustering ?: "JavaScript:D2H_ShowHelp(1507,helpURL,'wndExternal',CTXT_DISPLAY_FULLHELP )"}">
            <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
        </a>
    </h2>

    <form id="analysisForm">
        <fieldset class="inputFields">

            %{--High dimensional input--}%
            <div class="highDimContainer">
            <span>Select a <b>high dimensional</b> data node from the Data Set Explorer Tree and drag it into the box. If desired, use the High Dimensional Data button below the box to select one or more genes.</span>
                <div id='divIndependentVariable' class="queryGroupIncludeSmall highDimBox"></div>
                <div class="highDimBtns">
                    <button type="button" onclick="highDimensionalData.gather_high_dimensional_data('divIndependentVariable')">High Dimensional Data</button>
                    <button type="button" onclick="kmeansClustering.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
                </div>
            </div>

            %{--Display independent variable--}%
            <div id="displaydivIndependentVariable" class="independentVars"></div>

            <label for="txtClusters">Number of clusters:</label>
            <input type="text" id="txtClusters" value="2"/>

            <label for="txtMaxDrawNumber">
                Max rows to display:
                <g:if test="${grailsApplication.config.org.transmartproject.helpUrls.kMeansClusteringMaxRows}">
                    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.kMeansClusteringMaxRows}">
                        <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
                    </a>
                </g:if>
            </label>
            <input type="text" id="txtMaxDrawNumber"/>

        </fieldset>

        <fieldset class="toolFields">
            <div>
                <input type="checkbox" id="chkCalculateZscore" name="calculateZscore">
                <span>Calculate z-score on the fly</span>
            </div>
            <input type="button" value="Run" onClick="kmeansClustering.submit_job(this.form);" class="runAnalysisBtn">
        </fieldset>

</form>

</div>
