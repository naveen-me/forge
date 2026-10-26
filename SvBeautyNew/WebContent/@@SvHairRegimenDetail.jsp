<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import ="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations"%>
<%
	String str = "";

	String mid = "" ;
    String mode = "" ;
    String catid = "" ;
	String customerId = "" ;
	String prodType = "" ;
	String prodLine = "" ;
    String prodLineFlag = "" ;
    String prodTypeFlag = "" ;
    String prodId = "" ;		
    String pprice =  "" ;
    float tprice = 0.00f ;
	String prodmainimage = "" ;
    ProductVO products [] = new ProductVO[0] ;
    CustomerProfileVO cp = new CustomerProfileVO() ;
    BrandVO brand = new BrandVO() ;
    boolean finishflag = false ;
    String basicstring = "" ;
    boolean secondflag = false ;

	DecimalFormat ft    = new DecimalFormat();	
	//ft.applyLocalizedPattern("#0.00");   	
	ft.setMinimumFractionDigits(2);
    ft.setMaximumFractionDigits(2);	
	String cusid = "" ;
    String messagesent = "no" ;
	String brandlogofile = "images/shared/trans.gif" ;

   
    ArrayList prodlist = new ArrayList() ;
	ArrayList shampoo = new ArrayList();
	ArrayList tone = new ArrayList();
	ArrayList htreat = new ArrayList();
	ArrayList styling = new ArrayList();
	ArrayList conditioner = new ArrayList();
	ArrayList nightconditioner = new ArrayList();
	ArrayList exfoliant = new ArrayList();
	ArrayList finishprods = new ArrayList();
    ProductVO[] finish = new ProductVO[0] ;

    String shampooPrice = "" ;
    String conditionerPrice = "" ;
    String htreatPrice = "" ;
    String stylingPrice = "" ;
    String finishPrice = "" ;
    String finishexists = "no" ;
    String finishprod = "" ;
    String shampooprod = "" ;
    String conditionerprod = "" ;
    String htreatprod = "" ;
    String stylingprod = "" ;
    String shampooskip = "" ;
    String conditionerskip = "" ;
    String htreatskip = "" ;
    String stylingskip = "" ;

    ProductVO shampoopr  = new ProductVO() ;
    ProductVO conditionerpr  = new ProductVO() ;
    ProductVO htreatpr  = new ProductVO() ;
    ProductVO stylingpr  = new ProductVO() ;
    ProductVO finishpr  = new ProductVO() ;

    Hashtable brands= new Hashtable() ;
    Hashtable master = new Hashtable() ;
    Hashtable prodhash = new Hashtable() ;
    boolean noshampoo = false ;
    boolean noconditioner = false ;
    boolean nohtreat = false ;
    boolean nostyling = false ;
    boolean nofinish = false ;
    boolean shampoorepeat = false ;
    boolean conditionerrepeat = false ;
    boolean htreatrepeat = false ;
    boolean stylingrepeat = false ;
    ArrayList dynamic = new ArrayList() ;
    String warnings = "" ;
    String commontext = "Test a small amount of each new product on a hidden area (such as behind your ear) to make sure that your skin does not have any adverse reactions.  Always follow the recommended application quantity and frequency for each product.  Consult your physician before using these products if you are pregnant or nursing." ;
    String starrating = "" ;
    String starrating1 = "" ;

		 try
		 {
                System.out.println("In the begining ....") ;
                mid = (String) request.getAttribute("mid") ;							
                mode = (String) request.getAttribute("mode") ;							
                catid = (String) request.getAttribute("catid") ;							
                customerId = (String) request.getAttribute("customerId") ;							
                cusid = (String) request.getAttribute("cusid") ;							
                prodLine = (String) request.getAttribute("prodline") ;							
                prodLineFlag = (String) request.getAttribute("prodlineflag") ;							
                prodTypeFlag = (String) request.getAttribute("prodtypeflag") ;							
//			    products = (ProductVO[]) request.getAttribute("productlist") ;
			    prodlist = (ArrayList) request.getAttribute("productlist") ;
			    prodhash = (Hashtable) request.getAttribute("producthash") ;
                master = (Hashtable) request.getAttribute("masterData") ;
			    dynamic = (ArrayList) request.getAttribute("dynamic") ;
			    basicstring = (String) request.getAttribute("basicstring") ;
                brands = (Hashtable) request.getAttribute("brands") ;
                cp = (CustomerProfileVO) request.getAttribute("customerprofile") ;
                if (prodLine != null && prodLine.length() > 0)
                {
                   brand = (BrandVO) brands.get(prodLine) ;
                   brandlogofile  = "brand_images/"+(String)brand.getBrandLogoFile() ;
                }
                if (prodlist.size() > 0)
                {
                  shampoo = (ArrayList) prodlist.get(0) ;
                  conditioner =    (ArrayList) prodlist.get(1) ;           
                  htreat =    (ArrayList) prodlist.get(2) ;           
                  styling =    (ArrayList) prodlist.get(3) ;           
                  finishprods  = (ArrayList) prodlist.get(4) ;           
                }
                
                shampooskip = (String) request.getAttribute("shampooskip") ;
                conditionerskip = (String) request.getAttribute("conditionerskip") ;
                htreatskip = (String) request.getAttribute("htreatskip") ;
                stylingskip = (String) request.getAttribute("stylingskip") ;

                if (shampooskip!= null && shampooskip.equals("shampooSkip"))
                      shampoo = new ArrayList() ;
                else if (shampooskip == null)
                    shampooskip = "" ;

                if (conditionerskip!= null && conditionerskip.equals("conditionerSkip"))
                      conditioner = new ArrayList() ;
                else if (conditionerskip == null)
                    conditionerskip = "" ;

                if (htreatskip!= null && htreatskip.equals("htreatSkip"))
                      htreat = new ArrayList() ;
                else if (htreatskip == null)
                    htreatskip = "" ;

                if (stylingskip!= null && stylingskip.equals("stylingSkip"))
                      styling = new ArrayList() ;
                else if (stylingskip == null)
                    stylingskip = "" ;

                shampooprod = (String) request.getAttribute("shampooprod") ;
                conditionerprod = (String) request.getAttribute("conditionerprod") ;
                htreatprod = (String) request.getAttribute("htreatprod") ;
                stylingprod = (String) request.getAttribute("stylingprod") ;
                finishprod = (String) request.getAttribute("finishprod") ;
                finishexists = (String) request.getAttribute("finishexists") ;
                System.out.println("vbbbbbdxdfg") ;
                try
                {
                 if (request.getAttribute("finish") != null)
                   finish = (ProductVO[]) request.getAttribute("finish") ;
                } catch(Exception exx)
                {
                  System.out.println(exx+"  "+exx) ;
                }
                messagesent = (String) request.getAttribute("mailmessage") ;
                if (messagesent == null)
                  messagesent = "no" ;

                if (shampooprod !=null && shampooprod.length() >0)
                {
                    shampoopr = (ProductVO) prodhash.get(shampooprod.trim()) ;
                    shampooPrice = (String) shampoopr.getPrice() ;
                    tprice = tprice+Float.parseFloat(shampooPrice.substring(0,shampooPrice.length())) ;
                }
                else
                    noshampoo = true ;
                //System.out.println("vvvvvvvvvdsds3 "+noshampoo+"  "+tprice) ;				

                if (conditionerprod !=null && conditionerprod.length() >0)
                {
                    conditionerpr = (ProductVO) prodhash.get(conditionerprod.trim()) ;
                    conditionerPrice = (String) conditionerpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(conditionerPrice.substring(0,conditionerPrice.length())) ;
                }
                else
                {
                    noconditioner = true ;
                }
                //System.out.println("vvvvvvvvvdsds5 "+noconditioner) ;				

                if (htreatprod !=null && htreatprod.length() >0)
                {
                    htreatpr = (ProductVO) prodhash.get(htreatprod.trim()) ;
                    htreatPrice = (String) htreatpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(htreatPrice.substring(0,htreatPrice.length())) ;
                }
                else
                    nohtreat = true ;
                //System.out.println("vvvvvvvvvdsds7 "+nohtreat) ;				
				 
                if (stylingprod !=null && stylingprod.length() >0)
                {
                    stylingpr = (ProductVO) prodhash.get(stylingprod.trim()) ;
                    stylingPrice = (String) stylingpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(stylingPrice.substring(0,stylingPrice.length())) ;
                }
                else
                    nostyling = true ;
                //System.out.println("finish prod...... "+finishprod) ;
                if (finishprod !=null && finishprod.length() >0)
                {
                    finishpr = (ProductVO) prodhash.get(finishprod.trim()) ;
                    finishPrice = (String) finishpr.getPrice() ;
                  //  tprice = tprice+Float.parseFloat(finishPrice.substring(0,finishPrice.length())) ;
                }
                else
                    nofinish = true ;
                //System.out.println("vvvvvvvvvdsds9 "+nofinish) ;
                pprice = (String) ft.format(new Double(tprice)) ;

                if (dynamic.size() > 0)
                {
                     warnings = (String) dynamic.get(0) ;
                    // commontext = (String) dynamic.get(1) ;
                }
			}catch(Exception ex1)
			{
			  ex1.printStackTrace() ;
			}
