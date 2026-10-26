<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script Language="JavaScript">
	function newWindow(productId){
		window.open("productDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n", "", "height=600, width=800, scrollbars=yes,resizable=yes,left=0,top=0");
	}
</script>
</head>
<body>
	<logic:iterate id="productNames" name="products" indexId="count">
		<a href="javascript:newWindow('<bean:write name="productNames" property="key"/>')">
			<bean:write name="productNames" property="value"/><br>
		</a>
	</logic:iterate>
</body>
</html:html>
