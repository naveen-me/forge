<%@ page language="java" import="java.util.HashMap" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>

<html:html>
<head>
<script Language="JavaScript">
	function closeWindow(){
		window.close();
	}
</script>
</head>
<body>
	<table>
	<tr>
	<td><b>Brand Name:</b></td><td><bean:write name="productDetails" property="brandName"/></td>
	</tr>
	<tr>
	<td><b>Product Name:</b></td><td><bean:write name="productDetails" property="productName"/></td>
	</tr>
	</table>
	<table width="100%">
	<tr>
		<td valign="top"><a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=y&direc=n&ingred=n&glos=n">Package Details</a><br><br>
		<logic:present name="desc">
			<bean:write name="productDetails" property="productDescription" filter="false"/>
		</logic:present>
		</td>
		
		<td valign="top"><a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=y&ingred=n&glos=n">Directions</a><br><br>
		<logic:present name="direc">
			<bean:write name="productDetails" property="productDirections" filter="false"/>
		</logic:present>
		</td>
		<td valign="top"><a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=y&glos=n">Ingredients</a><br><br>
		<logic:present name="ingred">
			<logic:iterate id="ingredientNames" name="productDetails" property="ingredients">
				<% if((((HashMap)(request.getAttribute("ingredientsWithGlossary"))).containsKey(ingredientNames.toString().toUpperCase()))){%>
					<a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName=<bean:write name="ingredientNames"/>"><bean:write name="ingredientNames"/></a>, &nbsp
				<%} else {%>
				    <bean:write name="ingredientNames"/>, &nbsp
				<%} %>
			</logic:iterate>
		</logic:present>
		<logic:present name="glos">
            <b><u><%=request.getAttribute("ingName").toString().toUpperCase()%></b></u><br><br>
			<%= ((HashMap)request.getAttribute("ingredientsWithGlossary")).get(request.getAttribute("ingName").toString().toUpperCase()) %>
		</logic:present>
		</td>
	</tr>
	</table>


	<P><HR WIDTH="50%"><P>
    <table>
       <tr>
         <td>
	       <logic:present name="direc">
             <img src="images/shared/trans.gif" width="250" height="10"><a href="javascript:history.go(-1)">Back to previous data</a>
            </logic:present>
	       <logic:present name="ingred">
             <img src="images/shared/trans.gif" width="250" height="10"><a href="javascript:history.go(-1)">Back to previous data</a>
           </logic:present>
	       <logic:present name="glos">
             <img src="images/shared/trans.gif" width="250" height="10"><a href="javascript:history.go(-1)">Back to previous data</a>
           </logic:present>
           <img src="images/shared/trans.gif" width="25" height="10"><a href="javascript:closeWindow()">Close This Window</a>
         </td>
       </tr>
    </table>
	<!--<CENTER></CENTER> -->
</body>
</html:html>


