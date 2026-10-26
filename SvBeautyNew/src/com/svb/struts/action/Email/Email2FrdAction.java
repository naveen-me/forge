package com.svb.struts.action.Email;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Email2FrdAction extends Action{
	public ActionForward execute(ActionMapping mapping,
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("ima in email2frndaction classs");
		String msg = "<table bgcolor=\"#ffffff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"+
		 "<tr>"+
		 "<td width=\"463\" height=\"42\" style=\"padding-top:30px;\"><img"+ 
		 "src=\"brochure_showcase.JPG\" border=\"0\" width=\"463\" height=\"42\"></td>"+
		 "</tr>"+
		 "<tr>"+
		 "<td width=\"463\" style=\"font-family:Arial, Helvetica, sans-serif;"+ 
		 "font-size:11px; padding-top:20px; padding-bottom:50px;\">Dear <span style=\"font-family:Arial,"+ 
		 "Helvetica, sans-serif; font-size:11px; color:#67cc64;\">[friend's name from Share Regimen"+ 
		 "form]</span>,"+
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
		
		   request.setAttribute("msg", msg);
		String target = new String("Success");
		return mapping.findForward(target);
	}
	
	
	

}
