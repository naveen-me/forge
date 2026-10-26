            <%@ page
	import="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations"%>
<html>
<head>
<link href="css/SkinStyle.css" rel="stylesheet" type="text/css" />
<link href="css/pop-up.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/productDetails.js"></script>
</head>
<body>
	<%
		System.out.println("vbbbbb 666666");
		CustomerProfileVO cp = new CustomerProfileVO();
		Hashtable master = new Hashtable();
		ProductVO pr = new ProductVO();
		Hashtable ratings = new Hashtable();
		Hashtable ingredientPercentage = new Hashtable();
		Hashtable basePercentage = new Hashtable();
		Hashtable bumpScore = new Hashtable();
		String score = "";
		String scorecomment = "";
		String allergytext = "";
		String naturaltext = "";

		cp = (CustomerProfileVO) request.getAttribute("singlecustomer");
		pr = (ProductVO) request.getAttribute("singleproduct");

		String skintype = (String) cp.getSkinType();
		ArrayList skinconcerns = (ArrayList) cp.getSkinConcernList();
		ArrayList eyeconcerns = (ArrayList) cp.getEyeConcernList();
		ArrayList skinconditions = (ArrayList) cp.getSkinConditionList();
		ArrayList allergies = (ArrayList) cp.getAllergyList();
		String natural = (String) cp.getNatural();

		master = (Hashtable) request.getAttribute("hta");
		ratings = (Hashtable) request.getAttribute("ratings");
		ingredientPercentage = (Hashtable) request.getAttribute("ingredientPercentage");
		basePercentage = (Hashtable) request.getAttribute("basePercentage");
		bumpScore = (Hashtable) request.getAttribute("bumpScore");
		//  System.out.println("vbbbbb 7775455454777t45tttra "+ratings.size()) ;
		//  System.out.println("vbbbbb 7777777tttra "+ratings.size()) ;

		//    Enumeration en = ratings.keys() ;
		//   while (en.hasMoreElements())
		//  {
		//        String k = (String) en.nextElement() ;
		//          System.out.println("rrr "+k) ;
		//         System.out.println("dgdgf "+ratings.get(k)) ;
		//  }
	%>
	<%!String getAnalysisRating(String value) {
		String comment = "Not Ideal";
		float val = 0.0f;
		try {
			val = Float.parseFloat(value);
		} catch (Exception ex) {
			val = 0.0f;
		}
		if (val > 64.0f)
			comment = "Excellent";
		else if (val > 55.0f && val < 65.0f)
			comment = "Very Good";
		else if (val > 49.0f && val < 55.0f)
			comment = "Good";
		else if (val > 39.0f && val < 50.0f)
			comment = "Fair";
		else if (val < 40.0f)
			comment = "Not Ideal";

		System.out.println("value in method " + value + "  " + comment);
		return comment;
	}%>
	<script language="JavaScript">
<!--

function seeAnalysis(id1,id2,id3,id4) {
 document.popdet.attributeValue.value = id1 ;
 document.popdet.attributeScore.value = id3 ;
 document.popdet.attributeId.value = id2 ;
 document.popdet.attributeName.value = id4 ;
 document.popdet.action = "<%=response.encodeURL("prodanalysisdetail.do")%>";
			document.popdet.submit();
		}
	//-->
	</script>


	<table border="2">
		<tr>
			<th>SKIN CONCERN</th>
			<th>INGREDIENT PERCENTAGE</th>
			<th>BASE PERCENTAGE</th>
			<th>BUMP SCORE</th>
			<th>TOTAL SCORE</th>
			<th>Rating</th>
		</tr>
	
		<tr>
			<td><b><%=master.get(skintype)%></b></td>
			<td><%=ingredientPercentage.get(skintype) %></td>
			<td><%=basePercentage.get(skintype) %></td>
			<td><%=bumpScore.get(skintype) %></td>
			<td><%=ratings.get(skintype) %></td>
			<td><%=(String) getAnalysisRating((String)ratings.get(skintype))%> </td>
		</tr>
			<%
				if (skinconcerns.size() > 0) {
			%>
			<%
				for (int i = 0; i < skinconcerns.size(); i++) {
					  String comm = "" ;
			            comm = (String) getAnalysisRating((String)ratings.get(skinconcerns.get(i))) ;
			%>
		<tr>
			<td><b><%=master.get(skinconcerns.get(i))%></b></td>
			<td><%=ingredientPercentage.get(skinconcerns.get(i))%></td>
			<td><%=basePercentage.get(skinconcerns.get(i))%></td>
			<td><%=bumpScore.get(skinconcerns.get(i))%></td>
			<td><%=ratings.get(skinconcerns.get(i))%></td>
			<td><%=comm %></td>
		</tr>
		<%
				}
			%>
			<%
				}
			%>


			
		<%
			if (skinconditions.size() > 0) {
		%>
		<%
			for (int i = 0; i < skinconditions.size(); i++) {
				  String comm = "" ;
		            comm = (String) getAnalysisRating((String)ratings.get(skinconditions.get(i))) ;
		%>
		<tr>

			<td><b><%=master.get(skinconditions.get(i))%></b></td>
			<td><%=ingredientPercentage.get(skinconditions.get(i))%></td>
			<td><%=basePercentage.get(skinconditions.get(i))%></td>
			<td><%=bumpScore.get(skinconditions.get(i))%></td>
			<td><%=ratings.get(skinconditions.get(i))%></td>
			<td><%=comm %></td>
		</tr>
		<%
			}
		%>
		<%
			}
		%>
		<%
				if (eyeconcerns.size() > 0) {
			%>
			<%
				for (int i = 0; i < eyeconcerns.size(); i++) {
					  String comm = "" ;
			            comm = (String) getAnalysisRating((String)ratings.get(eyeconcerns.get(i))) ;
			%>
		<tr>
			<td><b><%=master.get(eyeconcerns.get(i))%></b></td>
			<td><%=ingredientPercentage.get(eyeconcerns.get(i))%></td>
			<td><%=basePercentage.get(eyeconcerns.get(i))%></td>
			<td><%=bumpScore.get(eyeconcerns.get(i))%></td>
			<td><%=ratings.get(eyeconcerns.get(i))%></td>
			<td><%=comm %></td>
		</tr>
<%
				}
			%>
			<%
				}
			%>
	</table>


<h3>Note : </h3>
<p>Total Score > 64.0 = <b>Excellent</b> </p>
<p>Total Score > 55.0 & Total Score < 65.0= <b>Very Good </b></p>
<p>Total Score > 49.0 & Total Score < 55.0 = <b>Good </b></p>
<p>Total Score > 39.0 & Total Score < 50.0 = <b>Fair </b></p>
<p>Total Score < 40.0 = <b>Not Ideal </b></p>

</body>
</html>