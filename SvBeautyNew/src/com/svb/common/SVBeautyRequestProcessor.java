/*
 * Created on Oct 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.svb.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.RequestProcessor;

/**
 * @author administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class SVBeautyRequestProcessor extends RequestProcessor {
	protected boolean processPreprocess(HttpServletRequest request,HttpServletResponse response) {
			HttpSession session = request.getSession(false);
		//logger.log(Level.DEBUG,"In request processor");
		try{
		//If no redirect user to login Page
	  //  response.addHeader("Pragma", "max-age = 3600");
	  //	response.addHeader("Expires", "3600");
	 //	response.addHeader("Cache-Control", "max-age = 3600");


		if(request.getRequestURL().toString().indexOf("pbsdemo.com")> 0 ){
		  return true;
		}

		System.out.println("vb test "+request.getServletPath()) ;
		
		if( request.getServletPath().equals("/productanalysis.do")  )
			return true;
		/**
		 * Added By Synoptek
		 * To address jsp to related struts action
		 * */
	// ************************************** Added by Synoptek(29-11-2019)  ********************************************* 
	//------------------------------------------start------------------------------------------------
		if( request.getServletPath().equals("/ingredientScore.do")  ) {
			return true;
		}
	//------------------------------------------end------------------------------------------------
		
		if( request.getServletPath().equals("/prodanalysisdetail.do") || request.getServletPath().equals("/home.do") ||  request.getServletPath().equals("/VGRegistrationAction.do") || request.getServletPath().equals("/CustomerRegistrationAction.do") || request.getServletPath().equals("/BeautyProfileRegistration.do") || request.getServletPath().equals("/beautyprofilehome.do"))
			return true;
		if(request.getServletPath().equals("/brandSelect.do") || request.getServletPath().equals("/webunsubcustomer.do") || request.getServletPath().equals("/personalcontactinfo.do") )
			return true;
		if( request.getServletPath().equals("/ingredients.do")  )
			return true;
		if( request.getServletPath().equals("/regimenDetail.do")  )
			return true;
		if( request.getServletPath().equals("/ingredientSearchAction.do")  )
			return true;
		if( request.getServletPath().equals("/brandDisc.do")  )
			return true;
		if( request.getServletPath().equals("/brandList.do")  )
			return true;
		if( request.getServletPath().equals("/productDetails.do")  )
			return true;
		if( request.getServletPath().equals("/profileDisc.do")  )
			return true;
		if( request.getServletPath().equals("/productDescPdfGenerator.do")  )
			return true;
		if( request.getServletPath().equals("/CustomerRegistrationAction.do")  )
			return true;
		if( request.getServletPath().equals("/customerselfprofileskinconcerns.do")  )
			return true;
		if( request.getServletPath().equals("/customerselfprofileNewskinconcerns.do")  )
			return true;
		/** Added by Synoptek(30-12-2019) [Excel export Action ] */ 
		/********************Start*******************************/
		if( request.getServletPath().equals("/exportProductWithIngrScore.do")  )
			return true;
		if( request.getServletPath().equals("/exportJsonRequestResponse.do")  )
			return true;
		/********************END*******************************/
		if( request.getServletPath().equals("/customerselfprofilehairconcerns.do")  )
			return true;
		if( request.getServletPath().equals("/regimenmail.do")  )
			return true;
		if( request.getServletPath().equals("/regimenshop.do")  )
			return true;
		if( request.getServletPath().equals("/regimenmailsent.do")  )
			return true;
		if( request.getServletPath().equals("/productregimen.do")  )
			return true;
		if( request.getServletPath().equals("/regimendetail.do")  )
			return true;
		if( request.getServletPath().equals("/hairregimenmail.do")  )
			return true;
		if( request.getServletPath().equals("/hairregimenshop.do")  )
			return true;
		if( request.getServletPath().equals("/hairregimenmailsent.do")  )
			return true;
		if( request.getServletPath().equals("/hairproductregimen.do")  )
			return true;
		if( request.getServletPath().equals("/hairregimendetail.do")  )
			return true;
		if( request.getServletPath().equals("/hairProductDetails.do")  )
			return true;
		if( request.getServletPath().equals("/hairproductanalysis.do")  )
			return true;
		if( request.getServletPath().equals("/hairprodanalysisdetail.do"))
			return true;
		if( request.getServletPath().equals("/hairproductDescPdfGenerator.do")  )
			return true;

		////logger.log(Level.DEBUG,"In request processor");

		//Check if userName attribute is there is session. If yes then that means user has allready loggedIn
		if( session != null && session.getAttribute("repid")!=null && !((String)session.getAttribute("repid")).trim().equals("null") &&  ((String)session.getAttribute("repid")).trim().length()>0 ){
			return true;
			////logger.log(Level.DEBUG,"In request processor");
		}else if(session==null || session.getAttribute("repid") == null){
			if (session.getAttribute("HeaderRequestObjectValues") == null)
			{
				//logger.log(Level.DEBUG,"ERROR: Oops! Session timed out, go back to your home page");
				System.out.println("Oops! Session timed out, go back to your home page");
				request.setAttribute("sessionMessage","Sorry, Your session expired!");
				if(request.getRequestURL().toString().indexOf("avoncustomerbeauty.com")> 0 ){
			      request.getRequestDispatcher("/customerregistration.jsp").forward(request,response);
				} else if(request.getRequestURL().toString().indexOf("avonvirtualgrandopening.com")> 0 ){
				  request.getRequestDispatcher("/vgregistration.jsp").forward(request,response);
				} else
			      request.getRequestDispatcher("/avonpersonalbeauty.jsp").forward(request,response);

			return false;
			}else
				return true;

		}}catch(Exception ex){
		}
		return true;
	}

	protected void processContent(HttpServletRequest request,HttpServletResponse response) {
			//Check if user is requesting ContactImageAction if yes then set image/gif as content type
			if( request.getServletPath().equals("/contactimage.do")){
				response.setContentType("image/gif");
				return;
			}
		super.processContent(request, response);
	}
}
