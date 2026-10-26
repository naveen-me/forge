<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList, com.svb.struts.bean.ProfileDetailBean" %>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script type="text/javascript">
function closeWindow(){
		
window.close();
	}

</script>
</head>
<body>

<table>
  <tr>
   <td><b>Fine lines & Wrinkles</b></td>
  </tr>
</table>
<table>
  <tr>
   <td><img src="images/shared/trans.gif" height="30"></td>
  </tr>
</table>
<table height="100%" >
	<logic:iterate id="profileservice" name="profiles" indexId="count">
		<TR valign="top">
		
        <TD><%=((ProfileDetailBean)profileservice).getSV_PROFILE_ATTRIBUTE_DESC()%></TD>
		
		</TR>
		</logic:iterate>
	<tr align="right"><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font size=2><a href="javascript:window.close();">close window</a></font></td></tr>
	</table>
	
</body>

</html:html>


