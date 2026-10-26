<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.svb.struts.bean.RegimenDetailsBean" %>

<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>

<html:html>
<head>
</head>
<body>
	<html:form action="/regimenDetail.do">
	<TABLE>
	<TR BORDER="1">
		<TD BORDER="1">REGIMEN EXPLANATION</TD>
		<TD><html:button property="regimen">BUY THIS REGIMEN</html:button>
	</TR>
	<logic:iterate id="regimen" name="regimenList" indexId="count">
	<TR>
	<TD><html:img src="<%= ((RegimenDetailsBean)regimen).getImageMedium() %>"/></TD>
	<TD><TABLE>
		<TR><TD><b><bean:write name="regimen" property="brandName"/></b></TD></TR>
		<TR><TD><bean:write name="regimen" property="productName"/></TD></TR>
		<TR><TD>$<bean:write name="regimen" property="price"/><br></TD><TR>
		<TR><TD><bean:write name="regimen" property="sellCopy"/></TD></TR>
		<TR><TD ALIGN="RIGHT"><html:link page="">Why was this recommended?</html:link></TD></TR>
	</TABLE></TD>
	</TR>
	<TD>
		<input type="text" name="qty<%= count.intValue()+1 %>" value="1" size="3">
		<html:checkbox property="productId" name="regimen" value="<%= ((RegimenDetailsBean)regimen).getProductId()%>"/>
	</TD>
	</logic:iterate>
	</TABLE>
	
	</html:form>
</body>
</html:html>