%>			
<head>
<title>Solution for beautiful hair</title>
<link href="css/SkinStyle.css" rel="stylesheet" type="text/css" />
<!--[if IE]><link href="css/ie.css" rel="stylesheet" type="text/css" /><![endif]-->
<script type="text/javascript" src="js/productDetails.js"></script>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/header.css" />
<link rel="stylesheet" href="css/hair-product.css" />
<link rel="stylesheet" href="css/pop-up.css" />
<link rel="stylesheet" href="css/footer.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Hair Care</title>
<script type="text/javascript" src="js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="js/jquery.bpopup-0.7.0.min.js"></script>
<script type="text/javascript">
		$(window).load(function(){
		$(".breadcrumb ul li:last-child a").css({"background-image":"none"});	
		/* ============== Product Thumb Image Vertical Align Middle ============== */
		$('div.selected-product-thumb-image, div.product-thumb-image').each(function(){
			var $div = $(this);
			var $divh = $div.height();
			var $img = $div.find("img");
			var $imgh = $img.height();
			var resh = $divh - $imgh;
			//console.log(resh);
			$img.css('margin-top', + resh / 2 + "px");
		});
		/* ============== Product Thumb Image Vertical Align Middle ============== */
		
		/* ============== Recommendation Details Alternet Color's ============== */	
		$(".recommendation-running-content ul li:odd").css({background:"#e2e2e2"});
		$(".recommendation-running-content ul li:even").css({background:"#FBFBFB"});
		/* ============== Recommendation Details Alternet Color's ============== */
		
		/* ============== Recommendation Details Expand Collapse ============== */
		$(".recommendation-header").click(function(){
			var $this = $(this), $header = $this.children("div:first"), addc = $header.attr('class');
			if(addc === 'collapse'){
  				$header.removeClass().addClass('expand');
			  	$this.parent().find('.recommendation-running-content').stop(true, true).slideUp();
			}else{
				$header.removeClass().addClass('collapse');
				$this.parent().find('.recommendation-running-content').stop(true, true).slideDown();
			}
		});
		/* ============== Recommendation Details Expand Collapse ============== */
		
		/* ============== Pop up Tab Java Script Start ================== */
		$('.tab-nav li a').each(function(e){
			$(this).click(function(e){
				e.preventDefault();
				$('.tab-nav li a').removeClass('active')
				$(this).addClass('active');
				var rel = $(this).attr('rel');
				$('.ProductContent').hide();
				$('#'+rel).fadeIn(500);
			})
		});
		
		$('.selected-product-thumb-image img, .product-thumb-image img').on('click',function(){
		  $('.product-pop-thumb-image img').attr('src',$(this).attr('src'));
  		  var $this = $(this).closest('div > .selected-product-thumb-image');
		  var $rating= $this.find('.product-rating');
		  /*var $name= $this.find('.product-name');
		  console.log($name);
		  $('#pop-up-outer').find('.product-rating').html($rating);
		  /*$('#pop-up-outer').find('.product-titel-des').html($name);*/
		  //console.log($rating);
		  
		  $('#pop-up-outer').bPopup();
		  
		});
		/* ============== Pop up Tab Java Script End   ================== */ 
	});
</script>
<!--[if gte IE 9]>
<link rel="stylesheet" type="text/css" href="css/ie9.css" />
<![endif]-->
<script type="text/javascript">
<!--
function submitInfo() {

if(changePrice() && regimenStatus()){

      document.regdet.target = "" ;
      document.regdet.finishCheck1.value ="truevb" ;
      document.regdet.action = "<%=response.encodeURL("hairregimenshop.do")%>" ;
      document.regdet.submit() ;
  }
}
function regimenStatus(){

  var status = false;
  
  if (document.regdet.shampooqty != null)
  if(document.regdet.shampooCheck.checked && document.regdet.shampooqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.conditionerqty != null)
  if(document.regdet.conditionerCheck.checked && document.regdet.conditionerqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.htreatqty != null)
  if(document.regdet.htreatCheck.checked && document.regdet.htreatqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.stylingqty != null)
  if(document.regdet.stylingCheck.checked && document.regdet.stylingqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.finishqty != null)
  if(document.regdet.finishCheck.checked && document.regdet.finishqty.value.length!=0)
  {
      status = true;
  }
  
  if(status)
  {
    return true;
  }
  else
  {
    alert("Please select an item before proceeding to shopping cart");
    return false;
  }
}
function call_pdf(id1)
{
      document.regdet.action = "<%=response.encodeURL("hairproductDescPdfGenerator.do")%>" ;
      document.regdet.target = "_blank" ;
      document.regdet.submit() ;
   // window.open("<%=response.encodeURL("productDescPdfGenerator.do")%>") ;
}
function skipThis(id)
{
  if(id=="shampoo")
  {
     for(i=0; i<document.regdet.shampooSelect.length; i++)
		document.regdet.shampooSelect[i].checked=0;
  }
  if (id == "conditioner")
  {
     for(i=0; i<document.regdet.conditionerSelect.length; i++)
		document.regdet.conditionerSelect[i].checked=0;
  }
  if(id=="htreat")
  {
     for(i=0; i<document.regdet.htreatSelect.length; i++)
		document.regdet.htreatSelect[i].checked=0;
  }
  if(id=="styling")
  {
     for(i=0; i<document.regdet.stylingSelect.length; i++)
		document.regdet.stylingSelect[i].checked=0;
  }
  newPrice('skip');
}

function selectProd(id,val, price)
{
    if (id == "shampoo")
    {
    	document.regdet.shampooSkip.checked=0;
		document.regdet.shampooprod.value=val ;
		document.regdet.selectshampooprice.value=price ;
	}
    if (id == "conditioner")
    {
        document.regdet.conditionerSkip.checked=0;
		document.regdet.conditionerprod.value=val ;
		document.regdet.selectconditionerprice.value=price ;
    }
    if (id == "htreat")
    {
        document.regdet.htreatSkip.checked=0;
     	document.regdet.htreatprod.value=val ;
		document.regdet.selecthtreatprice.value=price ;
	}
    if (id == "styling")
    {
       	document.regdet.stylingSkip.checked=0;
   		document.regdet.stylingprod.value=val ;
		document.regdet.selectstylingprice.value=price ;
	}
    newPrice(price);
}
function newPrice(itemPrice)
{
  var price = 0.0;

  var shampooqty = 0;
  var conditionerqty = 0;
  var htreatqty = 0;
  var stylingqty = 0;
  var finishqty = 0 ;

  var shampooPrice = parseFloat(document.regdet.shampooPrice.value);
  var conditionerPrice = parseFloat(document.regdet.conditionerPrice.value);
  var htreatPrice = parseFloat(document.regdet.htreatPrice.value);
  var stylingPrice = parseFloat(document.regdet.stylingPrice.value);
  var finishPrice = parseFloat(document.regdet.finishPrice.value);

  if (document.regdet.shampooqty != null && document.regdet.shampooqty.value != null)
    shampooqty = parseInt(document.regdet.shampooqty.value);
  if (document.regdet.conditionerqty != null && document.regdet.conditionerqty.value != null)
    conditionerqty = parseInt(document.regdet.conditionerqty.value);
  if (document.regdet.htreatqty != null && document.regdet.htreatqty.value !=null)
    htreatqty = parseInt(document.regdet.htreatqty.value);
  if (document.regdet.stylingqty != null && document.regdet.stylingqty.value != null)
    stylingqty = parseInt(document.regdet.stylingqty.value);
  if (document.regdet.finishqty != null && document.regdet.finishqty.value != null)
    finishqty = parseInt(document.regdet.finishqty.value);

  if (document.regdet.shampooCheck != null)
  if(document.regdet.shampooCheck.checked)
  {
    if(isNaN(shampooqty))
	  shampooqty = 0;
    price += shampooqty * shampooPrice;
  }

  if (document.regdet.conditionerCheck != null)
  if(document.regdet.conditionerCheck.checked)
  {
    if(isNaN(conditionerqty))
	  conditionerqty = 0;
    price += conditionerqty * conditionerPrice;
  }

  if (document.regdet.htreatCheck != null)
  if(document.regdet.htreatCheck.checked)
  {
    if(isNaN(htreatqty))
	  htreatqty = 0;
    price += htreatqty * htreatPrice
  }

  if (document.regdet.stylingCheck != null)
  if(document.regdet.stylingCheck.checked)
  {
    if(isNaN(stylingqty))
	  stylingqty = 0;
    price += stylingqty * stylingPrice
  }

  if (document.regdet.finishCheck != null)
  if(document.regdet.finishCheck.checked)
  {
    if(isNaN(finishqty))
	  finishqty = 0;
	price += finishqty * finishPrice;
  }

  if(!(itemPrice == 'skip'))
  {
    price += parseFloat(itemPrice);
  }
  document.getElementById("topPrice").innerHTML = "Total $"+price.toFixed(2);
  document.getElementById("totalBottom").innerHTML = "Total $"+price.toFixed(2);
}
function submit_this()
{
  if(regimenStatus()){
    document.regdet.target = "" ;
    document.regdet.action = "hairregimenmail.do" ;
    document.regdet.submit() ;
  }
}
function selectType(id)
{
  document.regdet.scrname.value = "skindetail" ;
  if (id == "shampoo")
    document.regdet.prodType.value = "1001" ;
  if (id == "conditioner")
    document.regdet.prodType.value = "1003" ;
  if (id == "htreat")
    document.regdet.prodType.value = "1005" ;
  if (id == "styling")
    document.regdet.prodType.value = "1004" ;
  document.regdet.prodTypeFlag.value = "yes" ;
  document.regdet.prodLineFlag.value = "no" ;
  document.regdet.target = "" ;
  document.regdet.action = "<%=response.encodeURL("hairregimendetail.do")%>" ;
  document.regdet.submit() ;
}

function checknumber(value){
  var num=/(^\d+$)|(^\d+\d+$)/
  if (num.test(value))
    return true;
  else
    return false;
}

