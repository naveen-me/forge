<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>


<html:html>
<head>
<script type="text/javascript" src="js/productDetails.js"></script>
<link rel="stylesheet" type="text/css" href='css/poductDetails.css'>
</head>
<body>
	<table>
	<logic:iterate id="productNames" name="products" indexId="count">
		<tr><td>
        	<a href="javascript:showMeProduct('<bean:write name="productNames" property="key"/>')"><bean:write name="productNames" property="value"/></a><br />
        </td></tr>
	</logic:iterate>
	</table>
	
	<div id="theLayer" style="position: absolute; visibility: hidden;width:600px;height:550px; left:12px; top:12px;bottom=12px;right:12px;">
		<table border="0" cellspacing="0" cellpadding="0" style="width: 600px; height: 550px;">
            <tr>
                <td valign="top" style="width: 100%">
                    <table border="0" width="100%" height="100%" cellspacing="0" cellpadding="0" style="border-right: #000000 1px solid;
                        border-top: #000000 1px solid; border-left: #000000 1px solid; border-bottom: #000000 1px solid;">
                        <tr>
                            <td style="text-align: right; background-image: url(bar.gif); height:5px; " valign="top">
                                Close&nbsp;<a href="#" onclick="hideMe();return false"><img src="img/b_close_button.gif"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td id="celltext" style="padding: 4px; width: 100%;" colspan="2">
                                <div id="pageDiv" style="width: 100%; height: 100%;"></div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>

</body>
</html:html>
