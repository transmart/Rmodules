<form>
    <br/>
    <br/>

    <table>
        <tr>
            <td>
                <g:each var="entry" in="${countDataFilesMap}">
                    <g:if test="${countDataFilesMap.size() > 1}">
                        <br />
                        <br />
                        <span class='AnalysisHeader'>${entry.key}</span>
                        <hr />
                    </g:if>
                    <g:renderAsTable file="${entry.value}"
                                     cls="AnalysisResults"
                                     separator="\t"
                                     columnIndexesNumber="1"
                                     rowIndexesNumber="1"/>
                </g:each>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <g:each var="entry" in="${statisticsDataFilesMap}">
                    <g:if test="${statisticsDataFilesMap.size() > 1}">
                        <br />
                        <br />
                        <span class='AnalysisHeader'>${entry.key}</span>
                        <hr />
                    </g:if>
                    <g:renderAsTable file="${entry.value}"
                                     cls="AnalysisResults"
                                     separator="\t"
                                     columnIndexesNumber="1"/>
                </g:each>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <a class='AnalysisLink' href="${resource(file: zipLink)}">Download raw R data</a>
            </td>
        </tr>
    </table>
</form>
