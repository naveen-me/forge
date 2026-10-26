/*
 * Created on Sep 18, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.generic;

/**
 * @author Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;

/**
 * @author $Author: vbrao $
 * @version $Revision: 1.1 $
 */

public class BaseExceptionHandler
    extends ExceptionHandler {

  public ActionForward execute(Exception ex,
                               ExceptionConfig config,
                               ActionMapping mapping,
                               ActionForm formInstance,
                               HttpServletRequest request,
                               HttpServletResponse response) throws ServletException {

    ActionForward forward = null;
    ActionError error = null;
    String property = null;

    /* Get the path for the forward either from the exception element
     * or from the input attribute.
     */
    String path = "error";
    if (config.getPath() != null) {
      path = config.getPath();
    }
    else {
      path = mapping.getInput();
    }
    // Construct the forward object
    forward = new ActionForward(path);

    /*
     * Figure out what type of exception has been thrown.
     */
    if (ex instanceof java.lang.Exception) {
      error = new ActionError("errors.internalError");
      ex.printStackTrace();
    }

    // Store the ActionError into the proper scope
    // The storeException method is defined in the parent class
    storeException(request, property, error, forward, config.getScope());

    request.setAttribute("error",ex);

    return forward;
  }
}

  /**
  *    $Log: BaseExceptionHandler.java,v $
  *    Revision 1.1  2005/03/29 01:09:16  vbrao
  *    Code from Aditya
  *  
  */