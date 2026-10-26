<%@ page import ="java.util.*,java.sql.*,java.io.*,com.svb.common.valueobjects.CustProfileVO,com.svb.customerprofile.formbeans.ProfileSummaryFormBean" %>

<%
	
	String cusid = "" ;
	String mid = "" ;
	String mode = "" ;
	String catid = "12873" ;
	ArrayList vtb = new ArrayList() ;
	
	Hashtable profdesc = new Hashtable() ;

	ProfileSummaryFormBean profilesForm = (ProfileSummaryFormBean)request.getAttribute("ProfilesFormBean");
	cusid = profilesForm.getCusid();
	mid = profilesForm.getMid();
	mode = profilesForm.getMode();
	
	vtb = profilesForm.getVtb();
	
    System.out.println("in page  vtb size "+vtb.size()) ;
    
  //  for (int i=0; i < vtb.size() ; i++)
   //   System.out.println("vbttt "+vtb.get(i)) ;

    catid = (String) request.getAttribute("catid") ;
   profdesc = (Hashtable) request.getAttribute("profdesc") ;
//
     System.out.println("in page prof desc size "+profdesc.size()) ;
	
	System.out.println("in page prof desc size "+profdesc.size()) ;
    System.out.println("in page ... mode  "+mode+"  catid "+catid+"  customerId "+cusid) ;
	
