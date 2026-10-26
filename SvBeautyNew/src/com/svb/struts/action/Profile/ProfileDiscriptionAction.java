package com.svb.struts.action.Profile;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.struts.service.ProfileDetailService;

public class ProfileDiscriptionAction extends Action{
	
	public ActionForward execute(ActionMapping mapping,
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException, ServletException {
		
		
		String target = new String("Success");
		ArrayList profiles= new ArrayList();
		 String profilevalue= request.getParameter("profileparam");
		 System.out.println("profilevalue:"+profilevalue);
		 
		 
	ProfileDetailService profileService= new ProfileDetailService();
	profiles = profileService.getProfileDisc(profilevalue);
	
	request.setAttribute("profiles", profiles);
		
		return mapping.findForward(target);
	}

}
