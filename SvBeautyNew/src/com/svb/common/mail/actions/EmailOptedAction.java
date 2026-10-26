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

import com.svb.common.mail.dal.WebSubDAL;

/**
 * @author Madhav
 *
 */
public class EmailOptedAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
			WebSubDAL dal = new WebSubDAL();
			String repId  = request.getParameter("repid");
			dal.subscribeRep(repId);
			return (mapping.findForward("success"));

	}
}
