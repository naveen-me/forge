/*
 * Created on Sept 23, 2005
 *
 */
package com.svb.ratings.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;

import com.svb.common.valueobjects.ProductVO;
import com.svb.ratings.dal.ProductRegimenDAL;
import com.svb.ratings.formbeans.ProductRegimenForm ;

/**
 * @author Neelima
 *
 */
public class SaveRegimenAction extends Action {

	private static Log log = LogFactory.getLog(SaveRegimenAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";
		String repId = "";
		String customerId = "";
		String dname = "" ;
		String regimenusage = "" ;
		String ethnicity = "" ;

		ProductVO products[] = new ProductVO[0];
		ProductRegimenForm regimenForm = (ProductRegimenForm)form;
		HttpSession session = request.getSession(false);
		ProductRegimenDAL prodRegimenDAL = new ProductRegimenDAL();
		String msg = "";

		try {

			if (regimenForm.getRepId() != null) {
				repId = regimenForm.getRepId();
				customerId = regimenForm.getCustomerId();
				dname = regimenForm.getDname();
				ethnicity = regimenForm.getCethnicity();
				regimenusage = regimenForm.getRegimenusage();
			}

			if(session.getAttribute("productsforcustomer"+customerId.trim()) != null)
				products = (ProductVO[])session.getAttribute("productsforcustomer"+customerId.trim());

			//Delete all the products before saving the modified regimen
			prodRegimenDAL.deleteModifiedRegimen(customerId,repId);

			if(products.length > 0) {
				msg = prodRegimenDAL.saveModifiedRegimen(customerId,repId,products);
				request.setAttribute("modifyRegimenFlag","yes");
			} else {
				request.setAttribute("modifyRegimenFlag","no");
			}

			request.setAttribute("productlist", products);
			request.setAttribute("repId", repId);
    		request.setAttribute("customerId", customerId);
    		request.setAttribute("dname", dname);
    		request.setAttribute("regimenusage", regimenusage);
    		request.setAttribute("ethnicity", ethnicity);
    		request.setAttribute("prodline", "BA");


		} catch(Exception e) {
			e.printStackTrace() ;
		}
		return (mapping.findForward(target));
	}

}