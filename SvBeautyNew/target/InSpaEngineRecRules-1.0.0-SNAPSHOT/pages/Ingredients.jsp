<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>

<html:html locale="true">
<head>
<title></title>
</head>

<body>
	<logic:iterate id="ing" name="ingredients" indexId="count">
		<a href="ingredientSearchAction.do?ing=<%= ing %>&link=<%= ing.toString().charAt(0) %>"><bean:write name="ing"/><br></a>
	</logic:iterate>
</body>

</html:html>