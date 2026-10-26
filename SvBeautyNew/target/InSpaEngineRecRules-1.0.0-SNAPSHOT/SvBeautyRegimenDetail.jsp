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
    boolean spfflag = false ;
    String basicstring = "" ;
    boolean secondflag = false ;
    String scrname = "" ;
    String scrname2 = "" ;
    
	DecimalFormat ft    = new DecimalFormat();	
	//ft.applyLocalizedPattern("#0.00");   	
	ft.setMinimumFractionDigits(2);
    ft.setMaximumFractionDigits(2);	
	String cusid = "" ;
    String messagesent = "no" ;
	String brandlogofile = "images/shared/trans.gif" ;
    String bnamestring = "" ;
    
   
    ArrayList prodlist = new ArrayList() ;
	ArrayList cleanse = new ArrayList();
	ArrayList tone = new ArrayList();
	ArrayList treat = new ArrayList();
	ArrayList etreat = new ArrayList();
	ArrayList moisturize = new ArrayList();
	ArrayList nightmoisturize = new ArrayList();
	ArrayList exfoliant = new ArrayList();
	ArrayList spfprods = new ArrayList();
    ProductVO[] spf = new ProductVO[0] ;

    String cleansePrice = "" ;
    String moisturizePrice = "" ;
    String treatPrice = "" ;
    String etreatPrice = "" ;
    String spfPrice = "" ;
    String spfexists = "no" ;
    String spfprod = "" ;
    String cleanseprod = "" ;
    String moisturizeprod = "" ;
    String treatprod = "" ;
    String etreatprod = "" ;
    String cleanseskip = "" ;
    String moisturizeskip = "" ;
    String treatskip = "" ;
    String etreatskip = "" ;

    ProductVO cleansepr  = new ProductVO() ;
    ProductVO moisturizepr  = new ProductVO() ;
    ProductVO treatpr  = new ProductVO() ;
    ProductVO etreatpr  = new ProductVO() ;
    ProductVO spfpr  = new ProductVO() ;

    Hashtable brands= new Hashtable() ;
    Hashtable master = new Hashtable() ;
    Hashtable prodhash = new Hashtable() ;
    boolean nocleanse = false ;
    boolean nomoisturize = false ;
    boolean notreat = false ;
    boolean noetreat = false ;
    boolean nospf = false ;
    boolean cleanserepeat = false ;
    boolean moisturizerepeat = false ;
    boolean treatrepeat = false ;
    boolean etreatrepeat = false ;
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
                scrname = (String) request.getAttribute("scrname") ;							
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
                  cleanse = (ArrayList) prodlist.get(0) ;
                  moisturize =    (ArrayList) prodlist.get(1) ;           
                  treat =    (ArrayList) prodlist.get(2) ;           
                  etreat =    (ArrayList) prodlist.get(3) ;           
                  spfprods  = (ArrayList) prodlist.get(4) ;           
                }
                
                cleanseskip = (String) request.getAttribute("cleanseskip") ;
                moisturizeskip = (String) request.getAttribute("moisturizeskip") ;
                treatskip = (String) request.getAttribute("treatskip") ;
                etreatskip = (String) request.getAttribute("etreatskip") ;

                if (cleanseskip!= null && cleanseskip.equals("cleanseSkip"))
                      cleanse = new ArrayList() ;
                else if (cleanseskip == null)
                    cleanseskip = "" ;

                if (moisturizeskip!= null && moisturizeskip.equals("moisturizeSkip"))
                      moisturize = new ArrayList() ;
                else if (moisturizeskip == null)
                    moisturizeskip = "" ;

                if (treatskip!= null && treatskip.equals("treatSkip"))
                      treat = new ArrayList() ;
                else if (treatskip == null)
                    treatskip = "" ;

                if (etreatskip!= null && etreatskip.equals("etreatSkip"))
                      etreat = new ArrayList() ;
                else if (etreatskip == null)
                    etreatskip = "" ;

                cleanseprod = (String) request.getAttribute("cleanseprod") ;
                moisturizeprod = (String) request.getAttribute("moisturizeprod") ;
                treatprod = (String) request.getAttribute("treatprod") ;
                etreatprod = (String) request.getAttribute("etreatprod") ;
                spfprod = (String) request.getAttribute("spfprod") ;
                spfexists = (String) request.getAttribute("spfexists") ;
                System.out.println("vbbbbbdxdfg:at skin page") ;
                try
                {
                 if (request.getAttribute("spf") != null)
                   spf = (ProductVO[]) request.getAttribute("spf") ;
                } catch(Exception exx)
                {
                  System.out.println(exx+"  "+exx) ;
                }
                messagesent = (String) request.getAttribute("mailmessage") ;
                if (messagesent == null)
                  messagesent = "no" ;
                System.out.println("cleanse prod 111111 "+cleanseprod+ "dddddddddddddd "+cleanseprod.length()) ;
                if (cleanseprod != null && !cleanseprod.equals("null") && cleanseprod.length() >0)
                {
                	System.out.println("cleanse prod 33333333 "+cleanseprod) ;
                    cleansepr = (ProductVO) prodhash.get(cleanseprod.trim()) ;
                    cleansePrice = (String) cleansepr.getPrice() ;
                    tprice = tprice+Float.parseFloat(cleansePrice.substring(0,cleansePrice.length())) ;
                }
                else
                {
                	System.out.println("cleanse prod 22222222 "+cleanseprod) ;
                    nocleanse = true ;
                }
                //System.out.println("vvvvvvvvvdsds3 "+nocleanse+"  "+tprice) ;				

                if (moisturizeprod !=null && !moisturizeprod.equals("null")&& moisturizeprod.length() >0)
                {
                    moisturizepr = (ProductVO) prodhash.get(moisturizeprod.trim()) ;
                    moisturizePrice = (String) moisturizepr.getPrice() ;
                    tprice = tprice+Float.parseFloat(moisturizePrice.substring(0,moisturizePrice.length())) ;
                }
                else
                {
                    nomoisturize = true ;
                }
                //System.out.println("vvvvvvvvvdsds5 "+nomoisturize) ;				

                if (treatprod !=null && !treatprod.equals("null")&& treatprod.length() >0)
                {
                    treatpr = (ProductVO) prodhash.get(treatprod.trim()) ;
                    treatPrice = (String) treatpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(treatPrice.substring(0,treatPrice.length())) ;
                }
                else
                    notreat = true ;
                //System.out.println("vvvvvvvvvdsds7 "+notreat) ;				
				 
                if (etreatprod !=null && !etreatprod.equals("null")&& etreatprod.length() >0)
                {
                    etreatpr = (ProductVO) prodhash.get(etreatprod.trim()) ;
                    etreatPrice = (String) etreatpr.getPrice() ;
                    tprice = tprice+Float.parseFloat(etreatPrice.substring(0,etreatPrice.length())) ;
                }
                else
                    noetreat = true ;
               // System.out.println("spf prod...... "+spfprod.length()) ;
                if (spfprod !=null && !spfprod.equals("null")&& spfprod.length() >0)
                {
                    spfpr = (ProductVO) prodhash.get(spfprod.trim()) ;
                    spfPrice = (String) spfpr.getPrice() ;
                    System.out.println(" 222222 spf prod...... "+spfpr) ;
                  //  tprice = tprice+Float.parseFloat(spfPrice.substring(0,spfPrice.length())) ;
                }
                else
                    nospf = true ;
                System.out.println("tprice"+tprice);
                pprice = (String) ft.format(new Double(tprice)) ;
                System.out.println("pprice"+pprice);

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
<%! String checkChecked(String val1, String val2)
{
   String retu = "" ;
       if (val1 != null && val2 != null)
	    if ( val1.equals(val2) )
	      retu = "checked" ;
	return retu ;
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
<title>SkinCare RegimenDetails Page</title>
<script type="text/javascript" src="js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="js/jquery.bpopup-0.7.0.min.js"></script>
<script language="javascript" src='js/jquery.uniform.js' ></script>
<!--[if gte IE 9]>
<link rel="stylesheet" type="text/css" href="css/ie9.css" />
<![endif]-->
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
					 $remove.append('<div class="styleing-product-please-note"><h1>Please note :</h1><p>A moisturize product wasn&#39;t selected. If you want to revise your choices, you can go back and add it to your regimen.</p></div><div class="remove-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>');
					 
					if(test === 'cleanseSkip'){
						document.regdet.cleanseprod.value = '';
						var cleansePrice = parseFloat(document.regdet.cleansePrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - cleansePrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
						
					}
					if(test === 'moisturizeSkip'){
						document.regdet.moisturizeprod.value = '';
						var moisturizePrice = parseFloat(document.regdet.moisturizePrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - moisturizePrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					if(test === 'treatSkip'){
						document.regdet.treatprod.value = '';
						var treatPrice = parseFloat(document.regdet.treatPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - treatPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					if(test === 'etreatSkip'){
						document.regdet.etreatprod.value = '';
						var etreatPrice = parseFloat(document.regdet.etreatPrice.value),
						   subTot=parseFloat(document.regdet.subTotal.value);
						price += subTot - etreatPrice;
						document.regdet.subTotal.value = price;
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
					
				});
			  $('#Radio1 input').on('click',function(){
				   var price=0.00;
				   var finPrice= parseFloat(document.regdet.spfPrice.value),
				      subTot=parseFloat(document.regdet.subTotal.value);
				 
				   if((document.regdet.spfCheck.checked)){
					   var finProd=$(this).closest('.product-image').find('.product-thumb-image').attr('productid');
					  price += finPrice + subTot;
					  document.regdet.subTotal.value = price.toFixed(2);
					  document.regdet.spfprod.value  = finProd;
					  document.regdet.spfqty.value  = "1";
					  $('#totalBottom').text('SUBTOTAL : $'+ price.toFixed(2));
					}else{
						price += subTot - finPrice;
						document.regdet.subTotal.value = price.toFixed(2);
						document.regdet.spfqty.value  = "";
						document.regdet.spfprod.value  = "";
						$('#totalBottom').text('SUBTOTAL : $'+price.toFixed(2));
					}
			   });				
	});
</script>
<script type="text/javascript">
<!--
function submitInfoNew() {
      //    console.log(document.regdet.spfprod);
	      if(document.regdet.cleanseprod.value != null && document.regdet.cleanseprod.value != '' ){
	    	  document.regdet.cleanseqty.value = 1;
	             
	      }
	      if(document.regdet.moisturizeprod.value != null && document.regdet.moisturizeprod.value != ''){
	    	  document.regdet.moisturizeqty.value = 1;
	      }
	      if(document.regdet.treatprod.value != null && document.regdet.treatprod.value != ''){
	    	  document.regdet.treatqty.value = 1;
	      }
	      if(document.regdet.etreatprod.value != null && document.regdet.etreatprod.value != ''){
	    	  document.regdet.etreatqty.value = 1;
	      }
	//      alert( document.regdet.cleanseprod.value +'==>'+document.regdet.cleanseqty.value);
	//      alert( document.regdet.moisturizeprod.value +'==>'+document.regdet.moisturizeqty.value);
	 //     alert( document.regdet.treatprod.value +'==>'+document.regdet.treatqty.value);
	 //     alert( document.regdet.etreatprod.value +'==>'+document.regdet.etreatqty.value);
	//      alert( document.regdet.spfprod.value +'==>'+document.regdet.spfqty.value);
	      document.regdet.target = "" ;
	      document.regdet.spfCheck1.value ="truevb" ;
	      document.regdet.action = "<%=response.encodeURL("regimenshop.do")%>" ;
	      document.regdet.submit() ;
	      
	 // }
	}
function regimenStatus(){

  var status = false;
  
  if (document.regdet.cleanseqty != null)
  if(document.regdet.cleanseCheck.checked && document.regdet.cleanseqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.moisturizeqty != null)
  if(document.regdet.moisturizeCheck.checked && document.regdet.moisturizeqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.treatqty != null)
  if(document.regdet.treatCheck.checked && document.regdet.treatqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.etreatqty != null)
  if(document.regdet.etreatCheck.checked && document.regdet.etreatqty.value.length!=0)
  {
      status = true;
  }
  if (document.regdet.spfqty != null)
  if(document.regdet.spfCheck.checked && document.regdet.spfqty.value.length!=0)
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
      document.regdet.action = "<%=response.encodeURL("productDescPdfGenerator.do")%>" ;
      document.regdet.target = "_blank" ;
      document.regdet.submit() ;
   // window.open("<%=response.encodeURL("productDescPdfGenerator.do")%>") ;
}
function skipThis(id)
{
  if(id=="cleanse")
  {
     for(i=0; i<document.regdet.cleanseSelect.length; i++)
		document.regdet.cleanseSelect[i].checked=0;
  }
  if (id == "moisturize")
  {
     for(i=0; i<document.regdet.moisturizeSelect.length; i++)
		document.regdet.moisturizeSelect[i].checked=0;
  }
  if(id=="treat")
  {
     for(i=0; i<document.regdet.treatSelect.length; i++)
		document.regdet.treatSelect[i].checked=0;
  }
  if(id=="etreat")
  {
     for(i=0; i<document.regdet.etreatSelect.length; i++)
		document.regdet.etreatSelect[i].checked=0;
  }
  newPrice('skip');
}

function selectProd(id,val, price)
{
    if (id == "cleanse")
    {
    	document.regdet.cleanseSkip.checked=0;
		document.regdet.cleanseprod.value=val ;
		document.regdet.selectcleanseprice.value=price ;
	}
    if (id == "moisturize")
    {
        document.regdet.moisturizeSkip.checked=0;
		document.regdet.moisturizeprod.value=val ;
		document.regdet.selectmoisturizeprice.value=price ;
    }
    if (id == "treat")
    {
        document.regdet.treatSkip.checked=0;
     	document.regdet.treatprod.value=val ;
		document.regdet.selecttreatprice.value=price ;
	}
    if (id == "etreat")
    {
       	document.regdet.etreatSkip.checked=0;
   		document.regdet.etreatprod.value=val ;
		document.regdet.selectetreatprice.value=price ;
	}
    newPrice(price);
}
function newPrice(itemPrice)
{
  var price = 0.0;

  var cleanseqty = 0;
  var moisturizeqty = 0;
  var treatqty = 0;
  var etreatqty = 0;
  var spfqty = 0 ;

  var cleansePrice = parseFloat(document.regdet.cleansePrice.value);
  var moisturizePrice = parseFloat(document.regdet.moisturizePrice.value);
  var treatPrice = parseFloat(document.regdet.treatPrice.value);
  var etreatPrice = parseFloat(document.regdet.etreatPrice.value);
  var spfPrice = parseFloat(document.regdet.spfPrice.value);

  if (document.regdet.cleanseqty != null && document.regdet.cleanseqty.value != null)
    cleanseqty = parseInt(document.regdet.cleanseqty.value);
  if (document.regdet.moisturizeqty != null && document.regdet.moisturizeqty.value != null)
    moisturizeqty = parseInt(document.regdet.moisturizeqty.value);
  if (document.regdet.treatqty != null && document.regdet.treatqty.value !=null)
    treatqty = parseInt(document.regdet.treatqty.value);
  if (document.regdet.etreatqty != null && document.regdet.etreatqty.value != null)
    etreatqty = parseInt(document.regdet.etreatqty.value);
  if (document.regdet.spfqty != null && document.regdet.spfqty.value != null)
    spfqty = parseInt(document.regdet.spfqty.value);

  if (document.regdet.cleanseCheck != null)
  if(document.regdet.cleanseCheck.checked)
  {
    if(isNaN(cleanseqty))
	  cleanseqty = 0;
    price += cleanseqty * cleansePrice;
  }

  if (document.regdet.moisturizeCheck != null)
  if(document.regdet.moisturizeCheck.checked)
  {
    if(isNaN(moisturizeqty))
	  moisturizeqty = 0;
    price += moisturizeqty * moisturizePrice;
  }

  if (document.regdet.treatCheck != null)
  if(document.regdet.treatCheck.checked)
  {
    if(isNaN(treatqty))
	  treatqty = 0;
    price += treatqty * treatPrice
  }

  if (document.regdet.etreatCheck != null)
  if(document.regdet.etreatCheck.checked)
  {
    if(isNaN(etreatqty))
	  etreatqty = 0;
    price += etreatqty * etreatPrice
  }

  if (document.regdet.spfCheck != null)
  if(document.regdet.spfCheck.checked)
  {
    if(isNaN(spfqty))
	  spfqty = 0;
	price += spfqty * spfPrice;
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
    document.regdet.action = "regimenmail.do" ;
    document.regdet.submit() ;
  }
}
function selectType(id)
{
  document.regdet.scrname.value = "skindetail" ;
  if (id == "cleanse")
    document.regdet.prodType.value = "0001" ;
  if (id == "moisturize")
    document.regdet.prodType.value = "0003" ;
  if (id == "treat")
    document.regdet.prodType.value = "0005" ;
  if (id == "etreat")
    document.regdet.prodType.value = "0004" ;
  document.regdet.prodTypeFlag.value = "yes" ;
  document.regdet.prodLineFlag.value = "no" ;
  document.regdet.target = "" ;
  document.regdet.action = "<%=response.encodeURL("regimendetail.do")%>" ;
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

  var cleanseqty = 0;
  var moisturizeqty = 0;
  var treatqty = 0;
  var etreatqty = 0;
  var spfqty = 0 ;
  
  var vcleanseprice = 0 ;
  var vmoisturizeprice = 0 ;
  var vtreatprice = 0 ;
  var vetreatprice = 0 ;
  
  var cleansePrice = parseFloat(document.regdet.cleansePrice.value);
  var moisturizePrice = parseFloat(document.regdet.moisturizePrice.value);
  var treatPrice = parseFloat(document.regdet.treatPrice.value);
  var etreatPrice = parseFloat(document.regdet.etreatPrice.value);
  var spfPrice = parseFloat(document.regdet.spfPrice.value);

  if (document.regdet.cleanseqty != null && document.regdet.cleanseqty.value != null)
    cleanseqty = parseInt(document.regdet.cleanseqty.value);
  if (document.regdet.moisturizeqty != null && document.regdet.moisturizeqty.value != null)
    moisturizeqty = parseInt(document.regdet.moisturizeqty.value);
  if (document.regdet.treatqty != null && document.regdet.treatqty.value !=null)
    treatqty = parseInt(document.regdet.treatqty.value);
  if (document.regdet.etreatqty != null && document.regdet.etreatqty.value != null)
    etreatqty = parseInt(document.regdet.etreatqty.value);
  if (document.regdet.spfqty != null && document.regdet.spfqty.value != null)
    spfqty = parseInt(document.regdet.spfqty.value);

  if (document.regdet.cleanseCheck != null)
  if(document.regdet.cleanseCheck.checked)
  {
    if(isNaN(cleanseqty))
	  cleanseqty = 0;
    price += cleanseqty * cleansePrice;
  }

  if (document.regdet.moisturizeCheck != null)
  if(document.regdet.moisturizeCheck.checked)
  {
    if(isNaN(moisturizeqty))
	  moisturizeqty = 0;
    price += moisturizeqty * moisturizePrice;
  }

  if (document.regdet.treatCheck != null)
  if(document.regdet.treatCheck.checked)
  {
    if(isNaN(treatqty))
	  treatqty = 0;
    price += treatqty * treatPrice
  }

  if (document.regdet.etreatCheck != null)
  if(document.regdet.etreatCheck.checked)
  {
    if(isNaN(etreatqty))
	  etreatqty = 0;
    price += etreatqty * etreatPrice
  }

  if (document.regdet.spfCheck != null)
  if(document.regdet.spfCheck.checked)
  {
    if(isNaN(spfqty))
	  spfqty = 0;
	price += spfqty * spfPrice;
  }

  if (document.regdet.selectcleanseprice != null)
  {
     vcleanseprice = parseInt(document.regdet.selectcleanseprice.value);
     price += vcleanseprice ;
  }
  if (document.regdet.selectmoisturizeprice != null)
  {
     vmoisturizeprice = parseInt(document.regdet.selectmoisturizeprice.value);
     price += vmoisturizeprice ;
  }
  if (document.regdet.selecttreatprice != null)
  {
     vtreatprice = parseInt(document.regdet.selecttreatprice.value);
     price += vtreatprice ;
  }
  if (document.regdet.selectetreatprice != null)
  {
     vetreatprice = parseInt(document.regdet.selectetreatprice.value);
     price += vetreatprice ;
  }


  //document.all("bottomPrice").innerHTML = "$"+price.toFixed(2);
  //document.getElementById("topPrice").innerHTML = "Total $"+price.toFixed(2);
  document.getElementById("totalBottom").innerHTML = "Total $"+price.toFixed(2);
}

// Function for New Price and adding the content to the HTML
////////////////////////////////////////////////////////////


function changePrice()
{
	
  if (document.regdet.cleanseqty != null && document.regdet.cleanseqty.value != null)
  if(!document.regdet.cleanseCheck.checked && document.regdet.cleanseqty.value.length!=0)
  {
      document.regdet.cleanseqty.value="";
  }
  if (document.regdet.moisturizeqty != null && document.regdet.moisturizeqty.value != null)
  if(!document.regdet.moisturizeCheck.checked && document.regdet.moisturizeqty.value.length!=0)
  {
      document.regdet.moisturizeqty.value="";
  }
  if (document.regdet.treatqty != null && document.regdet.treatqty.value !=null)
  if(!document.regdet.treatCheck.checked && document.regdet.treatqty.value.length!=0)
  {
      document.regdet.treatqty.value="";
  }
  if (document.regdet.etreatqty != null && document.regdet.etreatqty.value !=null)
  if(!document.regdet.etreatCheck.checked && document.regdet.etreatqty.value.length!=0)
  {
      document.regdet.etreatqty.value="";
  }
  if (document.regdet.spfqty != null && document.regdet.spfqty.value !=null)
  if(!document.regdet.spfCheck.checked && document.regdet.spfqty.value.length!=0)
  {
      document.regdet.spfqty.value="";
  }


  if (document.regdet.cleanseqty != null && document.regdet.cleanseqty.value != null)
  if(document.regdet.cleanseqty.value.length==0)
  {
    if((document.regdet.cleanseCheck.checked)){
	  //  alert("Quantity must not be empty");
        document.regdet.cleanseqty.value = "1" ;
//		document.regdet.cleanseqty.focus();
//		return false;
	}
  }else{
    if(!checknumber(document.regdet.cleanseqty.value)){
		document.regdet.cleanseqty.value="";
        document.regdet.cleanseqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.moisturizeqty != null && document.regdet.moisturizeqty.value != null)
  if(document.regdet.moisturizeqty.value.length==0)
  {
    if((document.regdet.moisturizeCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.moisturizeqty.value = "1";
//		document.regdet.moisturizeqty.focus();
//		return false;
	}
  }else{
    if(!checknumber(document.regdet.moisturizeqty.value)){
		document.regdet.moisturizeqty.value="";
        document.regdet.moisturizeqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.treatqty != null && document.regdet.treatqty.value !=null)
  if(document.regdet.treatqty.value.length==0)
  {
    if((document.regdet.treatCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.treatqty.value = "1";
//		document.regdet.treatqty.focus();
//		return false;
 	}
  }else{
    if(!checknumber(document.regdet.treatqty.value)){
		document.regdet.treatqty.value="";
        document.regdet.treatqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }

  if (document.regdet.etreatqty != null && document.regdet.etreatqty.value !=null)
  if(document.regdet.etreatqty.value.length==0)
  {
    if((document.regdet.etreatCheck.checked)){
//	    alert("Quantity must not be empty");
		document.regdet.etreatqty.value = "1";
//		document.regdet.etreatqty.focus();
//		return false;
	}
  }else{
   if(!checknumber(document.regdet.etreatqty.value)){
		document.regdet.etreatqty.value="";
        document.regdet.etreatqty.focus();
        alert("Quantity must be a number");
		return false;
	}
  }
  if (document.regdet.spfqty != null && document.regdet.spfqty.value !=null)
  if(document.regdet.spfqty.value.length==0)
  {
    if((document.regdet.spfCheck.checked)){
    alert("Quantity must not be empty");
		document.regdet.spfqty.value = "1";
//		document.regdet.spfqty.focus();
//		return false;
	}
  }else{
   if(!checknumber(document.regdet.spfqty.value)){
		document.regdet.spfqty.value="";
        document.regdet.spfqty.focus();
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
	 //     alert(document.regdet.scrname2.value) ;
	  //    console.log(document.regdet.scrname2.value) ;
		  if (document.regdet.scrname2.value = "regimendetailpage")
		  {
		    document.regdet.target = "" ;
		  
		    <% if (scrname2.equals("skinanalysisnobrand")) { %>
	        document.regdet.action = "<%=response.encodeURL("productregimen.do")%>" ;
	     <% } else { %>
	        document.regdet.singlebrandflag.value = "yes" ;
	        document.regdet.prodLineFlag.value = "yes" ;
            document.regdet.action = "<%=response.encodeURL("regimendetail.do")%>" ;
	     <% } %>
		    document.regdet.submit() ;
		  }
		  else
		  {
		
		    document.regdet.target = "" ;
		    document.regdet.action = "<%=response.encodeURL("productregimen.do")%>" ;
		    document.regdet.submit() ;
		  }

  
}

function submitBack() {
    //alert('at submitBcak');
	document.regdet.skinMethodToCall.value = "get";
	if(document.regdet.mode.value == "skin"){
		document.regdet.action = "<%=response.encodeURL("customerselfprofileskinconcerns.do")%>" ;
	}
	else if(document.regdet.mode.value == "skinNew"){
		document.regdet.action = "<%=response.encodeURL("customerselfprofileNewskinconcerns.do")%>" ;
	}
	document.regdet.submit();
}
//-->
</script>


<script  type="text/javascript">
function checkPageHeight()
{
 var ht = document.documentElement.scrollHeight ;
// alert(ht);
  // parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
   parent.location = "http://BeautyT1b.dstest.drugstore.com/partners/sv/recommender.asp?catid=12873#"+ht ;

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
        	<img src="images/BCOM-banner-skin.jpg" alt="" />
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
	            <strong>Please note:</strong><br />

      <% if (dynamic.size() > 0) { %>
        <%=warnings%><br />
      <% } %>
      <br />
      <%=commontext%></p>
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
                   
                   <div class="final-product-label-number cleanser"></div>
            <% if (!nocleanse) { cleanserepeat = true ; %>
               <input type="hidden" name="cleanseSkip" value="<%=cleanseskip%>">
               
            <% 
                 if (((ProductVO)cleansepr).getImage() != null)
                    prodmainimage = ((ProductVO)cleansepr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(cleansepr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="cleanseprod" value="<%=((ProductVO)cleansepr).getProdId()%>">
                        <input type="hidden" name="cleanseqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)cleansepr).getProdId()%>' brandId='<%=((ProductVO)cleansepr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" /> <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)cleansepr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)cleansepr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(cleansepr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                           
                   <% } %>

                   <% if (!cleanserepeat && nocleanse   ) { %>
                    <%     
                        String cleansenoprodtext = "" ;

                        if (cleanseskip.equals("cleanseSkip")) 
                        	if (cleanseskip.equals("cleanseSkip")) 
                            {
                                cleansenoprodtext = "You 've chosen to skip this step.<br />" ;
                                prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                            }
                            else
                            {
                                prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                                cleansenoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                            }
                   %>
                        <input type="hidden" name="cleanseprod" value="<%=((ProductVO)cleansepr).getProdId()%>">
                        <input type="hidden" name="cleanseqty" value="">
                   
                     <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A cleanse product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                         
                        </div><!-- Product Inner Image 1 End -->
                        
                        
                 <!-- Product Inner Image 2 Start -->
                 <div class="product-image">
                 	<div class="final-product-label-number moisturizer"></div>
                                 
                                 
            <% if (!nomoisturize) { moisturizerepeat = true ; %>
               <input type="hidden" name="moisturizeSkip" value="<%=moisturizeskip%>">
            <% 
                 if (((ProductVO)moisturizepr).getImage() != null)
                    prodmainimage = ((ProductVO)moisturizepr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(moisturizepr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                         <input type="hidden" name="moisturizeprod" value="<%=((ProductVO)moisturizepr).getProdId()%>">
                                        <input type="hidden" name="moisturizeqty" value="">
                         
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)moisturizepr).getProdId()%>' brandId='<%=((ProductVO)moisturizepr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" /> <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)moisturizepr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)moisturizepr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(moisturizepr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                   <% } %>
                   
                   <% if (!moisturizerepeat && nomoisturize   ) { %>
                   <input type="hidden" name="moisturizeSkip" value="<%=moisturizeskip%>">
                   <%     
                        String moisturizenoprodtext = "" ;

                        if (moisturizeskip.equals("moisturizeSkip")) 
                        {
                          moisturizenoprodtext = "You 've chosen to skip this step.<br />" ;
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                        }
                        else
                        {
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                          moisturizenoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                        }

                   %>
                                            <input type="hidden" name="moisturizeprod" value="<%=((ProductVO)moisturizepr).getProdId()%>">
                                        <input type="hidden" name="moisturizeqty" value="">
                   
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A moisturize product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                            
                            
                            
                        </div><!-- Product Inner Image 2 End -->
                        
                        
                        <!-- Product Inner Image 3 Start -->
                        <div class="product-image">
                        	<div class="final-product-label-number skin-treatment"></div>
                                 
                                 
                                 
            <% if (!notreat) { treatrepeat = true ; %>
               <input type="hidden" name="treatSkip" value="<%=treatskip%>">
            <% 
                 if (((ProductVO)treatpr).getImage() != null)
                    prodmainimage = ((ProductVO)treatpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(treatpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="treatprod" value="<%=((ProductVO)treatpr).getProdId()%>">
               <input type="hidden" name="treatqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)treatpr).getProdId()%>' brandId='<%=((ProductVO)treatpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" /> <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)treatpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)treatpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(treatpr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
                        </div>    
                   <% } %>
                   
                   <% if (!treatrepeat && notreat   ) { %>
                   <input type="hidden" name="treatSkip" value="<%=treatskip%>">
                   <%     
                        String treatnoprodtext = "" ;

                        if (treatskip.equals("treatSkip")) 
                        {
                          treatnoprodtext = "You 've chosen to skip this step.<br />" ;
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                        }
                        else
                        {
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                          treatnoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                        }

                   %>
                                           <input type="hidden" name="treatprod" value="<%=((ProductVO)treatpr).getProdId()%>">
               <input type="hidden" name="treatqty" value="">
                   
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A treat product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
                             </div>
                            <div class="edit-button"><a href="#" onclick="javascript:submitNoBrand()">edit selections</a></div>
                    <% } %>
                            
                            
                        </div><!-- Product Inner Image 3 End -->
                        <!-- Product Inner Image 4 Start -->
                        <div class="product-image">
                        	<div class="final-product-label-number eye-product"></div>
                                 
                                 
            <% if (!noetreat) { etreatrepeat = true ; %>
               <input type="hidden" name="etreatSkip" value="<%=etreatskip%>">
            <% 
                 if (((ProductVO)etreatpr).getImage() != null)
                    prodmainimage = ((ProductVO)etreatpr).getImage() ;  
                 else 
                    prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                    starrating1 = "" ;
                    starrating1 = (String)(etreatpr).getShadeImage() ;
                    String crev = "" ;
                    if (starrating1 !=null && starrating1.length() > 0)
                        crev = "yes" ;
             %>
                        <input type="hidden" name="etreatprod" value="<%=((ProductVO)etreatpr).getProdId()%>">
               <input type="hidden" name="etreatqty" value="">
                    	<div class="regimen">
                            <div class="selected-product-thumb-image" productId='<%=((ProductVO)etreatpr).getProdId()%>' brandId='<%=((ProductVO)etreatpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" /> <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)etreatpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)etreatpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(etreatpr.getPrice()))%></div>
                            <div class="remove-button"><a href="#">Remove</a></div>
						</div>                            
                   <% } %>

                   
                   <% if (!etreatrepeat && noetreat   ) { %>
                   <input type="hidden" name="etreatSkip" value="<%=etreatskip%>">
                   <%     
                        String etreatnoprodtext = "" ;

                        if (etreatskip.equals("etreatSkip")) 
                        {
                          etreatnoprodtext = "You 've chosen to skip this step.<br />" ;
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_skip_step" ;
                        }
                        else
                        {
                          prodmainimage = "http://s7d2.scene7.com/is/image/Drugstore/SV_product_na" ;  
                          etreatnoprodtext = "We did not find a product to recommend for this step based on <br> the hair characteristics you have selected<br />" ;
                        }

                   %>
                                           <input type="hidden" name="etreatprod" value="<%=((ProductVO)etreatpr).getProdId()%>">
               <input type="hidden" name="etreatqty" value="">
                   
                             <div class="styleing-product-please-note">
                             	<h1>Please note :</h1>
                                <p>A etreat product wasn't selected. If you want to revise your choices, you can go back and add it to your regimen.</p>
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
                        		<p>Adding a SPF product to your regimen is an excellent way to perfect your look.</p>
                        	</div>
                        </div><!-- Product Label And Number End -->
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
    
              <% if (!nospf) { %>
                  <% prodmainimage = ((ProductVO)spfpr).getImage() ;  %>
                  <%
                      starrating1 = "" ;
                      starrating1 = (String)((ProductVO)spfpr).getShadeImage() ;
                      String crev = "" ;
                      if (starrating1 !=null && starrating1.length() > 0)
                             crev = "yes" ;
                  %>
                        
                        	<div class="product-thumb-image" productId='<%=((ProductVO)spfpr).getProdId()%>' brandId='<%=((ProductVO)spfpr).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating1 !=null && starrating1.length() > 0) { %><img src="images/<%=starrating1%>" /> <% } %></div>
                            <div class="product-name"><strong><%=((ProductVO)spfpr).getProductLinePrime()%></strong>&nbsp;<%=((ProductVO)spfpr).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(spfpr.getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio" id="Radio1"><input type="radio" name="spfCheck" onClick="changePrice()"/></span>
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
<input type="hidden" name="spfexists" value="<%=spfexists%>">
<input type="hidden" name="singlebrandflag" value="">
<input type=hidden name=scrname2 value="regimendetailpage">
 <input type="hidden" name="spfprod" value="">
 <input type="hidden" name="spfqty" value="">
<input type="hidden" name="cleansePrice" value="<%=cleansePrice%>">
<input type="hidden" name="moisturizePrice" value="<%=moisturizePrice%>">
<input type="hidden" name="treatPrice" value="<%=treatPrice%>">
<input type="hidden" name="etreatPrice" value="<%=etreatPrice%>">
<input type="hidden" name="spfPrice" value="<%=spfPrice%>">
<input type=hidden name=scrname value="">
<input type="hidden" name="spfCheck1" value="">
<input type="hidden" name="subTotal" value="<%=pprice%>">  
<input type="hidden" name="skinMethodToCall"> 
<input type="hidden" name="nocleanse">
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