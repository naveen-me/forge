
<%@ page import ="java.util.*,java.io.*,java.text.DecimalFormat,com.svb.common.valueobjects.*,com.svb.common.ConstantDeclarations"%>
<%
	String str = "";

	String nonoff = "";
	String scrname = "";
	String scrname2 = "";
	String customerId = "";
	String prodId = "";
	String prodLine = "";
	String prodLineName = "" ;
	String prodLineFlag = "";
	String mid = "";
	String mode = "";
	String catid = "";
	String cusid = "";
	String prodmainimage = "";
	String prodmainprice = "";
	int prodmainpercen = 0;
	CustomerProfileVO cp = new CustomerProfileVO();
	String haircolor = "";
	String hairtype = "";
	String hairthickness = "";
	String hairtexture = "";
	String hairconcern = "";
	String natural = "";
	String natural_1 = "";
	String natural_2 = "";
	String atext = "";
	String thattext = "";
	String concerntext = "";
	String styletext = "";
	String naturaltext = "";
	boolean eflag = false ;
	String bnamestring = "" ;
    boolean bnameflag = false ;
    
    String shampoobrandflag = "" ;
    String conditionerbrandflag = "" ;
    String htreatbrandflag = "" ;
    String stylingbrandflag = "" ;
    
	ArrayList hairprocess = new ArrayList();
	ArrayList hairconditions = new ArrayList();
	ArrayList hairstyles = new ArrayList();
	ArrayList concerns = new ArrayList();
	ArrayList allergies = new ArrayList();
	Hashtable brandids = new Hashtable();

	DecimalFormat ft = new DecimalFormat();
	//ft.applyLocalizedPattern("#0.00");   	
	ft.setMinimumFractionDigits(2);
	ft.setMaximumFractionDigits(2);

	ArrayList prodlist = new ArrayList();
	ArrayList shampoo = new ArrayList();
	ArrayList htreat = new ArrayList();
	ArrayList styling = new ArrayList();
	ArrayList conditioner = new ArrayList();
	Hashtable fbrand = new Hashtable();
	ArrayList newprofiles2 = new ArrayList() ;
	String shampooprod = "";
	String conditionerprod = "";
	String htreatprod = "";
	String stylingprod = "";
	String shampooskip = "";

	String conditionerskip = "";
	String htreatskip = "";
	String stylingskip = "";
	Hashtable prodhash = new Hashtable();

	Hashtable master = new Hashtable();
	String starrating = "";
	Hashtable newhta = new Hashtable() ;
    ArrayList newprofiles = new ArrayList() ;
    Hashtable newratings = new Hashtable() ;
    String[] newprodid = new String[4] ;
    
    ArrayList bnames = new ArrayList() ;

	try {

		customerId = (String) request.getAttribute("customerId");
		mid = (String) request.getAttribute("mid");
		mode = (String) request.getAttribute("mode");
		catid = (String) request.getAttribute("catid");
		scrname = (String) request.getAttribute("scrname");
		scrname2 = (String) request.getAttribute("scrname2");
		prodLine = (String) request.getAttribute("prodline");
		bnamestring = (String) request.getAttribute("bnamestring");
        prodLineName = (String) request.getAttribute("prodLineName") ;		

        shampoobrandflag = (String)request.getAttribute("shampoobrandflag") ;	
        conditionerbrandflag = (String)request.getAttribute("conditionerbrandflag") ;	
        htreatbrandflag = (String)request.getAttribute("htreatbrandflag") ;	
        stylingbrandflag = (String)request.getAttribute("stylingbrandflag") ;	
        
        System.out.println("brand flags "+shampoobrandflag+"  "+conditionerbrandflag+"  "+htreatbrandflag+"  "+stylingbrandflag) ;
		prodlist = (ArrayList) request.getAttribute("productlist");
		master = (Hashtable) request.getAttribute("masterData");
		cp = (CustomerProfileVO) request
				.getAttribute("customerprofile");

		haircolor = (String) master.get(cp.getHaircolor());
		hairtype = (String) master.get(cp.getHairtype());
		hairthickness = (String) master.get(cp.getHairthickness());
		hairtexture = (String) master.get(cp.getHairtexture());
		hairprocess = (ArrayList) cp.getHairprocess();
		hairstyles = (ArrayList) cp.getHairstyles();
		hairconditions = (ArrayList) cp.getHairconditions();
		allergies = (ArrayList) cp.getAllergyList();
		natural = (String) cp.getNatural();
	
		//String brandnames=(String)request.getAttribute("bna");
		if (hairtexture.equals("average"))
			atext = "an";
		else
			atext = "a";
		if (hairprocess.size() > 0
				&& !hairprocess.get(0).equals("H999"))
			haircolor = haircolor + ", chemically treated";
		for (int i = 0; i < hairconditions.size(); i++) {
			concerns.add(hairconditions.get(i));
		}

		/*       for (int i=0; i < hairstyles.size() ; i++) 
		       {
		           concerns.add(hairstyles.get(i)) ;
		       }
		       for (int i=0; i < hairprocess.size() ; i++) 
		       {
		           concerns.add(hairprocess.get(i)) ;
		       }
		 */
		if (concerns.size() > 0
				&& !((String) concerns.get(0)).equals("R999")) {
			for (int i = 0; i < concerns.size(); i++) {
				String ctext = "";
				ctext = "<strong>"
						+ (String) master.get(concerns.get(i))
						+ "</strong>";
				if (i == 0)
					hairconcern = hairconcern + ctext;
				else if (i == concerns.size() - 1) {
					//if (!((String)hairstyles.get(0)).equals("E999"))
					//  hairconcern = hairconcern+", "+ctext ;
					//else
					hairconcern = hairconcern + " and " + ctext;
				} else
					hairconcern = hairconcern + ", " + ctext;
			}
		}

		if (hairstyles.size() > 0
				&& !((String) hairstyles.get(0)).equals("S999")) {
			for (int i = 0; i < hairstyles.size(); i++) {

				String ctext1 = "";
				ctext1 = " as well as styling results that include ";
				String ctext = "";
				ctext = "<strong>"
						+ (String) master.get(hairstyles.get(i))
						+ "</strong>";
				if (i == 0)
					styletext = styletext + ctext1 + ctext;
				else if (i == hairstyles.size() - 1) {
					styletext = styletext + " and " + ctext;
				} else
					styletext = styletext + ", " + ctext;
			}
		}

		if (concerns.size() > 0
				&& !((String) concerns.get(0)).equals("R999")) {
			//System.out.println("1111sff");
			concerntext = " The products selected address concerns of  ";
		}
		thattext = " that is <strong>" + hairthickness
				+ "</strong> in density and generally has " + atext
				+ " <strong>" + hairtexture + "</strong> texture.";
		if (natural.equals("A008")) {
			//System.out.println("I am in if natural") ;
			if (allergies.size() > 0
					&& !((String) allergies.get(0)).equals("L999")) {
				//System.out.println("I am in if natural yes allergies") ;
				naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients";
				for (int i = 0; i < allergies.size(); i++) {

					String ctext = "";
					ctext = "<strong>"
							+ (String) master.get(allergies.get(i))
							+ "</strong>";
					if (i == 0) {
						if (allergies.size() > 1)
							naturaltext = naturaltext
									+ " and that do not contain "
									+ ctext;
						else
							naturaltext = naturaltext
									+ " and that do not contain "
									+ ctext + ".";
					} else if (i == allergies.size() - 1) {
						naturaltext = naturaltext + " or " + ctext
								+ ".";
					} else
						naturaltext = naturaltext + ", " + ctext + "";
				}
			} else {
				//System.out.println("I am in if natural no allergies") ;
				naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients.";
			}
		} else {
			//System.out.println("I am in else natural") ;
			naturaltext = "";
			if (allergies.size() > 0
					&& !((String) allergies.get(0)).equals("L999")) {
				//System.out.println("I am in else natural yes allergies") ;
				naturaltext = "Your results also include products that do not contain ";
				for (int i = 0; i < allergies.size(); i++) {

					String ctext = "";
					ctext = "<strong>"
							+ (String) master.get(allergies.get(i))
							+ "</strong>";
					if (i == 0)
						if (allergies.size() > 1)
							naturaltext = naturaltext + ctext;
						else
							naturaltext = naturaltext + ctext + ".";
					else if (i == allergies.size() - 1) {
						naturaltext = naturaltext + " and " + ctext
								+ ".";
					} else
						naturaltext = naturaltext + ", " + ctext + "";
				}
			} else {
				//System.out.println("I am in else natural no allergies") ;
				naturaltext = "";
			}
		}

		if (prodlist.size() > 0)
		{
		 shampoo = (ArrayList) prodlist.get(0);
		 conditioner = (ArrayList) prodlist.get(1);
		 htreat = (ArrayList) prodlist.get(2);
		 styling = (ArrayList) prodlist.get(3);
		 fbrand = (Hashtable) prodlist.get(5);
		}
		
		shampooprod = (String) request.getAttribute("shampooprod");
		conditionerprod = (String) request
				.getAttribute("conditionerprod");
		htreatprod = (String) request.getAttribute("htreatprod");
		stylingprod = (String) request.getAttribute("stylingprod");

		shampooskip = (String) request.getAttribute("shampooskip");
		conditionerskip = (String) request
				.getAttribute("conditionerskip");
		htreatskip = (String) request.getAttribute("htreatskip");
		stylingskip = (String) request.getAttribute("stylingskip");
		
		ArrayList retulist = new ArrayList() ;
        retulist = (ArrayList) request.getAttribute("retuList") ;
        System.out.println("retur list size "+retulist.size()) ;
        
        newhta = (Hashtable) retulist.get(0) ;
        newprofiles = (ArrayList) retulist.get(1) ;
        newratings = (Hashtable) retulist.get(2) ;
        
        System.out.println(" 222222222  shampoo prod " + shampooprod+"condition prod " + conditionerprod+"treat prod " + htreatprod) ;
        
        bnames = (ArrayList) request.getAttribute("brandnames") ;
        
        if (bnames != null && bnames.size() > 0)
        	bnameflag = true ;
        
	} catch (Exception ex1) {
		ex1.printStackTrace();
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/header.css" />
<link rel="stylesheet" href="css/footer.css" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
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
<div id="wrapper">
	<!-- Container Start -->
    <div id="container">
    	
        <!-- Banner Image Start -->
        <div id="banner-image-outer">
        	<img src="images/BCOM-banner-hair.jpg" alt="" />
        </div><!-- Banner Image End -->
<link rel="stylesheet" href="css/pop-up.css" />
<link rel="stylesheet" href="css/hair-product.css" />
<link rel="stylesheet" href="css/uniform.default.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Hair Care Single Brand Recommendations</title>
<script type="text/javascript" src="js/jquery.bpopup-0.7.0.min.js"></script>
<script type="text/javascript" src="js/jquery.uniform.js"></script>
<script type="text/javascript" src="js/popup.js"></script>
<script language="javascript" src='js/jquery.tinyscrollbar.min.js' ></script>

<script type="text/javascript">
$(window).load(function(){
	$("input").uniform();
			 var $target=$("#pop-up-outer"),
	         $ingredpopupouter = $('#ingred-pop-up-outer'),
	         $aboutthebrandpopupouter = $('#about-the-brand-pop-up-outer');
		$(".breadcrumb ul li:last-child a").css({"background-image":"none"});
		
		/* ============== Drop Down List Start ======================= */
		$(".dropdown-content ul li:last-child a").css({"border-bottom":"none"});
		
		$('.see-recommendation-dropdown').hover(
		function() {
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
			   
		    $target.find('#Tab1 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>");
		    $target.find('#Tab2 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=y&glos=n'></iframe>");
		    $target.find('#Tab4 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=y&ingred=n&glos=n'></iframe>");
		    $target.find('#Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+brandId+"'></iframe>");
		    $target.find('#Tab6 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairproductanalysis.do?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
		    $target.find('#Tab3 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y'></iframe>");
		
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
		
 
		 
	    $target.find('#Tab1 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>");
	    $target.find('#Tab2 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=y&glos=n'></iframe>");
	    $target.find('#Tab4 div.tab-running-content-outer').html("<iframe frameborder='0'src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=y&ingred=n&glos=n'></iframe>");
	    $target.find('#Tab5 div.tab-running-content-outer').html("<iframe frameborder='0' src='brandDisc.do?brandparam="+brandId+"'></iframe>");
	    $target.find('#Tab6 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairproductanalysis.do?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
	    $target.find('#Tab3 div.tab-running-content-outer').html("<iframe frameborder='0' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=n&direc=n&ingred=n&glos=n&revw=y'></iframe>");
 
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
			    $ingredpopupouter.find('#ingredient div.tab-running-content-outer').html("<iframe frameborder='0' src='HairIngredientBenefits.jsp?customerId="+<%=customerId%>+"&productId="+productId+"'></iframe>");
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
		  $aboutthebrandpopupouter.find('.tab-nav li:first-child a').addClass('active');	
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
			
			
			if(test === 'shampoobrand'){
				document.pdet1.shampooprod.value = '<%=shampooprod%>' ;
				
				
			}
			if(test === 'conditionerbrand'){
				document.pdet1.conditionerprod.value = '<%=conditionerprod%>';
		
			}
			if(test === 'htreatbrand'){
				document.pdet1.htreatprod.value = '<%=htreatprod%>';
	
			}
			if(test === 'stylingbrand'){
				document.pdet1.stylingprod.value = '<%=stylingprod%>';

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
   	 frm.action = "<%=response.encodeURL("hairregimendetail.do")%>" ;
    frm.submit() ;


//document.pdet.submit();

}
function submitInfoOld() {
	   
    bflag = false ;
	  var i=0;
	  strcom="You have not selected a complete regimen. Please select a product ";
	  
   var frm= document.forms["pdet1"];
   
   if (frm.shampooSelect.checked != null && frm.shampooSelect.checked != undefined)
   {
	    //alert('value of:'+frm.shampooSelect.checked);
     if (frm.shampooSelect.checked)
     {
       bflag = true ;
	     frm.shampooprod.value="" ;
	     //alert('shampoo val'+frm.shampooprod.value);  
     }
	   else
	   {
		    //alert('at else');  
		 if(frm.shampooSelect.length)
	     {
	    	 
	     for(i=0; i < frm.shampooSelect.length ; i++)
	     {
	       if (frm.shampooSelect[i].checked)
	       {
	         bflag = true ;
	        // alert('shampoo prods'+frm.shampooSelect.length());
	         break ;
	       }
	      
	     }
	     }else
	     {
	    	 //alert('at else1');
	       if(frm.shampooSelect.checked)
	         bflag = true;
	     }
	     if (!bflag)
		  strcom +="<br /> for <strong>Step 1: Shampoo</strong> or check the <strong>Skip this step </strong>box";
	   } 
	 }else
		 alert('at else2');
		 frm.shampooSkip.value="shampooSkip";


   if (frm.conditionerSelect.checked != null && frm.conditionerSelect.checked != undefined)
   {
     bflag = false ;
     if (frm.conditionerSelect.checked)
     {
       bflag = true ;
	     frm.conditionerprod.value="" ;
	   }
	   else
	   {
	   	 if(frm.conditionerSelect.length)
	     {
	     for(i=0; i < frm.conditionerSelect.length ; i++)
	     {
	       if (frm.conditionerSelect[i].checked)
	       {
	         bflag = true ;
	         break ;
	       }
	     }
	     }else
	     {
	       if(frm.conditionerSelect.checked)
	         bflag = true;
	     }
		 if (!bflag)
			strcom+=" <br /> for <strong>Step 2: Conditioner</strong> or check the <strong>Skip this step</strong> box";
	   }
	 }else
		 frm.conditionerSkip.value="conditionerSkip";

   
   if (frm.htreatSelect.checked != null && frm.htreatSelect.checked != undefined)
   {
     bflag = false ;
     if (frm.htreatSelect.checked)
     {
       bflag = true ;
	     frm.htreatprod.value="" ;
	   }
	   else
	   {
	   	 if(frm.htreatSelect.length)
	     {
	     for(i=0; i < frm.htreatSelect.length ; i++)
	     {
	       if (frm.htreatSelect[i].checked)
	       {
	         bflag = true ;
	         break ;
	       }
	     }
	     }else
	     {
	       if(frm.htreatSelect.checked)
	         bflag = true;
	     }
	     if(!bflag)
			strcom+="<br /> for <strong>Step 3: Treat</strong> or check the <strong>Skip this step</strong> box";
	   }
	 }else
		 frm.htreatSkip.value="htreatSkip";

   
   if (frm.stylingSelect.checked != null && frm.stylingSelect.checked != undefined)
   {
     bflag = false ;
     if (frm.stylingSelect.checked)
     {
       bflag = true ;
	     frm.stylingprod.value="" ;
	   }
	   else
	   {
	   	 if(frm.stylingSelect.length)
	     {
	     for(i=0; i < frm.stylingSelect.length ; i++)
	     {
	       if (frm.stylingSelect[i].checked)
	       {
	         bflag = true ;
	         break ;
	       }
	     }
	     }else
	     {
	       if(frm.stylingSelect.checked)
	         bflag = true;
	     }
	     if (!bflag)
			 strcom+="<br /> for <strong>Step 4: Styling</strong> or check the <strong>Skip this step</strong> box";
	   }
	 }else
		 frm.stylingSkip.value="stylingSkip";
   frm.prodLine.value = "" ;
   frm.prodLineFlag.value = "" ;


	 if(strcom.length>66)
	 {
	   stylealert(strcom);
	 }
   else
   {
  	 
   frm.action = "<%=response.encodeURL("hairregimendetail.do")%>" ;
    frm.submit() ;
   }
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

  shampoo = false ;
}
function skipThis(id)
{
  if(id=="shampoo")
  {
  	 if(document.pdet1.shampooSelect.length)
       for(i=0; i<document.pdet1.shampooSelect.length; i++)
		  document.pdet1.shampooSelect[i].checked=0;
	 else
	    document.pdet1.shampooSelect.checked=0;
  }
  if (id == "conditioner")
  {
	if(document.pdet1.conditionerSelect.length)
     for(i=0; i<document.pdet1.conditionerSelect.length; i++)
		document.pdet1.conditionerSelect[i].checked=0;
	else
	  document.pdet1.conditionerSelect.checked=0;

  }
  if(id=="htreat")
  {
    if(document.pdet1.htreatSelect.length)
     for(i=0; i<document.pdet1.htreatSelect.length; i++)
		document.pdet1.htreatSelect[i].checked=0;
	else
	  document.pdet1.htreatSelect.checked=0;
  }
  if(id=="styling")
  {
    if(document.pdet1.stylingSelect.length)
     for(i=0; i<document.pdet1.stylingSelect.length; i++)
		document.pdet1.stylingSelect[i].checked=0;
	else
	  document.pdet1.stylingSelect.checked=0;
  }
}
function selectProd(id,val)
{
    if (id == "shampoo")
    {
    	document.pdet1.shampooSelect.checked=1;
		document.pdet1.shampooprod.value=val ;
	}
    if (id == "conditioner")
    {
        document.pdet1.conditionerSelect.checked=1;
		document.pdet1.conditionerprod.value=val ;
    }
    if (id == "htreat")
    {
        document.pdet1.htreatSelect.checked=1;
     	document.pdet1.htreatprod.value=val ;
	}
    if (id == "styling")
    {
       	document.pdet1.stylingSelect.checked=1;
   		document.pdet1.stylingprod.value=val ;
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
	document.pdet1.shampooprod.value = "" ;
	document.pdet1.conditionerprod.value = "" ;
	document.pdet1.htreatprod.value = "" ;
	document.pdet1.stylingprod.value = "" ;
	
	document.pdet1.action = "<%=response.encodeURL("hairregimendetail.do")%>" ;
	document.pdet1.submit();
}

function submitBack() {

	document.pdet1.skinMethodToCall.value = "get";
	document.pdet1.action = "<%=response.encodeURL("customerselfprofilehairconcerns.do")%>" ;
	document.pdet1.submit();
}
function submitBack1(){
	document.pdet1.action="<%=response.encodeURL("hairregimendetail.do")%>" ;
	document.pdet1.submit();
}
function submitNoBrand()
{
  document.pdet1.target = "" ;
  document.pdet1.action = "<%=response.encodeURL("hairproductregimen.do")%>" ;
  document.pdet1.submit() ;

}
//  End -->
</script>




  <form name="pdet1" method="post" action="">
  
        <!-- Steps Images Outer Start -->
        <div id="steps-images-outer">
        	<div class="step-1-image step-2-1"><a href="javascript:submitBack()"></a></div>
            <div class="step-2-image step-2-2"></div>
            <div class="step-3-image step-2-3"></div>
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
	                    <div id="go-back-to-all"><a href="javascript:submitNoBrand()"><span>GO BACK TO ALL RECOMMENDATIONS</span></a></div><!-- Go Back To All Recommendation End -->
  
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
                        	<div class="product-number-image one"></div>
                        	<% if (!shampoobrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                        
          <%
                                  	int prodlength = 4;
                                  	if (shampoo.size() > 4)
                                  		prodlength = 4;
                                  	else
                                  		prodlength = shampoo.size();

                                  	for (int i = 0; i < prodlength; i++) {
                                  		prodmainimage = ((ProductVO) (shampoo.get(i))).getImage();
                                  		brandids.put((String) ((ProductVO) (shampoo.get(i)))
                                  				.getProductLine(), "Y");
                                  		starrating = "";
                                  		starrating = ((ProductVO) (shampoo.get(i))).getShadeImage();
                                  		String crev = "";
                                  		if (starrating != null && starrating.length() > 0)
                                  			crev = "yes";
                                  %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                           <input type="hidden" name="shampoobrand" />
                        	<div class="product-thumb-image"  productId='<%=((ProductVO) (shampoo.get(i))).getProdId()%>' brandId='<%=((ProductVO) (shampoo.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                            <div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" />  <% } %></div>
                            <div class="product-name" productId='<%=((ProductVO) (shampoo.get(i))).getProdId()%>' brandId='<%=((ProductVO) (shampoo.get(i))).getProductLine()%>'><strong><%=master.get(((ProductVO)(shampoo.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (shampoo.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO)(shampoo.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="shampooSelect" value="<%=((ProductVO) (shampoo.get(i))).getProdId()%>" onClick="javascript:selectProd('shampoo','<%=((ProductVO) (shampoo.get(i))).getProdId()%>')" <%=checkChecked(shampooprod,
						((ProductVO) (shampoo.get(i))).getProdId(),scrname2)%>/></span>
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
                        	     if (prodlength > 0)
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	    //   for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(shampoo.get(0))).getProdId() ; 
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
                        	      } 
                        	    // }
                        	     %>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(shampoo.get(i))).getProdId() ; 
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
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(shampoo.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
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
                        	<div class="product-number-image two"></div>
                        	<% if (!conditionerbrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                         <%
                         	prodlength = 4;
                         	if (conditioner.size() > 4)
                         		prodlength = 4;
                         	else
                         		prodlength = conditioner.size();
                         	for (int i = 0; i < prodlength; i++) {
                         		prodmainimage = ((ProductVO) (conditioner.get(i))).getImage();
                         		brandids.put((String) ((ProductVO) (conditioner.get(i)))
                         				.getProductLine(), "Y");
                         		starrating = "";
                         		starrating = ((ProductVO) (conditioner.get(i))).getShadeImage();
                         		String crev = "";
                         		if (starrating != null && starrating.length() > 0)
                         			crev = "yes";
                         %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                           <input type="hidden" name="conditionerbrand" />
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (conditioner.get(i))).getProdId()%>' brandId='<%=((ProductVO) (conditioner.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" />  <% } %></div>
                            <div class="product-name" productId='<%=((ProductVO) (conditioner.get(i))).getProdId()%>' brandId='<%=((ProductVO) (conditioner.get(i))).getProductLine()%>'><strong><%=master.get(((ProductVO)(conditioner.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (conditioner.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO)(conditioner.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="conditionerSelect" value="<%=((ProductVO) (conditioner.get(i))).getProdId()%>" onClick="javascript:selectProd('conditioner','<%=((ProductVO) (conditioner.get(i))).getProdId()%>')" <%=checkChecked(conditionerprod,
						((ProductVO) (conditioner.get(i))).getProdId(),scrname2)%>/></span>
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
                        	<%  
                        	 newprofiles2 = new ArrayList() ;
                        	if (prodlength > 0)
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(conditioner.get(0))).getProdId() ; 
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
                        		//    	break ;
                        	      } 
                        	   //  }
                        	     %>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(conditioner.get(i))).getProdId() ; 
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
                             
                                    <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(conditioner.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                        	</ul>                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 2 Outer End -->

                    <!-- Product Image Row 3 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image three"></div>
                        	<% if (!htreatbrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                        <%
                        	prodlength = 4;
                        	if (htreat.size() > 4)
                        		prodlength = 4;
                        	else
                        		prodlength = htreat.size();

                        	for (int i = 0; i < prodlength; i++) {
                        		prodmainimage = ((ProductVO) (htreat.get(i))).getImage();
                        		brandids.put(
                        				(String) ((ProductVO) (htreat.get(i))).getProductLine(),
                        				"Y");
                        		starrating = "";
                        		starrating = ((ProductVO) (htreat.get(i))).getShadeImage();
                        		String crev = "";
                        		if (starrating != null && starrating.length() > 0)
                        			crev = "yes";
                        %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="htreatbrand" />
                            
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (htreat.get(i))).getProdId()%>' brandId='<%=((ProductVO) (htreat.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" />  <% } %></div>
                            <div class="product-name" productId='<%=((ProductVO) (htreat.get(i))).getProdId()%>' brandId='<%=((ProductVO) (htreat.get(i))).getProductLine()%>'><strong><%=master.get(((ProductVO)(htreat.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (htreat.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO)(htreat.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="htreatSelect" value="<%=((ProductVO) (htreat.get(i))).getProdId()%>" onClick="javascript:selectProd('htreat','<%=((ProductVO) (htreat.get(i))).getProdId()%>')" <%=checkChecked(htreatprod,
						((ProductVO) (htreat.get(i))).getProdId(),scrname2)%>/></span>
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
                                  <%          newprofiles2 = new ArrayList() ;      
                                  if (prodlength > 0)
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	     //  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(htreat.get(0))).getProdId() ; 
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
                        		//    if (eflag)
                        		 //   	break ;
                        	      } 
                        	   //  }
                        	     %>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(htreat.get(i))).getProdId() ; 
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
                              <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(htreat.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                           
                        	</ul>                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 3 Outer End -->

                    <!-- Product Image Row 4 Outer Start -->
                    <div class="product-images-outer">
                    	<!-- Product Label And Number Start -->
                        <div class="product-number-label">
                        	<div class="product-number-image fore"></div>
                        	<% if (!stylingbrandflag.equals("true"))  { %>
                        	 <div class="styleing-product-please-note">
                            	<h1>PLEASE NOTE:</h1>
                                <p>There are no <%=master.get(prodLine)%> products available for this step.</p>
                                <p style="margin-top:18px;">Here are products from other brands that address your needs.</p>
                            </div>
                            <% }  %>
                        </div><!-- Product Label And Number End -->
                        <%
                        	prodlength = 4;
                        	if (styling.size() > 4)
                        		prodlength = 4;
                        	else
                        		prodlength = styling.size();

                        	for (int i = 0; i < prodlength; i++) {
                        		prodmainimage = ((ProductVO) (styling.get(i))).getImage();
                        		brandids.put((String) ((ProductVO) (styling.get(i)))
                        				.getProductLine(), "Y");
                        		starrating = "";
                        		starrating = ((ProductVO) (styling.get(i))).getShadeImage();
                        		String crev = "";
                        		if (starrating != null && starrating.length() > 0)
                        			crev = "yes";
                        %>
                        <!-- Product Inner Image 1 Start -->
                        <div class="product-image">
                            <input type="hidden" name="stylingbrand" />
                           
                        	<div class="product-thumb-image" productId='<%=((ProductVO) (styling.get(i))).getProdId()%>' brandId='<%=((ProductVO) (styling.get(i))).getProductLine()%>'><img src="<%=prodmainimage%>" border="0" alt="" /></div>
                        	<div class="product-rating"><% if (starrating !=null && starrating.length() > 0) { %><img src="images/<%=starrating%>" /> <% } %></div>
                            <div class="product-name" productId='<%=((ProductVO) (styling.get(i))).getProdId()%>' brandId='<%=((ProductVO) (styling.get(i))).getProductLine()%>'><strong><%=master.get(((ProductVO)(styling.get(i))).getProductLine())%></strong>&nbsp;<%=((ProductVO) (styling.get(i))).getProdName()%></div>
                            <div class="product-price">$<%=ft.format(new Double(((ProductVO)(styling.get(i))).getPrice()))%></div>
                            <div class="select">
                            	<span class="select-radio"><input type="radio" name="stylingSelect" value="<%=((ProductVO) (styling.get(i))).getProdId()%>" onClick="javascript:selectProd('styling','<%=((ProductVO) (styling.get(i))).getProdId()%>')" <%=checkChecked(stylingprod,
						((ProductVO) (styling.get(i))).getProdId(),scrname2)%>/></span>
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
                                  <%         newprofiles2 = new ArrayList() ;     
                                  if (prodlength > 0)
                        	     for (int k=0; k < newprofiles.size() ; k++)
  	                             {  
                        	    	 System.out.println(" 2222222 dfdffdfdfd "+newprofiles.get(k)+"  k  "+k) ;
                        	  //     for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[0] = ((ProductVO)(styling.get(0))).getProdId() ; 
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
                        		//    if (eflag)
                        		//    	break ;
                        	      } 
                        	  //   }
                        	     %>
                        <% 	for (int k=0; k < newprofiles2.size() ; k++)
	                       {
  				                  System.out.println("dfdffdfdfd "+newprofiles2.get(k)) ;
  				                  
  				         %>         
                        		<li>
                                	<div class="recommendation-result-titel"><%=(String)newhta.get(newprofiles2.get(k))%></div>
                        	 <%  for (int i=0 ;  i < prodlength ; i++) {
                        		    newprodid[i] = ((ProductVO)(styling.get(i))).getProdId() ; 
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
                              <div class="recommendation-result"><a class="Benefits" href="#" productId='<%=((ProductVO)(styling.get(i))).getProdId() %>'>SEE INGREDIENT BENEFITS</a></div>
                               
                               <%} %>
                               </li>
                        	</ul>
                        </div>
                        <!-- Recommendation Details End -->

                    </div><!-- Product Image Row 4 Outer End -->


                </div><!-- Product Outer End -->

                <!-- Edit Review Button Start -->
                <div id="edit-review-button-outer">
                	<div class="edit-button"><a href="javascript:submitBack()"></a></div>
                    
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
<input type=hidden name=prodLinename value="<%=prodLineName%>">
<input type=hidden name=scrname value="skinanalysis">
<input type=hidden name=scrname2 value="skinanalysisbrand">
<input type="hidden" name="shampooprod" value="">
<input type="hidden" name="conditionerprod" value="">
<input type="hidden" name="htreatprod" value="">
<input type="hidden" name="stylingprod" value="">
<input type="hidden" name="skinMethodToCall">
<input type="hidden" name="bnamestring" value="<%=bnamestring%>">

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
	


    
</div><!-- Wrapper End -->
</body>
</html>