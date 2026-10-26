<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<jsp:include page="header.html" flush="true"/>

<link rel="stylesheet" href="css/questioner.css" />

 <!-- Body Content Outer Start -->
        <div id="body-running-content-outer">
	        
            <!-- Body Content Start -->
	        <div id="body-running-content">
	        	
	            <!-- Questioner Outer Start -->
	            <div id="questioner-outer">
	            	<h1>Tell us about you</h1>
	                
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
	                                <span>1. What color is your hair right now?(Natural or colored)</span>
	                            </div><!-- Question 1 End -->
	                            <!-- Answer 1 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="1" /></span><label></label> Blonde</li>
	                                    <li><span><input type="radio" name="1" /></span><label>Brown</label></li>
	                                    <li><span><input type="radio" name="1" /></span><label>Red</label></li>
	                                    <li><span><input type="radio" name="1" /></span><label>Black</label></li>
	                                    <li><span><input type="radio" name="1" /></span><label>Grey/White</label></li>
	                                    <li><span><input type="radio" name="1" /></span><label>Other</label></li>
	                            	</ul>
	                            </div><!-- Answer 1 End -->
	                        </li><!-- Questioin 1 End -->
	                                                
	                        <!-- Questioin 2 Start -->
	                        <li>
	                        	<!-- Question 2 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>2. What chemical treatments have you had recently?</span>
	                            </div><!-- Question 2 End -->
	                            <!-- Answer 2 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="checkbox" /></span><label></label> Color/Highlights</li>
	                                    <li><span><input type="checkbox" /></span><label>Perm</label></li>
	                                    <li><span><input type="checkbox" /></span><label>Relaxer</label></li>
	                                    <li><span><input type="checkbox" /></span><label>None of the above</label></li>
	                            	</ul>
	                            </div><!-- Answer 2 End -->
	                        </li><!-- Questioin 2 End -->
	                        
	                        <!-- Questioin 3 Start -->
	                        <li>
	                        	<!-- Question 3 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>3. How thick is your hari?</span>
	                            </div><!-- Question 3 End -->
	                            <!-- Answer 3 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="3" /></span><label></label>Very thick</li>
	                                    <li><span><input type="radio" name="3" /></span><label></label>Thick</li>
	                                    <li><span><input type="radio" name="3" /></span><label></label>Average</li>
	                                    <li><span><input type="radio" name="3" /></span><label></label>Thin</li>
	                                    <li><span><input type="radio" name="3" /></span><label></label>Very thin</li>
	                                    <li><span><input type="radio" name="3" /></span><label></label>Not Sure</li>
	                            	</ul>
	                            </div><!-- Answer 3 End -->
	                        </li><!-- Questioin 3 End -->                        
	                        
	                        <!-- Questioin 4 Start -->
	                        <li>
	                        	<!-- Question 4 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>4. What is your hari texture?</span>
	                            </div><!-- Question 4 End -->
	                            <!-- Answer 4 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="4" /></span><label></label>Very coarse</li>
	                                    <li><span><input type="radio" name="4" /></span><label></label>Coarse</li>
	                                    <li><span><input type="radio" name="4" /></span><label></label>Average</li>
	                                    <li><span><input type="radio" name="4" /></span><label></label>Fine</li>
	                                    <li><span><input type="radio" name="4" /></span><label></label>Very fine</li>
	                                    <li><span><input type="radio" name="4" /></span><label></label>Not sure</li>
	                            	</ul>
	                            </div><!-- Answer 4 End -->
	                        </li><!-- Questioin 4 End -->                        
	                        
	                        <!-- Questioin 5 Start -->
	                        <li>
	                        	<!-- Question 5 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>5. What concerns you about your hair? Choose all that apply.</span>
	                            </div><!-- Question 5 End -->
	                            <!-- Answer 5 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="checkbox" /></span><label></label>Dryness</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Breakage/Split ends</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Color fading</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Oily</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Flyaways/Frizz</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Sun/Chiorine damage</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Lim/Lifelessness</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Dandruff/Imitated scaip</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Hair loss</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Thinning hair</li>
	                                    <li><span><input type="checkbox" /></span><label></label>None of the above</li>
	                            	</ul>
	                            </div><!-- Answer 5 End -->
	                        </li><!-- Questioin 5 End -->
	                        
	                        <!-- Questioin 6 Start -->
	                        <li>
	                        	<!-- Question 6 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>6. What do you want from your styling products?Choose all that apply.</span>
	                            </div><!-- Question 6 End -->
	                            <!-- Answer 6 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="checkbox" /></span><label></label>Volume</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Make waves curly</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Shine</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Hold</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Less frizz</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Tame curls</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Straighten</li>
	                                    <li><span><input type="checkbox" /></span><label></label>None of the above</li>
	                            	</ul>
	                            </div><!-- Answer 6 End -->
	                        </li><!-- Questioin 6 End -->                        
	                        
	                        <!-- Questioin 7 Start -->
	                        <li>
	                        	<!-- Question 7 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>7. What look are you going for?</span>
	                            </div><!-- Question 7 End -->
	                            <!-- Answer 7 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="7" /></span><label></label>Soft and natural</li>
	                                    <li><span><input type="radio" name="7" /></span><label></label>Wavy, natural curls</li>
	                                    <li><span><input type="radio" name="7" /></span><label></label>Stylized, flexible hold</li>
	                                    <li><span><input type="radio" name="7" /></span><label></label>Stylized, flm hold</li>
	                                    <li><span><input type="radio" name="7" /></span><label></label>None of the above</li>
	                            	</ul>
	                            </div><!-- Answer 7 End -->
	                        </li><!-- Questioin 7 End -->                        
	                        
	                        <!-- Questioin 8 Start -->
	                        <li>
	                        	<!-- Question 8 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>8. Are there any ingredients you'd like to avoid? Choose all that apply.</span>
	                            </div><!-- Question 8 End -->
	                            <!-- Answer 8 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="checkbox" /></span><label></label>Fragrance</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Lanolin</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Parabens</li>
	                                    <li><span><input type="checkbox" /></span><label></label>Sulfates</li>
	                            	</ul>
	                            </div><!-- Answer 8 End -->
	                        </li><!-- Questioin 8 End -->                        
	                        
	                        <!-- Questioin 9 Start -->
	                        <li>
	                        	<!-- Question 9 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>9. What are are you?</span>
	                            </div><!-- Question 9 End -->
	                            <!-- Answer 9 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="9" /></span><label></label>Under25</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>25-35</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>36-46</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>46-55</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>56-65</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>66-75</li>
	                                    <li><span><input type="radio" name="9" /></span><label></label>76+</li>
	                            	</ul>
	                            </div><!-- Answer 9 End -->
	                        </li><!-- Questioin 9 End -->                        
	                        
	                        <!-- Questioin 10 Start -->
	                        <li>
	                        	<!-- Question 10 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>10. What ethnicity best describes you?</span>
	                            </div><!-- Question 10 End -->
	                            <!-- Answer 10Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="10" /></span><label></label>African American</li>
	                                    <li><span><input type="radio" name="10" /></span><label></label>Asian</li>
	                                    <li><span><input type="radio" name="10" /></span><label></label>Caucasian</li>
	                                    <li><span><input type="radio" name="10" /></span><label></label>Hispanic/Latino</li>
	                                    <li><span><input type="radio" name="10" /></span><label></label>Other</li>
	                            	</ul>
	                            </div><!-- Answer 10 End -->
	                        </li><!-- Questioin 10 End -->                        
	                        
	                        <!-- Questioin 11 Start -->
	                        <li>
	                        	<!-- Question 11 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>11. What is your gender?</span>
	                            </div><!-- Question 11 End -->
	                            <!-- Answer 11 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="11" /></span><label></label>Female</li>
	                                    <li><span><input type="radio" name="11" /></span><label></label>Male</li>
	                            	</ul>
	                            </div><!-- Answer 11 End -->
	                        </li><!-- Questioin 11 End -->                        
	                        
	                        <!-- Questioin 12 Start -->
	                        <li>
	                        	<!-- Question 12 Start -->
	                        	<div class="question">
	                            	<span class="star"></span>
	                                <span>12. Last one!Do you prefer natural and/or organic ingredients?</span>
	                            </div><!-- Question 12 End -->
	                            <!-- Answer 12 Start -->
	                            <div class="answers">
	                            	<ul>
	                            		<li><span><input type="radio" name="12" /></span><label></label>Yes</li>
	                                    <li><span><input type="radio" name="12" /></span><label></label>No</li>
	                            	</ul>
	                            </div><!-- Answer 12 End -->
	                        </li><!-- Questioin 12 End -->
	                        
	                	</ul>
	                </div><!-- Questions Content End -->
	                
	                <!-- Submit Button Start -->
	                <div class="questioner-submit-button"><a href="#"></a></div><!-- Submit Button End -->
	                
	            </div><!-- Questioner Outer End -->
	            
	        </div><!-- Body Content End -->
            
            <!-- Please Note Start -->
            <div id="please-note">
            	<strong>PLEASE NOTE:</strong> Information on this site is not meant to be a substitute for the advice from a medical professional. Prior to starting a new regimen, you should consult your own physician, particularly if you are currently using any medications, are pregnant or nursing. Test a small amount of each new product on a hidden area (Such as behind your ear) to make sure that your skin does not have any adverse reactions. Always follow the recommended application quantity and frequency for each product.
            </div><!-- Please Note End -->
            
            <!-- The world of beauty online start -->
            <div id="the-world-of-beauty"><img src="images/the-world-of-beauty.png" alt="" /></div><!-- The world of beauty online End -->
            
        </div><!-- Body Content Outer End-->
        
    </div><!-- Container End -->
<jsp:include page="footer.html" flush="true"/>
</body>
</html>