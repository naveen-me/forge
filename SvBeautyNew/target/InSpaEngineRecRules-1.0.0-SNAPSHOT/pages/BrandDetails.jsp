<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList, com.svb.struts.bean.BrandDetailsBean" %>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<link rel='stylesheet' type='text/css' href='css/SkinStyle.css' />
<link rel='stylesheet' type='text/css' href='css/pop-up.css' />
</head>
<body>


<!-- About The Brand Start -->
<div class="tab-running-content">
	<ul>
		<logic:iterate id="brand" name="brands" indexId="count">
		<li><%=((BrandDetailsBean)brand).getSV_BRAND_DESCRIPTION()%></li>
		</logic:iterate>
	</ul>
</div><!-- About The Brand End -->

	
</body>

</html:html>


