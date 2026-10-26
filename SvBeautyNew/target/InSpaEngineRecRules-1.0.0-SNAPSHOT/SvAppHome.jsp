<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
</head>
<body onload="javascript:submitPageForm();"> 


<%
    String svId = "" ;
       	if(svId == null || svId.equals("")){
	  	svId = (String)session.getAttribute("svId");
	}

    String mid = (String)request.getParameter("mid");
    String catid = (String)request.getParameter("catid");
    String mode = (String)request.getParameter("mode");
    if (catid != null && catid.equals("12875"))
       mode = "hair" ;
    System.out.println("catid mode "+catid+"  "+mode) ;
	session = request.getSession(false);
   	if(mid == null || mid.equals("")){
	  	mid = (String)session.getAttribute("mid");
	}
    if(mid == null || mid.equals(""))
         mid = "B0001" ;
    
	session = request.getSession(true);
	session.removeAttribute("fromavonbeautyprofile");
	session.removeAttribute("repexists");
	session.removeAttribute("customerIdinFlash");
	session.removeAttribute("repIdinFlash");
	session.removeAttribute("ProfilesFormBean");
%>

<script language="JavaScript">
<!--
function submitPageForm() {
	document.pageForm1.action = "<%=response.encodeURL("CustomerRegistrationAction.do")%>";

//       var ht = document.body.scrollHeight ;
//       parent.location = "http://beautyt1b.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;


     //  document.pageForm1.action = "http://sv-beauty.com/svbeauty/SvError404.jsp";
	document.pageForm1.submit();
}
//-->
</script>

<form name="pageForm1" method="post" action="">
<input type=hidden name="mid" value="<%=mid%>">
<input type=hidden name="catid" value="<%=catid%>">
<input type=hidden name="mode" value="<%=mode%>">
<input type=hidden name="svId" value="<%=svId%>">
<input type=hidden name="selfprofile" value="yes">
<input type=hidden name="repemail" value="demo@demo.com">
<table width="778" border="0" cellspacing="0" cellpadding="0">
	<tr valign="top"> 
    	<td colspan="3">
			<!--<table width="778" border="0" cellspacing="0" cellpadding="0">
				<tr height="15">
					<td height="15"></td>
				</tr>
				<tr height="20">
					<td><a href="javascript:submitPageForm()"><b> Product Recommender </b></a> </td>
				</tr>
				<tr height="30">
					<td class="footerlinks" align="center" height="30"></td>
				</tr>
			</table>-->
</form>
</body>
</html>