function displayPrice(){

  var price = 0.0;

  var shampooqty = 0;
  var conditionerqty = 0;
  var htreatqty = 0;
  var stylingqty = 0;
  var finishqty = 0 ;
  
  var vshampooprice = 0 ;
  var vconditionerprice = 0 ;
  var vhtreatprice = 0 ;
  var vstylingprice = 0 ;
  
  var shampooPrice = parseFloat(document.regdet.shampooPrice.value);
  var conditionerPrice = parseFloat(document.regdet.conditionerPrice.value);
  var htreatPrice = parseFloat(document.regdet.htreatPrice.value);
  var stylingPrice = parseFloat(document.regdet.stylingPrice.value);
  var finishPrice = parseFloat(document.regdet.finishPrice.value);

  if (document.regdet.shampooqty != null && document.regdet.shampooqty.value != null)
    shampooqty = parseInt(document.regdet.shampooqty.value);
  if (document.regdet.conditionerqty != null && document.regdet.conditionerqty.value != null)
    conditionerqty = parseInt(document.regdet.conditionerqty.value);
  if (document.regdet.htreatqty != null && document.regdet.htreatqty.value !=null)
    htreatqty = parseInt(document.regdet.htreatqty.value);
  if (document.regdet.stylingqty != null && document.regdet.stylingqty.value != null)
    stylingqty = parseInt(document.regdet.stylingqty.value);
  if (document.regdet.finishqty != null && document.regdet.finishqty.value != null)
    finishqty = parseInt(document.regdet.finishqty.value);

  if (document.regdet.shampooCheck != null)
  if(document.regdet.shampooCheck.checked)
  {
    if(isNaN(shampooqty))
	  shampooqty = 0;
    price += shampooqty * shampooPrice;
  }

  if (document.regdet.conditionerCheck != null)
  if(document.regdet.conditionerCheck.checked)
  {
    if(isNaN(conditionerqty))
	  conditionerqty = 0;
    price += conditionerqty * conditionerPrice;
  }

  if (document.regdet.htreatCheck != null)
  if(document.regdet.htreatCheck.checked)
  {
    if(isNaN(htreatqty))
	  htreatqty = 0;
    price += htreatqty * htreatPrice
  }

  if (document.regdet.stylingCheck != null)
  if(document.regdet.stylingCheck.checked)
  {
    if(isNaN(stylingqty))
	  stylingqty = 0;
    price += stylingqty * stylingPrice
  }

  if (document.regdet.finishCheck != null)
  if(document.regdet.finishCheck.checked)
  {
    if(isNaN(finishqty))
	  finishqty = 0;
	price += finishqty * finishPrice;
  }

  if (document.regdet.selectshampooprice != null)
  {
     vshampooprice = parseInt(document.regdet.selectshampooprice.value);
     price += vshampooprice ;
  }
  if (document.regdet.selectconditionerprice != null)
  {
     vconditionerprice = parseInt(document.regdet.selectconditionerprice.value);
     price += vconditionerprice ;
  }
  if (document.regdet.selecthtreatprice != null)
  {
     vhtreatprice = parseInt(document.regdet.selecthtreatprice.value);
     price += vhtreatprice ;
  }
  if (document.regdet.selectstylingprice != null)
  {
     vstylingprice = parseInt(document.regdet.selectstylingprice.value);
     price += vstylingprice ;
  }


  //document.all("bottomPrice").innerHTML = "$"+price.toFixed(2);
  document.getElementById("topPrice").innerHTML = "Total $"+price.toFixed(2);
  document.getElementById("totalBottom").innerHTML = "Total $"+price.toFixed(2);
}

// Function for New Price and adding the content to the HTML
////////////////////////////////////////////////////////////


