<%@ page import ="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations,com.svb.analysis.actions.HairAnalysisAction"%>
<html>
<head>
<link href="css/SkinStyle.css" rel="stylesheet" type="text/css" />
<link href="css/pop-up.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/productDetails.js"></script>
</head>
<body>
<%
    String customerId = "" ;
    String attributeId = "" ;
    ArrayList textdesc = new ArrayList() ;
    String headertext = "" ;
    String attribscore = "" ;
    String attribvalue = "" ;
    String attribname = "" ;
    String prodId     = "" ;

    String textdesc1  = "" ;
    HairAnalysisAction aa  =  new HairAnalysisAction() ;
   
  //  System.out.println("vbgfgfgfgfbbbb 666666") ;
//    CustomerProfileVO cp = new CustomerProfileVO() ;
//    Hashtable master  = new Hashtable() ;
//    ProductVO pr = new ProductVO() ;

    customerId = (String) request.getParameter("customerId") ;
    prodId = (String) request.getParameter("productId") ;
    attributeId  = (String) request.getAttribute("attributeId") ;
    attribscore  = (String) request.getAttribute("attribscore") ;
    attribvalue   = (String) request.getAttribute("attributeValue") ;
    attribname   = (String) request.getAttribute("attributeName") ;

//    cp = (CustomerProfileVO) request.getAttribute("singlecustomer") ;
//    pr = (ProductVO) request.getAttribute("singleproduct") ;

//    String skintype = (String) cp.getSkinType() ;
//    ArrayList skinconcerns = (ArrayList) cp.getSkinConcernList() ;
//    master = (Hashtable) request.getAttribute("hta") ;
    textdesc = (ArrayList) aa.IngredientBenefits(customerId, prodId, attributeId, attribvalue, attribname, attribscore) ;

  //  System.out.println("vbbbbb 777gfgfgffg7777" +textdesc.size()) ;
//    headertext = "<strong>"+attribscore+"</strong> for <strong>"+attribname+"</strong> because it contains:" ;
%>

<script language="JavaScript">
<!--

function seeAnalysis() {
 document.popdet.action = "<%=response.encodeURL("hairproductanalysis.do")%>" ;
 document.popdet.submit() ;
}
//-->
</script>
<form name="popdet" method="post" action="">
<div class="tab-running-content">
	<ul>
		<li><%=headertext%></li>
  
  <% for (int i=0; i < textdesc.size() ; i++) { 
     textdesc1 = "" ;
     textdesc1 = (String) textdesc.get(i) ;
  %>
  <li><%=textdesc1%></li>
  
 <% } %>
  <!--  <li><a href="javascript:seeAnalysis()">&lt;Back to product ratings</a></li>  -->
  </ul>
 </div> 
<input type=hidden name="customerId" value="<%=customerId%>">
<input type=hidden name="productId" value="<%=prodId%>">
<input type=hidden name="scrname" value="ingrbenefits"> 
</form>
</body>
</html>