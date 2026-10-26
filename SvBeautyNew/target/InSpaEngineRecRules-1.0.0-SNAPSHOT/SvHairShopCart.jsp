<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
</head>
<!--  <body onload="javascript:submitPageForm()">  -->
<body>
<%
    String shopcartstring = "" ;
    String catid = "" ;
    String SvProfileId = (String)request.getParameter("SvProfileId");	
    String mid = (String)request.getParameter("mid");
    catid = (String)request.getParameter("catid");
	session = request.getSession(false);
   	if(mid == null || mid.equals("")){
	  	mid = (String)session.getAttribute("mid");
	}
   	if(catid == null || catid.equals("")){
	  	catid = (String)session.getAttribute("catid");
	}
    if(mid == null || mid.equals(""))
         mid = "B0001" ;
    shopcartstring = (String) request.getAttribute("shopcartstring") ;
    System.out.println("shopcart "+shopcartstring) ;
    String submitProds = "86820~2|139320~1|151040~2|76573~1" ;
    
%>

<script language="JavaScript">
<!--
function submitPageForm() {
	document.pageForm3.submit();
}
//-->
</script>

<form name="pageForm3" method="post" action="http://www.drugstore.com/checkout/partners/sv/svpost.asp">
<input type=hidden name="mid" value="<%=mid%>">
<input type=hidden name="customerId" value="<%=mid%>">
<input type=hidden name="catid" value="12875">
<input type=hidden name="SvProfileId" value="<%=SvProfileId%>">
<input type=hidden name="SvCartData" value="<%=shopcartstring%>">

<table width="778" border="0" cellspacing="0" cellpadding="0">
	<tr valign="top"> 
    	<td colspan="3">
		 	<table width="778" border="0" cellspacing="0" cellpadding="0">
				<tr height="15">
					<td height="15"></td>
				</tr>
				<tr height="15">
					<td height="15">Welcome <%=SvProfileId%> & <%=mid%></td>
				</tr>
				<tr height="20">
					<td><u><b> Your Products ... </b></u> </td>
				</tr>
				<tr height="15">
					<td height="15">Product 1</td>
				</tr>
				<tr height="15">
					<td height="15">Product 2</td>
				</tr>
				<tr height="15">
					<td height="15">Product 3</td>
				</tr>
				<tr height="15">
					<td height="15">Product 4</td>
				</tr>
				<tr height="15">
					<td height="15"><%=shopcartstring%></td>
				</tr>
				<tr height="20">
					<td><a href="javascript:submitPageForm()"><b> Submit to Shopping Cart </b></a> </td>
				</tr>
				<tr height="30">
					<td class="footerlinks" align="center" height="30"></td>
				</tr>
			</table> 
</form>

</body>
</html>