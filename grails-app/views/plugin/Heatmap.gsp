%{--include js lib for heatmap dynamically--}%
<r:require modules="heatmap"/>
<r:layoutResources disposition="defer"/>

<div id="analysisWidget">

    <h2>
        Variable Selection
        <a href='JavaScript:D2H_ShowHelp(1505,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
            <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
        </a>
    </h2>

    <form id="analysisForm">
        <fieldset class="inputFields">

            %{--High dimensional input--}%
            <div class="highDimContainer">
                <span>Select a High Dimensional Data node from the Data Set Explorer Tree and drag it into the box.</span>
                <div id='divIndependentVariable' class="queryGroupIncludeSmall highDimBox"></div>
                <div class="highDimBtns">
                    <button type="button" onclick="highDimensionalData.gather_high_dimensional_data('divIndependentVariable')">High Dimensional Data</button>
                    <button type="button" onclick="heatMapView.clear_high_dimensional_input('divIndependentVariable')">Clear</button>
                </div>
            </div>

            %{--Display independent variable--}%
            <div id="displaydivIndependentVariable" class="independentVars"></div>

            <label for="txtMaxDrawNumber">Max rows to display:</label>
            <input type="text" id="txtMaxDrawNumber"/>

            <label for="txtImageWidth">Image Width (pixels):</label>
            <input type="text" id="txtImageWidth" value="800"/>

            <label>Image Height (pixels):</label>
            <input type="text" id="txtImageHeight" value="800"/>

            <label>Text Size (pointsize):</label>
            <input type="text" id="txtImagePointsize" value="12"/>
        </fieldset>

        <fieldset class="toolFields">
            <div>
                <input type="checkbox" id="chkGroupBySubject" name="doGroupBySubject">
                <span>Group by subject (instead of node) for multiple nodes</span>
            </div>
            <input type="button" value="Run" onClick="heatMapView.submit_job(this.form);" class="runAnalysisBtn">
        </fieldset>
    </form>

</div>
