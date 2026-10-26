<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/pager-taglib.tld" prefix="pg" %>

<html:html locale="true">
<head>
<title></title>

<script Language="JavaScript">
	function Blank_TextField_Validator(){
		if (document.forms[0].search.value  == ""){
			alert("Enter text to search ingredients");
			document.forms[0].search.focus();
			return (false);
		}
		return (true);
	}
</script>
  <style>
  /* Even row */
  .row1 {background-color:#C1CACA;}

  /* Odd row */
  .row0 {background-color:#AE848B;}
  </style>
<html:base/>
</head>

<body bgcolor="white">

	<html:errors/>
	
	<html:form action="/ingredientSearchAction.do" onsubmit="return Blank_TextField_Validator()">
		<html:text property="search"></html:text>
		<html:image src="../img/go_top_bty.gif" />
	</html:form>	

	<% 
		for(char ch='A'; ch<='Z'; ch++)
			out.println("<a href=../ingredientSearchAction.do?link="+ch+">"+ch+"</a>");
		out.println("<br>");
	%>
	
	<pg:pager url="../ingredientSearchAction.do" maxIndexPages="<%= Integer.parseInt(request.getAttribute("maxIndexPages").toString()) %>" maxPageItems="<%= Integer.parseInt(request.getAttribute("maxPageItems").toString())%>">
		<TABLE>
    		<TR>
    			<TH>Ingredient</th>
	        	<TH>Description</th>
	    	</TR>

		<logic:iterate id="ingredients" name="ingredientsMap" indexId="count" length="<%= request.getAttribute("length").toString() %>">
			<pg:item>
    		<TR>
			      <TD class='row<%= count.intValue() % 2 %>'>
        				<b><bean:write name="ingredients" property="key" filter="false"/></b>
			      </TD>
			      <TD class='row<%= count.intValue() % 2 %>'>
			        	<bean:write name="ingredients" property="value" filter="false"/>
			      </TD>
			      <logic:equal name="ingredients" property="key" value="Theasinensis extract">
			      		Hello World............
			      </logic:equal>
		    </TR>
		    </pg:item>
		</logic:iterate>
		</TABLE>
		<TABLE>
			<TR><TD>&nbsp;</TD></TR>
			<TR align="center">
				<TD>
					<pg:index>
					<pg:prev><a href="<%=pageUrl%>">[<< Prev]</a></pg:prev>
					<pg:pages>
						<a href="<%= pageUrl %>"><%= pageNumber %></a>
					</pg:pages>
					<pg:next><a href="<%= pageUrl%>">[Next >>]</a></pg:next>
					</pg:index>
				</TD>
			</TR>
		</TABLE>
	</pg:pager>

	<html:form action="/ingredientSearchAction.do" onsubmit="return Blank_TextField_Validator()">
		<html:text property="search"></html:text>
		<html:image src="../img/go_top_bty.gif" />
	</html:form>	
	
</body>
	
</html:html>
	
	