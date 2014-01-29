<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>subsetPanel.html</title>

<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="this is my page">
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css', file:'datasetExplorer.css')}">

</head>

<body>
	<form>
		<br />
		<br />
		
		<span class='AnalysisHeader'>Correlation Table (p-values on top right half, correlation coefficient on bottom left)</span>
		
		<br />
		<br />		
		
		${correlationData}
				
		<br />
		<br />

        <g:each var="location" in="${imageLocations}">
            <g:img file="${location}"></g:img>
        </g:each>

		<br />
		<br />
        <a href="${resource(file: zipLink)}" class="downloadLink">Download raw R data</a>
		
	</form>
</body>

</html>