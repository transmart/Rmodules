%{--include js lib for heatmap dynamically--}%
<r:require modules="waterfall"/>
<r:layoutResources disposition="defer"/>

<div id="analysisWidget">

    <h2>
        Variable Selection
        <g:if test="${grailsApplication.config.org.transmartproject.helpUrls.waterfall}">
            <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.waterfall}">
                <img src="${resource(dir: 'images', file: 'help/helpicon_white.jpg')}" alt="Help"/>
            </a>
        </g:if>
    </h2>

    <form id="analysisForm">

        %{--Input fields--}%
        <fieldset class="inputFields">

            <div class="highDimContainer">
                <span>
                    Select a <b>numerical</b> data node from the Data Set Explorer Tree and drag it into the box.
                </span>
                <div id='divDataNode' class="queryGroupIncludeSmall highDimBox"></div>
                <div class="highDimBtns">
                    <button type="button" onclick="waterfallView.clear_high_dimensional_input('divDataNode');">Clear</button>
                </div>
            </div>

            <fieldset class="waterfall-params">
                <label for="selLowRange">Low Range </label>
                <select id="selLowRange">
                    <option value="&lt;" selected="selected">&lt;</option>
                    <option value="&lt;=">&lt;=</option>
                    <option value="=">=</option>
                    <option value="&gt;">&gt;</option>
                    <option value="&gt;=">&gt;=</option>
                </select>
                <input id="txtLowRange">

                <label for="selHighRange">High Range </label>
                <select id="selHighRange">
                    <option value="&gt;" selected="selected">&gt;</option>
                    <option value="&gt;=">&gt;=</option>
                    <option value="&lt;">&lt;</option>
                    <option value="&lt;=">&lt;=</option>
                    <option value="=">=</option>
                </select>
                <input id="txtHighRange">
            </fieldset>

        </fieldset>

        %{--Tool buttons--}%
        <fieldset class="toolFields">
            <input type="button" value="Run" onClick="waterfallView.submit_job(this.form);" class="runAnalysisBtn">
        </fieldset>

    </form>

</div>
