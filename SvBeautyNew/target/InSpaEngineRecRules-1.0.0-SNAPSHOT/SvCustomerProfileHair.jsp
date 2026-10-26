
<%@ page import ="java.util.*,java.sql.*,java.io.*,com.svb.common.valueobjects.CustProfileVO,com.svb.customerprofile.formbeans.ProfileSummaryFormBean" %>

<%
	
	String cusid = "" ;
	String mid = "" ;
	String mode = "" ;
	String catid = "" ;
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
<title>HairCare Questionair Page</title>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/header.css" />
<link rel="stylesheet" href="css/footer.css" />
<link rel="stylesheet" href="css/questioner.css" />
<link rel="stylesheet" href="css/uniform.default.css" />

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
 //var pgh = document.getElementById
 //console.log(page_height);
//  console.log(ht);
  // parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht ;
 parent.location = "http://BeautyT1b.dstest.drugstore.com/partners/sv/recommender.asp?catid=12875#"+ht ;

}
</script>
</head>
<!-- <body id="bodylabel" onLoad="javascript:checkPageHeight();">    -->
<body>


<!-- Wrapper Start -->
<div id="wrapper">
	<!-- Container Start -->
    <div id="container">
    	
        <!-- Banner Image Start -->
        <div id="banner-image-outer">
        	<img src="images/BCOM-banner-hair.jpg" alt="" />
        </div><!-- Banner Image End -->

		<div id="steps-images-outer">
        	<div class="step-1-image step-1-1"></div>
            <div class="step-2-image step-1-2"></div>
            <div class="step-3-image step-1-3"></div>
        </div>

 <!-- Body Content Outer Start -->
        <div id="body-running-content-outer">
	        
            <!-- Body Content Start -->
	        <div id="body-running-content">
	        
	            <!-- Questioner Outer Start -->
	            <div id="questioner-outer">
	            	<h1>Tell us about yourself</h1>
	            	
	            	 <!-- Error You Missed Something Start -->
                    <div id="error-you-missed-something-outer" style="display:none;">
                    	<div class="error-icon-large"></div>
                        <div class="error-text-message">
                        	<span class="error-your-missed-something-text">You missed something!</span>
                            <span class="error-please-answer-text">Please answer each question so we can give you a complete recommendation.</span>
                        </div>
                    </div><!-- Error You Missed Something End -->
	            	
	         <form name="profile3" id="hair1" action="#">
                    <!-- Answer Required Start -->
                    <div id="answer-required">
                    	<span class="star"></span>
                        <span>ANSWER REQUIRED</span>
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
	                                <span>What color is your hair right now? (Natural or colored)</span>
	                            </div><!-- Question 1 End -->
	                            <!-- Answer 1 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio11" value="O001" class="required" <%=checkChecked("O001","71",vtb)%>/></span><label>Blonde</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio12" value="O002" <%=checkChecked("O002","71",vtb)%>/></span><label>Brown</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio13" value="O003" <%=checkChecked("O003","71",vtb)%>/></span><label>Red</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio14" value="O004" <%=checkChecked("O004","71",vtb)%>/></span><label>Black</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio15" value="O005" <%=checkChecked("O005","71",vtb)%>/></span><label>Grey/White</label></li>
	                                    <li class="one-two-three"><span><input type="radio" name="hair_Color" id="radio16" value="O999" <%=checkChecked("O999","71",vtb)%>/></span><label>Other</label></li>
	                            	</ul>
	                            </div><!-- Answer 1 End -->
	                        </li><!-- Questioin 1 End -->
	                        <!-- Questioin 2 Start -->
	                        <li>
	                        	<!-- Question 2 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                  <span class="number">2.</span>
	                                <span>What chemical treatments have you had recently?</span>
	                            </div><!-- Question 2 End -->
	                            <!-- Answer 2 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li class="one-two-three"><span><input type="checkbox" id="Q21" name="hair_Process" value="H001" class="required" <%=checkChecked("H001","73",vtb)%>/></span><label>Color/Highlights</label></li>
	                                    <li class="one-two-three"><span><input type="checkbox" id="Q22" name="hair_Process" value="H002" <%=checkChecked("H002","73",vtb)%>//></span><label>Perm</label></li>
	                                    <li class="one-two-three"><span><input type="checkbox" id="Q23" name="hair_Process" value="H003"/<%=checkChecked("H003","73",vtb)%>/></span><label>Relaxer</label></li>
	                                    <li class="one-two-three"><span><input type="checkbox" id="Q24" name="hair_Process" value="H999" class="none" <%=checkChecked("H999","73",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 2 End -->
	                        </li><!-- Questioin 2 End -->
	                        
	                        <!-- Questioin 3 Start -->
	                        <li>
	                        	<!-- Question 3 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                 <span class="number">3.</span>
	                                <span>How thick is your hair?</span>
	                            </div><!-- Question 3 End -->
	                            <!-- Answer 3 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li class="one-two-three"><span><input id="Radio31" type="radio" name="hair_Thickness" value="I001" class="required" <%=checkChecked("I001","74",vtb)%>/></span><label>Very thick</label></li>
	                                    <li class="one-two-three"><span><input id="Radio32" type="radio" name="hair_Thickness" value="I002" <%=checkChecked("I002","74",vtb)%>/></span><label>Thick</label></li>
	                                    <li class="one-two-three"><span><input id="Radio33" type="radio" name="hair_Thickness" value="I003" <%=checkChecked("I003","74",vtb)%>/></span><label>Average</label></li>
	                                    <li class="one-two-three"><span><input id="Radio34" type="radio" name="hair_Thickness" value="I004" <%=checkChecked("I004","74",vtb)%>/></span><label>Thin</label></li>
	                                    <li class="one-two-three"><span><input id="Radio35" type="radio" name="hair_Thickness" value="I005" <%=checkChecked("I005","74",vtb)%>/></span><label>Very thin</label></li>
	                                    <li class="one-two-three"><span><input id="Radio36" type="radio" name="hair_Thickness" value="I999" <%=checkChecked("I999","74",vtb)%>/></span><label>Not sure</label></li>
	                            	</ul>
	                            </div><!-- Answer 3 End -->
	                        </li><!-- Questioin 3 End -->                        
	                        
	                        <!-- Questioin 4 Start -->
	                        <li>
	                        	<!-- Question 4 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                 <span class="number">4.</span>
	                                <span>What is your hair texture?</span>
	                            </div><!-- Question 4 End -->
	                            <!-- Answer 4 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li class="one-two-three"><span><input id="Radio41" type="radio" name="hair_Texture" value="X001" class="required" <%=checkChecked("X001","75",vtb)%>/></span><label>Very coarse</label></li>
	                                    <li class="one-two-three"><span><input id="Radio42" type="radio" name="hair_Texture" value="X002" <%=checkChecked("X002","75",vtb)%>/></span><label>Coarse</label></li>
	                                    <li class="one-two-three"><span><input id="Radio43" type="radio" name="hair_Texture" value="X003" <%=checkChecked("X003","75",vtb)%>/></span><label>Average</label></li>
	                                    <li class="one-two-three"><span><input id="Radio44" type="radio" name="hair_Texture" value="X004" <%=checkChecked("X004","75",vtb)%>/></span><label>Fine</label></li>
	                                    <li class="one-two-three"><span><input id="Radio45" type="radio" name="hair_Texture" value="X005" <%=checkChecked("X005","75",vtb)%>/></span><label>Very fine</label></li>
	                                    <li class="one-two-three"><span><input id="Radio46" type="radio" name="hair_Texture" value="X999" <%=checkChecked("X999","75",vtb)%>/></span><label>Not sure</label></li>
	                            	</ul>
	                            </div><!-- Answer 4 End -->
	                        </li><!-- Questioin 4 End -->                        
	                        
	                        <!-- Questioin 5 Start -->
	                        <li>
	                        	<!-- Question 5 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                            	 <span class="number">5.</span>
	                                <span>What concerns you about your hair? Choose all that apply.</span>
	                            </div><!-- Question 5 End -->
	                            <!-- Answer 5 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q51" type="checkbox" name="hair_Conditions" value="R001" class="required" <%=checkChecked("R001","77",vtb)%>/></span><label>Dryness</label></li>
	                                    <li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R003" <%=checkChecked("R003","77",vtb)%>/></span><label>Breakage/Split ends</label></li>
	                                    <li><span><input id="Q510" type="checkbox" name="hair_Conditions" value="R010" <%=checkChecked("R010","77",vtb)%>/></span><label>Color fading</label></li>
	                                    <li><span><input id="Q58" type="checkbox" name="hair_Conditions" value="R008" <%=checkChecked("R008","77",vtb)%>/></span><label>Oily</label></li>
	                                    <li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R006" <%=checkChecked("R006","77",vtb)%>/></span><label>Flyaways/Frizz</label></li>
	                                    <li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R011" <%=checkChecked("R011","77",vtb)%>/></span><label>Sun damage</label></li>
	                                    <li><span><input id="Q52" type="checkbox" name="hair_Conditions" value="R002" <%=checkChecked("R002","77",vtb)%>/></span><label>Limp/Lifelessness</label></li>
	                                    <li><span><input id="Q55" type="checkbox" name="hair_Conditions" value="R999" class="none" <%=checkChecked("R999","77",vtb)%>/></span><label>None of the above</label></li>
	                                    <li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R012" <%=checkChecked("R012","77",vtb)%>/></span><label>Hair loss</label></li>
	                                    <li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R013" <%=checkChecked("R013","77",vtb)%>/></span><label>Thinning hair</label></li>
	                          			<li><span><input id="Q53" type="checkbox" name="hair_Conditions" value="R007" <%=checkChecked("R007","77",vtb)%>/></span><label>Dandruff/Irritated scalp</label></li>
	                            	</ul>
	                            </div><!-- Answer 5 End -->
	                        </li><!-- Questioin 5 End -->
	                        
	                        <!-- Questioin 6 Start -->
	                        <li>
	                        	<!-- Question 6 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                            	 <span class="number">6.</span>
	                                <span>What do you want from your styling products? Choose all that apply.</span>
	                            </div><!-- Question 6 End -->
	                            <!-- Answer 6 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q61" type="checkbox" name="hair_Styles" value="S001" class="required" <%=checkChecked("S001","76",vtb)%>/></span><label>Volume</label></li>
	                                    <li><span><input id="Q62" type="checkbox" name="hair_Styles" value="S005" <%=checkChecked("S005","76",vtb)%>/></span><label>Make waves curly</label></li>
	                                    <li><span><input id="Q63" type="checkbox" name="hair_Styles" value="S002" <%=checkChecked("S002","76",vtb)%>/></span><label>Shine</label></li>
	                                    <li><span><input id="Q64" type="checkbox" name="hair_Styles" value="S006" <%=checkChecked("S006","76",vtb)%>/></span><label>Hold</label></li>
	                                    <li><span><input id="Q65" type="checkbox" name="hair_Styles" value="S007" <%=checkChecked("S007","76",vtb)%>/></span><label>Less frizz</label></li>
	                                    <li><span><input id="Q66" type="checkbox" name="hair_Styles" value="S003" <%=checkChecked("S003","76",vtb)%>/></span><label>Tame curls</label></li>
	                                    <li><span><input id="Q67" type="checkbox" name="hair_Styles" value="S004" <%=checkChecked("S004","76",vtb)%>/></span><label>Straighten</label></li>
	                                    <li><span><input id="Q68" type="checkbox" name="hair_Styles" value="S999" class="none" <%=checkChecked("S999","76",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 6 End -->
	                        </li><!-- Questioin 6 End -->                        
	                        
	                        <!-- Questioin 7 Start -->
	                        <li>
	                        	<!-- Question 7 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                            	 <span class="number">7.</span>
	                                <span>What look are you going for?</span>
	                            </div><!-- Question 7 End -->
	                            <!-- Answer 7 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio71" type="radio" name="hair_Finish" value="F001" class="required" <%=checkChecked("F001","78",vtb)%>/></span><label>Soft and natural</label></li>
	                                    <li><span><input id="Radio74" type="radio" name="hair_Finish" value="F004" <%=checkChecked("F004","78",vtb)%>/></span><label>Wavy, natural curls</label></li>
	                                    <li><span><input id="Radio72" type="radio" name="hair_Finish" value="F002" <%=checkChecked("F002","78",vtb)%>/></span><label>Stylized, flexible hold</label></li>
	                                    <li><span><input id="Radio73" type="radio" name="hair_Finish" value="F003" <%=checkChecked("F003","78",vtb)%>/></span><label>Stylized, firm hold</label></li>
	                                    <li><span><input id="Radio75" type="radio" name="hair_Finish" value="F999" <%=checkChecked("F999","78",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 7 End -->
	                        </li><!-- Questioin 7 End -->                        
	                        
	                        <!-- Questioin 8 Start -->
	                        <li>
	                        	<!-- Question 8 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                            	 <span class="number">8.</span>
	                                <span>Are there any ingredients you'd like to avoid? Choose all that apply.</span>
	                            </div><!-- Question 8 End -->
	                            <!-- Answer 8 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q81" type="checkbox" name="allergy"  value="L001" class="required" <%=checkChecked("L001","56",vtb)%>/></span><label>Fragrance</label></li>
	                                    <li><span><input id="Q82" type="checkbox" name="allergy" value="L005" <%=checkChecked("L005","56",vtb)%>/></span><label>Lanolin</label></li>
	                                    <li><span><input id="Q83" type="checkbox" name="allergy" value="L006" <%=checkChecked("L006","56",vtb)%>/></span><label>Parabens</label></li>
	                                    <li><span><input id="Q84" type="checkbox" name="allergy" value="L007" <%=checkChecked("L007","56",vtb)%>/></span><label>Sulfates</label></li>
	                                    <li><span><input id="Q85" type="checkbox" name="allergy" value="L999" class="none" <%=checkChecked("L999","56",vtb)%>/></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 8 End -->
	                        </li><!-- Questioin 8 End -->                        
	                        
	                        <!-- Questioin 9 Start -->
	                        <li>
	                        	<!-- Question 9 Start -->
	                        	<div class="question">
	                        		<span class="no-star"></span>
	                            	 <span class="number">9.</span>
	                                <span>What age are you?</span>
	                            </div><!-- Question 9 End -->
	                            <!-- Answer 9 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Q91"type="radio" name="age" value="G004" <%=checkChecked("G004","55",vtb)%>/></span><label>Under 25</label></li>
	                                    <li><span><input id="Q92"type="radio" name="age" value="G001" <%=checkChecked("G001","55",vtb)%>/></span><label>25-35</label></li>
	                                    <li><span><input id="Q93"type="radio" name="age" value="G002" <%=checkChecked("G002","55",vtb)%>/></span><label>36-46</label></li>
	                                    <li><span><input id="Q94"type="radio" name="age" value="G003" <%=checkChecked("G003","55",vtb)%>/></span><label>46-55</label></li>
	                                    <li><span><input id="Q95"type="radio" name="age" value="G005" <%=checkChecked("G005","55",vtb)%>/></span><label>56-65</label></li>
	                                    <li><span><input id="Q96"type="radio" name="age" value="G006" <%=checkChecked("G006","55",vtb)%>/></span><label>66-75</label></li>
	                                    <li><span><input id="Q97"type="radio" name="age" value="G007" <%=checkChecked("G007","55",vtb)%>/></span><label>76+</label></li>
	                            	</ul>
	                            </div><!-- Answer 9 End -->
	                        </li><!-- Questioin 9 End -->                        
	                        
	                        <!-- Questioin 10 Start -->
	                        <li>
	                        	<!-- Question 10 Start -->
	                        	<div class="question">
	                        		<span class="no-star"></span>
	                            	 <span class="number">10.</span>
	                                <span>What ethnicity best describes you?</span>
	                            </div><!-- Question 10 End -->
	                            <!-- Answer 10Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio11" type="radio" name="ethnicity" value="N001" <%=checkChecked("N001","58",vtb)%>/></span><label>African American</label></li>
	                                    <li><span><input id="Radio12" type="radio" name="ethnicity" value="N002" <%=checkChecked("N002","58",vtb)%>/></span><label>Asian</label></li>
	                                    <li><span><input id="Radio13" type="radio" name="ethnicity" value="N003" <%=checkChecked("N003","58",vtb)%>/></span><label>Caucasian</label></li>
	                                    <li><span><input id="Radio14" type="radio" name="ethnicity" value="N005" <%=checkChecked("N005","58",vtb)%>/></span><label>Hispanic/Latino</label></li>
	                                    <li><span><input id="Radio15" type="radio" name="ethnicity" value="N009" <%=checkChecked("N009","58",vtb)%>/></span><label>Other</label></li>
	                            	</ul>
	                            </div><!-- Answer 10 End -->
	                        </li><!-- Questioin 10 End -->                        
	                        
	                        <!-- Questioin 11 Start -->
	                        <li>
	                        	<!-- Question 11 Start -->
	                        	<div class="question">
	                            	<span class="no-star"></span>
	                            	 <span class="number">11.</span>
	                                <span>What is your gender?</span>
	                            </div><!-- Question 11 End -->
	                            <!-- Answer 11 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio6" type="radio" name="gender" value="GE02" <%=checkChecked("GE02","59",vtb)%>/></span><label>Female</label></li>
	                                    <li><span><input id="Radio5" type="radio" name="gender" value="GE01" <%=checkChecked("GE01","59",vtb)%>/></span><label>Male</label></li>
	                            	</ul>
	                            </div><!-- Answer 11 End -->
	                        </li><!-- Questioin 11 End -->                        
	                        
	                        <!-- Questioin 12 Start -->
	                        <li>
	                        	<!-- Question 12 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                            	 <span class="number">12.</span>
	                                <span>Last one! Do you prefer natural and/or organic ingredients?</span>
	                            </div><!-- Question 12 End -->
	                            <!-- Answer 12 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input id="Radio19" type="radio" name="natural" value="A008" class="required" <%=checkChecked("A008","57",vtb)%>/></span><label>Yes</label></li>
	                                    <li><span><input id="Radio20" type="radio" name="natural" value="A009" <%=checkChecked("A009","57",vtb)%>/></span><label>No preference</label></li>
	                            	</ul>
	                            </div><!-- Answer 12 End -->
	                        </li><!-- Questioin 12 End -->                        
	                        
	                       
	                                                
	                        
		                        
	                	</ul>
	                </div><!-- Questions Content End -->
	                <!-- Answer All Mandatory Questions Start -->
                      <div class="answer-all-mandatory-questions" style="display: none;"> * Please Answer all mandatory questions</div>
                    <!-- Answer All Mandatory Questions End -->
	                <!-- Submit Button Start -->
	                <div class="questioner-submit-button"><a href="prod" id="but"></a></div><!-- Submit Button End -->
	               <!--<input type="submit" value="Submit" />-->
	               
    <input type=hidden name="cusid" value="<%=cusid%>"/>
    <input type=hidden name="mid" value="<%=mid%>"/>
    <input type=hidden name="catid" value="<%=catid%>"/>
    <input type=hidden name="mode" value="<%=mode%>"/>
    <input type=hidden name="customerId" value="<%=cusid%>"/>
    <input type=hidden name="scrname" value="hairprofile">
    <input type="hidden" name="skinMethodToCall">
    
	                </form>
	               
	            
	            </div><!-- Questioner Outer End -->
	           
	        </div><!-- Body Content End -->
            
            <!-- Please Note Start -->
            <div id="please-note">
            	<strong>PLEASE NOTE:</strong> Information on this site is not meant to be a substitute for the advice from a medical professional. Prior to starting a new regimen, you should consult your own physician, particularly if you are currently using any medications, are pregnant or nursing. Test a small amount of each new product on a hidden area (Such as behind your ear) to make sure that your skin does not have any adverse reactions. Always follow the recommended application quantity and frequency for each product.
            </div><!-- Please Note End -->
           
    
<script type="text/javascript" src="js/validate.js"></script>

<script type="text/javascript">
  $(function(){
 /* ============== Jquery Validation Script Start   ================== */
 /* commented by VB     var Options = {
   		  errorPlacement: function(error, element) {
		     error.prependTo( element.parents('li:eq(1)') );
		     $('#error-you-missed-something-outer').show();
   	   },
   	invalidHandler: function(form, validator) {
   		if (!validator.numberOfInvalids())
	         return;
	
   	var pHeaight=$(validator.errorList[0].element).offset().top - 70;
	     $('html, body').animate({
	         scrollTop: pHeaight
	     }, 200);

	   	},
   	   success : function(label) {
   		label.slideUp('1000').remove();
     	}
     	
      }; */
      var Options = {
       		  errorPlacement: function(error, element) {
    		     error.prependTo( element.parents('li:eq(1)') );
    		     $('#error-you-missed-something-outer').show();
       	   },
       	invalidHandler: function(form, validator) {
       		if (!validator.numberOfInvalids())
    	         return;
    	var windowPos = $(parent.window).height(),
    	    errorPos=$(validator.errorList[0].element).offset().top - 70;
       	if (errorPos > windowPos) {
       		$(parent.window).animate({
       	      scrollTop: errorPos
       	    }, 1000);
       	  }

    	   	},
       	   success : function(label) {
       		label.slideUp('1000').remove();
         	}
         	
          };    
  $('#hair1').validate(Options);
      
      
    $('#but').click(function(e){
    	e.preventDefault();
    	var $valid= $("#hair1").valid();
    	
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
	document.profile3.action = "<%=response.encodeURL("customerselfprofilehairconcerns.do")%>";
	document.profile3.skinMethodToCall.value = "add";
	document.profile3.submit();
}

</script>
</div><!-- Body Content Outer End-->
        
    </div><!-- Container End -->
    
</div><!-- Wrapper End -->
</body>
</html>