%>
<%! String checkChecked(String val1, String val2, ArrayList vtb)
{
  // System.out.println("val1 "+val1+"  val2 "+val2+"  vtb size "+vtb.size()) ;
   String retu = "" ;
	 if (vtb.size() > 0 )
	 {
	   if ( vtb.contains(val2+val1) )
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
<link rel="stylesheet" href="css/questioner.css" />
<link rel="stylesheet" href="css/footer.css" />
<link rel="stylesheet" href="css/uniform.default.css" />

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>SkinCare Questioner Page</title>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script language="javascript" src='js/jquery.uniform.js' ></script>

<script type="text/javascript">
	$(function(){
		$("input").uniform();
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
   parent.location = "http://BeautyT1b.dstest.drugstore.com/partners/sv/recommender.asp?catid=12873#"+ht ;

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
        
        <!-- Steps Images Outer Start -->
        <div id="steps-images-outer">
        	<div class="step-1-image skin-step-1-1"></div>
            <div class="step-2-image skin-step-1-2"></div>
            <div class="step-3-image skin-step-1-3"></div>
        </div><!-- Steps Images Outer End -->
      
       <!-- Body Content Outer Start -->
        <div id="body-running-content-outer">
	        
            <!-- Body Content Start -->
	        <div id="body-running-content">
	        	
               <form name="profile2" id="skin1" action="#">
                  <input type="hidden" name="skinMethodToCall">
                  <input type="hidden" name="xx" value="1"/>
	             <input type=hidden name="cusid" value="<%=cusid%>"/>
                 <input type=hidden name="mid" value="<%=mid%>"/>
                  <input type=hidden name="catid" value="<%=catid%>"/>
                  <input type=hidden name="mode" value="<%=mode%>"/>
               <input type=hidden name="customerId" value="<%=cusid%>"/>
                <input type=hidden name="scrname" value="general"/>
	            <!-- Questioner Outer Start -->
	            <div id="questioner-outer">
	            	<h1>Tell us about yourself</h1>
	            	<div id="error-you-missed-something-outer" style="display: none;">
                    	<div class="error-icon-large"></div>
                        <div class="error-text-message">
                        	<span class="error-your-missed-something-text">You missed something!</span>
                            <span class="error-please-answer-text">Please answer each question so we can give you a complete recommendation.</span>
                        </div>
                    </div>	                
                    <!-- Answer Required Start -->
                    <div id="answer-required">
                    	<span class="star"></span>
                        <span>Answer Required</span>
                    </div><!-- Answer Required Start -->
                    
	                <!-- Questions Content Start -->
	                <div id="questions-content">
	                	<ul>
	                		<!-- Questioin 1 Start -->
	                        <li>
	                        	<!-- Question 1 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span class="number">1.</span>
                                    <span>Shortly after washing your face, how does your skin feel?</span>
	                            </div><!-- Question 1 End -->
	                            <!-- Answer 1 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li class="one-two-three"><span><input type="radio" name="skin_type" value="T001" <%=checkChecked("T001","51",vtb)%> class="required error"/></span><label>Tight/Dry</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="skin_type" value="T004" <%=checkChecked("T004","51",vtb)%>/></span><label>Oily</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="skin_type" value="T003" <%=checkChecked("T003","51",vtb)%>/></span><label>Oily & dry</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="skin_type" value="T002" <%=checkChecked("T002","51",vtb)%>/></span><label>Normal</label></li>
	                            	</ul>
	                            </div><!-- Answer 1 End -->
	                        </li><!-- Questioin 1 End -->
	                       <!-- Questioin 2 Start -->
	                        <li>
	                        	<!-- Question 2 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span class="number">2.</span>
                                    <span>What concerns you about your facial skin? Choose all that apply.</span>
	                            </div><!-- Question 2 End -->
	                            <!-- Answer 2 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q21" type="checkbox" name="skin_concern" value="C022" class="required" <%=checkChecked("C022","53",vtb)%>/></span><label>Fine Lines/Wrinkles</label></li>
	                                    <li><span><input id="Q26" type="checkbox" name="skin_concern" value="C012" <%=checkChecked("C012","53",vtb)%>/></span><label>Enlarged pores</label></li>
	                                    <li><span><input id="Q2" type="checkbox" name="skin_concern" value="C024" <%=checkChecked("C024","53",vtb)%>/></span><label>Flakiness</label></li>
                                        
                                        <li><span><input id="Q25" type="checkbox" name="skin_concern" value="C023" <%=checkChecked("C023","53",vtb)%>/></span><label>Dryness/Dehydration</label></li>
	                                    <li><span><input id="Q22" type="checkbox" name="skin_concern" value="C003" <%=checkChecked("C003","53",vtb)%>/></span><label>Deep creases</label></li>
	                                    <li><span><input id="Q27" type="checkbox" name="skin_concern" value="C019" <%=checkChecked("C019","53",vtb)%>/></span><label>Redness/Blotchiness</label></li>
                                        <li><span><input id="Q212" type="checkbox" name="skin_concern" value="D008" <%=checkChecked("D008","52",vtb)%>/></span><label>Occasional acne</label></li>
	                                    <li><span><input id="Q213" type="checkbox" name="skin_concern" value="D002" <%=checkChecked("D002","52",vtb)%>/></span><label>Persistent acne</label></li>
                                        
	                                    <li><span><input id="Q210" type="checkbox" name="skin_concern" value="D003" <%=checkChecked("D003","52",vtb)%>/></span><label>Rosacea</label></li>
	                                    <li><span><input id="Q23" type="checkbox" name="skin_concern" value="C020" <%=checkChecked("C020","53",vtb)%>/></span><label>Loss of Firmness/Elasticity</label></li>
                                        <li><span><input id="Q28" type="checkbox" name="skin_concern" value="C007" <%=checkChecked("C007","53",vtb)%>/></span><label>Age spots</label></li>
                                        <li><span><input id="Q214" type="checkbox" class = "none" name="skin_concern" value="C999" <%=checkChecked("C999","53",vtb)%>/></span><label>None of the above</label></li>
	                                    <li><span><input id="Q24" type="checkbox" name="skin_concern" value="C004" <%=checkChecked("C004","53",vtb)%>/></span><label>Dull skin</label></li>
                                        
                                        <li><span><input id="Q29" type="checkbox" name="skin_concern" value="D001" <%=checkChecked("D001","52",vtb)%>/></span><label>Sensitive skin</label></li>
	                                    <li><span><input id="Q211" type="checkbox" name="skin_concern" value="D006" <%=checkChecked("D006","52",vtb)%>/></span><label>Hyperpigmentation</label></li>
                                        
	                            	</ul>
	                            </div><!-- Answer 2 End -->
	                        </li><!-- Questioin 2 End -->
	                        
	                        <!-- Questioin 3 Start -->
	                        <li>
	                        	<!-- Question 3 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span class="number">3.</span>
                                    <span>What concerns you about the skin around your eyes? Choose all that apply.</span>
	                            </div><!-- Question 3 End -->
	                            <!-- Answer 3 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q31" type="checkbox" name="eye_concern" value="E001" class="required" <%=checkChecked("E001","54",vtb)%>/></span><label>Puffiness</label></li>
	                                    <li><span><input id="Q32" type="checkbox" name="eye_concern" value="C002" <%=checkChecked("C002","54",vtb)%>/></span><label>Dark Circles</label></li>
	                                    <li><span><input id="Q33" type="checkbox" name="eye_concern" value="C001" <%=checkChecked("C001","54",vtb)%>/></span><label>Fine Lines/Wrinkles</label></li>
	                                    <li><span><input id="Q34" type="checkbox" name="eye_concern" value="E004" <%=checkChecked("E004","54",vtb)%>/></span><label>Deep creases</label></li>
	                                    <li><span><input id="Q35" type="checkbox" class = "none" name="eye_concern" class = "none" value="E999" <%=checkChecked("E999","54",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 3 End -->
	                        </li><!-- Questioin 3 End -->                        
	                        
	                        <!-- Questioin 4 Start -->
	                        <li>
	                        	<!-- Question 4 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span class="number">4.</span>
                                    <span>Are there any ingredients you'd like to avoid? Choose all that apply. </span>
	                            </div><!-- Question 4 End -->
	                            <!-- Answer 4 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q41" type="checkbox" name="allergy"  value="L001" <%=checkChecked("L001","56",vtb)%> class="required" /></span><label>Fragrance</label></li>
	                                    <li><span><input id="Q42" type="checkbox" name="allergy" value="L005" <%=checkChecked("L005","56",vtb)%>/></span><label>Lanolin</label></li>
	                                    <li><span><input id="Q43" type="checkbox" name="allergy" value="L004" <%=checkChecked("L004","56",vtb)%>/></span><label>Nuts</label></li>
	                                    <li><span><input id="Q44" type="checkbox" name="allergy" value="L006" <%=checkChecked("L006","56",vtb)%>/></span><label>Parabens</label></li>
	                                    <li><span><input id="Q45" type="checkbox" class = "none" name="allergy" class = "none" value="L999" <%=checkChecked("L999","56",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 4 End -->
	                        </li><!-- Questioin 4 End -->                        
	                        
	                        <!-- Questioin 5 Start -->
	                        <li>
	                        	<!-- Question 5 Start -->
	                        	<div class="question">
	                                <span class="number">5.</span>
                                    <span>What age are you?</span>
	                            </div><!-- Question 5 End -->
	                            <!-- Answer 5 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q91"type="radio" name="age" value="G004" <%=checkChecked("G004","55",vtb)%>/></span><label>Under 25</label></li>
	                                    <li><span><input id="Q92"type="radio" name="age" value="G001" <%=checkChecked("G001","55",vtb)%>/></span><label>25-35</label></li>
	                                    <li><span><input id="Q93"type="radio" name="age" value="G002" <%=checkChecked("G002","55",vtb)%>/></span><label>36-45</label></li>
	                                    <li><span><input id="Q94"type="radio" name="age" value="G003" <%=checkChecked("G003","55",vtb)%>/></span><label>46-55</label></li>
	                                    <li><span><input id="Q95"type="radio" name="age" value="G005" <%=checkChecked("G005","55",vtb)%>/></span><label>56-65</label></li>
	                                    <li><span><input id="Q96"type="radio" name="age" value="G006" <%=checkChecked("G006","55",vtb)%>/></span><label>66-75</label></li>
	                                    <li><span><input id="Q97"type="radio" name="age" value="G007" <%=checkChecked("G007","55",vtb)%>/></span><label>76+</label></li>
	                            	</ul>
	                            </div><!-- Answer 5 End -->
	                        </li><!-- Questioin 5 End -->
	                        
	                        <!-- Questioin 6 Start -->
	                        <li>
	                        	<!-- Question 6 Start -->
	                        	<div class="question">
	                                <span class="number">6.</span>
                                    <span>What ethnicity best describes you?</span>
	                            </div><!-- Question 6 End -->
	                            <!-- Answer 6 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio11" type="radio" name="ethnicity" value="N001" <%=checkChecked("N001","58",vtb)%>/></span><label>African American</label></li>
	                                    <li><span><input id="Radio12" type="radio" name="ethnicity" value="N002" <%=checkChecked("N002","58",vtb)%>/></span><label>Asian</label></li>
	                                    <li><span><input id="Radio13" type="radio" name="ethnicity" value="N003" <%=checkChecked("N003","58",vtb)%>/></span><label>Caucasian</label></li>
	                                    <li><span><input id="Radio14" type="radio" name="ethnicity" value="N005" <%=checkChecked("N005","58",vtb)%>/></span><label>Hispanic/Latino</label></li>
	                                    <li><span><input id="Radio15" type="radio" name="ethnicity" value="N009" <%=checkChecked("N009","58",vtb)%>/></span><label>Other</label></li>
	                            	</ul>
	                            </div><!-- Answer 6 End -->
	                        </li><!-- Questioin 6 End -->                        
	                        
	                        <!-- Questioin 7 Start -->
	                        <li>
	                        	<!-- Question 7 Start -->
	                        	<div class="question">
	                                <span class="number">7.</span>
                                    <span>What is your gender?</span>
	                            </div><!-- Question 7 End -->
	                            <!-- Answer 7 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio6" type="radio" name="gender" value="GE02" <%=checkChecked("GE02","59",vtb)%>/></span><label>Female</label></li>
	                                    <li><span><input id="Radio5" type="radio" name="gender" value="GE01" <%=checkChecked("GE01","59",vtb)%>/></span><label>Male</label></li>
	                            	</ul>
	                            </div><!-- Answer 7 End -->
	                        </li><!-- Questioin 7 End -->                        
	                        
	                        <!-- Questioin 8 Start -->
	                        <li>
	                        	<!-- Question 8 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span class="number">8.</span>
                                    <span>Last one! Do you prefer natural and/or organic ingredients?</span>
	                            </div><!-- Question 8 End -->
	                            <!-- Answer 8 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio19" type="radio" name="natural" value="A008" <%=checkChecked("A008","57",vtb)%> class="required"/></span><label>Yes</label></li>
	                                    <li><span><input id="Radio20" type="radio" name="natural" value="A009" <%=checkChecked("A009","57",vtb)%>/></span><label>No preference</label></li>
	                            	</ul>
	                            </div><!-- Answer 8 End -->
	                        </li><!-- Questioin 8 End -->
	                              
	                                           
	                	</ul>
	                </div><!-- Questions Content End -->
	                <!-- Answer All Mandatory Questions Start -->
                      <div class="answer-all-mandatory-questions" style="display: none;"> * Please Answer all mandatory questions</div>
                    <!-- Answer All Mandatory Questions End -->
	                
	                <!-- Submit Button Start -->
	                <div class="questioner-submit-button"><a href="prod" id="but"></a></div><!-- Submit Button End -->
	            </div><!-- Questioner Outer End -->
                </form>
	        </div><!-- Body Content End -->
	        
	
    
   
  
        </form>    
            <!-- Please Note Start -->
            <div id="please-note">
            	<strong>PLEASE NOTE:</strong> Information on this site is not meant to be a substitute for the advice from a dermatologist or other medical professional. Prior to starting a new skin care regimen, you should consult your own physician, particularly if you are currently using any medications, pregnant or nursing. Always read the complete manufacturer's instructions for each product. If at any time excessive or persistent irritation occurs, discontinue use and seek medical attention.
            </div><!-- Please Note End -->
            
            
        </div><!-- Body Content Outer End-->        
    </div><!-- Container End -->
    </div><!-- Wrapper End -->
    
<script type="text/javascript" src="js/validate.js"></script>

<script type="text/javascript">
  $(function() {
	  /* ============== Jquery Validation Script Start   ================== */
      var Options = {
                  errorPlacement: function(error, element) {
                     error.prependTo( element.parents('li:eq(1)') );
                     $('#error-you-missed-something-outer').show();
           },
        invalidHandler: function(form, validator) {
                if (!validator.numberOfInvalids())
                 return;
             $('html, body').animate({
                 scrollTop: $(validator.errorList[0].element).offset().top
             }, 200);

                },
           success : function(label) {
                label.slideUp('1000').remove();
        }

      };
  $('#skin1').validate(Options);


    $('#but').click(function(e){
        e.preventDefault();
        var $valid= $("#skin1").valid();

        if($valid){
        $('#error-you-missed-something-outer').hide();
       
         submitPageForm();
        }
        else{
        	$('div.answer-all-mandatory-questions').show();
        }
    });
    /* ============== Jquery Validation Script End   ================== */ 
    
  /* ============== Checkbox none of the above Script Start   ================== */
    $('input[type="checkbox"]').click(function(){
    	var $this = $(this);
		   if($this.attr('checked') == 'checked' && $this.hasClass('none')){
			   $this.parents('li:eq(0)').siblings().find('input[type="checkbox"]:checked').each(function() {
			     $(this).removeAttr('checked');
			     $(this).closest('span.checked').removeClass();
          }); 
		   }else{
			   var $none = $(this).closest('ul').find('.none');
			   if($this.attr('checked') == 'checked')
				 {
				   $this.attr('checked','checked');
				   $this.closest('span').addClass('checked');
				   $none.removeAttr('checked');
				   $none.closest('span.checked').removeClass();
				 } else{
					 $this.removeAttr('checked','checked');
					 $this.closest('span').removeClass();  
				 }
			   }
		
     });
   /* ============== Checkbox none of the above Script End   ================== */

    /* ============== Jquery error-you-missed-something-outer Script Start   ================== */
    $("input").click(function(){
        var count = $('#questions-content').find('label.error');
        if(count.length == 1 || count.length == 0)
        	 $('#error-you-missed-something-outer').fadeOut(500, function(){$(this).hide()});
        else 
        	$('#error-you-missed-something-outer').show();
    });
    /* ============== Jquery error-you-missed-something-outer Script Start   ================== */

  });
function submitPageForm() {
	    document.profile2.action = "<%=response.encodeURL("customerselfprofileskinconcerns.do")%>" ;
        document.profile2.skinMethodToCall.value = "add";
		document.profile2.submit();
	}

</script>


</body>
</html>

