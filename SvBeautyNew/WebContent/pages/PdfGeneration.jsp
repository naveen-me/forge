<html>
<head>
<title>Your personal skin care regimen</title>
</head>

<body>


<OBJECT CLASSID="clsid:CA8A9780-280D-11CF-A24D-444553540000" WIDTH="100%" HEIGHT="100%"> 
<PARAM NAME="SRC" VALUE="pdfFiles/<%= request.getAttribute("fileName") %>">
<EMBED SRC="pdfFiles/<%= request.getAttribute("fileName") %>" HEIGHT="100%" WIDTH="100%">
     <NOEMBED> Your browser does not support embedded PDF files. </NOEMBED> 
    </EMBED>
</OBJECT> 


</body>

</html>