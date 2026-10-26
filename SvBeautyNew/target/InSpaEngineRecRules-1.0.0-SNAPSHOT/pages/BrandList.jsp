<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList, com.svb.struts.bean.BrandDetailsBean" %>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script type="text/javascript">
function newWindow(brandid){
		
document.open("brandDisc.do?brandparam="+brandid+"&popup=y&desc=y&direc=n&ingred=n", 
"", "height=600, width=800, scrollbars=yes,status=yes,resizable=yes, toolbar=yes,location=yes,menubar=yes,left=0,top=0");
	}

</script>
</head>
<body>


<table>
<tr>
<th>Brand ID</th>
<th>&nbsp;</th>
<th>Brand Name</th>
<th>&nbsp;</th>
<th>Brand SubID</th>
<th>&nbsp;</th>
<th>Brand SubName</th>
<th>&nbsp;</th>
<th>Brand VenderID</th>
<th>&nbsp;</th>
<th>Brand VenderName</th>


</tr>
	<logic:iterate id="brand" name="brands" indexId="count">
		<TR>
		
        <TD><%=((BrandDetailsBean)brand).getSV_BRAND_ID() %></TD>
		<td>&nbsp;</td>
		<TD><%=((BrandDetailsBean)brand).getSV_BRAND_NAME() %></TD>
		<td>&nbsp;</td>
		<TD><%=((BrandDetailsBean)brand).getSV_BRAND_SUB_ID() %></TD>
		<td>&nbsp;</td>
		<TD><%=((BrandDetailsBean)brand).getSV_BRAND_SUB_NAME() %></TD>
		<td>&nbsp;</td>
		<TD><%=((BrandDetailsBean)brand).getSV_BRAND_VENDOR_ID() %></TD>
		<td>&nbsp;</td>
		<TD><%=((BrandDetailsBean)brand).getSV_BRAND_VENDOR_NAME() %></TD>
		<td>&nbsp;</td>
		<TD><a href="javascript:newWindow('<%=((BrandDetailsBean)brand).getSV_BRAND_ID()%>')">
			Tell me More
		</a></TD></TR>
	
	</logic:iterate>
	</table>
</body>
</html:html>