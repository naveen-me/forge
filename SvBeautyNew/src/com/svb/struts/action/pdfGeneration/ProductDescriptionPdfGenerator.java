/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/
package com.svb.struts.action.pdfGeneration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.documents.pdf.RegimenDescriptionPdf;
import com.svb.documents.pdf.SamplePdf;
import com.svb.regimenmail.javabeans.RegimenTrackingDAL;

public class ProductDescriptionPdfGenerator extends Action {

	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
		
		response.setContentType("application/pdf");
		String customerId = "" ;
		
		if (request.getParameter("customerId") != null)
			customerId = request.getParameter("customerId") ;
		System.out.println("customerId "+customerId) ;
		String regimenId = "sv"+customerId ;
		//String filePath = "pdfFiles";
		String filePath = getServlet().getServletContext().getRealPath("/") + "pdfFiles";
		File folder = new File(filePath);
		folder.mkdir();
		
		File pdfFile = new File(filePath, regimenId+".pdf");
		File page1 = new File(filePath, regimenId+"page1.pdf");
		File page2 = new File(filePath, regimenId+"page2.pdf");
		File page3 = new File(filePath, regimenId+"page3.pdf");
		File page4 = new File(filePath, regimenId+"page4.pdf");
		
		RegimenDescriptionPdf pdf = new RegimenDescriptionPdf();
		pdf.pdfForRegimen(regimenId, pdfFile, page1, page2, page3, page4, filePath, request);

		request.setAttribute("fileName", regimenId+".pdf");
		page1.delete();
		page2.delete();
		page3.delete();
		page4.delete();
		
		RegimenTrackingDAL rtd = new RegimenTrackingDAL() ;
        rtd.insertCustomerPDF(customerId,"01") ;

		request.setAttribute("file", pdfFile);

		String target = "Success";
		return mapping.findForward(target);
	}
}


