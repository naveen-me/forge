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
    String scrname2 = "" ;
    String bnamestring = "" ;    
   
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
			    scrname2 = (String) request.getAttribute("scrname2") ;
			    bnamestring = (String) request.getAttribute("bnamestring") ;
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
                System.out.println("shampoo prod 111111 "+shampooprod+ "dddddddddddddd "+shampooprod.length()) ;
                if (shampooprod != null && !shampooprod.equals("null") && shampooprod.length() >0)
                {
                	System.out.println("shampoo prod 33333333 "+shampooprod) ;
                    shampoopr = (ProductVO) prodhash.get(shampooprod.trim()) ;
                    shampooPrice = (String) shampoopr.getPrice() ;
                    tprice = tprice+Float.parseFloat(shampooPrice.substring(0,shampooPrice.length())) ;
                }
                else
                {
                	System.out.println("shampoo prod 22222222 "+shampooprod) ;
                    noshampoo = true ;
                }
                //System.out.println("vvvvvvvvvdsds3 "+noshampoo+"  "+tprice) ;				

                if (conditionerprod !=null && !conditionerprod.equals("null")&& conditionerprod.length() >0)
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

                if (htreatprod !=null && !htreatprod.equals("null")&& htreatprod.length() >0)
                {
                    htreatpr = (ProductVO) prodhash.get(htreatprod.trim()) ;
                    htreatPrice = (String) htreatpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(htreatPrice.substring(0,htreatPrice.length())) ;
                }
                else
                    nohtreat = true ;
                //System.out.println("vvvvvvvvvdsds7 "+nohtreat) ;				
				 
                if (stylingprod !=null && !stylingprod.equals("null")&& stylingprod.length() >0)
                {
                    stylingpr = (ProductVO) prodhash.get(stylingprod.trim()) ;
                    stylingPrice = (String) stylingpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(stylingPrice.substring(0,stylingPrice.length())) ;
                }
                else
                    nostyling = true ;
                System.out.println("finish prod...... "+finishprod.length()) ;
                if (finishprod !=null && !finishprod.equals("null")&& finishprod.length() >0)
                {
                    finishpr = (ProductVO) prodhash.get(finishprod.trim()) ;
                    finishPrice = (String) finishpr.getPrice() ;
                    System.out.println(" 222222 finish prod...... "+finishpr) ;
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
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/header.css" />
<link rel="stylesheet" href="css/hair-product.css" />
<link rel="stylesheet" href="css/pop-up.css" />
<link rel="stylesheet" href="css/footer.css" />
<link rel="stylesheet" href="css/uniform.default.css" />

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Hair Care</title>
<script type="text/javascript" src="js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="js/jquery.bpopup-0.7.0.min.js"></script>
<script type="text/javascript" src="js/jquery.uniform.js"></script>
<script type="text/javascript">
        
		$(window).load(function(){
			$("input").uniform();
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
		  				$header.removeClass().addClass('expand').next().find('span').text('VIEW');
					  	$this.parent().find('.recommendation-running-content').stop(true, true).slideUp();
					}else{
						$header.removeClass().addClass('collapse').next().find('span').text('CLOSE');
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
					});
				});
		
				$('.product-thumb-image img').on('click',function(){
				    var productId=$(this).closest('div').attr('productId'),
					    brandId=$(this).closest('div').attr('brandId');
				  $('#pop-up-outer').find('#Tab1 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>");
				  $('#pop-up-outer').find('#Tab2 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=y&glos=n'></iframe>");
				  $('#pop-up-outer').find('#Tab4 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=y&ingred=n&glos=n'></iframe>");
				  $('#pop-up-outer').find('#Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+brandId+"'></iframe>");
				  $('#pop-up-outer').find('#Tab6 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairproductanalysis.do?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
				  $('#pop-up-outer').find('#Tab3 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y'></iframe>");
				  
				   $('.product-pop-thumb-image img').attr('src',$(this).attr('src'));
				   
					var $rating=$(this).closest('div > .product-thumb-image').next('.product-rating');
					var $name=$rating.next('.product-name');
					var $price=$name.next('.product-price');
					 $('#pop-up-outer').find('.product-name-label').html('<img src="images/trans.gif" width="270" height="74" />');
					 $('#pop-up-outer').find('.product-rating').html($rating.html());
					 $('#pop-up-outer').find('.product-titel-des').html('<span>'+$name.text()+'</span>');
					 $('#pop-up-outer').find('.product-price').text($price.text());
				  
				  $('#pop-up-outer').bPopup();
				});
				/* ============== Pop up Tab Java Script End   ================== */
				$('.remove-button > a').on('click',function(e){
					e.preventDefault();
					 var price=0.00;
					 var $remove = $(this).closest('.product-image'),
					    test=$remove.find("input:hidden").attr('name');
					
					 $remove.find('.regimen').remove();
					 $remove.append('<div class="styleing-product-please-note"><h1>Please note :</h1><p>A '+test.replace("Skip", "")+' product wasn&#39;t selected. If you want to revise your choices, you can go back and add it to your regimen.</p></div><div class="remove-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>');
					 
					 
					if(test === 'shampooSkip'){
						document.regdet.shampooprod.value = '';
						var shampooPrice = parseFloat(document.regdet.shampooPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - shampooPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
						
					}
					if(test === 'conditionerSkip'){
						document.regdet.conditionerprod.value = '';
						var conditionerPrice = parseFloat(document.regdet.conditionerPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - conditionerPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					if(test === 'htreatSkip'){
						document.regdet.htreatprod.value = '';
						var htreatPrice = parseFloat(document.regdet.htreatPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - htreatPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					if(test === 'stylingSkip'){
						document.regdet.stylingprod.value = '';
						var stylingPrice = parseFloat(document.regdet.stylingPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - stylingPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					
				});
			  $('#Radio1 input').on('click',function(){
				   var price=0.0;
				   var finPrice= parseFloat(document.regdet.finishPrice.value),
				      subTot=parseFloat(document.regdet.subTotal.value);
				  
				//   console.log(finPrice+'  >> '+subTot) ;
				   if((document.regdet.finishCheck.checked)){
					  price += finPrice + subTot;
					  var finProd=$(this).closest('.product-image').find('.product-thumb-image').attr('productid');
					//  console.log(finProd);
					  document.regdet.subTotal.value = price.toFixed(2);
					  document.regdet.finishprod.value  = finProd;
					  document.regdet.finishqty.value  = "1";
					  $('#totalBottom').text('SUBTOTAL : $'+ price.toFixed(2));
					}else{
						price += subTot - finPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						document.regdet.finishqty.value  = "";
						document.regdet.finishprod.value  = "";
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
			   });		
							
	});
</script>
<script type="text/javascript">
<!--
function submitInfoNew() {
//if(changePrice() && regimenStatus()){
	
      if(document.regdet.shampooprod.value != null && document.regdet.shampooprod.value != ''){
    	  document.regdet.shampooqty.value = 1;
      }
      if(document.regdet.conditionerprod.value != null && document.regdet.conditionerprod.value != ''){
    	  document.regdet.conditionerqty.value = 1;
      }
      if(document.regdet.htreatprod.value != null && document.regdet.htreatprod.value != ''){
    	  document.regdet.htreatqty.value = 1;
      }
      if(document.regdet.stylingprod.value != null && document.regdet.stylingprod.value != ''){
    	  document.regdet.stylingqty.value = 1;
      }
     
      document.regdet.target = "" ;
      document.regdet.finishCheck1.value ="truevb" ;
      document.regdet.action = "<%=response.encodeURL("hairregimenshop.do")%>" ;
  //    alert("bbbbbbbbb") ;
      document.regdet.submit() ;
  //    alert("jjjjjjjjjjjjjjjjjjj bbbbbbbbb") ;
      
 // }
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
 //   alert("Please select an item before proceeding to shopping cart");
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
 // document.getElementById("topPrice").innerHTML = "Total $"+price.toFixed(2);
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
  //document.getElementById("topPrice").innerHTML = "Total $"+price.toFixed(2);
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
	  //  alert("Quantity must not be empty");
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
    alert("Quantity must not be empty");
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


function submitNoBrand()
{
		//  alert(document.regdet.scrname2.value+"  "+document.regdet.prodLine.value) ;
		
		  
		  
		  if (document.regdet.scrname2.value = "regimendetailpage")
		  {
		    document.regdet.target = "" ;
		   
		  
		    <% if (scrname2.equals("skinanalysisnobrand")) { %>
		        
		        document.regdet.action = "<%=response.encodeURL("hairproductregimen.do")%>" ;
		     <% } else { %>
		        document.regdet.singlebrandflag.value = "yes" ;
		        document.regdet.prodLineFlag.value = "yes" ;
		        document.regdet.action = "<%=response.encodeURL("hairregimendetail.do")%>" ;
		     <% } %>
		    document.regdet.submit() ;
		  }
		  else
		  {
		
		    document.regdet.target = "" ;
		    document.regdet.action = "<%=response.encodeURL("hairproductregimen.do")%>" ;
		    document.regdet.submit() ;
		  }
	//	

	  
}

function submitBack() {

	document.regdet.skinMethodToCall.value = "get";
	document.regdet.action = "<%=response.encodeURL("customerselfprofilehairconcerns.do")%>" ;
	document.regdet.submit();
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


<script type="text/javascript">
function checkPageHeight()
{
 var ht = document.documentElement.scrollHeight ;
// alert(ht);
  // parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
   parent.location = "http://BeautyT1b.dstest.drugstore.com/partners/sv/recommender.asp?catid=12875#"+ht ;

}
</script>
</head>
<!--  <body onLoad="javascript:checkPageHeight();">  -->
<body>

<!-- Wrapper Start -->
<body id="RegimenDetail"  style="margin-left:0px;">
<div id="wrapper">
  <form name="regdet" method="post" action="">
	<!-- Container Start -->
    <div id="container">

        
        
        <!-- Banner Image Start -->
        <div id="banner-image-outer">
        	<img src="images/BCOM-banner-hair.jpg" alt="" />
        </div><!-- Banner Image End -->

        <!-- Steps Images Outer Start -->
        <div id="steps-images-outer">
        	<div class="step-1-image step-3-1"><a href="javascript:submitBack()">&nbsp;</a></div>
            <div class="step-2-image step-3-2"><a href="javascript:submitNoBrand()"></a></div>
            <div class="step-3-image step-3-3"></div>
        </div><!-- Steps Images Outer End -->

        <!-- Body Content Outer Start -->
        <div id="body-running-content-outer">

            <!-- Running Content Start -->
	        <div id="running-content">
	             <%=basicstring%>
                </div><!-- Running Content End -->

                <!-- Product Outer Start -->
                <div id="product-outer">

                    <!-- Product Image Row 1 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image your-regimen"></div>
                        </div><!-- Product Label And Number End -->
                        <!-- Product Inner Image 1 Start -->
                   <div class="product-image">
                   
                   <div class="final-product-label-number shampoo"></div>
            <% if (!noshampoo) { shampoorepeat = true ; %>
               <input type="hidden" name="shampooSkip" value="<%=shampooskip%>">
            <% 
                 if (((ProductVO)shampoopr).getImage() != null)
                    prodmainimage = ((ProductVO)shampoopr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(shampoopr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="shampooprod" value="<%=((ProductVO)shampoopr).getProdId()%>">
                        <input type="hidden" name="shampooqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)shampoopr).getProdId()%>' brandId='<%=((ProductVO)shampoopr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" />  <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)shampoopr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)shampoopr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(shampoopr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                           
                   <% } %>

                   <% if (!shampoorepeat && noshampoo   ) { %>
                    <%     
                        String shampoonoprodtext = "" ;

                        if (shampooskip.equals("shampooSkip")) 
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
                                           <input type="hidden" name="shampooprod" value="<%=((ProductVO)shampoopr).getProdId()%>">
                        <input type="hidden" name="shampooqty" value="">
                   
                     <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A Shampoo product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                         
                        </div><!-- Product Inner Image 1 End -->
                        
                        
                 <!-- Product Inner Image 2 Start -->
                 <div class="product-image">
                 	<div class="final-product-label-number conditioner"></div>
                                 
                                 
            <% if (!noconditioner) { conditionerrepeat = true ; %>
               <input type="hidden" name="conditionerSkip" value="<%=conditionerskip%>">
               
            <% 
                 if (((ProductVO)conditionerpr).getImage() != null)
                    prodmainimage = ((ProductVO)conditionerpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(conditionerpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                         <input type="hidden" name="conditionerprod" value="<%=((ProductVO)conditionerpr).getProdId()%>">
                        <input type="hidden" name="conditionerqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)conditionerpr).getProdId()%>' brandId='<%=((ProductVO)conditionerpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" />  <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)conditionerpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)conditionerpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(conditionerpr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                   <% } %>
                   
                   <% if (!conditionerrepeat && noconditioner   ) { %>
                   <input type="hidden" name="conditionerSkip" value="<%=conditionerskip%>">
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
                      <input type="hidden" name="conditionerprod" value="<%=((ProductVO)conditionerpr).getProdId()%>">
                        <input type="hidden" name="conditionerqty" value="">
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A conditioner product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                            
                            
                            
                        </div><!-- Product Inner Image 2 End -->
                        
                        
                        <!-- Product Inner Image 3 Start -->
                        <div class="product-image">
                        	<div class="final-product-label-number treatment"></div>
                                 
                                 
                                 
            <% if (!nohtreat) { htreatrepeat = true ; %>
               <input type="hidden" name="htreatSkip" value="<%=htreatskip%>">
              
            <% 
                 if (((ProductVO)htreatpr).getImage() != null)
                    prodmainimage = ((ProductVO)htreatpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(htreatpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="htreatprod" value="<%=((ProductVO)htreatpr).getProdId()%>">
                         <input type="hidden" name="htreatqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)htreatpr).getProdId()%>' brandId='<%=((ProductVO)htreatpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" />  <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)htreatpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)htreatpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(htreatpr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                   <% } %>
                   
                   <% if (!htreatrepeat && nohtreat   ) { %>
                   <input type="hidden" name="htreatSkip" value="<%=htreatskip%>">
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
                    <input type="hidden" name="htreatprod" value="<%=((ProductVO)htreatpr).getProdId()%>">
                         <input type="hidden" name="htreatqty" value="">
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A treat product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                            
                            
                        </div><!-- Product Inner Image 3 End -->
                        <!-- Product Inner Image 4 Start -->
                        <div class="product-image">
                        	<div class="final-product-label-number styling-product"></div>
                                 
                                 
            <% if (!nostyling) { stylingrepeat = true ; %>
               <input type="hidden" name="stylingSkip" value="<%=stylingskip%>">
               
               
            <% 
                 if (((ProductVO)stylingpr).getImage() != null)
                    prodmainimage = ((ProductVO)stylingpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(stylingpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="stylingprod" value="<%=((ProductVO)stylingpr).getProdId()%>">
                        <input type="hidden" name="stylingqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)stylingpr).getProdId()%>' brandId='<%=((ProductVO)stylingpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" />  <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)stylingpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)stylingpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(stylingpr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
						</div>                            
                   <% } %>
                   
                   <% if (!stylingrepeat && nostyling   ) { %>
                   <input type="hidden" name="stylingSkip" value="<%=stylingskip%>">
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
                     <input type="hidden" name="stylingprod" value="<%=((ProductVO)stylingpr).getProdId()%>">
                        <input type="hidden" name="stylingqty" value="">
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A styling product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                            
                            
                            
                        </div><!-- Product Inner Image 4 End -->

                    </div><!-- Product Image Row 1 Outer End -->

                    <!-- You Might Also Like Start -->
                    <div class="you-might-also-like-label">
                    	<span></span>
                    </div><!-- You Might Also Like End -->

                     <!-- Product Image Row 2 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="add-finishing-touch">
                        		<div class="product-number-image finishing-touch"></div>
                        		<p>Adding a finishing product to your regimen is an excellent way to perfect your look.</p>
                        	</div>
                        </div><!-- Product Label And Number End -->
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
    
              <% if (!nofinish) { %>
                  <% prodmainimage = ((ProductVO)finishpr).getImage() ;  %>
                  <%
                      starrating1 = "" ;
                      starrating1 = (String)((ProductVO)finishpr).getShadeImage() ;
                      String crev = "" ;
                      if (starrating1 !=null && starrating1.length() > 0)
                             crev = "yes" ;
                  %>

                        	<div class="product-thumb-image" productId='<%=((ProductVO)finishpr).getProdId()%>' brandId='<%=((ProductVO)finishpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" />  <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)finishpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)finishpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(finishpr.getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio" id="Radio1" ><input type="radio" name="finishCheck" /></span>
                            	<span class="select-label">SELECT</span>
                           </div>
                           
                  <%  }  %>           
                            
                        </div><!-- Product Inner Image 1 End -->
                        <!-- Product Inner Image 2 Start -->
                        <div class="product-image"></div><!-- Product Inner Image 2 End -->
                        <!-- Product Inner Image 3 Start -->
                        <div class="product-image"></div><!-- Product Inner Image 3 End -->
                        <!-- Product Inner Image 4 Start -->
                        <div class="product-image"></div><!-- Product Inner Image 4 End -->

                    </div><!-- Product Image Row 2 Outer End -->

                    <!-- Sub Total Start -->
                    <div class="sub-total" id="totalBottom">SUBTOTAL : $<%=pprice%></div><!-- Sub Total End -->

                </div><!-- Product Outer End -->

                <!-- Edit Review Button Start -->
                <div id="edit-review-button-outer">
                	<div class="edit-selection-button"><a href="javascript:submitNoBrand()"></a></div>
                    <div class="add-selection-to-bag-button"><a href="javascript:submitInfoNew()"></a></div>
                </div><!-- Edit Review Button End -->

            <!-- Please Note Start -->
            <div id="please-note">
            	<strong>PLEASE NOTE:</strong> Information on this site is not meant to be a substitute for the advice from a medical professional. Prior to starting a new regimen, you should consult your own physician, particularly if you are currently using any medications, are pregnant or nursing. Test a small amount of each new product on a hidden area (Such as behind your ear) to make sure that your skin does not have any adverse reactions. Always follow the recommended application quantity and frequency for each product.
            </div><!-- Please Note End -->

        </div><!-- Body Content Outer End-->

    </div><!-- Container End -->
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
<input type="hidden" name="finishprod" value="">
<input type="hidden" name="singlebrandflag" value="">
<input type="hidden" name="finishqty" value="">
<input type="hidden" name="finishexists" value="<%=finishexists%>">
<input type="hidden" name="shampooPrice" value="<%=shampooPrice%>">
<input type="hidden" name="conditionerPrice" value="<%=conditionerPrice%>">
<input type="hidden" name="htreatPrice" value="<%=htreatPrice%>">
<input type="hidden" name="stylingPrice" value="<%=stylingPrice%>">
<input type="hidden" name="finishPrice" value="<%=finishPrice%>">
<input type="hidden" name="finishCheck1" value="">
<input type="hidden" name="scrname2" value="regimendetailpage">
<input type="hidden" name="subTotal" value="<%=pprice%>">
<input type=hidden name=scrname value="">
<input type="hidden" name="skinMethodToCall">
<input type="hidden" name="noshampoo">
<input type="hidden" name="bnamestring" value="<%=bnamestring %>">


     <!-- Pop Up Start -->
	<div id="pop-up-outer" style="display:none;">
	<!-- Close Button Start -->
    <div class="pop-up-close-button"><a href="#" class="bClose"></a></div><!-- Close Button End -->

    <!-- Product Thumb Image & Detials Start -->
    <div class="product-thumb-image-details-outer">

        <!-- Product Thumb Image Start -->
        <div class="product-pop-thumb-image"><img src="images/product-thumb-1.png" width="154" height="130" alt="" /></div><!-- Product Thumb Image End -->

        <!-- Product Name Label Start -->
        <div class="product-name-label">bareMinerals</div><!-- Product Name Label End -->

        <!-- product Rating Start -->
        <div class="product-rating">
            <ul>
                <li><a class="give-points" href="#"></a></li>
                <li><a class="give-points" href="#"></a></li>
                <li><a class="give-points" href="#"></a></li>
                <li><a class="give-points" href="#"></a></li>
                <li><a class="no-points" href="#"></a></li>
            </ul>
        </div><!-- product Rating End -->

        <!-- Product Titel Des Start -->
        <div class="product-titel-des">
        	<span class="label-1">BAREMINERALS</span>
            <span>All-Over Face Color</span>
        </div><!-- Product Titel Des End -->

        <!-- Product Price Start -->
        <div class="product-price">$35.00</div><!-- Product Price End -->

    </div><!-- Product Thumb Image & Detials End -->

    <!-- Product Tab Outer Start -->
    <div class="product-tab-outer">

    	<!-- Tab Nav Start -->
        <div class="tab-nav">
        	<ul>
        		<li style="width:174px;"><a href="#" class="active" rel="Tab1">PRODUCT INFORMATION</a></li>
                <li style="width:116px;"><a href="#" rel="Tab2">INGREDIENTS &nbsp; &nbsp;</a></li>
                <li style="width:89px;"><a href="#" rel="Tab3">REVIEWS &nbsp; &nbsp;</a></li>
                <li style="width:107px;"><a href="#" rel="Tab4">DIRECTIONS &nbsp;</a></li>
                <li style="width:147px;"><a href="#" rel="Tab5">ABOUT THE BRAND &nbsp;</a></li>
                <li style="width:156px;"><a href="#" rel="Tab6">WHY RECOMMENDED</a></li>
        	</ul>
        </div><!-- Tab Nav End-->

        <!-- Product Tab Content Outer Start -->
        <div class="product-tab-content-outer">
        	<div id="Tab1" class="ProductContent" style="display:block;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 1 Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
            <div id="Tab2" class="ProductContent" style="display:none;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 2 Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
            <div id="Tab3" class="ProductContent" style="display:none;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 3Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
            <div id="Tab4" class="ProductContent" style="display:none;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 4 Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
            <div id="Tab5" class="ProductContent" style="display:none;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 5 Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
            <div id="Tab6" class="ProductContent" style="display:none;">
                <table cellspacing="0" cellpadding="0" border="0" width="100%">
                  <tbody><tr>
                    <td height="300" align="center" valign="middle" style="font-size:18px; color:#333;">Tab 6 Content Goes Here...</td>
                  </tr>
                </tbody></table>
            </div>
        </div><!-- Product Tab Content Outer End -->


    </div><!-- Product Tab Outer End -->

	</div><!-- Pop Up End -->

</div><!-- Wrapper End -->
</form>
</body>
</html>