/*
 * Created on Mar 11, 2002
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common;

import java.security.acl.Group;

import org.omg.PortableInterceptor.NON_EXISTENT;
import org.xhtmlrenderer.swing.NoNamespaceHandler;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConstantDeclarations {

	/*
	 * Common
	 *
	 */

	public static final String CURRENT_DUE  	  = "100";
	public static final String THIS_WEEK	 	  = "101";
	public static final String TWO_WEEKS		  = "102";
	public static final String THIS_MONTH		  = "103";
	public static final String THREE_MONTHS		  = "104";
	public static final String SIX_MONTHS		  = "105";
	public static final String ONE_YEAR			  = "106";
	public static final String ALL				  = "99";
	public static final String HOME_PHONE         = "CM01";
	public static final String WORK_PHONE		  = "CM02";
	public static final String MOBILE_PHONE		  = "CM03";
	public static final String EMAIL			  = "CM04";
	public static final String VISIT			  = "CM05";
	public static final String ANY  			  = "CM99";
	/**
	* Added by Synoptek 
	* Constants with assigned value 
	*/
	public static final String ZERO_INGR_SCO_ALLERGY = "Zero Ingredient Score and Allergy";
	public static final String ZERO_INGR_SCO= "Zero Ingredient Score";
	public static final String ALLERGY = "Allergy";
	public static final String ZERO_FORMU_SCO= "Zero Formulation Score";
	public static final String FORM_NOT_FOUND= "Formulation not found";
	public static final String CAT_BRND_COMBINATION= "Category-Sub_Category-Brand Combination";
	public static final String COMBNTION_NOT_FOUND= "Not Found in Category-Sub_Category-Brand Combination";
	public static final String ELIMINATION_RULE= "Elimination Rule";
	public static final String INGR_NOT_FOUND= "Ingredient Not Found";
	public static final String PROD_SCORE_SUCCESS = "Product Scored Successfully";
	public static final String PROD_NOT_FOUND = "Product Not Found";
	
	public static final String ZERO_INGR_SCO_ALLERGY_CODE = "ERROR_ZERO_SCORE_ALLERGY";
	public static final String ZERO_INGR_SCO_CODE = "ERROR_ZERO_INGREDIENT_SCORE";
	public static final String ALLERGY_CODE = "ERROR_ALLERGY";
	public static final String ZERO_FORMU_SCO_CODE = "ERROR_ZERO_FORMULATION_SCORE";
	public static final String FORM_NOT_FOUND_CODE = "ERROR_FORMULATION_NOT_FOUND";
	public static final String CAT_BRND_COMBINATION_CODE = "ERROR_CAT_SUBCAT_BRAND";
	public static final String COMBNTION_NOT_FOUND_CODE = "ERROR_NOT_FOUND_CAT_SUBCAT_BRAND";
	public static final String ELIMINATION_RULE_CODE = "ERROR_ELIMINATION";
	public static final String INGR_NOT_FOUND_CODE = "ERROR_INGREDIENT_NOT_FOUND";
	public static final String PROD_SCORE_SUCCESS_CODE = "SUCCESS";
	public static final String PROD_NOT_FOUND_CODE = "ERROR_NOT_FOUND";


	public static final Float SCORE_MORE_THAN = 50.0f;
	public static final String SKIN_CONDITION_OA 	= "D008";
	public static final String SKIN_AGE_UNDER25 	= "G004";
	public static final String SKIN_AGE_BET56to65 	= "G005";
	public static final String SKIN_AGE_BET56to76 	= "G006";
	public static final String SKIN_AGE_OVER76 	= "G007";

	/*API related constants define*/
	// Skin concern selection
	public static final String SKIN_CONCERN_SELECTION_AGESPOT 	= "C007";
	public static final String SKIN_CONCERN_SELECTION_REDNESS 	= "C019";
	public static final String SKIN_CONCERN_SELECTION_DECONECK	= "C030";
	public static final String SKIN_CONCERN_SELECTION_PERSACNE 	= "D002";
	public static final String SKIN_CONCERN_SELECTION_HYPIG 	= "D006";
	public static final String SKIN_CONCERN_SELECTION_OCCACNE 	= "D008";
	public static final String SKIN_CONCERN_SELECTION_FLAKINESS_DRYNESS 	= "C025";//Added by synoptek 01-18-2021 we show a night moisturizer, 1) Dry Skin or 2) Dryness/Dehydration).
	//Added 01-18-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol" - Start
	public static final String DOESNOT_CONTAIN_RENTIOL= "CE03";
	public static final String SKIN_TAKING_RENTIOL= "L015";
	//Added 01-18-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol" - End

	// When to use
	public static final String WHEN_TO_USE_DLYCLE = "Daily Cleanser";
	public static final String WHEN_TO_USE_DLYMOI = "Daily Moisturizer";
	public static final String WHEN_TO_USE_DLYTREAT = "Daily Treatment";
	public static final String WHEN_TO_USE_DLYMOISPF = "Daily Moisturizer with SPF";
	public static final String WHEN_TO_USE_NGHTLYMOI = "Nightly Moisturizer";
	public static final String WHEN_TO_USE_TRGTREAT = "Targeted Treatment";
	public static final String WHEN_TO_USE_WKLYTREAT = "Weekly Treatment";
	public static final String WHEN_TO_USE_DLYEYETREAT = "Daily Eye Treatment";
	public static final String WHEN_TO_USE_WKLYEYETREAT = "Weekly Eye Treatment";
	public static final String WHEN_TO_USE_DLYFCSUN = "Daily Facial Sunscreen";
	public static final String WHEN_TO_USE_NGHTLYTREAT = "Nightly Treatment";
	public static final String WHEN_TO_USE_TRGEXFOLI = "Targeted Exfoliator";
	
	public static final String DAILY = "Daily";
	public static final String WEEKLY = "Weekly";
	public static final String ATHOMEWEEKLY = "At Home Weekly";


	public static final String TARGETEDONE = "Targeted-1";
	public static final String TARGETEDTWO = "Targeted-2";
	// Multi - Benefit attibute
	public static final String MULBENIFIT_MULTIFUNCLE = "Multi-Functional Cleanser";
	public static final String MULBENIFIT_NOSPF = "Does not have SPF";
	public static final String MULBENIFIT_HASSPF = "Has SPF";

	public static final String REGULAR_ACTIVITIES_EXCERSISEOFTEN = "RE01";
	public static final String REGULAR_ACTIVITIES_LOTSCOMPSCNTIME = "RE02";
	public static final String REGULAR_ACTIVITIES_TRAVELBYAIR = "RE03";

	
	// Life style based RecEngine2 attribute
	public static final String LIFESTYLEBASEDRECS2_LOTSCOMPSCNTIME = "lots of computer screen time";

	// Product Sub Category
	public static final String PRECLEANSE = "1001";
	public static final String FWASHCLEANSE = "1002";
	public static final String TONMIST = "1004";
	public static final String FACIAL_WIPES = "1005";
	public static final String MOISTURIZERS = "1006";
	public static final String NIGHTCREAMS = "1007";
	public static final String EYETREATSERUM= "1012";
	public static final String EYEMASK= "1013";
	public static final String ACNESPOT = "1015";
	public static final String MASKS = "1016";
	public static final String PEELS = "1017";
	public static final String SERUM = "1018";
	public static final String AGESPOTSSUNDAMAGE = "1019";
	public static final String EXFOLIATORS = "1020";
	public static final String ACNE = "1021";
	public static final String BOOSTERS = "1022";
	public static final String DECNECK = "1024";
	public static final String FCSUNSCREEN = "1029";
	public static final String REDNESS = "1037";
	public static final String SERUMSHYDRATOR ="1038";
	public static final String SERUM_RETINOL ="1039";
	public static final String SERUMSVITAMINC ="1040";
	public static final String OILBASE="0015";

	
	public static final String NO_ALLERGY_PREFERENCE = "L999";//Added by Shivam For No Allergy Preference - 25-08-2023
	//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - Start
	public static final String SKIN_PRODUCT = "SKIN";
	public static final String EYE_PRODUCT = "EYE";
	public static final String ANY_SKIN_TYPE = "ANY";
	//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - End
	
	//Added by Shivam For Display Rule changes - 23/10/2023 - Start
	public static final float SCORE_80 = 70.0f; //70 before adding 10 to the final score
	//Added by Shivam For Display Rule changes - 23/10/2023 - End
	
	// Add/Deduct Scores as per Business Rule Changes - Shivam - 09/10/2024 - Start
	public static final float CLEANER_SCORE_80 = 65f; //65 before adding 5 to the final score
	public static final float MOISTURIZER_SCORE_80 = 65f; //65 before adding 5 to the final score
	public static final float TREATMENT_SCORE_80 = 60f; //60 before adding 10 to the final score
	public static final float EYE_SCORE_80 = 70f;
	// Add/Deduct Scores as per Business Rule Changes - Shivam - 09/10/2024 - End
	
	public static final String SKIN_SPECIFICATION_PREGNANT_NURSING = "CE01";//Added to display RX Warning Text for Pregnant/Nursing concern - Shivam - 20/03/2024

	/*
	 * Customer Registration Section
	 * */
