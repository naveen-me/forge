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

/**
 * @author Madhav
 *
 */
public class EmailOptInAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

			return (mapping.findForward("success"));

	}
}
