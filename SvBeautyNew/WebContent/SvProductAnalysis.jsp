<%@ page import ="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations"%>
<html>
<head>
<link href="css/SkinStyle.css" rel="stylesheet" type="text/css" />
<link href="css/pop-up.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/productDetails.js"></script>
</head>
<body>
<%
    System.out.println("vbbbbb 666666") ;
    CustomerProfileVO cp = new CustomerProfileVO() ;
    Hashtable master  = new Hashtable() ;
    ProductVO pr = new ProductVO() ;
    Hashtable ratings = new Hashtable() ;
    String score = "" ;
    String scorecomment = "" ;
    String allergytext = "" ;
    String naturaltext = "" ;

    cp = (CustomerProfileVO) request.getAttribute("singlecustomer") ;
    pr = (ProductVO) request.getAttribute("singleproduct") ;

    String skintype = (String) cp.getSkinType() ;
    ArrayList skinconcerns = (ArrayList) cp.getSkinConcernList() ;
    ArrayList eyeconcerns = (ArrayList) cp.getEyeConcernList() ;
    ArrayList skinconditions = (ArrayList) cp.getSkinConditionList() ;
    ArrayList allergies = (ArrayList) cp.getAllergyList() ;
    String natural = (String) cp.getNatural() ;

    master = (Hashtable) request.getAttribute("hta") ;
    ratings = (Hashtable) request.getAttribute("ratings") ;
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
<%! String getAnalysisRating(String value)
{
   String comment = "Not Ideal" ;
   float val = 0.0f ;
   try
   {
      val = Float.parseFloat(value) ;
   } catch (Exception ex)
   {
      val = 0.0f ;
   }
   if (val > 64.0f)
     comment = "Excellent" ;
   else if (val > 55.0f && val < 65.0f)
     comment = "Very Good" ;
   else if (val > 49.0f && val < 55.0f)
     comment = "Good" ;
   else if (val > 39.0f && val < 50.0f)
     comment = "Fair" ;
   else if (val < 40.0f)
     comment = "Not Ideal" ;
   
   System.out.println("value in method "+value+"  "+comment) ;
   return comment ;
}
%>
<script language="JavaScript">
<!--

function seeAnalysis(id1,id2,id3,id4) {
 document.popdet.attributeValue.value = id1 ;
 document.popdet.attributeScore.value = id3 ;
 document.popdet.attributeId.value = id2 ;
 document.popdet.attributeName.value = id4 ;
 document.popdet.action = "<%=response.encodeURL("prodanalysisdetail.do")%>" ;
 document.popdet.submit() ;
}
//-->
</script>
<form name="popdet" method="post" action="">
<div class="heading">
	<div class="hair-concern-label">SKIN CONCERN:</div>
	<div class="product-rating-label">PRODUCT RATING:</div>
	<div class="rating-detail-label">RATING DETAIL:</div>
</div>

<!-- Why Recommended Start -->
<div class="why-recommended">
	<ul>
		<!-- Loop Li Start -->
    <%
            String comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(skintype)) ;
            if (!comm.equals("Not Ideal")) { 
    %>
  <li>
    <div class="hair-concern"><%=master.get(skintype)%></div>
    <div class="product-rating"><%=comm%></div>
    <td width="35">&nbsp;</td>
    <div class="rating-detail"><a href="javascript:seeAnalysis('<%=skintype%>','51','<%=comm%>','<%=master.get(skintype)%>')">See active ingredients</a></div>
  </li>
    <% } %>
  <% if (skinconcerns.size() > 0) { %>
   <% for (int i=0; i < skinconcerns.size() ; i++) { %>
    <%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(skinconcerns.get(i))) ;
            if (!comm.equals("Not Ideal")) { 
    %>
  <li>
    <div class="hair-concern"><%=master.get(skinconcerns.get(i))%></div>
    <div class="product-rating"><%=comm%></div>
    <div class="rating-detail"><a href="javascript:seeAnalysis('<%=skinconcerns.get(i)%>','53','<%=comm%>','<%=master.get(skinconcerns.get(i))%>')">See active ingredients</a></div>
  </li>
    <% } %>
       <% } %>
    <% } %>
  <% if (skinconditions.size() > 0) { %>
   <% for (int i=0; i < skinconditions.size() ; i++) { %>
    <%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(skinconditions.get(i))) ;
            if (!comm.equals("Not Ideal")) { 
    %>
  <li>
		<div class="hair-concern"><%=master.get(skinconditions.get(i))%></div>
	    <div class="product-rating"><%=comm%></div>
    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=skinconditions.get(i)%>','52','<%=comm%>','<%=master.get(skinconditions.get(i))%>')">See active ingredients</a></div>
  </li>
    <% } %>
       <% } %>
    <% } %>
  <% if (eyeconcerns.size() > 0) { %>
   <% for (int i=0; i < eyeconcerns.size() ; i++) { %>
    <%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(eyeconcerns.get(i))) ;
            if (!comm.equals("Not Ideal")) { 
    %>
  <li>
		   <div class="hair-concern"><%=master.get(eyeconcerns.get(i))%></div>
    	   <div class="product-rating"><%=comm%></div>
    	   <div class="rating-detail"><a href="javascript:seeAnalysis('<%=eyeconcerns.get(i)%>','54','<%=comm%>','<%=master.get(eyeconcerns.get(i))%>')">See active ingredients</a></div>
  </li>
    <% } %>
       <% } %>
    <% } %>
  
<!-- Loop Li End -->
		
	</ul>
	
</div>
    <div class="why-recommended-bottom">
	<ul>
      <% for (int i=0; i < allergies.size() ; i++) { %>
        <% if (!allergies.get(i).equals("L999")) { %>
      <li>Does not contain <%=master.get(allergies.get(i))%></li>
        <% } } %>
      <% if (natural != null && natural.equals("A008")) { %>
      <li>Formulation is Natural/Botanical</li>
      <% } %>
    </ul>
    </div>
  

<input type=hidden name="customerId" value="<%=cp.getCustomerId()%>">
<input type=hidden name=productId value="<%=pr.getProdId()%>">
<input type=hidden name=attributeId value="">
<input type=hidden name=attributeValue value="">
<input type=hidden name=attributeScore value="">
<input type=hidden name=attributeName value="">
</form>
</body>
</html>