//	public static final String VALIDATION_EMAIL="The Avon Representative Email address entered is invalid. Please enter a valid Email";
	public static final String VALIDATION_EMAIL = "The Avon Representative Email entered is invalid. Please enter a valid Avon rep Email address.";
	public static final String VALIDATION_PROFILE = "You are already registered. Do you want to update your profile?";
	//public static final String EMAILTOREP_SUBJECT = "New Customer Registration";
	public static final String EMAILTOREP_SUBJECT = "completed Beauty Profile";
	public static final String EMAILORDERTOREP_SUBJECT = "wants to order";
	public static final String EMAILCOLORORDERTOREP_SUBJECT = "wants more information";
	public static final String CUST_ENQUIRY_EMAIL_SUBJECT = "interested in Avon Opportunity";
	public static final String REMINDERPERIOD = "15";

	public String MessageBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname)
	{
		String EMAILTOREP_MESSAGE = "Dear "+repname+",\n\nYour Customer, "+fname + " "+lname+", just entered his/her profile information at www.avoncustomerbeauty.com!"+
		                            "\n\nYour Customer's email address is "+CustomerEmail+"\n\nTo see this Customer's profile, find the Customer name in your Customer"+
									" Database section of your Avon Personal Beauty System.  Click on 'View Profile'. The profile information your Customer just provided "+
									"will automatically be there.\n\nIn the same section, click on 'Show Regimen'.  "+
									"This will show you the regimens (all Brands) presented to your Customer.\n\nHowever, if you have chosen 'Never Show Regimen' "+
									"(in your Selling Tools - Invite Customers to Self Profile), your Customer did not see a product regimen.  Instead, the Customer "+
									"saw a page indicating that you will be in touch.\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String ABPMessageBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname)
	{
		String EMAILTOREP_MESSAGE = "Dear "+repname+",\n\nYour Customer, "+fname + " "+lname+", just entered his/her profile information at www.avonbeautyprofile.com!"+
		                            "\n\nYour Customer's email address is "+CustomerEmail+"\n\nTo see this Customer's profile, find the Customer name in your Customer"+
									" Database section of your Avon Personal Beauty System.  Click on 'View Profile'. The profile information your Customer just provided "+
									"will automatically be there.\n\nIn the same section, click on 'Show Regimen'.  "+
									"This will show you the regimens (all Brands) presented to your Customer.\n\nHowever, if you have chosen 'Never Show Regimen' "+
									"(in your Selling Tools - Invite Customers to Self Profile), your Customer did not see a product regimen.  Instead, the Customer "+
									"saw a page indicating that you will be in touch.\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String VGMessageBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname)
	{
		String EMAILTOREP_MESSAGE = "Dear "+repname+",\n\nYour Customer, "+fname + " "+lname+", just entered his/her profile information at www.avonvirtualgrandopening.com!"+
		                            "\n\nYour Customer's email address is "+CustomerEmail+"\n\nTo see this Customer's profile, find the Customer name in your Customer"+
									" Database section of your Avon Personal Beauty System.  Click on 'View Profile'. The profile information your Customer just provided "+
									"will automatically be there.\n\nIn the same section, click on 'Show Regimen'.  "+
									"This will show you the regimens (all Brands) presented to your Customer.\n\nHowever, if you have chosen 'Never Show Regimen' "+
									"(in your Selling Tools - Invite Customers to Self Profile), your Customer did not see a product regimen.  Instead, the Customer "+
									"saw a page indicating that you will be in touch.\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String ANEWMessageBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname, String repid)
	{
		String text1 = "";
		if(repid.equals("newrep")) {
			text1 = "\n\nIf you haven't tried Avon's Personal Beauty System, you are "+
					"invited to a free 30-day trial of this amazing new tool. Just go "+
					"to youravon.com, and click the Personal Beauty System link to begin "+
					"your free trial. Your Customers that visit the site during the "+
					"Anew Through the Ages event will appear on your Customer list. "+
					"You will be able to use this tool to build your sales, and "+
					"better service your Customers. The trial is free, with no obligation "+
					"or automatic billing.";
		}
		String EMAILTOREP_MESSAGE = "Dear "+repname+",\n\nYour Customer, "+fname + " "+lname+", just entered her profile information at www.anewthroughtheages.com!"+
									text1+
		                            "\n\nYour Customer's email address is "+CustomerEmail+"\n\nTo see this Customer's profile, find the Customer name in your Customer"+
									" Database section of your Avon Personal Beauty System.  Click on 'View Profile'. The profile information your Customer just provided "+
									"will automatically be populated.\n\nVisit the PBS site today. We think you will love it! "+
									"And don't forget to contact your Customers to take "+
									"their orders for the Anew Through The Ages product "+
									"bundles being offered in C3 and C4."+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String CustomerEnquiryVGOBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname,String visited)
	{
		String str = "";
		if(visited.equals("yes"))
			str = "viewed";
		else
			str = "did not view";

		String EMAILTOREP_MESSAGE = "Dear "+repname+","+
									"\n\nYour Customer, "+fname + " "+lname+", came to the Personal Beauty System at www.avonvirtualgrandopening.com "+
									"and has requested information about the opportunities at AVON. Your "+
									"customer "+str+" the Leadership video before clicking \"Tell me more.\""+
		                            "\n\nYour Customer's email address is "+CustomerEmail+
									"\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String CustomerEnquirySelfProfileBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname,String visited)
	{
		String str = "";
		if(visited.equals("yes"))
			str = "viewed";
		else
			str = "did not view";

		String EMAILTOREP_MESSAGE = "Dear "+repname+","+
									"\n\nYour Customer, "+fname + " "+lname+", came to the Personal Beauty System at www.avoncustomerbeauty.com "+
									"and has requested information about the opportunities at AVON. Your "+
									"customer "+str+" the Leadership video before clicking \"Tell me more.\""+
		                            "\n\nYour Customer's email address is "+CustomerEmail+
									"\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	public String CustomerEnquiryAnewSelfProfileBody(String fname,String lname,String CustomerId,String CustomerEmail, String repname,String visited)
	{
		String str = "";
		if(visited.equals("yes"))
			str = "viewed";
		else
			str = "did not view";

		String EMAILTOREP_MESSAGE = "Dear "+repname+","+
									"\n\nYour Customer, "+fname + " "+lname+", came to the Personal Beauty System at www.anewthroughtheages.com "+
									"and has requested information about the opportunities at AVON. Your "+
									"customer "+str+" the Leadership video before clicking \"Tell me more.\""+
		                            "\n\nYour Customer's email address is "+CustomerEmail+
									"\n\nWe're sure your Customer will be excited to hear from you!"+
                                    "\n\nWarmest Regards, \nAvon Personal Beauty System"+
									"\n\nTHIS IS AN AUTOMATED MESSAGE FROM THE PERSONAL BEAUTY SYSTEM.\n\nDo not reply to this message unless you want to reach the Personal Beauty System's Customer Service Department."+
									"\n\nTo send a response email to your Customer, copy and paste the Customer's email address in a new email message window.";
		return EMAILTOREP_MESSAGE;
	}

	/*
	 * Constant Declaration for Selling Tools Section
	 *
	 */

	/*
	 * Constant Declaration for Selling Tools Reminder Section
	 *
	 */

	public static final String REMINDER_STATUS_COMPLETE_ID = "C";
	public static final String REMINDER_STATUS_DELETE_ID   = "D";
	public static final String REMINDER_STATUS_ACTIVE_ID   = "N";
	public static final String REMINDER_ACTION_DELETE      = "delete";
	public static final String PRODUCT_DEAL_MAIL_SUBJECT   = "Product Deal";
	public static final String REMINDER_ACTION_COMPLETE    = "complete";
	public static final String EMAIL_STATUS_NOT_SEND_ID    = "N";
	public static final String REMINDER_TYPE_BIRTHDAY      = "1002";
	public static final String REMINDER_TYPE_ANNIVERSAY    = "1001";
	public static final String REMINDER_TYPE_PRE_SCHEDULED_FOLLOWUP      = "1003";
	public static final String REMINDER_TYPE_REPLENISHMENT_FOLLOWUP    = "1004";
	public static final String REMINDER_TYPE_SELFPROFILED_FOLLOWUP      = "1005";
	public static final String CONTACT_TYPE_ATTRIB_ID      = "61";
	public static final String NO_REMINDERS_MSG            = " There are no Reminders in your Reminders List at this time";


	/*
	 * Constant Declarations for Selling Tools Correspondance Section
	 *
	 */
	public static final String EMAIL_STATUS_SEND_ID 	= "S";
	public static final String EMAIL_STATUS_DELETE_ID   = "D";
	public static final String EMAIL_STATUS_ACTIVE_ID   = "N";

	public static final String EMAIL_ACTION_DELETE      = "delete";
	public static final String EMAIL_ACTION_SEND        = "send";
	public static final String EMAIL_SORT_FIRST_NAME    = "firstName";
	public static final String EMAIL_SORT_LAST_NAME     = "lastName";
	public static final String EMAIL_SORT_SUBJECT       = "subject";
	public static final String DEFAULT_PROD_LINE 		= "02";
	public static final String DEFAULT_PROD_TYPE 		= "99";
	public static final String DEFAULT_OLD_NEW	 		= "0";

	/*
	 * Constant Declarations for Get Products Section
	 *
	 */
	public static final String SKIN_TYPE_ID 	= "51";
	public static final String SKIN_CONDITION_ID 	= "52";
	public static final String SKIN_CONCERN_ID 	= "53";
	public static final String HAND_CONCERN_ID 	= "58";
	public static final String REGIMEN_PRODUCT_USAGE 	= "Q012";
	public static final String SKIN_AGE_ID 	= "55";
	public static final String SKIN_ALLERGY_ID 	= "56";
	public static final String SKIN_TYPE_DRY 	= "T001";
	public static final String SKIN_TYPE_NORMAL 	= "T002";
	public static final String SKIN_TYPE_NORMAL_OILY 	= "T003";
	public static final String SKIN_TYPE_OILY 	= "T004";
	public static final String SKIN_CONDITION_SS 	= "D001";
	public static final String SKIN_CONDITION_AA 	= "D002";
	public static final String SKIN_CONDITION_RS 	= "D003";
	public static final String SKIN_CONDITION_AD 	= "D005";
	public static final String SKIN_CONDITION_PC 	= "D006";
	public static final String SKIN_CONCONCERN_FLE 	= "C001";
	public static final String SKIN_CONCONCERN_DCE 	= "C002";
	public static final String SKIN_CONCONCERN_FFH 	= "C003";
	public static final String SKIN_CONCONCERN_DSK 	= "C004";
	public static final String SKIN_CONCONCERN_ASF 	= "C007";
	public static final String SKIN_CONCONCERN_NWR 	= "C009";
	public static final String SKIN_CONCONCERN_ALP 	= "C010";
	public static final String SKIN_CONCONCERN_ASH 	= "C011";
	public static final String SKIN_CONCONCERN_EFP 	= "C012";
	public static final String SKIN_CONCONCERN_FWS 	= "C013";
	public static final String HB_CONCONCERN_CEL 	= "H001";
	public static final String HB_CONCONCERN_LAS 	= "H002";
	public static final String HB_CONCONCERN_ASH 	= "H003";
	public static final String HB_CONCONCERN_BSM 	= "H004";
	public static final String HB_CONCONCERN_SM 	= "H005";
	public static final String HB_CONCONCERN_LOT 	= "H006";
// Get RX flag from the sv_product_rx_warning for ingredients in the product - start
	public static final String SKIN_CONDITION_ACNE 	= "CE05"; 
	public static final String SKIN_CONDITION_STERIODS 	= "CE04"; 
	public static final String SKIN_CONDITION_RETINOL 	= "CE03";
//Get RX flag from the sv_product_rx_warning for ingredients in the product - end
	public static final String SKIN_NO_CONCERNS = "C999";

	public static final String SKIN_AGE_30 	= "G001";
	public static final String SKIN_AGE_45 	= "G002";
	public static final String SKIN_AGE_55 	= "G003";

	public static final String ATTRIBUTE_BASE = "01";
	public static final String ATTRIBUTE_INGR = "02";

	public static final String USAGE_QUERY_CLEANSE 	  = "Q001";
	public static final String USAGE_QUERY_TONE 	  = "Q002";
	public static final String USAGE_QUERY_EXFOLI 	  = "Q007";
	public static final String USAGE_QUERY_NIGHTMOI	  = "Q004";
	public static final String USAGE_QUERY_REGIMEN	  = "Q010";
	public static final String USAGE_QUERY_ETREAT     = "Q015";
	public static final String USAGE_QUERY_SHAVECREAM = "Q011";
	public static final String USAGE_QUERY_SHAVEBALM  = "Q013";

	public static final String USAGE_ANSWER_YES	     = "A008";
	public static final String USAGE_ANSWER_NO	     = "A009";
	public static final String USAGE_ANSWER_BASIC	 = "A016";
	public static final String USAGE_ANSWER_COMPLETE = "A017";
	public static final String USAGE_ANSWER_DAILY	 = "A004";
	public static final String USAGE_ANSWER_WEEKLY	 = "A005";
	public static final String USAGE_ANSWER_DONTUSE	 = "A007";

	public static final String PRODUCT_MASTER_TYPE_CLEANSE	  = "0001";
	public static final String PRODUCT_MASTER_TYPE_MOIST	  = "0003";
	public static final String PRODUCT_MASTER_TYPE_TREAT	  = "0005";
	public static final String PRODUCT_MASTER_TYPE_ETREAT	  = "0004";
	public static final String PRODUCT_MASTER_TYPE_TONE	      = "03";
	public static final String PRODUCT_MASTER_TYPE_UV	      = "0009";
	public static final String PRODUCT_MASTER_TYPE_HAND   	  = "07";
	public static final String PRODUCT_MASTER_TYPE_BODY  	  = "08";
	public static final String PRODUCT_MASTER_TYPE_HANDBODY	  = "09";
	public static final String PRODUCT_MASTER_TYPE_EXFOLI	  = "11";
	public static final String PRODUCT_MASTER_TYPE_SHAVECREAM = "12";
	public static final String PRODUCT_MASTER_TYPE_SHAVEBALM  = "13";
	public static final String PRODUCT_MASTER_TYPE_ALL   	  = "99";
	public static final String PRODUCT_MASTER_TYPE_SHAVE 	  = "SHAVE";
	public static final String PRODUCT_MASTER_TYPE_PRIMER 	  = "0012";

	public static final String PRODUCT_TYPE_CLEAN		= "0005";
	public static final String PRODUCT_TYPE_MOIST_DAY	= "0001";
	public static final String PRODUCT_TYPE_MOIST_DN	= "0003";
	public static final String PRODUCT_TYPE_MOIST_NIGHT	= "0002";
	public static final String PRODUCT_TYPE_TONER		= "0008";
	public static final String PRODUCT_TYPE_ETREAT_DAY	= "0012";
	public static final String PRODUCT_TYPE_ETREAT_DN	= "0014";
	public static final String PRODUCT_TYPE_ETREAT_NIGHT	= "0013";
	public static final String PRODUCT_TYPE_LIP			= "0015";
	public static final String PRODUCT_TYPE_LIP_DN		= "0031";
	public static final String PRODUCT_TYPE_LIP_NIGHT	= "0030";
	public static final String PRODUCT_TYPE_FACE_DAY	= "0009";
	public static final String PRODUCT_TYPE_FACE_NIGHT	= "0010";
	public static final String PRODUCT_TYPE_FACE_DN	= "0011";
	public static final String PRODUCT_TYPE_MASK	= "0020";
	public static final String PRODUCT_TYPE_SCRUB	= "0021";

	public static final String PRODUCT_LINE_ANEW		= "0002";
	public static final String PRODUCT_LINE_AVON		= "0001";
	public static final String PRODUCT_LINE_CLSK		= "0003";
	public static final String PRODUCT_LINE_BCMG		= "0009";
	public static final String PRODUCT_LINE_MULT		= "9999";
	public static final String PRODUCT_LINE_ANEW_RETR	= "0015";
	public static final String PRODUCT_LINE_ANEW_ULTM	= "0012";
	public static final String PRODUCT_LINE_ANEW_TRPL	= "0008";
	public static final String PRODUCT_LINE_ANEW_CLNC	= "0014";
	
//	Group Function
	public static final String GROUP_FUNCTION_AS = "FG001";
	public static final String GROUP_FUNCTION_CI = "FG002";
	public static final String GROUP_FUNCTION_DA = "FG003";
	public static final String GROUP_FUNCTION_HP = "FG004";
	public static final String GROUP_FUNCTION_OE = "FG005";
	public static final String GROUP_FUNCTION_PARABENS = "FG006";
	public static final String GROUP_FUNCTION_PHTHALATES = "FG007";
	public static final String GROUP_FUNCTION_SF = "FG008";
	public static final String GROUP_FUNCTION_HC = "FG009";
	public static final String GROUP_FUNCTION_HA = "FG010";

//	Non Clean Group
	public static final String NONCLEAN_GROUP_FC = "NC001";
	public static final String NONCLEAN_GROUP_ETHANOLAMINES = "NC002";
	public static final String NONCLEAN_GROUP_FR = "NC003";
	public static final String NONCLEAN_GROUP_HD = "NC004";
	public static final String NONCLEAN_GROUP_SULFATES = "NC005";
	public static final String NONCLEAN_GROUP_SF = "NC006";
	public static final String NONCLEAN_GROUP_PHTHALATES = "NC007";
	public static final String NONCLEAN_GROUP_PARABENS = "NC008";
	
	
	/*
	 * Replenishement
	 *
	 */
	public static final String REPLENISHMENT_REQUIRED 	 		= "R";
	public static final String REPLENISHMENT_COMPLETE_ID 		= "C";
	public static final String REPLENISHMENT_DELETE_ID  		= "D";
	public static final String REPLENISHMENT_ACTION_DELETE      = "delete";
	public static final String REPLENISHMENT_ACTION_COMPLETE    = "complete";
    public static final String NO_REPLENISHMENTS_MSG            = " There are no Products/Customers in your Replenishment List at this time";

	/*
	 * Mail Section
	 */

	public static final String PRODUCT_RECOMMENDATION_MAIL_TYPE     = "C";
	public static final String PRODUCT_DEAL_MAIL_TYPE     = "Q";
	public static final String PRODUCT_RECOMMENDATION_MAIL_SUBJECT  = "Product Recommendation";
	public static final String PRODUCT_RECOMMENDATION_MAIL_NOTES    = "Product Recommendation <i>from Target List</i>";
	public static final String PRODUCT_REPLENISHMENT_MAIL_SUBJECT   = "Replenishment Product Reminder";
	public static final String PRODUCT_REPLENISHMENT_MAIL_TYPE      = "R";
	public static final String PRODUCT_REPLENISHMENT_MAIL_NOTES     = "Replenishment Product Reminder";



	/*
	 * Contact History Section
	 *
	 */

	public static final String CONTACT_MAIL      = "CT03";
	public static final String PRODUCT_PROMOTION = "CP01";
	public static final String REPLENISHMENT_PURPOSE = null;

	/*
	 * Invite Section
    */

	public static final String INVITE_CONTACT_TYPE_ID 		=	"CT04";
	public static final String INVITE_CONTACT_PURPOSE_ID	=	"CP06";
	public static final String INVITE_CONTACT_NOTES			=	"Invited Customer To Self profile";
	public static final String INVITE_MAIL_SUBJECT			=	"Self Profile";
	public static final String INVITE_MAIL_TYPE				=	"I";
	public static final String INVITE_MAIL_FLAG				=	"S";

	public static final String VGO_CONTACT_PURPOSE_ID	=	"CP07";
	public static final String VGO_MAIL_SUBJECT			=	"Virtual Grand Opening";
	public static final String VGO_MAIL_TYPE		    =	"G";
	public static final String VGO_CONTACT_NOTES		=	"Invited Customer To Virtual Grand Opening";





	public static final String PRODUCT_LINE_PRIME_AVON = "01";
	public static final String PRODUCT_LINE_PRIME_ANEW = "02" ;
	public static final String PRODUCT_LINE_PRIME_CLSK = "03" ;
	public static final String PRODUCT_LINE_PRIME_BCMG = "04";
	public static final String PRODUCT_LINE_PRIME_MENS = "MS";

	public static final String PRODUCT_USAGE_NATING = "P001";
	public static final String PRODUCT_USAGE_CUTEDT = "P002";
	public static final String PRODUCT_USAGE_AGEBNF = "P003";
	public static final String PRODUCT_USAGE_PRDSKT = "P004";
	public static final String PRODUCT_USAGE_BTFPKG = "P005";
	public static final String PRODUCT_USAGE_ATHDTR = "P006";

	public static final String CUSTOMER_REGIMEN     = "Y";
	public static final String NO_CUSTOMER_REGIMEN  = "N";
	//public static final String MAIL_SENT_TEXT       = "The ability to send real time emails will be functional beginning May 22nd";
	public static final String MAIL_SENT_TEXT       = "Your email has been sent";
	public static final String MAIL_SERVER_DOMAIN   =  "avonpersonalbeauty.com" ; //"stylevalet.com";
	//public static final String MAIL_HOST		    =   "209.130.126.54"; //"172.20.9.20" ;
	public static final String MAIL_HOST		    =   "172.20.9.20" ;
	//public static final String MAIL_MANAGER         =  "mailmanager@avonpersonalbeauty.com" ; //"mailmanager@stylevalet.com";
	public static final String MAIL_MANAGER         =  "AvonBeauty@avonpersonalbeauty.com" ; //"mailmanager@stylevalet.com";
	public static final String CUSTOMER_SERVICE     =  "customerservice@avonpersonalbeauty.com" ; //"mailmanager@stylevalet.com";
	//public static final String MAIL_SERVER_DOMAIN   = "avonpersonalbeautysneakpeek.com";

	public static final String TUTORIAL_HOST        = "www.avonpersonalbeauty.com";
	
	public static final String REGULAR_ACTIVITIES_NOMAKEUP = "MF01";//Added By Synoptek 01-18-2021 if customer select little or No make up then do not recomded Pre-clense
	public static final String REGULAR_ACTIVITIES_MAKEUP ="MF03";
	public static final String REGULAR_MAKEUP_MELT ="818951021353";
	
	//Score Messages
	public static final String GREAT_SCORE = "Great!";
	public static final String VERY_GOOD_SCORE = "Very Good!";
	public static final String GOOD_SCORE = "Good!";
	public static final String JUST_OK_SCORE = "Just Ok!";
	public static final String NOT_GOOD_SCORE = "Not Good!";

	public static final int IDEAL_FETCH_SIZE = 2000;//Added to improve performance of loop iterations - Shivam - 01/08/2024
}
