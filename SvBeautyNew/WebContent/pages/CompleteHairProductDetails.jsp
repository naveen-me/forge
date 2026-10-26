<%@ page language="java" import="java.util.HashMap,com.svb.struts.bean.ProductDetailsBean" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%

   String[][]  creviews = new String[0][0] ;
   creviews = (String[][]) request.getAttribute("creviews") ;

%>
<html:html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1"> 
<link rel="stylesheet" href="css/pop-up.css" />
<script language="javascript" src='js/productDetails.js' ></script>
</head>
<body>
	
	<div class="tab-running-content">
		<ul>
			<li>
				<logic:present name="desc">
					<bean:write name="productDetails" property="productDescription" filter="false"/>
				</logic:present>
				<logic:present name="direc">
					<bean:write name="productDetails" property="productDirections" filter="false"/>
				</logic:present>
				
				<logic:present name="ingred">
				<!-- Ingredent Div Start -->
				<div class="ingred">
					<ul>
						<li>
							<div class="ingredients-label">Ingredients: </div>
							<div class="ingredients-content">	
								<logic:iterate id="ingredientNames" name="productDetails" property="ingredients" indexId="content">
				                    <% String icount = (String) ((ProductDetailsBean)request.getAttribute("productDetails")).getIngredientsCount() ; %>
				                    <% //System.out.println("cbbbbc "+content+" fffff "+icount); %>
									<% if((((HashMap)(request.getAttribute("ingredientsWithGlossary"))).containsKey(ingredientNames.toString().toUpperCase()))){%>
				                      <% if (true) { %> 
										<a href="hairProductDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName=<bean:write name="ingredientNames"/>"><bean:write name="ingredientNames"/></a>
				                      <% } else { %>
										<a href="hairProductDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName=<bean:write name="ingredientNames"/>"><bean:write name="ingredientNames"/></a>,
				                      <% } %> 
									<%} else {%>
				                       <% if (true) { %> 
									    <bean:write name="ingredientNames"/> 
				                       <% } else { %>
									    <bean:write name="ingredientNames"/>,
				                       <% } %> 
									<%} %>
								</logic:iterate>
								</div>
						</li>
					</ul>
				</div><!-- Ingredent Div End -->
								
				</logic:present>
				
				<logic:present name="glos">
				<!-- Inner UL 1 Start  -->
				<ul>
					<li><%= request.getAttribute("ingName").toString() %></li>
					<li><%= ((HashMap)request.getAttribute("ingredientsWithGlossary")).get(request.getAttribute("ingName").toString().toUpperCase()) %></li>
					<% if (request.getAttribute("scrname") != null && ((String)request.getAttribute("scrname")).equals("whyrec")) { %>
					<li><a href="hairprodanalysisdetail.do?productId=<%= request.getAttribute("productId")%>&customerId=<%= request.getAttribute("customerId")%>&attributeId=<%= request.getAttribute("attributeId")%>&attributeValue=<%= request.getAttribute("attributeValue")%>&attributeName=<%= request.getAttribute("attributeName")%>&attributeScore=<%= request.getAttribute("attributeScore")%>">< Back to see active ingredients</a></li>
	                 <% } else if (request.getAttribute("scrname") != null && ((String)request.getAttribute("scrname")).equals("ingrbenefits")) { %>
					   <li><a href="javascript:history.go(-1)">< Back to see active ingredients</a></li>
					<% } else { %>
					<li><a href="hairProductDetails.do?productId=<%= request.getParameter("productId") %>&popup=y&desc=n&direc=n&ingred=y&glos=n">< Back to list of ingredients</a></li>
					<% } %>
				</ul><!-- Inner UL 1 End  -->
				</logic:present>
				
				<logic:present name="revw">
                <% String totavgrating = "" ;%>
                <% if (creviews.length > 0) { 
                     totavgrating = (String) creviews[0][3] ; %>
                 
                 
                 <!-- Inner Review Outer Start -->
                 <div class="review-outer">
	
					<div class="avrage-reviews">
						<ul>
							<li>
								<div class="avrage-review-label">Average Rating:</div>
								<div class="avrage-review-image-text"><img src="images/<%=totavgrating%>" width="63" height="12" border="0"></img><span>3.0(Based on 18 reviews)</span></div>
							</li>
						</ul>
					</div>
				
				
					<div class="review-content">
						<ul>
						<% for (int i=0; i < creviews.length; i++) { %>
							<li>
								<div class="review-content-ratting-image"><img src="images/<%=creviews[i][0]%>" width="63" height="12" border="0"></img></div>
								<div class="review-content-product-desc"><%=creviews[i][1]%><br/> -- by Anonymos on <%=creviews[i][2]%></div>
							</li>
						<% } %>	
						</ul>
					</div>
				
				  </div><!-- Inner Review Outer End -->  
				  
                 <% } %>  
				</logic:present>
				
			</li><!-- Outer Li End -->
		</ul><!-- Outer Ul End -->
	</div><!-- Outer Div.tab-running-content End -->
	
</body>
</html:html>


