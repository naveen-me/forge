<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import ="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations"%>
<%
	String str = "";
	
	String nonoff = "" ;
	String scrname = "" ;
	String scrname2 = "" ;
	String customerId = "" ;
	String prodId = "" ;
	String prodLine = "" ;
    String prodLineFlag = "" ;
	String mid = "" ;
    String mode = "" ;
    String catid = "" ;
	String cusid = "" ;
	String prodmainimage = "" ;
	String prodmainprice = "" ;
	int prodmainpercen = 0 ;
    CustomerProfileVO cp = new CustomerProfileVO() ;
    String skintype = "" ;
    String skinconcern = "" ;
    String skincondition = "" ;
    String natural = "" ;
    String natural_1 = "" ;
    String natural_2 = "" ;
    String thattext = "" ;
    String naturaltext = "" ;
    boolean eflag = false ;
    String bnamestring = "" ;
    boolean bnameflag = false ;
    ArrayList skinconcerns = new ArrayList() ;
    ArrayList skinconditions = new ArrayList() ;
    String eyeconcern = "" ;
    ArrayList eyeconcerns = new ArrayList() ;
    ArrayList concerns = new ArrayList() ;
    ArrayList allergies = new ArrayList() ;
    Hashtable brandids = new Hashtable() ;
     
	DecimalFormat ft    = new DecimalFormat();	
	//ft.applyLocalizedPattern("#0.00");   	
	ft.setMinimumFractionDigits(2);
    ft.setMaximumFractionDigits(2);	
          

    ArrayList prodlist = new ArrayList() ;
	ArrayList cleanse = new ArrayList();
	ArrayList tone = new ArrayList();
	ArrayList treat = new ArrayList();
	ArrayList etreat = new ArrayList();
	ArrayList moisturize = new ArrayList();
	ArrayList nightmoisturize = new ArrayList();
	ArrayList exfoliant = new ArrayList();
    ArrayList spf = new ArrayList() ;
    Hashtable fbrand = new Hashtable() ;
    ArrayList newprofiles2 = new ArrayList() ;
    String cleanseprod = "" ;
    String moisturizeprod = "" ;
    String treatprod = "" ;
    String etreatprod = "" ;
    String cleanseskip = "" ;
    String moisturizeskip = "" ;
    String treatskip = "" ;
    String etreatskip = "" ;
    Hashtable prodhash = new Hashtable() ;
    String cleansebrandflag = "" ;
    String moisturizebrandflag = "" ;
    String treatbrandflag = "" ;
    String etreatbrandflag = "" ;
 
    Hashtable master = new Hashtable() ;
    String starrating = "" ;
    Hashtable newhta = new Hashtable() ;
    ArrayList newprofiles = new ArrayList() ;
    Hashtable newratings = new Hashtable() ;
    String[] newprodid = new String[4] ;
    ArrayList bnames = new ArrayList() ;
    
		 try
		 {


                customerId = (String) request.getAttribute("customerId") ;							
                mid = (String) request.getAttribute("mid") ;							
                mode = (String) request.getAttribute("mode") ;							
                catid = (String) request.getAttribute("catid") ;							
                scrname = (String) request.getAttribute("scrname") ;							
                scrname2 = (String) request.getAttribute("scrname2") ;							
                prodLine = (String) request.getAttribute("prodline");
                bnamestring = (String) request.getAttribute("bnamestring") ;
                
                System.out.println("scrname 2 "+scrname2) ;
			    prodlist = (ArrayList) request.getAttribute("productlist") ;
                master = (Hashtable) request.getAttribute("masterData") ;
                cp = (CustomerProfileVO) request.getAttribute("customerprofile") ;
                
                cleansebrandflag = (String)request.getAttribute("cleansebrandflag") ;	
                moisturizebrandflag = (String)request.getAttribute("moisturizebrandflag") ;	
                treatbrandflag = (String)request.getAttribute("treatbrandflag") ;	
                etreatbrandflag = (String)request.getAttribute("etreatbrandflag") ;	

                  
                skintype = (String) master.get(cp.getSkinType()) ;
                skinconcerns = (ArrayList) cp.getSkinConcernList() ;
                eyeconcerns = (ArrayList) cp.getEyeConcernList() ;
                skinconditions = (ArrayList) cp.getSkinConditionList() ;
                allergies = (ArrayList) cp.getAllergyList() ;
                natural = (String) cp.getNatural() ;
                skinconcern = "" ;
                for (int i=0; i < skinconcerns.size() ; i++) 
                {
                    concerns.add(skinconcerns.get(i)) ;
                }
                for (int i=0; i < skinconditions.size() ; i++) 
                {
                    concerns.add(skinconditions.get(i)) ;
                }
                
              if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999"))
              {
                for (int i=0; i < concerns.size() ; i++) 
                {
                    String ctext = "" ;
                    ctext = "<strong>"+(String) master.get(concerns.get(i))+"</strong>" ;
                    if (i == 0)
                       skinconcern = skinconcern+ctext ;
                    else if (i == concerns.size()-1)
                    {
                       if (!((String)eyeconcerns.get(0)).equals("E999"))
                          skinconcern = skinconcern+", "+ctext ;
                       else
                          skinconcern = skinconcern+" and "+ctext ;
                    }
                    else
                       skinconcern = skinconcern+", "+ctext ;
                }
               }
               if (eyeconcerns.size() > 0 &&!((String)eyeconcerns.get(0)).equals("E999"))
               {
                for (int i=0; i < eyeconcerns.size() ; i++) 
                {
                    
                    String ctext1 = "" ;
                    if (!((String) concerns.get(0)).equals("C999"))
                     ctext1 = " and eye concerns of " ;
                    else
                     ctext1 = " eye concerns of " ;
                    String ctext = "" ;
                    ctext = "<strong>"+(String) master.get(eyeconcerns.get(i))+"</strong>" ;
                    if (i == 0)
                       skinconcern = skinconcern+ctext1+ctext ;
                    else if (i == eyeconcerns.size()-1)
                    {
                          skinconcern = skinconcern+" and "+ctext ;
                    }
                    else
                       skinconcern = skinconcern+", "+ctext ;
                }
               }

               if (concerns.size() > 0 && ((String)concerns.get(0)).equals("C999") && !((String)eyeconcerns.get(0)).equals("E999"))
               {
                 //System.out.println("1111sff");
                 thattext =  " that address " ;
               }
               else if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999") && ((String)eyeconcerns.get(0)).equals("E999"))
               {
                 //System.out.println("2222sff");
                 thattext =  " that address " ;
               }
               else if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999") && !((String)eyeconcerns.get(0)).equals("E999"))
               {
                 //System.out.println("333sff");
                 thattext =  " that address " ;
               }
               else 
               {
                 //System.out.println("4444sff");
                 thattext =  " " ;
               } 

                if (natural.equals("A008"))
                {
                   //System.out.println("I am in if natural") ;
                   if (allergies.size() > 0 &&  !((String)allergies.get(0)).equals("L999"))
                   {
                      //System.out.println("I am in if natural yes allergies") ;
                      naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients" ;
                      for (int i=0; i < allergies.size() ; i++) 
                      {
                    
                         String ctext = "" ;
                         ctext = "<strong>"+(String) master.get(allergies.get(i))+"</strong>" ;
                         if (i == 0)
                         {
                           if (allergies.size() > 1)
                              naturaltext = naturaltext+" and that do not contain "+ctext ;
                           else
                              naturaltext = naturaltext+" and that do not contain "+ctext+"." ;
                         }
                         else if (i == allergies.size()-1)
                         {
                          naturaltext = naturaltext+" or "+ctext+"." ;
                         }
                         else
                          naturaltext = naturaltext+", "+ctext+"" ;
                      }
                   }
                   else
                   {
                      //System.out.println("I am in if natural no allergies") ;
                      naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients." ;
                   }
                }
                else
                {  
                   //System.out.println("I am in else natural") ;
                   naturaltext = "" ;
                   if (allergies.size() > 0 && !((String)allergies.get(0)).equals("L999"))
                   {
                      //System.out.println("I am in else natural yes allergies") ;
                      naturaltext = "Your results also include products that do not contain " ;
                      for (int i=0; i < allergies.size() ; i++) 
                      {
                    
                         String ctext = "" ;
                         ctext = "<strong>"+(String) master.get(allergies.get(i))+"</strong>" ;
                         if (i == 0)
                           if (allergies.size() > 1)
                             naturaltext = naturaltext+ctext ;
                           else
                             naturaltext = naturaltext+ctext+"." ;
                         else if (i == allergies.size()-1)
                         {
                          naturaltext = naturaltext+" and "+ctext+"." ;
                         }
                         else
                          naturaltext = naturaltext+", "+ctext+"" ;
                      }
                   }
                   else
                   {
                      //System.out.println("I am in else natural no allergies") ;
                      naturaltext = "" ;
                   }
                }

                cleanse = (ArrayList) prodlist.get(0) ;
                tone =    (ArrayList) prodlist.get(1) ;           
                exfoliant =    (ArrayList) prodlist.get(2) ;           
                moisturize =    (ArrayList) prodlist.get(3) ;           
                treat =    (ArrayList) prodlist.get(5) ;           
                etreat =    (ArrayList) prodlist.get(6) ;           
                fbrand =    (Hashtable) prodlist.get(8) ;           
                System.out.println("fbrand size "+fbrand.size()) ;
                
                
                cleanseprod = (String) request.getAttribute("cleanseprod") ;
                moisturizeprod = (String) request.getAttribute("moisturizeprod") ;
                treatprod = (String) request.getAttribute("treatprod") ;
                etreatprod = (String) request.getAttribute("etreatprod") ;
				 
                cleanseskip = (String) request.getAttribute("cleanseskip") ;
                moisturizeskip = (String) request.getAttribute("moisturizeskip") ;
                treatskip = (String) request.getAttribute("treatskip") ;
                etreatskip = (String) request.getAttribute("etreatskip") ;

                ArrayList retulist = new ArrayList() ;
                retulist = (ArrayList) request.getAttribute("retuList") ;
                System.out.println("retur list size "+retulist.size()) ;
                
                newhta = (Hashtable) retulist.get(0) ;
                newprofiles = (ArrayList) retulist.get(1) ;
                newratings = (Hashtable) retulist.get(2) ;
                         System.out.println("New profiles size "+newprofiles.size()) ;
                         
                bnames = (ArrayList) request.getAttribute("brandnames") ;
                if (bnames !=null && bnames.size() > 0)
                	bnameflag = true ;
                
			}catch(Exception ex1)
			{
			  ex1.printStackTrace() ;
			}
