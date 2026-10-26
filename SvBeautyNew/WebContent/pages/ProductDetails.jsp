<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script type="text/javascript" src="../SVBeauty/js/productDetails.js"></script>
<link rel="stylesheet" type="text/css" href='../SVBeauty/css/poductDetails.css'>
</head>
<body>
	<table>
	<logic:iterate id="productNames" name="products" indexId="count">
		<tr><td>
        	<a href="javascript:showMe('<bean:write name="productNames" property="key"/>')"><bean:write name="productNames" property="value"/></a><br />
        	</td>
        	<td>
        		<a href="javascript:showRecPopUp('1')">Why was this recommended?</a>
        	</td>
        </tr>
	</logic:iterate>
	</table>
	
	<div id="theLayer" style="position: absolute; visibility: hidden;width:700px;height:500px; left:12px; top:12px;bottom=12px;right:12px; overflow:auto;">
	<table border="0" bgcolor="#FFFFFF" width="100%" height="100%" cellspacing="0" cellpadding="3"  style="border-right: #000000 1px solid;
                  border-top: #000000 1px solid; border-left: #000000 1px solid; border-bottom: #000000 1px solid;">
		<tr><td style="height:5px; font-family:courier; font-size:10pt; color:#000000" valign="middle" align="right">Close
		<a href="#" onclick="hideMe();return false"><img src="img/b_close_button.gif" border=0/></a></td></tr>
		<%-- BRAND NAME --%>
		<tr><td style="text-align: left; height:5px; font-family: Arial; font-size: 14pt; color: #333333; font-weight:bold;" valign="middle">Brand Name----</td></tr>
		<tr><td style="text-align: left; height:5px;font-family: Arial;font-size: 11pt;color: #333333;" valign="middle">Product Name------</td></tr>
		<tr><td height="14px"></td></tr>
		<tr><td id="celltext" style="padding: 4px; width: 100%;" colspan="2"><div id="pageDiv" style="width:100%; height:100%"></div></td></tr>
    </table>
	</div>

</body>
</html:html>
