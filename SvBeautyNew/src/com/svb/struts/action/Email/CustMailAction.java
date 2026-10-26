/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/
package com.svb.struts.action.Email;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.struts.form.CustMailForm;
import com.svb.struts.service.Email2FrdService;

public class CustMailAction extends Action {

	
	

	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
		
		CustMailForm cmf= (CustMailForm)form;
		Email2FrdService service=new Email2FrdService();
		
		   String target = "failure";
		   String to="";
		   String from="";
		   String pass="";
		   String filepath="";
		   String var="";
		   String toname="";
		   String fromname="";
		   
		   
		   to=cmf.getTo_email();
		   from=cmf.getFrom_email();
		   pass=cmf.getPassword();
		   filepath=cmf.getAttachfile();
		   var=cmf.getChkbox1();
		   fromname=cmf.getFrom_name();
		   toname=cmf.getTo_name();
		   //cusmsg=cmf.getCustomerMessage();
		   String cusmsg="<table bgcolor=\"#ffffff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"+
			 "<tr>"+
			 "<td width=\"463\" height=\"42\" style=\"padding-top:30px;\"><img"+ 
			 "src=\"brochure_showcase.JPG\" border=\"0\" width=\"463\" height=\"42\"></td>"+
			 "</tr>"+
			 "<tr>"+
			 "<td width=\"463\" style=\"font-family:Arial, Helvetica, sans-serif;"+ 
			 "font-size:11px; padding-top:20px; padding-bottom:50px;\">Dear <span style=\"font-family:Arial,"+ 
			 "Helvetica, sans-serif; font-size:11px; color:#67cc64;\">"+toname+" </span>,"+
			 "<br><br>I just wanted to let you know that you can get personalized skin care"+ 
			 "recommendations online at Beauty.com."+ 
			 "<br><br>Take a look!"+
			 "<br>"+
			 "<br><a href=\"http://www.Beauty.com/SkinAnalysis\" style=\"font-family:Arial, Helvetica,"+ 
			 "sans-serif; font-size:11px; color:#cb66cc;\">www.Beauty.com/SkinAnalysis</a>"+
			 "<br><br><span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px;"+ 
			 "color:#67cc64;\">[included only if customer checked the box to give friend their"+ 
			 "regimen]</span>"+	
			 "<br>"+	
			 "If you're interested in learning more, my personalized regimen is attached."+ 
			 "<br><span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px;"+ 
			 "color:#67cc64;\">[PDF here]</span></td>"+
			 "</tr>"+
			 "</table>";
			   

		   System.out.println("cusmsg:"+toname+"***"+fromname+"********"+cusmsg);
		   if(var==null)
		   {var="somevalue";}
		   System.out.println("true/false"+service.getCustomer(from, to));
		   if(service.getCustomer(from, to))
		   
		   {
		   
		   if(var.equalsIgnoreCase("selected"))
			{
				System.out.println("88888888888888");
				if(service.getEmailDetails(to, from, pass, filepath,cusmsg))
				target = "success";
			}
			else
			{
				System.out.println("99999999999");
				//System.out.println("in action the boolen value is:"+service.getEmailDetails(to, from, pass,cusmsg));
				if(service.getEmailDetails(to, from, pass,cusmsg))
				 target = "success";
			}
		   
		   
		   }
		   else
		   {
			   
			   target="failure";
		   }
			 //String suce= service.getEmailDetails(to, from, pass, var, file);
		   
		   System.out.println("return flag in action:"+target);
		
			return mapping.findForward(target);
		}
		
	
	}