%>			
<%! String checkChecked(String val1, String val2, String val3)
{
   System.out.println("val1 "+val1+"  val2 "+val2+"  val3  "+val3) ;
   String retu = "" ;
   if (!val3.equals("skinanalysisbrand") && !val3.equals("skinanalysisnobrand"))
   {
       
       if (val1 != null && val2 != null)
	    if ( val1.equals(val2) )
	      retu = "checked" ;
   }
   return retu ;
}
%>
<head>
<title>SkinCare Single Brand Page</title>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/header.css" />
<link rel="stylesheet" href="css/footer.css" />
<link rel="stylesheet" href="css/questioner.css" />
<link rel="stylesheet" href="css/uniform.default.css" />

<script type="text/javascript" src="js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="js/popup.js"></script>
<script type="text/javascript" src="js/jquery.bpopup-0.7.0.min.js"></script>
<script language="javascript" src='js/jquery.uniform.js' ></script>

<script type="text/javascript">
	$(function(){
		$('div#questions-content > ul > li:even').addClass("odd");
		$('div#questions-content > ul > li:odd').addClass("even");
		$(".breadcrumb ul li:last-child a").css({"background-image":"none"});		
	});
</script>
<!--[if gte IE 9]>
<link rel="stylesheet" type="text/css" href="css/ie9.css" />
<![endif]-->
<script  type="text/javascript">
function checkPageHeight()
{
 var ht = document.documentElement.scrollHeight ;
// alert(ht);
  // parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
   parent.location = "http://BeautyT1b.dstest.drugstore.com/partners/sv/recommender.asp?catid=12873#"+ht ;

}
</script>
<link rel="stylesheet" href="css/pop-up.css" />
<link rel="stylesheet" href="css/hair-product.css" />



<script type="text/javascript">

