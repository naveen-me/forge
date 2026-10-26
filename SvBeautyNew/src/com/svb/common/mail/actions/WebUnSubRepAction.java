/*
 * Created on Mar 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.mail.actions;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.struts.action.*;

import com.svb.common.mail.dal.WebUnSubDAL;

/**
 * @author Madhav
 *
 */
public class WebUnSubRepAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {
			String email    = request.getParameter("repEmail");
			WebUnSubDAL dal = new WebUnSubDAL();
			if(email!=null)
				dal.unSubscribeRep(email);


		} catch (Exception ex1)	{
			////logger.log(Level.DEBUG,ex1) ;
			ex1.printStackTrace();
		}

		return (mapping.findForward("success"));

	}
}
