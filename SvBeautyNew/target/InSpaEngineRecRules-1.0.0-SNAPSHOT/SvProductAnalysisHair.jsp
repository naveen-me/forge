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

     	    String haircolor = "" ;
    	    String hairtype = "" ;
    	    String hairthickness = "" ;
    	    String hairtexture = "" ;
    	    String hairfinish = "" ;
            String natural = "" ;

    	    ArrayList hairprocess = new ArrayList() ;
    	    ArrayList hairconditions = new ArrayList() ;
    	    ArrayList hairstyles = new ArrayList() ;
            ArrayList allergies = new ArrayList() ;
    	    ArrayList concerns = new ArrayList() ;
            
            haircolor = cp.getHaircolor() ;
            hairtype = cp.getHairtype() ;
            hairthickness = cp.getHairthickness() ;
            hairtexture = cp.getHairtexture() ;
            hairfinish = cp.getHairfinish() ;

            hairprocess = (ArrayList) cp.getHairprocess() ;
            hairstyles = (ArrayList) cp.getHairstyles() ;
            hairconditions = (ArrayList) cp.getHairconditions() ;
            allergies = (ArrayList) cp.getAllergyList() ;
            natural = (String) cp.getNatural() ;

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
   if (val > 89.0f)
     comment = "Excellent" ;
   else if (val > 69.0f && val < 90.0f)
     comment = "Very Good" ;
   else if (val > 59.0f && val < 70.0f)
     comment = "Very Good" ;
   else if (val > 49.0f && val < 60.0f)
     comment = "Good" ;
   else if (val < 50.0f)
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
 document.popdet.action = "<%=response.encodeURL("hairprodanalysisdetail.do")%>" ;
 document.popdet.submit() ;
}
//-->
</script>
<form name="popdet" method="post" action="">
<div class="heading">
			<div class="hair-concern-label">HAIR CONCERN:</div>
			<div class="product-rating-label">PRODUCT RATING:</div>
			<div class="rating-detail-label">RATING DETAIL:</div>
</div>
<!-- Why Recommended Start -->
<div class="why-recommended">
	<ul>
		<!-- Loop Li Start -->
	    <%
	            String comm = "" ;
	            comm = (String) getAnalysisRating((String)ratings.get(hairtype)) ;
	            String hairtype1 =  (String) master.get(hairtype) ;
	            System.out.println("hairtype "+hairtype) ;
	            if (hairtype.equals("Y001"))
	                hairtype1 = "dry hair" ;
	            if (hairtype.equals("Y002"))
	                hairtype1 = "oily hair" ;
	            if (hairtype.equals("Y003"))
	                hairtype1 = "normal hair" ;
	            if (!comm.equals("Not Ideal")) { 
	    %>
	    <li>
	    	<div class="hair-concern"><%=hairtype1%></div>
	    	<div class="product-rating"><%=comm%></div>
	    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairtype%>','72','<%=comm%>','<%=master.get(hairtype)%>')">See active ingredients</a></div>
	    </li>
	     <% } %>
	     
	      <% if (hairprocess.size() > 0) { %>
	   <% for (int i=0; i < hairprocess.size() ; i++) { %>
	    <%
	            comm = "" ;
	            comm = (String) getAnalysisRating((String)ratings.get(hairprocess.get(i))) ;
	            if (!comm.equals("Not Ideal")) { 
    %>
    
	     	<li>
		    	<div class="hair-concern"><%=master.get(hairprocess.get(i))%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairprocess.get(i)%>','73','<%=comm%>','<%=master.get(hairprocess.get(i))%>')">See active ingredients</a></div>
		    </li>
    <% } %>
      <% } %>
   <% } %>
   	
   	
   	<%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(hairthickness)) ;
            String thickness = (String) master.get(hairthickness) ;
            if(thickness!=null && thickness.length()>0){
            if (thickness.equals("average"))
              thickness = "average (thickness)" ;}
            if (!comm.equals("Not Ideal")) { 
            
    %>
    
    		<li>
		    	<div class="hair-concern"><%=thickness%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairthickness%>','74','<%=comm%>','<%=master.get(hairthickness)%>')">See active ingredients</a></div>
		    </li>
	 <% } %>	    
		    
		    
	<%
            comm = "" ;
    
            comm = (String) getAnalysisRating((String)ratings.get(hairtexture)) ;
            String texture = (String) master.get(hairtexture) ;
            if(texture!=null && texture.length()>0){
            if (texture.equals("average"))
                 texture = "average (texture)" ;
    }
            if (!comm.equals("Not Ideal")) { 
    %>
    		<li>
		    	<div class="hair-concern"><%=texture%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairtexture%>','75','<%=comm%>','<%=master.get(hairtexture)%>')">See active ingredients</a></div>
		    </li>
	<% } %>	   
	
	
	
	<%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(hairfinish)) ;
            if (!comm.equals("Not Ideal")) { 
    %> 
    
    		<li>
		    	<div class="hair-concern"><%=master.get(hairfinish)%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairfinish%>','78','<%=comm%>','<%=master.get(hairfinish)%>')">See active ingredients</a></div>
		    </li>
	 <% } %>
	 
	 
	 
	 <% if (hairconditions.size() > 0) { %>
   <% for (int i=0; i < hairconditions.size() ; i++) { %>
    <%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(hairconditions.get(i))) ;
            if (!comm.equals("Not Ideal")) { 
    %>	    
    	    <li>
		    	<div class="hair-concern"><%=master.get(hairconditions.get(i))%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairconditions.get(i)%>','77','<%=comm%>','<%=master.get(hairconditions.get(i))%>')">See active ingredients</a></div>
		    </li>
		    
	 <% } %>
       <% } %>
    <% } %>	    
    
    <% if (hairstyles.size() > 0) { %>
<!--  <tr>
    <td style="text-align: left; font-family: Arial; font-size: 11px; color: rgb(51, 51, 51);"><strong>Styling Goals:</strong></td>
    <td width="35">&nbsp;</td>
    <td style="	font-family: Arial;	font-size: 11px;color: #333333;	font-weight:bold;"></td>
    <td width="35">&nbsp;</td>
    <td style="text-align: left; font-family: Arial; font-size: 11px; color: rgb(51, 51, 51);"></td>
  </tr> -->
   <% for (int i=0; i < hairstyles.size() ; i++) { %>
    <%
            comm = "" ;
            comm = (String) getAnalysisRating((String)ratings.get(hairstyles.get(i))) ;
            if (!comm.equals("Not Ideal")) { 
    %>
    		 <li>
		    	<div class="hair-concern"><%=master.get(hairstyles.get(i))%></div>
		    	<div class="product-rating"><%=comm%></div>
		    	<div class="rating-detail"><a href="javascript:seeAnalysis('<%=hairstyles.get(i)%>','76','<%=comm%>','<%=master.get(hairstyles.get(i))%>')">See active ingredients</a></div>
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