<html>
<head>
</head>
<!-- <body onload=javascript:submitPageForm()">-->
<body>
<%
    String svId = "" ;
       	if(svId == null || svId.equals("")){
	  	svId = (String)session.getAttribute("svId");
	}

    String mid = (String)request.getParameter("mid");
    //String catid = (String)request.getParameter("catid");
    //String mode = (String)request.getParameter("mode");
    //String catid="12875";  
    //String mode="hair";
    /*if (mode == null)
      mode = "skin" ;
	session = request.getSession(false);

   	if(mid == null || mid.equals("")){
	  	mid = (String)session.getAttribute("mid");
	}
    if(mid == null || mid.equals(""))
         mid = "B0001" ;
*/    
//	session = request.getSession(true);
//	session.removeAttribute("fromavonbeautyprofile");
%>

<script language="JavaScript">
<!--
function submitPageForm() {
	 //alert("vbb "+document.pageForm1.mode.value);  
	 //alert("vbb "+document.pageForm1.catid.value);  
	 //alert("vbb "+document.pageForm1.mid.value);  
	 document.pageForm1.mode.value = "hair" ;
	document.pageForm1.action = "<%=response.encodeURL("CustomerRegistrationAction.do")%>";
	document.pageForm1.submit();
}
function submitPageForm1() {
	 //alert("vbb "+document.pageForm1.mode.value);  
	 //alert("vbb "+document.pageForm1.catid.value);  
	 //alert("vbb "+document.pageForm1.mid.value);  
	 document.pageForm1.mode.value = "skin" ;
	document.pageForm1.action = "<%=response.encodeURL("CustomerRegistrationAction.do")%>";
	document.pageForm1.submit();
}
function submitPageForm2() {
	 //alert("vbb "+document.pageForm1.mode.value);  
	 //alert("vbb "+document.pageForm1.catid.value);  
	 //alert("vbb "+document.pageForm1.mid.value);  
	 document.pageForm1.mode.value = "skinNew" ;
	document.pageForm1.action = "<%=response.encodeURL("CustomerRegistrationAction.do")%>";
	document.pageForm1.submit();
}
//-->
</script>

<form name="pageForm1" method="post" action="">
<input type=hidden name="mid" value="<%=mid%>">
<input type=hidden name="catid" value="">
<input type=hidden name="svId" value="<%=svId%>">
<input type=hidden name="mode" value="">
<input type=hidden name="repemail" value="demo@demo.com">
<table width="778" border="0" cellspacing="0" cellpadding="0">
	<tr valign="top"> 
    	<td colspan="3">
			<table width="778" border="0" cellspacing="0" cellpadding="0">
				<tr height="15">
					<td height="15"></td>
				</tr>
				<tr height="20">
					<td><a href="javascript:submitPageForm()"><b> Hair Care Product Recommender </b></a> </td>
				</tr>
				<tr height="20">
					<td><a href="javascript:submitPageForm1()"><b> Skin Care Product Recommender </b></a> </td>
				</tr>
				<tr height="20">
					<td><a href="javascript:submitPageForm2()"><b> New Skin Care Product Recommender </b></a> </td>
				</tr>
				<tr height="30">
					<td class="footerlinks" align="center" height="30"></td>
				</tr>
			</table>
</form>
</body>
</html>