<html>
<head>
</head>
<body onload="javascript:submitPageForm()">
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
    System.out.println("shopcart information=====> "+shopcartstring) ;
    String submitProds = "86820~2|139320~1|151040~2|76573~1" ;
         
%>

<script language="JavaScript">
<!--
function submitPageForm() {
	document.pageForm3.submit();
}
//-->
</script>

<form name="pageForm3" method="post" action="">
<input type=hidden name="mid" value="<%=mid%>">
<input type=hidden name="customerId" value="<%=mid%>">
<input type=hidden name="catid" value="<%=catid%>">
<input type=hidden name="SvProfileId" value="<%=SvProfileId%>">
<input type=hidden name="SvCartData" value="<%=shopcartstring%>">
<input type=hidden name="aid" value="337879">
<input type=hidden name="aparam" value="">

</form>

</body>
</html>