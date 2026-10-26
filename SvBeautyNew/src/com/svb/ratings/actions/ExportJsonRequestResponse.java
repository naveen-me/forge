package com.svb.ratings.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

public class ExportJsonRequestResponse extends DispatchAction {
	/**
	* Added by Synoptek (03-03-2020)
	* added to get request-response for each selection
	*/
	private static Log log = LogFactory.getLog(ExportJsonRequestResponse.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		  File logFile = new File(System.getProperty("catalina.home"), "logs/JSON_REQ_RES.log");
	      String contents = FileUtils.readFileToString(logFile);

	      String target = "success";
	      try {	    	  
	    	  response.setContentType("text/plain");
	    	  response.setHeader("Content-disposition","attachment; filename=JSON_Request_Response.txt"); // Used to name the download file and its format
	    
	    	  OutputStream out = response.getOutputStream();
	          FileInputStream in = new FileInputStream(logFile);
	          byte[] buffer = new byte[4096];
	          int length;
	          while ((length = in.read(buffer)) > 0){
	             out.write(buffer, 0, length);
	          }
	          in.close();
	          out.flush();
	          
			target = "success";			
		} catch (Exception e) {
			e.printStackTrace();
			target="failure";
			return (mapping.findForward(target));
		}
		return null;
	}
}
