<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList, com.svb.struts.bean.ProfileDetailBean" %>

<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script type="text/javascript">
function newWindow(profilevalue){
		
window.open("profileDisc.do?profileparam="+profilevalue+"&popup=y&desc=y&direc=n&ingred=n", 
"", "height=600, width=600, scrollbars=yes,status=no,resizable=yes, toolbar=no,location=no,menubar=no,left=0,top=0");
	}

</script>
</head>
<body>


<table>
<tr>
<th>Profile Attribute Value</th>
</tr>
	<logic:iterate id="profileservice" name="profiles" indexId="count">
		<TR>
		
    	<TD><a href="javascript:newWindow('<%=((ProfileDetailBean)profileservice).getSV_PROFILE_ATTRIBUTE_VALUE()%>')"><%=((ProfileDetailBean)profileservice).getSV_PROFILE_ATTRIBUTE_VALUE() %></a></TD>
	</logic:iterate>
	</table>
</body>
</html:html>