<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri="/WEB-INF/lib/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/lib/struts-html-el.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/lib/struts-logic-el.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/lib/c.tld" prefix="c"%>
<%
  response.sendRedirect("SvError404.jsp") ;
%>
<html>
<head>
<title>Error Page</title>
</head>
<body>
<h1>ERROR</h1>

You have reached this page because of the following error(s):<br>
<br>

<html:messages id= "messages" message= "true">
  <bean:write name="messages"/> <br >
</html:messages >

<c:out value="${requestScope.message}"/>	 
</body>
</html>

