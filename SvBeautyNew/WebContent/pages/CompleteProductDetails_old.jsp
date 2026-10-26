<%@ page language="java" import="java.util.HashMap" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>

<html:html>
<head>
<script language="javascript" src='js/productDetails.js' ></script>
<link rel="stylesheet" type="text/css" href='css/productDetails.css'>
</head>
<body>
	<table>
		<tr><td class="header"><bean:write name="productDetails" property="brandName"/></td></tr>
		<tr><td class="textStyle"><bean:write name="productDetails" property="productName"/></td></tr>
		<tr><td height="14px"></td></tr>
		
	</table>

	<table cellspacing="0" cellpadding="0" border="0" >
  	 	<tr><td><img src="img/spacer.gif" height="10" width="0" vspace="0" hspace="0" border="0" alt=""/></td></tr>

		<tr>
			<td colspan="2">
			<table id="TblTabbedContainer" cellspacing="0" cellpadding="0" border="0" width="100%">
			<tr>
			<td align="left" valign="middle" id="TdTabStrip" >
				<table cellspacing="0" cellpadding="0" border="0">
					<tr>
					<td><table id="tdPromos" class="hiddenTab" cellspacing="0" cellpadding="0" border="0"><tr><td class="imgTab"><img id="imgPromos" src="img/arrow_right.gif" alt="Hidden Tab" vspace="2" hspace="0" valign="middle" border="0" width="3" height="5"></td>
			  		<td><input type="button" class="TblPDetailsBtn" name="btnPromos" id="btnPromos" value="Package Details" onclick="move('productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=y&direc=n&ingred=n&glos=n');"/></td></tr></table>
					</td>  		
	  			
	  				<td><img src="img/spacer.gif" height="0" width="5" vspace="0" hspace="0" border="0" alt=""/></td>
		  		
					<td><table id="tdingredients" class="hiddenTab" cellspacing="0" cellpadding="0" border="0"><tr><td class="imgTab"><img id="imgingredients" src="img/arrow_right.gif" alt="Hidden Tab" vspace="2" hspace="0" valign="middle" border="0" width="3" height="5"></td><td>
				   	<input type="button" class="TblPDetailsBtn" name="btningredients" id="btningredients" value="Ingredients" onclick="move('productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=y&glos=n')"/></td></tr></table>
					</td>
				
					<td><img src="img/spacer.gif" height="0" width="5" vspace="0" hspace="0" border="0" alt=""/></td>
									
					<td><table id="tdDirections" class="hiddenTab" cellspacing="0" cellpadding="0" border="0"><tr><td class="imgTab"><img id="imgDirections" src="img/arrow_right.gif" alt="Hidden Tab" vspace="2" hspace="0" valign="middle" border="0" width="3" height="5"></td><td>
					<input type="button" class="TblPDetailsBtn" name="btnDirections" id="btnDirections" value="Directions" onclick="move('productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=y&ingred=n&glos=n')"/></td></tr></table>
					</td>
	
					<td><img src="img/spacer.gif" height="0" width="5" vspace="0" hspace="0" border="0" alt=""/></td>
					</tr>
		   		</table>
			</td>
		</tr>
	</table>
	
	
	<table class="textStyle">
		<tr>
		 <td>
			<logic:present name="desc">
				<script language="javascript">MakeTabActive("Promos", "img/white-arrow-gray.gif", true);</script>
				<bean:write name="productDetails" property="productDescription" filter="false"/>
			</logic:present>
		
			<logic:present name="direc">
				<script language="javascript">MakeTabActive("Directions", "img/white-arrow-gray.gif", true);</script>
				<bean:write name="productDetails" property="productDirections" filter="false"/>
			</logic:present>
		
			<logic:present name="ingred">
				<script language="javascript">MakeTabActive("ingredients", "img/white-arrow-gray.gif", true);</script>
				<logic:iterate id="ingredientNames" name="productDetails" property="ingredients">
					<% if((((HashMap)(request.getAttribute("ingredientsWithGlossary"))).containsKey(ingredientNames.toString().toUpperCase()))){%>
						<a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName=<bean:write name="ingredientNames"/>"><bean:write name="ingredientNames"/></a>, &nbsp
					<%} else {%>
					    <bean:write name="ingredientNames"/>, &nbsp;
					<%} %>
				</logic:iterate>
			</logic:present>
		
			<logic:present name="glos">
				<script language="javascript">MakeTabActive("ingredients", "img/white-arrow-gray.gif", true);</script>
					<p class="header2"><%= request.getAttribute("ingName").toString() %></p>
					<p class="textStyle"><%= ((HashMap)request.getAttribute("ingredientsWithGlossary")).get(request.getAttribute("ingName").toString().toUpperCase()) %></p>
					<br><br><br>
					<p align="right"><a href="productDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=y&glos=n"><< Back To the Ingredients</a></p>
			</logic:present>
					
		</td>
		<tr>
		</tr>   	
	</table>
	
</body>
</html:html>