function changePrice()
{
  if (document.regdet.shampooqty != null && document.regdet.shampooqty.value != null)
  if(!document.regdet.shampooCheck.checked && document.regdet.shampooqty.value.length!=0)
  {
      document.regdet.shampooqty.value="";
  }
  if (document.regdet.conditionerqty != null && document.regdet.conditionerqty.value != null)
  if(!document.regdet.conditionerCheck.checked && document.regdet.conditionerqty.value.length!=0)
  {
      document.regdet.conditionerqty.value="";
  }
  if (document.regdet.htreatqty != null && document.regdet.htreatqty.value !=null)
  if(!document.regdet.htreatCheck.checked && document.regdet.htreatqty.value.length!=0)
  {
      document.regdet.htreatqty.value="";
  }
  if (document.regdet.stylingqty != null && document.regdet.stylingqty.value !=null)
  if(!document.regdet.stylingCheck.checked && document.regdet.stylingqty.value.length!=0)
  {
      document.regdet.stylingqty.value="";
  }
  if (document.regdet.finishqty != null && document.regdet.finishqty.value !=null)
  if(!document.regdet.finishCheck.checked && document.regdet.finishqty.value.length!=0)
  {
      document.regdet.finishqty.value="";
  }


  if (document.regdet.shampooqty != null && document.regdet.shampooqty.value != null)
  if(document.regdet.shampooqty.value.length==0)
  {
    if((document.regdet.shampooCheck.checked)){
//	    alert("Quantity must not be empty");
        document.regdet.shampooqty.value = "1" ;
//		document.regdet.shampooqty.focus();
//		return false;
	}
  }else{
    if(!checknumber(document.regdet.shampooqty.value)){
		document.regdet.shampooqty.value="";
        document.regdet.shampooqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.conditionerqty != null && document.regdet.conditionerqty.value != null)
  if(document.regdet.conditionerqty.value.length==0)
  {
    if((document.regdet.conditionerCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.conditionerqty.value = "1";
//		document.regdet.conditionerqty.focus();
//		return false;
	}
  }else{
    if(!checknumber(document.regdet.conditionerqty.value)){
		document.regdet.conditionerqty.value="";
        document.regdet.conditionerqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.htreatqty != null && document.regdet.htreatqty.value !=null)
  if(document.regdet.htreatqty.value.length==0)
  {
    if((document.regdet.htreatCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.htreatqty.value = "1";
//		document.regdet.htreatqty.focus();
//		return false;
 	}
  }else{
    if(!checknumber(document.regdet.htreatqty.value)){
		document.regdet.htreatqty.value="";
        document.regdet.htreatqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.stylingqty != null && document.regdet.stylingqty.value !=null)
  if(document.regdet.stylingqty.value.length==0)
  {
    if((document.regdet.stylingCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.stylingqty.value = "1";
//		document.regdet.stylingqty.focus();
//		return false;
	}
  }else{
   if(!checknumber(document.regdet.stylingqty.value)){
		document.regdet.stylingqty.value="";
        document.regdet.stylingqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }
  if (document.regdet.finishqty != null && document.regdet.finishqty.value !=null)
  if(document.regdet.finishqty.value.length==0)
  {
    if((document.regdet.finishCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.finishqty.value = "1";
//		document.regdet.finishqty.focus();
//		return false;
	}
  }else{
   if(!checknumber(document.regdet.finishqty.value)){
		document.regdet.finishqty.value="";
        document.regdet.finishqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  displayPrice();
  return true;

}
function checkPageHeight()
{
 var ht = document.body.scrollHeight ;
// alert(ht);
 parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
 //parent.location = "http://hqweb2k301s.dsstage.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
}
function submitNoBrand()
{
  document.regdet.target = "" ;
  document.regdet.action = "<%=response.encodeURL("hairproductregimen.do")%>" ;
  document.regdet.submit() ;

}
//-->
</script>
</head>
<%! String checkChecked(String val1, String val2)
{
   String retu = "" ;
       if (val1 != null && val2 != null)
	    if ( val1.equals(val2) )
	      retu = "checked" ;
	return retu ;
}
%>
<body id="RegimenDetail"  onLoad="javascript:checkPageHeight();displayPrice();" style="margin-left:0px;">
<!-- Wrapper Start -->
<div id="wrapper">
	<!-- Container Start -->
    <div id="container">
    	
        <!-- Header Outer Start -->
        <div id="header-outer">
        	
            <!-- Header Black Top Outer Start -->
            <div id="header-top-black-outer">
            	<!-- Header Top Black Left Button Outer Start -->
                <div id="header-top-black-left-button-outer">
                	<div class="header-top-left-black-left-buttons">
                    	<ul>
                        	<li class="drugstore"><a href="#"></a></li>
                            <li class="beauty"><a href="#"></a></li>
                        </ul>
                    </div>
                </div><!-- Header Top Black Left Button Outer End -->
                <!-- Header Top Black Right Button Outer Start -->
                <div id="header-top-black-right-button-outer">
                	<div class="header-top-black-right-buttons">
                    	<ul>
                    		<li>Welcome, <span>Holly</span></li>
                    		<li><a href="#">SIGN OUT</a></li>
                    		<li><a href="#">YOUR ACCOUNT</a></li>
                    		<li class="Chatting"><a href="#"><span>2</span></a></li>
                    		<li><a href="#">YOUR LIST</a></li>
                    		<li class="help-right-line"><a href="#">HELP</a></li>
                    		<li class="bag">
                            	<span class="sites">2 SITES</span>
                                <span class="bag">1 BAG</span>
                            </li>
                    		<li class="bag-icon"><span>10</span></li>
                    		<li class="price-amount">$8245.00</li>
                    		<li class="checkout-button"><a href="#">Checkout</a></li>
                    	</ul>
                    </div>
                </div><!-- Header Top Black Right Button Outer End -->
            </div><!-- Header Black Top Outer End -->
            
            <!-- Logo Banner Outer Start -->
            <div id="logo-banner-outer">
            	<!-- Logo Start -->
            	<div class="logo"><a href="../index.html"></a></div><!-- Logo End -->
                <!-- Logo Right Outer Start -->
                <div id="logo-right-outer">
                	<div class="learnmore">You've earned $35 in drugstore.com dollars (Valid 4/1 - 4/30) <span class="learnmore-orange">LEARN MORE</span></div>
                    
                    <!-- Freeshipping New In Outer Start -->
                    <div id="free-shipping-new-in-outer">
                    	<!-- Free Shipping Start -->
	                    <div id="free-shipping">
	                    	<h1>FREE SHIPPING</h1>
	                        <span>ON ANY ORDER OF $25 OR MORE <span class="arrow"></span></span>
	                    </div><!-- Free Shipping End -->
	                    
	                    <!-- New In Start -->
	                    <div id="new-in">
	                    	<div class="new-in-lap-icon"></div>
	                        <div class="new-in">
	                        	<h1>NEW IN</h1>
	                            <span>ERNO LASZLO HOLLYWOOD GIFT SET <span class="arrow"></span></span>
	                        </div>
	                    </div><!-- New In End -->

						<!-- Search Box Outer Start -->
						<div id="search-box-outer">
							<div class="search-input-box"><input type="text" /></div>
							<div class="search-iocn"><a href="#"></a></div>
						</div><!-- Search Box Outer End -->

                    </div><!-- Freeshipping New In Outer End -->
                    
                </div><!-- Logo Right Outer End -->
            </div><!-- Logo Banner Outer End -->
            
            <!-- Nav Outer Start -->
            <div id="nav">
            	<ul>
            		<li><a href="#" class="brands">BRANDS</a></li>
                    <li><a href="#" class="skin-care">SKIN CARE</a></li>
                    <li><a href="#" class="makeup">MAKEUP</a></li>
                    <li><a href=""# class="fragrance">FRAGRANCE</a></li>
                    <li><a href="#" class="bath-body">BATH & BODY</a></li>
                    <li><a href="#" class="hair-care">HAIR CARE</a></li>
                    <li><a href="#" class="accessories">ACCESSORIES</a></li>
                    <li><a href="#" class="advice">ADVICE & TRENDS</a></li>
            	</ul>
            </div><!-- Nav Outer End -->
            
        </div><!-- Header Outer End -->
        
        <!-- Breadcrumb Outer Start -->
        <div id="breadcrumb-outer">
        	<div class="breadcrumb">
            	<ul>
            		<li><a href="#">Home</a></li>
            		<li><a href="#">Hair Care</a></li>
            		<li><a href="#">Hair Consultation</a></li>
            	</ul>
            </div>
        </div><!-- Breadcrumb Outer End -->
        
        <!-- Banner Image Start -->
        <div id="banner-image-outer">
        	<img src="../images/banner.png" alt="" />
        </div><!-- Banner Image End -->
        
        <!-- Steps Images Outer Start -->
        <div id="steps-images-outer">
        	<div class="step-1-image step-3-1"><a href="index.html"></a></div>
            <div class="step-2-image step-3-2"><a href="hair-product.html"></a></div>
            <div class="step-3-image step-3-3"></div>
        </div><!-- Steps Images Outer End -->
         <!-- Body Content Outer Start -->
        <div id="body-running-content-outer">
        
    <form name="regdet" method="post" action="">
     <% if (messagesent != null && messagesent.equals("yes")) { %>
       <div id="emailSent"><strong>Thanks!</strong> &nbsp;We've sent your friend an email to tell them about this great Beauty.com feature.</div>
     <% } %>
     <% if (prodLineFlag != null && prodLineFlag.equals("yes")) { %>
    <table id="brand">
      <tr>
        <td><img src="<%=brandlogofile%>" size="100%" /></td>
        <td><a href="javascript:selectBrandDetail('<%=prodLine%>')">Tell me about this brand</a></td>
      </tr>
    </table>
     <% } %>
      <!-- Running Content Start -->
	        <!--<div id="running-content">	
	            Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
                </div>--><!-- Running Content End -->
                
    <table width="736">
      <tr>
        <td class="header"> <p class="header">Your solution for beautiful hair</p>
      <!--<p>Based on your hair type, hair concerns and preferences - here's your personalized
        hair care regimen.</p>--></td>
        <td id="total"><div id=topPrice style="font-weight:bold;font-size:12px"><p>Total : $<%=pprice%></div></p></td>
      </tr>
    </table>
    <div id="ingredientWarnings">
    <!--<p><strong>Please note:</strong><br /> -->
      <%=basicstring%><br />
    <%--  <br />
      <%=commontext%></p> --%>
      </div>
    
      <div id="buyRegimen">
       <p><a href="#" onclick="javascript:submitInfo();return false;"><img src="images/b_buy_this_regimen.gif" border="0" id="buyRegimenBttn"></a></p>
         <!--<p><a href="javascript:call_pdf('id')" class="arrow">VIEW REGIMEN DETAILS</a><br /> -->
        <a href="javascript:submit_this()" class="arrow">EMAIL TO A FRIEND</a></p>
      </div>

    <!-- shampoo 1 -->
    <% if (!noshampoo) { shampoorepeat = true ; %>
    <input type="hidden" name="shampooSkip" value="<%=shampooskip%>">
    <table class="results">
         <%     if (((ProductVO)shampoopr).getImage() != null)
                    prodmainimage = ((ProductVO)shampoopr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(shampoopr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
         %>
      <tr>
        <td class="stepNumber">Step 1: Shampoo</td>
        <td>&nbsp;</td>
        <td class="spacer">&nbsp;</td>
        <td class="purchaseIndividual">Purchase products<br />
          individually:</td>
      </tr>
      <tr>
        <td class="productImage"><a name="cphref" href="#cphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)shampoopr).getProdName()%>','<%=((ProductVO)shampoopr).getProdId()%>','<%=((ProductVO)shampoopr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>"  border="0"/></a></td>
        <td class="productText"><p><span class="productTitle"><%=((ProductVO)shampoopr).getProductLinePrime()%></span><br />
            <a class="productText" name="cphref" href="#cphref"  onclick="javascript:showMeHairProduct('<%=((ProductVO)shampoopr).getProdName()%>','<%=((ProductVO)shampoopr).getProdId()%>','<%=((ProductVO)shampoopr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><%=((ProductVO)shampoopr).getProdName()%></a><br />
            <% if (starrating1 !=null && starrating1.length() > 0) { %>
            <img src="images/<%=starrating1%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)shampoopr).getPrice()%></p>
            <%  if (((ProductVO)shampoopr).getSellCopy() != null) { %>
          <p><%=((ProductVO)shampoopr).getSellCopy()%></p>
            <% } %>
          <p class="whyRecommended"><a name="cwhref" href="#cwhref" onclick="javascript:showHairRecPopUp('<%=((ProductVO)shampoopr).getProductLinePrime()%>','<%=((ProductVO)shampoopr).getProdName()%>','<%=customerId%>','<%=((ProductVO)shampoopr).getProdId()%>','',event,true);return false;">Why was this recommended?</a></p></td>
        <td></td>
        <td class="purchaseCheckboxFirst"><input id="Text1" class="textStyle" style="width: 27px" type="text" name="shampooqty" value="1" size="3" onkeyup="changePrice()"/>
          <input id="Checkbox1" type="checkbox"  name="shampooCheck" checked onClick="changePrice()"/></td>
      </tr>
    </table>
    <% } %>
    <!-- shampoo 1 end -->
   <!-- shampoo 3 begins -->
     <div class="clear"></div>
     <% if (!shampoorepeat && prodTypeFlag != null && prodTypeFlag.equals("yes") && shampoo.size() > 0 ) { %>
      <input type="hidden" name="selectshampooprice" value="0">
    <table>
      <tr>
        <td style="width:165px;height:15px;">&nbsp;</td>
        <td style="width:185px;height:15px;">&nbsp;</td>
        <td style="width:185px;height:15px;">&nbsp;</td>
        <td style="width:185px;height:15px;vertical-align: top;font-weight:bold;border-bottom:solid 1px #666666;">Purchase products<br/>
          individually:</td>
      </tr>
     </table>
      <div class="skippedStep">
      <br />
      <p class="header2">Step 1: Shampoo</p>
    <p class="skipStep">
      <input id="Checkbox1" type="checkbox"  name="shampooSkip" value="shampooSkip" onClick="javascript:skipThis('shampoo')" <%=checkChecked("shampooSkip", shampooskip)%>/>
      Skip this step</p>
    <div class="border"></div>
    <p id="skippedNote">Complete your regimen by choosing from these top products:</p>
    <table class="productResults">
     <%  
                        shampoorepeat = true ;
//                        secondflag = true ;
			            int prodlength = 4 ;
			            if (shampoo.size() > 4) 
					       prodlength = 4 ;
				        else
					       prodlength = shampoo.size() ;
      %>
      <tr>
	                        <% for (int i=0 ;  i < prodlength ; i++) {
  							   prodmainimage = ((ProductVO)(shampoo.get(i))).getImage() ;
                               starrating = "" ;
                               starrating = ((ProductVO)(shampoo.get(i))).getShadeImage() ;
                               String crev = "" ;
                               if (starrating !=null && starrating.length() > 0)
                                  crev = "yes" ;
                            %>
      
        <td class="radioButton"><input id="Radio5" type="radio"  name="shampooSelect" value="<%=((ProductVO)(shampoo.get(i))).getProdId()%>" onClick="javascript:selectProd('shampoo','<%=((ProductVO)(shampoo.get(i))).getProdId()%>','<%=((ProductVO)(shampoo.get(i))).getPrice()%>')" <%=checkChecked(shampooprod, ((ProductVO)(shampoo.get(i))).getProdId())%>/></td>
        <td class="product"><p><a name="cphref" href="#cphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(shampoo.get(i))).getProdName()%>','<%=((ProductVO)(shampoo.get(i))).getProdId()%>','<%=master.get(((ProductVO)(shampoo.get(i))).getProductLine())%>',event,event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" alt="" border="0"/></a><br />
            <strong><%=master.get(((ProductVO)(shampoo.get(i))).getProductLine())%></strong><br />
            <a class="productText"  name="cphref" href="#cphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(shampoo.get(i))).getProdName()%>','<%=((ProductVO)(shampoo.get(i))).getProdId()%>','<%=master.get(((ProductVO)(shampoo.get(i))).getProductLine())%>',event,'<%=crev%>');return false;" class="header3"><%=((ProductVO)(shampoo.get(i))).getProdName()%></a><br />
            <% if (starrating !=null && starrating.length() > 0) { %>
            <img src="images/<%=starrating%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)(shampoo.get(i))).getPrice()%><br />
          </p></td>
        <% } %>  
      </tr>
      <tr>
        <% for (int i=0 ;  i < prodlength ; i++) {  %>
        <td >&nbsp;</td>
        <td><p> <a name="cwhref" href="#cwhref" onClick="javascript:showHairRecPopUp('<%=master.get(((ProductVO)(shampoo.get(i))).getProductLine())%>','<%=((ProductVO)(shampoo.get(i))).getProdName()%>','<%=customerId%>','<%=((ProductVO)(shampoo.get(i))).getProdId()%>','',event,true);return false;">Why was this<br />
            recommended?</a></p></td>
         <%}%>   
      </tr>
    </table>
   </div>
   <% } %>
   <!-- shampoo 3 ends -->
   <!-- shampoo 2 begin -->
      <% if (!shampoorepeat && noshampoo   ) { %>
     <input type="hidden" name="shampooSkip" value="<%=shampooskip%>">
     <table class="results">
         <%     
                 String shampoonoprodtext = "" ;

                 if (shampooskip.equals("shampooSkip")) 
                 {
                     shampoonoprodtext = "You 've chosen to skip this step.<br />" ;
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                 }
                 else
                 {
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                     shampoonoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                 }

         %>
      <tr>
        <td class="stepNumber">Step 1: Shampoo</td>
        <td>&nbsp;</td>
        <td class="spacer">&nbsp;</td>
        <td class="purchaseIndividual">Purchase products<br />
          individually:</td>
      </tr>
      <tr>
        <td class="productImage"><img src="<%=prodmainimage%>"  border="0"/></td>
        <td class="productText"><p><%=shampoonoprodtext%></p>
          <p><br /></p>
          <% if (!shampooskip.equals("shampooSkip"))  {%>
          <p class="whyRecommended"><a href="javascript:selectType('shampoo')">Show products from different brands</a></p></td>
          <% } %>
        <td></td>
        <td class="purchaseCheckboxFirst"><img src="images/shared/trans.gif" width="27" border="0"></td>
      </tr>
    </table>
      <% } %>
   <!-- shampoo 2 end -->
    <!-- conditioner 1 -->
           <% if (!noconditioner) { conditionerrepeat = true ;%>
    <input type="hidden" name="conditionerSkip" value="<%=conditionerskip%>">
    <table class="results">
         <%     if (((ProductVO)conditionerpr).getImage() != null)
                    prodmainimage = ((ProductVO)conditionerpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(conditionerpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
         %>
      <tr>
        <td class="stepNumber">Step 2: Conditioner</td>
        <td></td>
        <td class="spacer">&nbsp;</td>
        <td class="purchaseIndividual">&nbsp;<br />
        </td>
      </tr>
      <tr>
        <td class="productImage"><a name="mphref" href="#mphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)conditionerpr).getProdName()%>','<%=((ProductVO)conditionerpr).getProdId()%>','<%=((ProductVO)conditionerpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>"  border="0"/></a></td>
        <td class="productText"><p><span class="productTitle"><%=((ProductVO)conditionerpr).getProductLinePrime()%></span><br />
            <a class="productText"  name="mphref" href="#mphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)conditionerpr).getProdName()%>','<%=((ProductVO)conditionerpr).getProdId()%>','<%=((ProductVO)conditionerpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><%=((ProductVO)conditionerpr).getProdName()%></a><br />
            <% if (starrating1 !=null && starrating1.length() > 0) { %>
            <img src="images/<%=starrating1%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)conditionerpr).getPrice()%></p>
            <%  if (((ProductVO)conditionerpr).getSellCopy() != null) { %>
          <p><%=((ProductVO)conditionerpr).getSellCopy()%></p>
            <% } %>
          <p class="whyRecommended"><a name="mwhref" href="#mwhref" onclick="javascript:showHairRecPopUp('<%=((ProductVO)conditionerpr).getProductLinePrime()%>','<%=((ProductVO)conditionerpr).getProdName()%>','<%=customerId%>','<%=((ProductVO)conditionerpr).getProdId()%>','',event,true);return false;">Why was this recommended?</a></p></td>
        <td></td>
        <td class="purchaseCheckbox"><input id="Text1" class="textStyle" style="width: 27px" type="text" name="conditionerqty" value="1" size="3" onkeyup="changePrice()"/>
          <input id="Checkbox1" type="checkbox"  name="conditionerCheck" checked onClick="changePrice()"/></td>
      </tr>
    </table>
    <% } %>
    <!-- conditioner 1 end -->
   <!-- conditioner 3 begins -->
     <% if (!conditionerrepeat && prodTypeFlag != null && prodTypeFlag.equals("yes") && conditioner.size() > 0 ) { %>
      <input type="hidden" name="selectconditionerprice" value="0">
     <div class="skippedStep">
      <br />
    <p class="header2">Step 2: Conditioner</p>
    <p class="skipStep">
      <input id="Checkbox2" type="checkbox"  name="conditionerSkip" value="conditionerSkip" onClick="javascript:skipThis('conditioner')" <%=checkChecked("conditionerSkip", conditionerskip)%>/>
      Skip this step</p>
    <div class="border"></div>
    <p id="skippedNote">Complete your regimen by choosing from these top products:</p>
    <table class="productResults">
     <%  
                        conditionerrepeat = true ;
			            int prodlength = 4 ;
			            if (conditioner.size() > 4) 
					       prodlength = 4 ;
				        else
					       prodlength = conditioner.size() ;
      %>
      <tr>
	                        <% for (int i=0 ;  i < prodlength ; i++) {
  							   prodmainimage = ((ProductVO)(conditioner.get(i))).getImage() ;
                               starrating = "" ;
                               starrating = ((ProductVO)(conditioner.get(i))).getShadeImage() ;
                               String crev = "" ;
                               if (starrating !=null && starrating.length() > 0)
                                  crev = "yes" ;
                            %>
      
        <td class="radioButton"><input id="Radio5" type="radio"  name="conditionerSelect" value="<%=((ProductVO)(conditioner.get(i))).getProdId()%>" onClick="javascript:selectProd('conditioner','<%=((ProductVO)(conditioner.get(i))).getProdId()%>','<%=((ProductVO)(conditioner.get(i))).getPrice()%>')" <%=checkChecked(conditionerprod, ((ProductVO)(conditioner.get(i))).getProdId())%>/></td>
        <td class="product"><p><a name="mphref" href="#mphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(conditioner.get(i))).getProdName()%>','<%=((ProductVO)(conditioner.get(i))).getProdId()%>','<%=master.get(((ProductVO)(conditioner.get(i))).getProductLine())%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" alt="" border="0"/></a><br />
            <strong><%=master.get(((ProductVO)(conditioner.get(i))).getProductLine())%></strong><br />
            <a class="productText"  name="mphref" href="#mphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(conditioner.get(i))).getProdName()%>','<%=((ProductVO)(conditioner.get(i))).getProdId()%>','<%=master.get(((ProductVO)(conditioner.get(i))).getProductLine())%>',event,'<%=crev%>');return false;" class="header3"><%=((ProductVO)(conditioner.get(i))).getProdName()%></a><br />
            <% if (starrating !=null && starrating.length() > 0) { %>
            <img src="images/<%=starrating%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)(conditioner.get(i))).getPrice()%><br />
          </p></td>
        <% } %>  
      </tr>
      <tr>
        <% for (int i=0 ;  i < prodlength ; i++) {  %>
        <td >&nbsp;</td>
        <td><p> <a name="mwhref" href="#mwhref" onClick="javascript:showHairRecPopUp('<%=master.get(((ProductVO)(conditioner.get(i))).getProductLine())%>','<%=((ProductVO)(conditioner.get(i))).getProdName()%>','<%=customerId%>','<%=((ProductVO)(conditioner.get(i))).getProdId()%>','',event,true);return false;">Why was this<br />
            recommended?</a></p></td>
         <%}%>   
      </tr>
    </table>
   </div>
     <% } %>
   <!-- conditioner 3 ends -->
      <!-- conditioner 2 begin -->
        <% if (!conditionerrepeat && noconditioner   ) { %>
     <input type="hidden" name="conditionerSkip" value="<%=conditionerskip%>">
     <table class="results">
         <%     
                 String conditionernoprodtext = "" ;
                 if (conditionerskip.equals("conditionerSkip")) 
                 {
                     conditionernoprodtext = "You 've chosen to skip this step.<br />" ;
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                 }
                 else
                 {
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                     conditionernoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                 }

         %>
      <tr>
        <td class="stepNumber">Step 2: Conditioner</td>
        <td>&nbsp;</td>
        <td class="spacer">&nbsp;</td>
        <% if (secondflag) { %>
        <td class="purchaseIndividual">Purchase products<br />
          individually:</td>
        <% } else { %>
        <td class="spacer">&nbsp;</td>
        <% } %>
      </tr>
      <tr>
        <td class="productImage"><img src="<%=prodmainimage%>"  border="0"/></td>
        <td class="productText"><p><%=conditionernoprodtext%><br /></p>
          <p></p>
          <% if (!conditionerskip.equals("conditionerSkip"))  {%>
          <p class="whyRecommended"><a href="javascript:selectType('conditioner')">Show products from different brands</a></p></td>
          <% } %>
        <td></td>
        <td class="purchaseIndividual"><img src="images/shared/trans.gif" width="27" border="0"></td>
      </tr>
    </table>
      <% } %>
   <!-- conditioner 2 end -->
    <!-- htreat 1 -->
           <% if (!nohtreat) { htreatrepeat = true ;%>
    <input type="hidden" name="htreatSkip" value="<%=htreatskip%>">
    <table class="results">
         <%     if (((ProductVO)htreatpr).getImage() != null)
                   prodmainimage = ((ProductVO)htreatpr).getImage() ;  
                else 
                   prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                   starrating1 = "" ;
                   starrating1 = (String)(htreatpr).getShadeImage() ;
                   String crev = "" ;
                   if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
         %>
      <tr>
        <td class="stepNumber">Step 3: Treat</td>
        <td></td>
        <td class="spacer">&nbsp;</td>
        <td class="purchaseIndividual">&nbsp;</td>
      </tr>
      <tr>
        <td class="productImage"><a name="tphref" href="#tphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)htreatpr).getProdName()%>','<%=((ProductVO)htreatpr).getProdId()%>','<%=((ProductVO)htreatpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" border="0" /></a></td>
        <td class="productText"><p><span class="productTitle"><%=((ProductVO)htreatpr).getProductLinePrime()%></span><br />
            <a class="productText"  name="tphref" href="#tphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)htreatpr).getProdName()%>','<%=((ProductVO)htreatpr).getProdId()%>','<%=((ProductVO)htreatpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"> <%=((ProductVO)htreatpr).getProdName()%></a><br />
            <% if (starrating1 !=null && starrating1.length() > 0) { %>
            <img src="images/<%=starrating1%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)htreatpr).getPrice()%></p>
            <%  if (((ProductVO)htreatpr).getSellCopy() != null) { %>
          <p> <%=((ProductVO)htreatpr).getSellCopy()%></p>
            <% } %>
          <p class="whyRecommended"><a name="twhref" href="#twhref" onclick="javascript:showHairRecPopUp('<%=((ProductVO)htreatpr).getProductLinePrime()%>','<%=((ProductVO)htreatpr).getProdName()%>','<%=customerId%>','<%=((ProductVO)htreatpr).getProdId()%>','',event,true);return false;">Why was this recommended?</a></p></td>
        <td></td>
        <td class="purchaseCheckbox"><input id="Text2" class="textStyle" style="width: 27px" type="text" name="htreatqty" value="1" size="3" onkeyup="changePrice()"/>
          <input id="Checkbox2" type="checkbox"  name="htreatCheck" checked onClick="changePrice()"/></td>
      </tr>
    </table>
    <% } %>
    <!-- htreat 1 end -->
   <!-- htreat 3 begins -->
     <% if (!htreatrepeat && prodTypeFlag != null && prodTypeFlag.equals("yes") && htreat.size() > 0 ) { %>
      <input type="hidden" name="selecthtreatprice" value="0">
      <div class="skippedStep">
      <br />
    <p class="header2">Step 3: Treat</p>
    <p class="skipStep">
      <input id="Checkbox3" type="checkbox"  name="htreatSkip" value="htreatSkip" onClick="javascript:skipThis('htreat')" <%=checkChecked("htreatSkip", htreatskip)%>/>
      Skip this step</p>
    <div class="border"></div>
    <p id="skippedNote">Complete your regimen by choosing from these top products:</p>
    <table class="productResults">
     <%  
                        htreatrepeat = true ;
			            int prodlength = 4 ;
			            if (htreat.size() > 4) 
					       prodlength = 4 ;
				        else
					       prodlength = htreat.size() ;
      %>
      <tr>
	                        <% for (int i=0 ;  i < prodlength ; i++) {
  							   prodmainimage = ((ProductVO)(htreat.get(i))).getImage() ;
                               starrating = "" ;
                               starrating = ((ProductVO)(htreat.get(i))).getShadeImage() ;
                               String crev = "" ;
                               if (starrating !=null && starrating.length() > 0)
                                  crev = "yes" ;
                            %>
      
        <td class="radioButton"><input id="Radio5" type="radio"  name="htreatSelect" value="<%=((ProductVO)(htreat.get(i))).getProdId()%>" onClick="javascript:selectProd('htreat','<%=((ProductVO)(htreat.get(i))).getProdId()%>','<%=((ProductVO)(htreat.get(i))).getPrice()%>')" <%=checkChecked(htreatprod, ((ProductVO)(htreat.get(i))).getProdId())%>/></td>
        <td class="product"><p><a name="tphref" href="#tphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(htreat.get(i))).getProdName()%>','<%=((ProductVO)(htreat.get(i))).getProdId()%>','<%=master.get(((ProductVO)(htreat.get(i))).getProductLine())%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" alt="" border="0"/></a><br />
            <strong><%=master.get(((ProductVO)(htreat.get(i))).getProductLine())%></strong><br />
            <a class="productText"  name="tphref" href="#tphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(htreat.get(i))).getProdName()%>','<%=((ProductVO)(htreat.get(i))).getProdId()%>','<%=master.get(((ProductVO)(htreat.get(i))).getProductLine())%>',event,'<%=crev%>');return false;" class="header3"><%=((ProductVO)(htreat.get(i))).getProdName()%></a><br />
            <% if (starrating !=null && starrating.length() > 0) { %>
            <img src="images/<%=starrating%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)(htreat.get(i))).getPrice()%><br />
          </p></td>
        <% } %>  
      </tr>
      <tr>
        <% for (int i=0 ;  i < prodlength ; i++) {  %>
        <td >&nbsp;</td>
        <td><p> <a name="twhref" href="#twhref" onClick="javascript:showHairRecPopUp('<%=master.get(((ProductVO)(htreat.get(i))).getProductLine())%>','<%=((ProductVO)(htreat.get(i))).getProdName()%>','<%=customerId%>','<%=((ProductVO)(htreat.get(i))).getProdId()%>','',event,true);return false;">Why was this<br />
            recommended?</a></p></td>
         <%}%>   
      </tr>
    </table>
   </div>
     <% } %>
   <!-- htreat 3 ends -->
   <!-- htreat 2 begin -->
        <% if (!htreatrepeat && nohtreat   ) { %>
     <input type="hidden" name="htreatSkip" value="<%=htreatskip%>">
     <table class="results">
         <%     
                 String htreatnoprodtext = "" ;
                 if (htreatskip.equals("htreatSkip")) 
                 {
                     htreatnoprodtext = "You 've chosen to skip this step.<br />" ;
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                 }
                 else
                 {
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                     htreatnoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                 }

         %>
      <tr>
        <td class="stepNumber">Step 3: Treat</td>
        <td>&nbsp;</td>
        <td class="spacer">&nbsp;</td>
        <td class="spacer">&nbsp;</td>
      </tr>
      <tr>
        <td class="productImage"><img src="<%=prodmainimage%>"  border="0"/></td>
        <td class="productText"><p><%=htreatnoprodtext%><br /></p>
          <p><br /></p>
          <% if (!htreatskip.equals("htreatSkip"))  {%>
          <p class="whyRecommended"><a href="javascript:selectType('htreat')">Show products from different brands</a></p></td>
          <% } %>
        <td></td>
        <td class="purchaseIndividual"><img src="images/shared/trans.gif" width="27" border="0"></td>
      </tr>
    </table>
      <% } %>
   <!-- htreat 2 end -->
    <!-- Styling 1 -->
    <% if (!nostyling) { stylingrepeat = true ;%>
    <input type="hidden" name="stylingSkip" value="<%=stylingskip%>">
    <table class="results">
         <%     if (((ProductVO)stylingpr).getImage() != null)
                 prodmainimage = ((ProductVO)stylingpr).getImage() ;  
                else 
                 prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                 starrating1 = "" ;
                 starrating1 = (String)(stylingpr).getShadeImage() ;
                 String crev = "" ;
                 if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
         %>
      <tr>
        <td class="stepNumber">Step 4: Styling</td>
        <td></td>
        <td class="spacer">&nbsp;</td>
        <td class="purchaseIndividual">&nbsp;</td>
      </tr>
      <tr>
        <td class="productImage"><a name="ephref" href="#ephref" onclick="javascript:showMeHairProduct('<%=((ProductVO)stylingpr).getProdName()%>','<%=((ProductVO)stylingpr).getProdId()%>','<%=((ProductVO)stylingpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" border="0"/></a></td>
        <td class="productText"><p><span class="productTitle"><%=((ProductVO)stylingpr).getProductLinePrime()%></span><br />
            <a class="productText"  name="ephref" href="#ephref" onclick="javascript:showMeHairProduct('<%=((ProductVO)stylingpr).getProdName()%>','<%=((ProductVO)stylingpr).getProdId()%>','<%=((ProductVO)stylingpr).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><%=((ProductVO)stylingpr).getProdName()%></a><br />
            <% if (starrating1 !=null && starrating1.length() > 0) { %>
            <img src="images/<%=starrating1%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)stylingpr).getPrice()%></p>
          <%  if (((ProductVO)stylingpr).getSellCopy() != null) { %>
          <p><%=((ProductVO)stylingpr).getSellCopy()%></p>
          <% } %>
          <p class="whyRecommended"><a name="ewhref" href="#ewhref" onclick="javascript:showHairRecPopUp('<%=((ProductVO)stylingpr).getProductLinePrime()%>','<%=((ProductVO)stylingpr).getProdName()%>','<%=customerId%>','<%=((ProductVO)stylingpr).getProdId()%>','',event,true);return false;">Why was this recommended?</a></p></td>
        <td></td>
        <td class="purchaseCheckbox"><input id="Text3" class="textStyle" style="width: 27px" type="text" name="stylingqty" value="1" size="3" onkeyup="changePrice()"/>
          <input id="Checkbox3" type="checkbox"  name="stylingCheck" checked onClick="changePrice()"/></td>
      </tr>
    </table>
    <% } %>
    <!-- Styling 1 end -->
   <!-- styling 3 begins -->
     <% if (!stylingrepeat && prodTypeFlag != null && prodTypeFlag.equals("yes") && styling.size() > 0 ) { %>
      <input type="hidden" name="selectstylingprice" value="0">
      <div class="skippedStep">
      <br />
    <p class="header2">Step 4: Styling</p>
    <p class="skipStep">
      <input id="Checkbox3" type="checkbox"  name="stylingSkip" value="stylingSkip" onClick="javascript:skipThis('styling')" <%=checkChecked("stylingSkip", stylingskip)%>/>
      Skip this step</p>
    <div class="border"></div>
    <p id="skippedNote">Complete your regimen by choosing from these top products:</p>
    <table class="productResults">
     <%  
                        stylingrepeat = true ;
			            int prodlength = 4 ;
			            if (styling.size() > 4) 
					       prodlength = 4 ;
				        else
					       prodlength = styling.size() ;
      %>
      <tr>
	                        <% for (int i=0 ;  i < prodlength ; i++) {
  							   prodmainimage = ((ProductVO)(styling.get(i))).getImage() ;
                               starrating = "" ;
                               starrating = ((ProductVO)(styling.get(i))).getShadeImage() ;
                               String crev = "" ;
                               if (starrating !=null && starrating.length() > 0)
                                  crev = "yes" ;
                            %>
      
        <td class="radioButton"><input id="Radio5" type="radio"  name="stylingSelect" value="<%=((ProductVO)(styling.get(i))).getProdId()%>" onClick="javascript:selectProd('styling','<%=((ProductVO)(styling.get(i))).getProdId()%>','<%=((ProductVO)(styling.get(i))).getPrice()%>')" <%=checkChecked(stylingprod, ((ProductVO)(styling.get(i))).getProdId())%>/></td>
        <td class="product"><p><a name="ephref" href="#ephref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(styling.get(i))).getProdName()%>','<%=((ProductVO)(styling.get(i))).getProdId()%>','<%=master.get(((ProductVO)(styling.get(i))).getProductLine())%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" alt="" border="0"/></a><br />
            <strong><%=master.get(((ProductVO)(styling.get(i))).getProductLine())%></strong><br />
            <a class="productText"  name="ephref" href="#ephref" onclick="javascript:showMeHairProduct('<%=((ProductVO)(styling.get(i))).getProdName()%>','<%=((ProductVO)(styling.get(i))).getProdId()%>','<%=master.get(((ProductVO)(styling.get(i))).getProductLine())%>',event,'<%=crev%>');return false;" class="header3"><%=((ProductVO)(styling.get(i))).getProdName()%></a><br />
            <% if (starrating !=null && starrating.length() > 0) { %>
            <img src="images/<%=starrating%>" width="63" height="12" border="0"></img><br />
            <% } %>
            $<%=((ProductVO)(styling.get(i))).getPrice()%><br />
          </p></td>
        <% } %>  
      </tr>
      <tr>
        <% for (int i=0 ;  i < prodlength ; i++) {  %>
        <td >&nbsp;</td>
        <td><p> <a name="ewhref" href="#ewhref" onClick="javascript:showHairRecPopUp('<%=master.get(((ProductVO)(styling.get(i))).getProductLine())%>','<%=((ProductVO)(styling.get(i))).getProdName()%>','<%=customerId%>','<%=((ProductVO)(styling.get(i))).getProdId()%>','',event,true);return false;">Why was this<br />
            recommended?</a></p></td>
         <%}%>   
      </tr>
    </table>
   </div>
     <% } %>
   <!-- styling 3 ends -->
   <!-- styling 2 begin -->
        <% if (!stylingrepeat && nostyling   ) { %>
     <input type="hidden" name="stylingSkip" value="<%=stylingskip%>">
     <table class="results">
         <%     
                 String stylingnoprodtext = "" ;
                 if (stylingskip.equals("stylingSkip")) 
                 {
                     stylingnoprodtext = "You 've chosen to skip this step.<br />" ;
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                 }
                 else
                 {
                     prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                     stylingnoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                 }

         %>
      <tr>
        <td class="stepNumber">Step 4: Styling</td>
        <td>&nbsp;</td>
        <td class="spacer">&nbsp;</td>
        <td class="spacer">&nbsp;</td>
      </tr>
      <tr>
        <td class="productImage"><img src="<%=prodmainimage%>"  border="0"/></td>
        <td class="productText"><p><%=stylingnoprodtext%><br /></p>
          <p><br /></p>
          <% if (!stylingskip.equals("stylingSkip"))  {%>
          <p class="whyRecommended"><a href="javascript:selectType('styling')">Show products from different brands</a></p></td>
          <% } %>
        <td></td>
        <td class="purchaseIndividual"><img src="images/shared/trans.gif" width="27" border="0"></td>
      </tr>
    </table>
      <% } %>
   <!-- styling 2 end -->
    <!-- finish begins -->
          <% if (!nofinish) { %>
           <table class="results sun">
            <tr>
             <td class="content"><p class="header">The Finishing Touch </p>
              <% prodmainimage = ((ProductVO)finishpr).getImage() ;  %>
              <%
                  starrating1 = "" ;
                  starrating1 = (String)((ProductVO)finishpr).getShadeImage() ;
                  String crev = "" ;
                  if (starrating1 !=null && starrating1.length() > 0)
                         crev = "yes" ;
              %>
               <p>Adding a finishing product to your regimen is an excellent way to perfect your look and keep everything in place. To complete your style, we suggest:</p>
                 <a name="sphref" href="#sphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)finishpr).getProdName()%>','<%=((ProductVO)finishpr).getProdId()%>','<%=((ProductVO)(finishpr)).getProductLinePrime().replaceAll("'","\'")%>',event,'<%=crev%>');return false;"><img src="<%=prodmainimage%>" border="0" class="sun" /></a>
                 <p class="productText"><span class="productTitle"><%=((ProductVO)finishpr).getProductLinePrime()%></span><br />
                  <a class="productText" name="sphref" href="#sphref" onclick="javascript:showMeHairProduct('<%=((ProductVO)finishpr).getProdName()%>','<%=((ProductVO)finishpr).getProdId()%>','<%=((ProductVO)finishpr).getProductLinePrime()%>',event,'<%=crev%>');return false;"><%=((ProductVO)finishpr).getProdName()%></a><br />
                   <% if (starrating1 !=null && starrating1.length() > 0) { %>
                     <img src="images/<%=starrating1%>" width="63" height="12" border="0"></img><br />
                   <% } %>
                   $<%=((ProductVO)finishpr).getPrice()%> </p>
                    <%  if (((ProductVO)finishpr).getSellCopy() != null) { %>
                     <p class="productText"><%=((ProductVO)finishpr).getSellCopy()%></p>
                    <% } %>
                    <p class="whyRecommended"> <a name="ewhref" href="#ewhref" onclick="javascript:showHairRecPopUp('<%=((ProductVO)(finishpr)).getProductLinePrime()%>','<%=((ProductVO)finishpr).getProdName()%>','<%=customerId%>','<%=((ProductVO)finishpr).getProdId()%>','',event,true);return false;">Why was this recommended?</a></p></td>
               <td class="purchaseCheckbox"><input id="Text4" class="textStyle" style="width: 27px" type="text"  name="finishqty" value="1" size="3" onkeyup="changePrice()"/>
                     <input id="Checkbox4" type="checkbox"  name="finishCheck" onClick="changePrice()"/></td>
            </tr>
          </table>
      <%  }  %>
      <div id="totalBottom">
          <p>Total : $<%=pprice%></p>
      </div>
    <div id="submit"><a href="javascript:submitNoBrand()"><img src="images/b_change_your_selections.gif" border="0" id="editResponse"/></a><a href="javascript:submitInfo()"><img src="images/b_buy_selected_products.gif" border="0"/></a></div>
    <div id="legal">
    <p>Information on this site is not meant to be a substitute for the advice of a hair care or medical professional.  Always read the complete manufacturer’s instructions for each product.  If at any time excessive or persistent irritation, hair loss or other side effect occurs, discontinue use and seek medical attention.  Consult with your physician before using these products if you are pregnant, nursing, or have allergies or other sensitivities.</p></div>

<input type=hidden name="customerId" value="<%=customerId%>">
<input type=hidden name="cusid" value="<%=customerId%>">
<input type=hidden name="mid" value="<%=mid%>">
<input type=hidden name="catid" value="<%=catid%>">
<input type=hidden name="mode" value="<%=mode%>">
<input type=hidden name=prodLine value="<%=prodLine%>">
<input type=hidden name=prodType value="<%=prodType%>">
<input type=hidden name=prodLineFlag value="<%=prodLineFlag%>">
<input type=hidden name=prodTypeFlag value="<%=prodTypeFlag%>">
<input type=hidden name=prodId value="<%=prodId%>">
<input type=hidden name=prodid value="<%=prodId%>">
<input type="hidden" name="shampooprod" value="<%=shampooprod%>">
<input type="hidden" name="conditionerprod" value="<%=conditionerprod%>">
<input type="hidden" name="htreatprod" value="<%=htreatprod%>">
<input type="hidden" name="stylingprod" value="<%=stylingprod%>">
<input type="hidden" name="finishprod" value="<%=finishprod%>">
<input type="hidden" name="finishexists" value="<%=finishexists%>">
<input type="hidden" name="shampooPrice" value="<%=shampooPrice%>">
<input type="hidden" name="conditionerPrice" value="<%=conditionerPrice%>">
<input type="hidden" name="htreatPrice" value="<%=htreatPrice%>">
<input type="hidden" name="stylingPrice" value="<%=stylingPrice%>">
<input type="hidden" name="finishPrice" value="<%=finishPrice%>">
<input type="hidden" name="finishCheck1" value="">
<input type=hidden name=scrname value="">
</form>
<!-- DHTML window code -->
</div>
<div id="whyRec" style="position: absolute; visibility: hidden;width:650px; left:12px; top:12px;bottom=12px;right:12px;">
  <table style="border-collapse: collapse;" bgcolor="#ffffff" border="1" bordercolor="#111111" cellpadding="10" cellspacing="0">
    <tbody><tr>
      <td width="100%"><table style="border-collapse: collapse;" id="AutoNumber1" border="0" bordercolor="#111111" cellpadding="0" cellspacing="0" width="640">
          <tbody><tr>
            <td class="close" colspan="5" style="font-family: Arial; font-size: 10px;" align="right" width="100%">Close <a href="#" onclick="hideRecPopUp('1');return false"><img src="images/b_close_button.gif" align="absbottom" border="0" height="13" hspace="0" width="13"></a></td>
          </tr>
          <tr>
            <td class="header" colspan="5" width="100%" style="text-align: left; font-family: Arial; font-size: 14px; font-weight:bold; color: rgb(51, 51, 51);">Why was this recommended?</td>
          </tr>
          <tr>
            <td colspan="5" height="14" width="100%"></td>
          </tr>
          <tr>
            <td class="header2" colspan="5" width="100%"><div id="brand2Div" style="text-align: left; font-family: Arial; font-size: 11px; font-weight:bold; color: rgb(51, 51, 51);">philosophy</div></td>
          </tr>
          <tr>
            <td class="textStyle" colspan="5" width="100%"><div id="prName2Div" style="text-align: left; font-family: Arial; font-size: 11px; color: rgb(51, 51, 51);">philosophy purity made simple, one-step facial shampoor, 16 fl oz</div></td>
          </tr>
          <tr>
            <td class="textStyle" colspan="5" width="100%"><div id="whyDiv" style="width: 100%;"><iframe style="margin: 0px; padding: 0px; width: 650px; height: 300px;" src="" frameborder="no"></iframe></div></td>
          </tr>
        </tbody></table></td>
    </tr>
  </tbody></table>
</div>
<div id="brDetailLayer" style="position: absolute; visibility: hidden; position: absolute; z-index: 13; top: 54px; left: 106px; width: 118px; height: 15px"">
     <table border="1" cellpadding="12" cellspacing="0" style="border-collapse: collapse" bordercolor="#666666" width="450" bgcolor="#FFFFFF" >
            <tr>
                    <td valign="top" style="width: 100%">
                    <table border="0" width="100%" height="100%" cellspacing="0" cellpadding="0" >
                         <tr>
                          <td colspan="3" style="height: 11px">
                          </td>
                         </tr>
                         <tr>
                             <td class="header2" colspan="2" style="text-align: left; font-family: Arial; font-size: 11px; font-weight:bold; color: rgb(51, 51, 51);"><%=master.get(prodLine)%></td>
                             <td style="font-family: Arial; font-size: 10px; text-align:right" width: 100%; height: 25px;>Close
                                <a class="closeLink" href="javascript:hideMeBrandDetail()"><img src="images/b_close_button.gif" width="13" height="13" border="0" align="absbottom"/></a>
                             </td>
                         </tr>
                         <tr>
                          <td colspan="3" style="height: 14px">
                          </td>
                         </tr>
                         <tr>
                          <td  colspan="3" class="textStyle" style="height: 11px"><div id="brDetDiv" style="text-align: left; font-family: Arial; font-size: 11px; color: #333333;">Brand Information</div></td>
                         </tr>
                         <tr>
                          <td colspan="3" style="height: 56px">
                          </td>
                    </table>
                </td>
            </tr>
        </table>
    </div>
<div id="prLayer" style="position: absolute; visibility: hidden;width:650px; left:12px; top:12px;bottom=12px;right:12px;">
<input id="productId" value="83421" type="hidden">
<table style="border-collapse: collapse;" bgcolor="#ffffff" border="1" bordercolor="#666666" cellpadding="12" cellspacing="0">
<tbody><tr>
<td width="100%"><table style="border-collapse: collapse;" id="AutoNumber1" border="0" bordercolor="#111111" cellpadding="0" cellspacing="0" width="540">
    <tbody><tr>
      <td class="close" colspan="5" style="font-family: Arial; font-size: 10px;" align="right" width="100%">Close <a href="#" onclick="hideMe();return false"><img src="images/b_close_button.gif" align="absbottom" border="0" height="13" hspace="0" width="13"></a></td>
    </tr>
    <tr>
      <td class="header2" colspan="5" width="100%"><div id="brandDiv" style="text-align: left; font-family: Arial; font-size: 11px; font-weight:bold; color: rgb(51, 51, 51);">philosophy</div></td>
    </tr>
    <tr>
      <td class="textStyle" colspan="5" width="100%"><div id="prNameDiv" style="text-align: left; font-family: Arial; font-size: 11px; color: rgb(51, 51, 51);">philosophy purity made simple, one-step facial cleanser, 16 fl oz</div></td>
    </tr>
    <tr>
      <td colspan="5" height="14" width="100%"></td>
    </tr>
    <tr>
      <td><table width="400" cellspacing="0" cellpadding="0">
          <tbody><tr>
            <td width="130"><table id="tdPromos" class="hiddenTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgPromos" src="images/arrow_right.gif" alt="Promos" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsBtn" name="btnPromos" id="btnPromos" value="Package Details" onclick="hairmove('&popup=y&desc=y&direc=n&ingred=n&glos=n', 'Promos','yes');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table></td>
            <td><img src="images/shared/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
            <td width="125"><table id="tdingredients" class="selectedTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgingredients" src="images/white-arrow-gray.gif" alt="ingredients" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsSelectedBtn" name="btningredients" id="btningredients" value="ingredients" onclick="hairmove('&popup=y&desc=n&direc=n&ingred=y&glos=n', 'ingredients','yes');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table></td>
            <td><img src="images/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
            <td width="75"><table id="tdReviews" class="selectedTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgReviews" src="images/white-arrow-gray.gif" alt="Reviews" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="75">
                      <tbody><tr>
                        <td><input class="TblPDetailsSelectedBtn" name="btnReviews" id="btnReviews" value="Reviews" onclick="hairmove('&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y', 'Reviews','yes');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table>
            </td>
            <td><img src="images/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
            <td width="125"><table id="tdDirections" class="hiddenTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgDirections" src="images/arrow_right.gif" alt="Directions" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsBtn" name="btnDirections" id="btnDirections" value="Directions" onclick="hairmove('&popup=y&desc=n&direc=y&ingred=n&glos=n', 'Directions','yes');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table></td>
            <td><img src="images/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
          </tr>
        </tbody></table></td>
    </tr>
    <tr>
      <td><div id="pageDiv" style="width: 100%;"><iframe style="margin: 0px; padding: 0px; width: 600px; height: 300px;" src="images/productDetails.htm" frameborder="1"></iframe></div></td>
    </tr>
  </tbody></table>
</td></tr></tbody></table>
</div>
<!-- pr_1_layer -->
<div id="pr_1_layer" style="position: absolute; visibility: hidden;width:650px; left:12px; top:12px;bottom=12px;right:12px;">
<input id="product1Id" value="83421" type="hidden">
<table style="border-collapse: collapse;" bgcolor="#ffffff" border="1" bordercolor="#666666" cellpadding="12" cellspacing="0">
<tbody><tr>
<td width="100%"><table style="border-collapse: collapse;" id="AutoNumber1" border="0" bordercolor="#111111" cellpadding="0" cellspacing="0" width="540">
    <tbody><tr>
      <td class="close" colspan="5" style="font-family: Arial; font-size: 10px;" align="right" width="100%">Close <a href="#" onclick="hideMe();return false"><img src="images/b_close_button.gif" align="absbottom" border="0" height="13" hspace="0" width="13"></a></td>
    </tr>
    <tr>
      <td class="header2" colspan="5" width="100%"><div id="brand1Div" style="text-align: left; font-family: Arial; font-size: 11px; font-weight:bold; color: rgb(51, 51, 51);">philosophy</div></td>
    </tr>
    <tr>
      <td class="textStyle" colspan="5" width="100%"><div id="prName1Div" style="text-align: left; font-family: Arial; font-size: 11px; color: rgb(51, 51, 51);">philosophy purity made simple, one-step facial cleanser, 16 fl oz</div></td>
    </tr>
    <tr>
      <td colspan="5" height="14" width="100%"></td>
    </tr>
    <tr>
      <td><table width="450" cellspacing="0" cellpadding="0">
          <tbody><tr>
            <td width="130"><table id="tdPromos1" class="hiddenTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgPromos1" src="images/arrow_right.gif" alt="Promos" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsBtn" name="btnPromos1" id="btnPromos1" value="Package Details" onclick="hairmove('&popup=y&desc=y&direc=n&ingred=n&glos=n', 'Promos1','no');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table></td>
            <td><img src="images/shared/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
            <td width="125"><table id="tdingredients1" class="selectedTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgingredients1" src="images/white-arrow-gray.gif" alt="ingredients" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsSelectedBtn" name="btningredients1" id="btningredients1" value="ingredients" onclick="hairmove('&popup=y&desc=n&direc=n&ingred=y&glos=n', 'ingredients1','no');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table>
            </td>
            <td><img src="images/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
            <td width="125"><table id="tdDirections1" class="hiddenTab" border="0" cellpadding="0" cellspacing="0">
                <tbody><tr>
                  <td class="imgTab"><img id="imgDirections1" src="images/arrow_right.gif" alt="Directions" valign="middle" border="0" height="5" hspace="0" vspace="2" width="3"></td>
                  <td><table width="125">
                      <tbody><tr>
                        <td><input class="TblPDetailsBtn" name="btnDirections1" id="btnDirections1" value="Directions" onclick="hairmove('&popup=y&desc=n&direc=y&ingred=n&glos=n', 'Directions1','no');" type="button"></td>
                      </tr>
                    </tbody></table></td>
                </tr>
              </tbody></table></td>
            <td><img src="images/spacer.gif" alt="" border="0" height="0" hspace="0" vspace="0" width="5"></td>
          </tr>
        </tbody></table></td>
    </tr>
    <tr>
      <td><div id="page1Div" style="width: 100%;"><iframe style="margin: 0px; padding: 0px; width: 600px; height: 300px;" src="images/productDetails.htm" frameborder="1"></iframe></div></td>
    </tr>
  </tbody></table>
</td></tr></tbody></table>
</div>
<!-- pr_1_layer end -->
</body>
</html>