$(window).load(function(){
	$("input").uniform();
	 var $target=$("#pop-up-outer"),
         $ingredpopupouter = $('#ingred-pop-up-outer'),
         $aboutthebrandpopupouter = $('#about-the-brand-pop-up-outer');
		$(".breadcrumb ul li:last-child a").css({"background-image":"none"});
		/* ============== Drop Down List Start ======================= */

		$(".dropdown-content ul li:last-child a").css({"border-bottom":"none"});
		$('.see-recommendation-dropdown').hover(function() {
		  var $self = $('.dropdown-content', this);
		      $self.stop(true, true);
		       $self.hide().slideDown(500);
			},
			function() {
				var $self = $('.dropdown-content', this);
					$self.stop(true, true);
					$self.slideUp(500);
		  });	

	  /* ============== Drop Down List End   ======================= */

		/* ============== Product Thumb Image Vertical Align Middle ============== */
		$('div.product-thumb-image').each(function(){
			var $div = $(this),
			    $divh = $div.height(),
			    $img = $div.find("img"),
			    $imgh = $img.height(),
			    resh = $divh - $imgh;
			//console.log(resh);
			$img.css('margin-top', + resh / 2 + "px");
		});
		/* ============== Product Thumb Image Vertical Align Middle ============== */

		/* ============== Recommendation Details Alternet Color's ============== */
			$(".recommendation-running-content").each(function() {
				$(this).find('ul > li:even').css({background:"#e2e2e2"});
				$(this).find('ul > li:odd').css({background:"#FBFBFB"});
		    });
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
   $(".product-thumb-image img").click(function(e){
			
		    var productId=$(this).closest('div').attr('productId'),
		        brandId=$(this).closest('div').attr('brandId'),
		        $rating=$(this).closest('div > .product-thumb-image').next('.product-rating'),
		        $name=$rating.next('.product-name'),
		        $price=$name.next('.product-price');
		    $('.product-pop-thumb-image img').attr('src',$(this).attr('src'));
			   
		    $target.find('#Tab1 div.tab-running-content-outer').html("<iframe frameborder='0' src='productDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>");
		    $target.find('#Tab2 div.tab-running-content-outer').html("<iframe frameborder='0'src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=y&glos=n'></iframe>");
		    $target.find('#Tab4 div.tab-running-content-outer').html("<iframe frameborder='0'src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=y&ingred=n&glos=n'></iframe>");
		    $target.find('#Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+brandId+"'></iframe>");
		    $target.find('#Tab6 div.tab-running-content-outer').html("<iframe frameborder='0' src='productanalysis.do?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
		    $target.find('#Tab3 div.tab-running-content-outer').html("<iframe frameborder='0' src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y'></iframe>");
	  
		
			$target.find('.product-name-label').html('<img src="images/trans.gif" width="270" height="74" />');
			$target.find('.product-rating').html($rating.html());
			$target.find('.product-titel-des').html('<span>'+$name.text()+'</span>');
			$target.find('.product-price').text($price.text());
	  
			$target.find('#Tab1').css("display","block").siblings().css("display","none");
			$target.find('.tab-nav li a').removeClass('active');
			$target.find('.tab-nav li:first-child a').addClass('active');	
	  
			
			centerPopup($target,e);
			loadPopup($target);
		});
   $(".product-name").click(function(e){
	     
		var pnameImage = $(this).parent('.product-image').find('.product-thumb-image > img');
		var pimage = $(this).parent('.product-image').find('.product-thumb-image'),
		     $rating=pimage.siblings('.product-rating'),
	        $name=pimage.siblings('.product-name'),
	        $price=pimage.siblings('.product-price'),
			productId=pimage.attr('productId'),
	        brandId=pimage.attr('brandId');
		//	console.log(productId) ;
		
		$('.product-pop-thumb-image img').attr('src',pnameImage.attr('src'));
		

		 
	    $target.find('#Tab1 div.tab-running-content-outer').html("<iframe frameborder='0' src='productDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>");
	    $target.find('#Tab2 div.tab-running-content-outer').html("<iframe frameborder='0'src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=y&glos=n'></iframe>");
	    $target.find('#Tab4 div.tab-running-content-outer').html("<iframe frameborder='0'src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=y&ingred=n&glos=n'></iframe>");
	    $target.find('#Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+brandId+"'></iframe>");
	    $target.find('#Tab6 div.tab-running-content-outer').html("<iframe frameborder='0' src='productanalysis.do?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
	    $target.find('#Tab3 div.tab-running-content-outer').html("<iframe frameborder='0' src='productDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y'></iframe>");

	    $target.find('.product-name-label').html('<img src="images/trans.gif" width="270" height="74" />');
		$target.find('.product-rating').html($rating.html());
		$target.find('.product-titel-des').html('<span>'+$name.text()+'</span>');
		$target.find('.product-price').text($price.text());
		$target.find('#Tab1').css("display","block").siblings().css("display","none");
		$target.find('.tab-nav li a').removeClass('active');
		$target.find('.tab-nav li:first-child a').addClass('active');	
		
		centerPopup($target,e);
		loadPopup($target);
	});
		   
       $(".bClose").click(function(e){
		   e.preventDefault();
			disablePopup($target);
	    });
	/* ============== Pop up Tab Java Script End   ================== */

	/* ============== Select Product Selected =============== */
		$("input:radio").on('click',function(){
			$(this).closest('.product-image').addClass('fill-color').siblings().removeClass('fill-color');
		});
		
	/* ============== Select Product Selected =============== */
	
	/* ============== SEE INGREDIENT BENEFITS Pop up Start =============== */
		$('a.Benefits').on('click',function(e){
			e.preventDefault();
			$ingredpopupouter.find('.tab-nav li:first-child a').addClass('active');	
			var productId=$(this).attr('productId');
			
			    $ingredpopupouter.find('#ingredient').css({display:"block"});
			    $ingredpopupouter.find('#ingredient div.tab-running-content-outer').html("<iframe frameborder='0' src='IngredientBenefits.jsp?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
			    centerPopup($ingredpopupouter,e);
				loadPopup($ingredpopupouter);	
			
		});
		  $(".bClose1").click(function(e){
			   e.preventDefault();
				disablePopup($ingredpopupouter);
		    });	
		/* ============== SEE INGREDIENT BENEFITS Pop up End =============== */
		
	/* ============== TEll Me About Brand Pop up Start =============== */
	  $('.tell-me-about-this-brand > a').on('click',function(e){
		  e.preventDefault();
		  $ingredpopupouter.find('.tab-nav li:first-child a').addClass('active');	
		  $aboutthebrandpopupouter.find('#about-Tab5').css({display:"block"});
		  $aboutthebrandpopupouter.find('#about-Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+<%= prodLine%>+"'></iframe>");
		  centerPopup( $aboutthebrandpopupouter,e);
		  loadPopup( $aboutthebrandpopupouter);

	  });	
	  $(".bClose2").click(function(e){
		   e.preventDefault();
			disablePopup( $aboutthebrandpopupouter);
	    });	
	 /* ============== TEll Me About Brand Pop up ENd =============== */	

	  /* ============== SEE select Product Start =============== */
			$('input:radio').each(function(){
	    <% if (!scrname2.equals("skinanalysisnobrand") && !scrname2.equals("skinanalysisbrand")) { %>			
        	 var $remove = $(this).closest('.product-image'),
			    test=$remove.find("input:hidden").attr('name');
			
			if(test === 'cleansebrand'){
				document.pdet1.cleanseprod.value = '<%=cleanseprod%>' ;
			}
			if(test === 'moisturizebrand'){
				document.pdet1.moisturizeprod.value = '<%=moisturizeprod%>';
			}
			if(test === 'treatbrand'){
				document.pdet1.treatprod.value = '<%=treatprod%>';
			}
			if(test === 'etreatbrand'){
				document.pdet1.etreatprod.value = '<%=etreatprod%>';
			}
        <% } %>
		
		if($(this).is(':checked'))
       	 $(this).closest('.product-image').addClass('fill-color').siblings().removeClass('fill-color');
       
		});
     /* ============== SEE select Product End =============== */

	});

</script>

<script type="text/javascript">
function submitInfo() {
	
	         var frm= document.forms["pdet1"];
	  //       console.log(frm.cleanseprod.value) ;
	    	 frm.action = "<%=response.encodeURL("regimendetail.do")%>" ;
	         frm.submit() ;
	    
	 	
		//document.pdet.submit();
	     
	}

function submitInfoOld() {
	
	      bflag = false ;
		  var i=0;
		  strcom="You have not selected a complete regimen. Please select a product ";
     	 //alert("fffff") ;
	     var frm= document.forms["pdet1"];
	    //alert("fffffff rrrrrrrrrr4444444rrrr "+frm.action) ;
	     
	     if (frm.cleanseSelect.checked != null && frm.cleanseSelect.checked != undefined)
	     {
	      //alert("111") ;
	       if (frm.cleanseSelect.checked)
	       {
	          //alert("111 b ") ;
	          
	         bflag = true ;
	         // alert('at flag');
		     frm.cleanseprod.value="" ;
		     
	       }
		   else
		   {
	       //alert("111 c") ;

			 if(frm.cleanseSelect.length)
		     {
		     // alert("111 d") ;

		     for(i=0; i < frm.cleanseSelect.length ; i++)
		     {
		       if (frm.cleanseSelect[i].checked)
		       {
		         bflag = true ;
		         break ;
		       }
		      
		     }
		     }else
		     {
		    	// alert('at else');
		       if(frm.cleanseSelect.checked)
		         bflag = true;
		     }
		     if (!bflag)
			  strcom +="<br /> for <strong>Step 1: cleanse</strong> or check the <strong>Skip this step </strong>box";
		   } 
		 }else
			 frm.cleanseSkip.value="cleanseSkip";


	     if (frm.moisturizeSelect.checked != null && frm.moisturizeSelect.checked != undefined)
	     {
	       bflag = false ;
	       if (frm.moisturizeSelect.checked)
	       {
	         bflag = true ;
		     frm.moisturizeprod.value="" ;
		   }
		   else
		   {
		   	 if(frm.moisturizeSelect.length)
		     {
		     for(i=0; i < frm.moisturizeSelect.length ; i++)
		     {
		       if (frm.moisturizeSelect[i].checked)
		       {
		         bflag = true ;
		         break ;
		       }
		     }
		     }else
		     {
		       if(frm.moisturizeSelect.checked)
		         bflag = true;
		     }
			 if (!bflag)
				strcom+=" <br /> for <strong>Step 2: moisturize</strong> or check the <strong>Skip this step</strong> box";
		   }
		 }else
			 frm.moisturizeSkip.value="moisturizeSkip";

	     if (frm.treatSelect.checked != null && frm.treatSelect.checked != undefined)
	     {
	       bflag = false ;
	       if (frm.treatSelect.checked)
	       {
	         bflag = true ;
		     frm.treatprod.value="" ;
		   }
		   else
		   {
		   	 if(frm.treatSelect.length)
		     {
		     for(i=0; i < frm.treatSelect.length ; i++)
		     {
		       if (frm.treatSelect[i].checked)
		       {
		         bflag = true ;
		         break ;
		       }
		     }
		     }else
		     {
		       if(frm.treatSelect.checked)
		         bflag = true;
		     }
		     if(!bflag)
				strcom+="<br /> for <strong>Step 3: Treat</strong> or check the <strong>Skip this step</strong> box";
		   }
		 }else
			 frm.treatSkip.value="treatSkip";

	     if (frm.etreatSelect.checked != null && frm.etreatSelect.checked != undefined)
	     {
	       bflag = false ;
	       if (frm.etreatSelect.checked)
	       {
	         bflag = true ;
		     frm.etreatprod.value="" ;
		   }
		   else
		   {
		   	 if(frm.etreatSelect.length)
		     {
		     for(i=0; i < frm.etreatSelect.length ; i++)
		     {
		       if (frm.etreatSelect[i].checked)
		       {
		         bflag = true ;
		         break ;
		       }
		     }
		     }else
		     {
		       if(frm.etreatSelect.checked)
		         bflag = true;
		     }
		     if (!bflag)
				 strcom+="<br /> for <strong>Step 4: Eye</strong> or check the <strong>Skip this step</strong> box";
		   }
		 }else
			 frm.etreatSkip.value="etreatSkip";
	     frm.prodLine.value = "" ;
	     frm.prodLineFlag.value = "" ;


		 if(strcom.length>66)
		 {
		// alert('vijaya222222222');
		   stylealert(strcom);
		 }
	     else
	     {
	  	 //alert('vijaya1');
	    	 frm.action = "<%=response.encodeURL("regimendetail.do")%>" ;
	         frm.submit() ;
	     }
	 	
	 	
	//document.pdet1.submit();
	     
	}

function stylealert(alertMsg, source)
{        
    document.getElementById('newalert').style.position = 'absolute';
    document.getElementById('newalert').style.left = document.getElementById('submitImg').offsetLeft - 350;
    document.getElementById('newalert').style.top = document.getElementById('submitImg').offsetTop - 250;
    document.getElementById('newalert').style.zIndex = 3;
    document.getElementById('newalert').style.visibility = 'visible';    
    
    if(isNN)
    {
	  document.newalert.visibility = "show";
	  document.newalert.top=document.newalert.offsetTop - 200;
	  document.newalert.left = document.newalert.offsetLeft - 200;
    }else if(isFF || isOP)
    {
      document.getElementById('newalert').style.top = "1100px";
      document.getElementById('newalert').style.left = "400px";
      document.getElementById('newalert').style.visibility = 'visible';    
    }
      document.getElementById('alertMsg').innerHTML = alertMsg;
}
function closeAlert()
{
    document.getElementById('newalert').style.visibility = 'hidden';
}

function mandatoryfields(){

  cleanse = false ;
}
function skipThis(id)
{
  if(id=="cleanse")
  {
  	 if(document.pdet1.cleanseSelect.length)
       for(i=0; i<document.pdet1.cleanseSelect.length; i++)
		  document.pdet1.cleanseSelect[i].checked=0;
	 else
	    document.pdet1.cleanseSelect.checked=0;
  }
  if (id == "moisturize")
  {
	if(document.pdet1.moisturizeSelect.length)
     for(i=0; i<document.pdet1.moisturizeSelect.length; i++)
		document.pdet1.moisturizeSelect[i].checked=0;
	else
	  document.pdet1.moisturizeSelect.checked=0;

  }
  if(id=="treat")
  {
    if(document.pdet1.treatSelect.length)
     for(i=0; i<document.pdet1.treatSelect.length; i++)
		document.pdet1.treatSelect[i].checked=0;
	else
	  document.pdet1.treatSelect.checked=0;
  }
  if(id=="etreat")
  {
    if(document.pdet1.etreatSelect.length)
     for(i=0; i<document.pdet1.etreatSelect.length; i++)
		document.pdet1.etreatSelect[i].checked=0;
	else
	  document.pdet1.etreatSelect.checked=0;
  }
}
function selectProd(id,val)
{
	//console.log(val) ;
    if (id == "cleanse")
    {
    	document.pdet1.cleanseSelect.checked=1;
		document.pdet1.cleanseprod.value=val ;
	}
    if (id == "moisturize")
    {
        document.pdet1.moisturizeSelect.checked=1;
		document.pdet1.moisturizeprod.value=val ;
    }
    if (id == "treat")
    {
        document.pdet1.treatSelect.checked=1;
     	document.pdet1.treatprod.value=val ;
	}
    if (id == "etreat")
    {
       	document.pdet1.etreatSelect.checked=1;
   		document.pdet1.etreatprod.value=val ;
	}
  /*  if (id == "shampoo")
       if (!document.pdet.shampooSkip.checked)
       {
			document.pdet.shampooprod.value=val ;
	   }
    if (id == "conditioner")
       if (!document.pdet.conditionerSkip.checked)
       {
			document.pdet.conditionerprod.value=val ;
	   }
    if (id == "htreat")
       if (!document.pdet.htreatSkip.checked)
       {
			document.pdet.htreatprod.value=val ;
	   }
    if (id == "styling")
       if (!document.pdet.stylingSkip.checked)
       {
			document.pdet.stylingprod.value=val ;
	   }
	   */
}


function selectThis(id) {

	document.pdet1.prodLine.value = id;
	document.pdet1.prodLineFlag.value = "yes" ;
	document.pdet1.cleanseprod.value = "" ;
	document.pdet1.moisturizeprod.value = "" ;
	document.pdet1.treatprod.value = "" ;
	document.pdet1.etreatprod.value = "" ;
	
	document.pdet1.action = "<%=response.encodeURL("regimendetail.do")%>" ;
	document.pdet1.submit();
}

function submitBack() {

	document.pdet1.skinMethodToCall.value = "get";
	if(document.pdet1.mode.value == "skin"){
		document.pdet1.action = "<%=response.encodeURL("customerselfprofileskinconcerns.do")%>" ;
	}
	else if(document.pdet1.mode.value == "skinNew"){
		document.pdet1.action = "<%=response.encodeURL("customerselfprofileNewskinconcerns.do")%>" ;
	}
	document.pdet1.submit();
}
function submitBack1(){
	document.pdet1.action="<%=response.encodeURL("productregimen.do")%>" ;
	document.pdet1.submit();
}
function submitNoBrand()
{
  document.pdet1.target = "" ;
  document.pdet1.action = "<%=response.encodeURL("regimendetail.do")%>" ;
  document.pdet1.submit() ;

}

</script>
</head>

<!--  <body onLoad="javascript:checkPageHeight();">  -->
<body>

<!-- Wrapper Start -->
<div id="wrapper">
	<!-- Container Start -->
    <div id="container">
    	
        <!-- Banner Image Start -->
        <div id="banner-image-outer">
        	<img src="images/BCOM-banner-skin.jpg" alt="" />
        </div><!-- Banner Image End -->
 




  <form name="pdet1" method="post" action="">
  
        <!-- Steps Images Outer Start -->
        <div id="steps-images-outer">
        	<div class="step-1-image skin-step-2-1"><a href="javascript:submitBack()"></a></div>
            <div class="step-2-image skin-step-2-2"></div>
            <div class="step-3-image skin-step-2-3"></div>
        </div><!-- Steps Images Outer End -->

        <!-- Body Content Outer Start -->
        <div id="body-running-content-outer" style="margin-top:-20px;">

            <!-- Running Content Start -->
	        <!-- Running Content End -->
         
                <!-- Product Outer Start -->
                <div id="product-outer"> 
                <!-- Brand Name And Tell Me About This Brand Outer -->
                    <div id="brand-name-tell-me-about-outer">
                    	
                        <!-- Brand Name Start -->
                        <div class="brand-name"><%=master.get(prodLine)%></div><!-- Brand Name End -->
                        
                        <!-- Tell Me About This Brand Start -->
                        <div class="tell-me-about-this-brand"><a href="#">TELL ME ABOUT THIS BRAND</a></div><!-- Tell Me About This Brand End -->
                        
                    </div><!-- Brand Name And Tell Me About This Brand End -->
                    <!-- Go Back To All Recommendation And See Recommendation Outer Start -->
                	<div id="go-back-to-all-and-see-recommendation-outer">
	                    <!-- Go Back To All Recommendation Start -->
	                    <div id="go-back-to-all"><a href="javascript:submitBack1()">&nbsp;<span>GO BACK TO ALL RECOMMENDATIONS</span></a></div><!-- Go Back To All Recommendation End -->
                           
                    <!-- See Recommendations Outer Start -->
                    <div id="see-recommendation" style="margin-top:14px;">
                    	<span class="see-recommendtaion-label">SEE RECOMMENDATION BY BRAND:</span>
	                       <div class="see-recommendation-dropdown">
	                        	<span>SELECT A BRAND</span>
	                            <div class="down-arrow"><a href="#"></a></div>
	                            <div class="dropdown-content" style="display:none;">
	                        	<ul>
	                        		<%
	                        		    if (bnameflag)
	                        			for (int i = 0; i < bnames.size(); i++) {
	                        				String bn = (String) bnames.get(i);
	                        				String bd = bn.substring(0, 4);
	                        				String bna = bn.substring(4, bn.length());
	                        		%>
	                        		<li><a href="javascript:selectThis('<%=bd%>')" id="<%=bd%>"><%=bna%></a></li>
	                        		<input type="hidden" name="brandname" value="<%= bnames.get(i)%>">
	                        		<!-- <li><%=bnames%></li> -->
	                        	<%}%>		
	                        	 <%
	                        	      if (!bnameflag && bnamestring != null && bnamestring.length() > 0)
	                        	      {
	                        	    	  StringTokenizer stt = new StringTokenizer(bnamestring,"|") ;
	                        	          while (stt.hasMoreTokens())
	                        	          {
	                        	          	String bn = stt.nextToken() ;
	                        	          	String bd = bn.substring(0, 4);
	                        				String bna = bn.substring(4, bn.length());
	                        		  %>
	                        		   <li><a href="javascript:selectThis('<%=bd%>')" id="<%=bd%>"><%=bna%></a></li>
	                        		  <%
	                        	          }
	                        	      }
	                        	  %>
	                        	</ul>
	                        </div>
	                        </div>
                    </div><!-- See Recommendations Outer End -->
                    </div><!-- Go Back To All Recommendation And See Recommendation Outer End -->
                    <!-- Product Image Row 1 Outer Start -->
                    <div class="product-images-outer" style="margin-top:3px;">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image skin-one"></div>
                        	<% if (!cleansebrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                        
          <%
                                  	int prodlength = 4;
                                  	if (cleanse.size() > 4)
                                  		prodlength = 4;
                                  	else
                                  		prodlength = cleanse.size();

                                  	for (int i = 0; i < prodlength; i++) {
                                  		prodmainimage = ((ProductVO) (cleanse.get(i))).getImage();
                                  		brandids.put((String) ((ProductVO) (cleanse.get(i)))
                                  				.getProductLine(), "Y");
                                  		starrating = "";
                                  		starrating = ((ProductVO) (cleanse.get(i))).getShadeImage();
                                  		String crev = "";
                                  		if (starrating != null && starrating.length() > 0)
                                  			crev = "yes";
                                  %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="cleansebrand" />
                        	<div class="product-thumb-image"  productId='<%=((ProductVO) (cleanse.get(i))).getProdId()%>' brandId='<%=((ProductVO) (cleanse.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" /> <% } %></div>
                            <div class="product-name"><strong><%=master.get(((ProductVO)(cleanse.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (cleanse.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO) (cleanse.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="cleanseSelect" value="<%=((ProductVO) (cleanse.get(i))).getProdId()%>" onClick="javascript:selectProd('cleanse','<%=((ProductVO) (cleanse.get(i))).getProdId()%>')" 
                            	<%=checkChecked(cleanseprod, ((ProductVO) (cleanse.get(i))).getProdId(), scrname2)%>/></span>
                                <span class="select-label">SELECT</span>
                            </div>
                        </div><!-- Product Inner Image 1 End -->
           <%
           	}
           %>  
                        

                        <!-- Recommendation Details Start -->
                        <div class="recommendation-header">
                        	<div class="collapse"></div>
                            <div class="heading-label"><span>CLOSE</span> RECOMMENDATION DETAILS</div>
                        </div>
                        <div class="recommendation-running-content">
                        	<ul>
                        	                        	 <%  
                        	    
                        	     newprofiles2 = new ArrayList() ;
                                 if  (prodlength > 0) 
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		       newprodid[0] = ((ProductVO)(cleanse.get(0))).getProdId() ; 
                        		       String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[0].trim() ;
                        		       String ratingsval = (String) newratings.get(ratingskey) ;
                        		       
                        		       if (ratingsval == null || ratingsval.length() == 0 || ratingsval == "null")
                        		    	   ratingsval = "No Val" ;
                        		//       System.out.println(" 555555555 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		       if (ratingsval != null && ratingsval.length() > 0 && ratingsval != "null")
                        		       {
                        		       
                        		         System.out.println(" 4444444444 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		         newprofiles2.add(newprofiles.get(k)) ;
                        		       }
                        		       else
                        		       {
                        		    	System.out.println(" 22222222 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		    	//newprofiles.remove((String)newprofiles.get(k)) ;
                        		    	eflag = true ;
                        		       }
                        		 //   if (eflag)
                        		 //   	break ;
                        	     // } 
                        	     }%>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  		            
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(cleanse.get(i))).getProdId() ;
                     		       String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[i].trim() ;
                     		       String ratingsval = (String) newratings.get(ratingskey) ;
                    		       if (ratingsval == null || ratingsval.length() == 0 || ratingsval == "null")
                    		    	   ratingsval = "" ;
                        	 %>
                        		    <div class="recommendation-result"><%=ratingsval%></div>
                        	<%  }
                            %>
                                    
                            <% }  %>
                                </li>
                              <li>
                                <div class="recommendation-result-titel"></div>
                            <%for(int i=0;i<prodlength;i++){
                             %>	
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(cleanse.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                           
                        	</ul>
                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 1 Outer End -->

                    <!-- Product Image Row 2 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image skin-two"></div>
                        	<% if (!moisturizebrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        	
                        </div><!-- Product Label And Number End -->
                         <%
                         	prodlength = 4;
                         	if (moisturize.size() > 4)
                         		prodlength = 4;
                         	else
                         		prodlength = moisturize.size();
                         	for (int i = 0; i < prodlength; i++) {
                         		prodmainimage = ((ProductVO) (moisturize.get(i))).getImage();
                         		brandids.put((String) ((ProductVO) (moisturize.get(i)))
                         				.getProductLine(), "Y");
                         		starrating = "";
                         		starrating = ((ProductVO) (moisturize.get(i))).getShadeImage();
                         		String crev = "";
                         		if (starrating != null && starrating.length() > 0)
                         			crev = "yes";
                         %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="moisturizebrand" />
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (moisturize.get(i))).getProdId()%>' brandId='<%=((ProductVO) (moisturize.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" /> <% } %></div>
                            <div class="product-name"><strong><%=master.get(((ProductVO)(moisturize.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (moisturize.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO) (moisturize.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="moisturizeSelect" value="<%=((ProductVO) (moisturize.get(i))).getProdId()%>" onClick="javascript:selectProd('moisturize','<%=((ProductVO) (moisturize.get(i))).getProdId()%>')" <%=checkChecked(moisturizeprod,
						((ProductVO) (moisturize.get(i))).getProdId(), scrname2)%>/></span>
                                <span class="select-label">SELECT</span>
                            </div>
                        </div><!-- Product Inner Image 1 End -->
           <%
           	}
           %>  
                        <!-- Recommendation Details Start -->
                        <div class="recommendation-header">
                        	<div class="expand"></div>
                            <div class="heading-label"><span>VIEW</span> RECOMMENDATION DETAILS</div>
                        </div>
                        <div class="recommendation-running-content" style="display:none;">
                        	<ul>
                                  <%           newprofiles2 = new ArrayList() ;
                                 if  (prodlength > 0) 
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(moisturize.get(0))).getProdId() ; 
                        		    String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[0].trim() ;
                        		    String ratingsval = (String) newratings.get(ratingskey) ;
                        		    if (ratingsval != null && ratingsval.length() > 0 && ratingsval != "null")
                        		    {
                        		       
                        		       System.out.println("ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		       newprofiles2.add(newprofiles.get(k)) ;
                        		    }
                        		    else
                        		    {
                        		    	System.out.println(" 22222222 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		    	//newprofiles.remove((String)newprofiles.get(k)) ;
                        		    	eflag = true ;
                        		    }
                        		 //   if (eflag)
                        		 //   	break ;
                        	     // } 
                        	     }%>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(moisturize.get(i))).getProdId() ; 
                      		       String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[i].trim() ;
                      		       String ratingsval = (String) newratings.get(ratingskey) ;
                     		       if (ratingsval == null || ratingsval.length() == 0 || ratingsval == "null")
                     		    	   ratingsval = "" ;
                              %>
                        		    <div class="recommendation-result"><%=ratingsval%></div>
                        	<%  }
                            %>
                                    
                            <% }  %>
                                </li>
                              <li>
                                <div class="recommendation-result-titel"></div>
                            <%for(int i=0;i<prodlength;i++){
                             %>	
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(moisturize.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                           
                        	</ul>
                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 2 Outer End -->

                    <!-- Product Image Row 3 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image skin-three"></div>
                        	<% if (!treatbrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        	
                        </div><!-- Product Label And Number End -->
                        <%
                        	prodlength = 4;
                        	if (treat.size() > 4)
                        		prodlength = 4;
                        	else
                        		prodlength = treat.size();

                        	for (int i = 0; i < prodlength; i++) {
                        		prodmainimage = ((ProductVO) (treat.get(i))).getImage();
                        		brandids.put(
                        				(String) ((ProductVO) (treat.get(i))).getProductLine(),
                        				"Y");
                        		starrating = "";
                        		starrating = ((ProductVO) (treat.get(i))).getShadeImage();
                        		String crev = "";
                        		if (starrating != null && starrating.length() > 0)
                        			crev = "yes";
                        %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="treatbrand" />
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (treat.get(i))).getProdId()%>' brandId='<%=((ProductVO) (treat.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" /> <% } %></div>
                            <div class="product-name"><strong><%=master.get(((ProductVO)(treat.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (treat.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO) (treat.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="treatSelect" value="<%=((ProductVO) (treat.get(i))).getProdId()%>" onClick="javascript:selectProd('treat','<%=((ProductVO) (treat.get(i))).getProdId()%>')" <%=checkChecked(treatprod,
						((ProductVO) (treat.get(i))).getProdId(), scrname2)%>/></span>
                                <span class="select-label">SELECT</span>
                            </div>
                        </div><!-- Product Inner Image 1 End -->
           <%
           	}
           %>  
                        <!-- Recommendation Details Start -->
                        <div class="recommendation-header">
                        	<div class="expand"></div>
                            <div class="heading-label"><span>VIEW</span> RECOMMENDATION DETAILS</div>
                        </div>
                        <div class="recommendation-running-content" style="display:none;">
                        	<ul>
                                  <%           newprofiles2 = new ArrayList() ;     
                                  if  ( prodlength > 0) 
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(treat.get(0))).getProdId() ; 
                        		    String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[0].trim() ;
                        		    String ratingsval = (String) newratings.get(ratingskey) ;
                        		    if (ratingsval != null && ratingsval.length() > 0 && ratingsval != "null")
                        		    {
                        		       
                        		       System.out.println("ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		       newprofiles2.add(newprofiles.get(k)) ;
                        		    }
                        		    else
                        		    {
                        		    	System.out.println(" 22222222 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		    	//newprofiles.remove((String)newprofiles.get(k)) ;
                        		    	eflag = true ;
                        		    }
                        		 //   if (eflag)
                        		 //   	break ;
                        	    //  } 
                        	     }%>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(treat.get(i))).getProdId() ; 
                      		       String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[i].trim() ;
                      		       String ratingsval = (String) newratings.get(ratingskey) ;
                     		       if (ratingsval == null || ratingsval.length() == 0 || ratingsval == "null")
                     		    	   ratingsval = "" ;
                              %>
                        		    <div class="recommendation-result"><%=ratingsval%></div>
                        	<%  }
                            %>
                                    
                            <% }  %>
                                </li>
                              <li>
                                <div class="recommendation-result-titel"></div>
                            <%for(int i=0;i<prodlength;i++){
                             %>	
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(treat.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                           
                        	</ul>
                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 3 Outer End -->

                    <!-- Product Image Row 4 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image skin-fore"></div>
                        	<% if (!etreatbrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                        <%
                        	prodlength = 4;
                        	if (etreat.size() > 4)
                        		prodlength = 4;
                        	else
                        		prodlength = etreat.size();

                        	for (int i = 0; i < prodlength; i++) {
                        		prodmainimage = ((ProductVO) (etreat.get(i))).getImage();
                        		brandids.put((String) ((ProductVO) (etreat.get(i)))
                        				.getProductLine(), "Y");
                        		starrating = "";
                        		starrating = ((ProductVO) (etreat.get(i))).getShadeImage();
                        		String crev = "";
                        		if (starrating != null && starrating.length() > 0)
                        			crev = "yes";
                        %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="etreatbrand" />
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (etreat.get(i))).getProdId()%>' brandId='<%=((ProductVO) (etreat.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" /> <% } %></div>
                            <div class="product-name"><strong><%=master.get(((ProductVO)(etreat.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (etreat.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO) (etreat.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="etreatSelect" value="<%=((ProductVO) (etreat.get(i))).getProdId()%>" onClick="javascript:selectProd('etreat','<%=((ProductVO) (etreat.get(i))).getProdId()%>')" <%=checkChecked(etreatprod,
						((ProductVO) (etreat.get(i))).getProdId(), scrname2)%>/></span>
                                <span class="select-label">SELECT</span>
                            </div>
                        </div><!-- Product Inner Image 1 End -->
           <%
           	}
           %>                          <!-- Recommendation Details Start -->
                        <div class="recommendation-header">
                        	<div class="expand"></div>
                            <div class="heading-label"><span>VIEW</span> RECOMMENDATION DETAILS</div>
                        </div>
                        <div class="recommendation-running-content" style="display:none;">
                        	<ul>
                                  <%          newprofiles2 = new ArrayList() ; 
                                  if  ( prodlength > 0) 
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(etreat.get(0))).getProdId() ; 
                        		    String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[0].trim() ;
                        		    String ratingsval = (String) newratings.get(ratingskey) ;
                        		    if (ratingsval != null && ratingsval.length() > 0 && ratingsval != "null")
                        		    {
                        		       
                        		       System.out.println("ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		       newprofiles2.add(newprofiles.get(k)) ;
                        		    }
                        		    else
                        		    {
                        		    	System.out.println(" 22222222 ratings val "+ratingsval+"   ratings key "+ratingskey) ;
                        		    	//newprofiles.remove((String)newprofiles.get(k)) ;
                        		    	eflag = true ;
                        		    }
                        		  //  if (eflag)
                        		  //  	break ;
                        	     // } 
                        	     }%>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(etreat.get(i))).getProdId() ; 
                      		       String ratingskey = ((String)newprofiles.get(k)).trim()+newprodid[i].trim() ;
                      		       String ratingsval = (String) newratings.get(ratingskey) ;
                     		       if (ratingsval == null || ratingsval.length() == 0 || ratingsval == "null")
                     		    	   ratingsval = "" ;
                        	  %>
                        		    <div class="recommendation-result"><%=ratingsval%></div>
                        	<%  }
                            %>
                                    
                            <% }  %>
                                </li>
                              <li>
                                <div class="recommendation-result-titel"></div>
                            <%for(int i=0;i<prodlength;i++){
                             %>	
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(etreat.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                           
                        	</ul>
                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 4 Outer End -->


                </div><!-- Product Outer End -->

                <!-- Edit Review Button Start -->
                <div id="edit-review-button-outer">
                	<div class="edit-button"><a href="javascript:submitBack()">&nbsp;</a></div>
                    
                    <div class="review-button"><a href="#" onClick="javascript:submitInfo();"></a></div> 
                </div><!-- Edit Review Button End -->

            <!-- Please Note Start -->
            <div id="please-note">
            	<strong>PLEASE NOTE:</strong> Information on this site is not meant to be a substitute for the advice from a medical professional. Prior to starting a new regimen, you should consult your own physician, particularly if you are currently using any medications, are pregnant or nursing. Test a small amount of each new product on a hidden area (Such as behind your ear) to make sure that your skin does not have any adverse reactions. Always follow the recommended application quantity and frequency for each product.
            </div><!-- Please Note End -->
<input type=hidden name="customerId" value='<%=customerId%>'>
<input type=hidden name="cusid" value='<%=customerId%>'>
<input type=hidden name="mid" value='<%=mid%>'>
<input type=hidden name="catid" value="<%=catid%>">
<input type=hidden name="mode" value="<%=mode%>">
<input type=hidden name="prodLineFlag" value="<%=prodLineFlag%>">
<input type=hidden name=prodLine value="<%=prodLine%>">
<input type=hidden name=scrname value="skinanalysis">
<input type=hidden name=scrname2 value="skinanalysisbrand">
<input type="hidden" name="cleanseprod" value="">
<input type="hidden" name="moisturizeprod" value="">
<input type="hidden" name="treatprod" value="">
<input type="hidden" name="etreatprod" value="">
<input type="hidden" name="skinMethodToCall">
<input type="hidden" name="bnamestring" value="<%=bnamestring %>">
</form>

</div><!-- Body Content Outer End-->
        
    </div><!-- Container End -->
  
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
        	<!-- Tab 1 Content Start  -->
        	<div id="Tab1" class="ProductContent" style="display:block;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 1 Content End  -->
            
            <!-- Tab 2 Content Start  -->
            <div id="Tab2" class="ProductContent" style="display:none;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 2 Content End  -->
            
            <!-- Tab 3 Content Start  -->
            <div id="Tab3" class="ProductContent" style="display:none;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 3 Content End  -->
            
            <!-- Tab 4 Content Start  -->
            <div id="Tab4" class="ProductContent" style="display:none;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 4 Content End  -->
            
            
            <!-- Tab 5 Content Start  -->
            <div id="Tab5" class="ProductContent" style="display:none;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 5 Content End  -->
            
            <!-- Tab 6 Content Start  -->
            <div id="Tab6" class="ProductContent" style="display:none;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 6 Content End  -->
            
        </div><!-- Product Tab Content Outer End -->


    </div><!-- Product Tab Outer End -->

	</div><!-- Pop Up End -->
	
	
	
	
	
	
	<!-- INGREDIENT BENEFITS Pop Up Start -->
	<div id="ingred-pop-up-outer" style="display:none;">
	<!-- Close Button Start -->
    <div class="pop-up-close-button"><a href="#" class="bClose1"></a></div><!-- Close Button End -->

    <!-- Product Tab Outer Start -->
    <div class="product-tab-outer">

    	<!-- Tab Nav Start -->
        <div class="tab-nav">
        	<ul>
                <li style="width:165px;"><a href="#" class="active">INGREDIENT BENEFITS</a></li>
        	</ul>
        </div><!-- Tab Nav End-->

        <!-- Product Tab Content Outer Start -->
        <div class="product-tab-content-outer">
        	<!-- Tab 1 Content Start  -->
        	<div class="ProductContent" id="ingredient" style="display:block;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 1 Content End  -->
            
        </div><!-- Product Tab Content Outer End -->


    </div><!-- Product Tab Outer End -->

	</div><!-- INGREDIENT BENEFITS Pop Up End -->	
	
	
	<!-- About The Brand Pop Up Start -->
	<div id="about-the-brand-pop-up-outer" style="display:none;">
	<!-- Close Button Start -->
    <div class="pop-up-close-button"><a href="#" class="bClose2"></a></div><!-- Close Button End -->
    
    <!-- Product Tab Outer Start -->
    <div class="product-tab-outer">
    
    	<!-- Tab Nav Start -->
        <div class="tab-nav">
        	<ul>
                <li style="width:147px;"><a href="#" class="active" rel="about-Tab5">ABOUT THE BRAND &nbsp;</a></li>
        	</ul>
        </div><!-- Tab Nav End-->
    	        <!-- Product Tab Content Outer Start -->
        <div class="product-tab-content-outer">
            <!-- Tab 5 Content Start  -->
            <div id="about-Tab5" class="ProductContent">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 5 Content End  -->
        </div><!-- Product Tab Content Outer End -->
    
    
    </div><!-- Product Tab Outer End -->
	
	</div><!-- About The Brand Pop Up End -->
	
<!-- INGREDIENT BENEFITS Pop Up Start -->
	<div id="ingred-pop-up-outer" style="display:none;">
	<!-- Close Button Start -->
    <div class="pop-up-close-button"><a href="#" class="bClose"></a></div><!-- Close Button End -->

    <!-- Product Tab Outer Start -->
    <div class="product-tab-outer">

    	<!-- Tab Nav Start -->
        <div class="tab-nav">
        	<ul>
                <li style="width:160px;"><a href="#" class="active">INGREDIENT BENEFITS</a></li>
        	</ul>
        </div><!-- Tab Nav End-->

        <!-- Product Tab Content Outer Start -->
        <div class="product-tab-content-outer">
        	<!-- Tab 1 Content Start  -->
        	<div class="ProductContent" id="ingredient" style="display:block;">
                <div class="tab-running-content-outer"></div>
            </div><!-- Tab 1 Content End  -->
            
        </div><!-- Product Tab Content Outer End -->


    </div><!-- Product Tab Outer End -->

	</div><!-- INGREDIENT BENEFITS Pop Up End -->
	


    
</div><!-- Wrapper End -->
</body>
</html>