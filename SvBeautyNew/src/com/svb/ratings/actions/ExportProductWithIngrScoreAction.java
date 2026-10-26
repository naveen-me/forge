package com.svb.ratings.actions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.svb.analysis.actions.AnalysisDetailAction;
import com.svb.common.ConstantDeclarations;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.ratings.formbeans.ProductRegimenForm;
import com.svb.ratings.javabeans.ExportDataFeature;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.struts.service.DetailsService;
import com.svb.webservices.model.RecProductModel;
import com.svb.webservices.model.RecProductWithIngrScoreModel;
import com.svb.webservices.serviceImpl.ProductRecommend;

public class ExportProductWithIngrScoreAction extends DispatchAction {
	/**
	* Added By Synoptek
	* added for export score related functionality
	*/
	private static Log log = LogFactory.getLog(ExportProductWithIngrScoreAction.class);
	RecommendationEngine rec = new RecommendationEngine();
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		String target = "success";
		String customerId = "";
		String cusid = "";
		String mid = "";
		String mode = "";
		String catid = "";
		String prodType = "";
		String prodLine = "";
		boolean prodrec = false;

		CustomerProfileVO cp = new CustomerProfileVO();
		ProductVO productVOList[] = new ProductVO[0];
		ArrayList eliminatedProducts = new ArrayList();
		ArrayList  prodlist = new ArrayList();
		ArrayList eliminatedProductList = new ArrayList();
		Hashtable master = new Hashtable();
		Hashtable brand = new Hashtable(); 
		
		ExportDataFeature recEng = new ExportDataFeature();
		DetailsService service = new DetailsService();

		HttpSession session = request.getSession(false);
		ProductRegimenForm regimenForm = (ProductRegimenForm) form;
		prodType = regimenForm.getProdType();
		prodLine = regimenForm.getProdLine();
		cusid = regimenForm.getCusid();
		if (cusid == null || cusid.length() == 0)
			cusid = (String) request.getAttribute("cusid");
		if (cusid.trim().length() == 0)
			cusid = (String) session.getAttribute("cusid");

		if (mid.trim().length() == 0)
			mid = (String) session.getAttribute("mid");
		if (mode.trim().length() == 0)
			mode = (String) session.getAttribute("mode");
		if (catid.trim().length() == 0)
			catid = (String) session.getAttribute("catid");
		customerId = cusid;
		try {
			CustomerTrackBean ctb = new CustomerTrackBean();
			ctb.insertCustomerTrack("Skin", customerId, request.getParameter("scrname"), "Product Recommendation",
					"PROD", "");
			master = (Hashtable) rec.getMasterDataOne();
			prodrec = true;
			if (prodrec) {
				Hashtable zeroRatingEliminatedIngredients = new Hashtable();
				Hashtable eligibleProductsHashtable = new Hashtable();
				Hashtable all_products = new Hashtable();
				Hashtable eliminatedRuleProducts = new Hashtable();
				Hashtable products = new Hashtable();
				prodlist = new ArrayList();
				eliminatedProductList = new ArrayList();
				/*Get all products and separate in recommend products and eliminated products due to ingredient's zero rating*/
				products = recEng.getProductList(customerId, prodType, "", prodLine, cp, "");
				
				/*Eliminated products due to ingredient's zero rating*/
				zeroRatingEliminatedIngredients = (Hashtable) products.get("product under Zero ratings elimination list");
				
				/*Recommended products list*/
				all_products=(Hashtable) products.get("all products");
				
				/*products eligible for recommendation*/
				eligibleProductsHashtable = (Hashtable) all_products.get("eligible products");
				
				/*Products eliminated due to elimination rule*/
				eliminatedRuleProducts=(Hashtable) all_products.get("eliminated products");
				
				/*Calculate for only eligible products ingredients, ratings, bump score */
				productVOList = recEng.getProduct(eligibleProductsHashtable,zeroRatingEliminatedIngredients,customerId, prodType, "", prodLine, cp, "");
				
				/*Convert Recommend ProductVO to ArrayList */
				prodlist = (ArrayList) recEng.getProductListFromVO(productVOList);
				
				/*Convert is to be delete flag  = true eliminated ProductVO to ArrayList*/
				eliminatedProductList = recEng.getEliminatedProductListFromVO(productVOList);
				
				/*Combine all eliminated products */
				eliminatedProducts = getProductArray(cp,eliminatedProductList,eliminatedRuleProducts,master);
			}
			List<List<RecProductModel>> recProductModel = recEng.getProductWithIngrScoreList(prodlist);
			List<List<RecProductModel>> eliminatedProductModel = recEng.getEliminatedProductList(eliminatedProducts);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Recommended Product's Data");
			XSSFSheet selectionSheet = workbook.createSheet("User's Selection");
			XSSFSheet eliminationSheet = workbook.createSheet("Eliminated Products");

			XSSFFont boldFont = workbook.createFont();
	        boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			XSSFCellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(boldFont);
			// Iterate over data and write to sheet
			String[] header = new String[200];
			header[0] = "Brand Name";
			header[1] = "Product Name";
			header[2] = "Product Category";
			header[3] = "Product Sub-Category";
			header[4] = "When To Use";
			header[5] = "Total Score %";
			header[6] = "Ingredient Score %";
			header[7] = "Formulation Score %";
			header[8] = "Bump Score";
			header[9] = "Ingredient Weighted Score";
			header[10] = "Formulation Weighted Score";
			header[11]=  "Ingredient Att Count";
			header[12]=  "Formulation Att Count";
			header[13]=  "Total Ingredient Count";
			header[14]=  "Ingredient Count Used In Calc";
			int count=0;
			 for(int i = 15; i<header.length-1; i=i+2) {
				 count++;
	            	header[i]="Ingredient Name "+count;
	            	header[i+1]="Score";
	            }
		    
			Row headerRow = sheet.createRow(0);
			// Create cells
			for (int i = 0; i < header.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellStyle(headerCellStyle);
				cell.setCellValue(header[i]);
				
			}
			//sheet.createFreezePane(0, 30);
			int rowNum = 1;
			for (int i = 0; i <4; i++) {
				for (RecProductModel cleanser : recProductModel.get(i)) {
					Row row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue((String) master.get((String) cleanser.getBrandName()));
					row.createCell(1).setCellValue(cleanser.getProductName());
					row.createCell(2).setCellValue(cleanser.getProductCategory());
					row.createCell(3).setCellValue(cleanser.getProductSubCategory());
					row.createCell(4).setCellValue(cleanser.getProductWhenToUse());
					
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 07/10/2024 - Start
					// Added to prevent score from exceeding 100
					//row.createCell(5).setCellValue(cleanser.getScore());
					row.createCell(5)
							.setCellValue(Float.parseFloat(cleanser.getScore()) > 100f ? "100.0" : cleanser.getScore());
					// Add/Deduct Scores as per Business Rule Changes - Shivam - 07/10/2024 - End
					row.createCell(6).setCellValue(cleanser.getIngrScore());
					row.createCell(7).setCellValue(cleanser.getBaseScore());	
					row.createCell(8).setCellValue(cleanser.getBumpScore());
					row.createCell(9).setCellValue(cleanser.getIngrWeightage());
					row.createCell(10).setCellValue(cleanser.getBaseWeightage());	
					row.createCell(11).setCellValue(cleanser.getIngrCount());
					row.createCell(12).setCellValue(cleanser.getBaseCount());
					row.createCell(13).setCellValue(cleanser.getTotalIngrCount());
					row.createCell(14).setCellValue(cleanser.getIngredients().size());
					for (int k = 0; k < cleanser.getIngredients().size(); k++) {
						row.createCell(15 + 2 * k)
								.setCellValue(cleanser.getIngredients().get(k).getIngredientName());
						row.createCell(16 + 2 * k)
								.setCellValue(cleanser.getIngredients().get(k).getScore());
					}
				}
			}
			// Resize all columns to fit the content size
			for (int i = 0; i < header.length; i++) {
				sheet.autoSizeColumn(i);
			}
			
			String[] selectionHeader = new String[12];
			selectionHeader[0] = "Skin Type";
			selectionHeader[1] = "Skin Concern/ Skin Condition";
			selectionHeader[2] = "Eye Concern";
			selectionHeader[3] = "Allergy";
			selectionHeader[4]=  "Age";
			selectionHeader[5]=  "Ethenicity";
			selectionHeader[6]=  "Natural";
			selectionHeader[7]=  "Specification";

		    
			Row selectionHeaderRow = selectionSheet.createRow(0);
			// Create cells
			for (int i = 0; i < selectionHeader.length; i++) {
				Cell selectionCell = selectionHeaderRow.createCell(i);
				selectionCell.setCellStyle(headerCellStyle);
				selectionCell.setCellValue(selectionHeader[i]);
			}
			//selectionSheet.createFreezePane(0, 30);
			int selectionRowNum = 1;
			Row selectionRow = selectionSheet.createRow(selectionRowNum++);
			
			ArrayList skinConcCond = new ArrayList();
			ArrayList econc = new ArrayList();
			ArrayList aller = new ArrayList();
			ArrayList specific = new ArrayList();
			
			if (cp.getSkinConditionList() != null ) {
				for (int i = 0; i < cp.getSkinConditionList().size(); i++) {
					skinConcCond.add((String) master.get(((String) cp.getSkinConditionList().get(i)).trim()));
				}
			}
			if (cp.getSkinConcernList() != null) {
				for (int i = 0; i < cp.getSkinConcernList().size(); i++) {
					skinConcCond.add((String) master.get(((String) cp.getSkinConcernList().get(i)).trim()));
				}
			}
			if (cp.getEyeConcernList().size() != 0) {
				for (int i = 0; i < cp.getEyeConcernList().size(); i++) {
					econc.add((String) master.get(((String) cp.getEyeConcernList().get(i)).trim()));
				}
			}
			if (cp.getAllergyList().size() != 0) {
				for (int i = 0; i < cp.getAllergyList().size(); i++) {
					aller.add((String) master.get(((String) cp.getAllergyList().get(i)).trim()));
				}
			}
			if (cp.getSpecification().size() != 0) {
				for (int i = 0; i < cp.getSpecification().size(); i++) {
					specific.add((String) master.get(((String) cp.getSpecification().get(i)).trim()));
				}
			}
			String skinConcCondition = String.join(",", skinConcCond);
			String eyeConcern = String.join(",", econc);
			String allergy=String.join(",", aller);
			String specification=String.join(",", specific);
			
			selectionRow.createCell(0).setCellValue((String) master.get((String) cp.getSkinType()));
			selectionRow.createCell(1).setCellValue(skinConcCondition);
			selectionRow.createCell(2).setCellValue(eyeConcern);
			selectionRow.createCell(3).setCellValue(allergy);
			selectionRow.createCell(4).setCellValue((String) master.get((String) cp.getAge()));
			selectionRow.createCell(5).setCellValue((String) master.get((String) cp.getEthnicity()));
			selectionRow.createCell(6).setCellValue((String) master.get((String) cp.getNatural()));
			selectionRow.createCell(7).setCellValue(specification);

			// Resize all columns to fit the content size
			for (int i = 0; i < selectionHeader.length; i++) {
				selectionSheet.autoSizeColumn(i);
			}
			String[] eliminationHeader = new String[7];
			eliminationHeader[0] = "Brand Name";
			eliminationHeader[1] = "Product Name";
			eliminationHeader[2] = "Product Category";
			eliminationHeader[3] = "Product Sub-Category";
			eliminationHeader[4] = "When To Use";
			eliminationHeader[5] = "Elimination reason";
			eliminationHeader[6] = "Eliminated Ingredient/Formulation Name/Reason";
		
			Row eliminationHeaderRow = eliminationSheet.createRow(0);
			// Create cells
			for (int i = 0; i < eliminationHeader.length; i++) {
				Cell cell = eliminationHeaderRow.createCell(i);
				cell.setCellStyle(headerCellStyle);
				cell.setCellValue(eliminationHeader[i]);
				
			}
			int eliminationRowNum = 1;
		
			for (int i = 0; i <4; i++) {
				for (RecProductModel cleanser : eliminatedProductModel.get(i)) {
					Row elimination = eliminationSheet.createRow(eliminationRowNum++);
					elimination.createCell(0).setCellValue((String) master.get((String) cleanser.getBrandName()));
					elimination.createCell(1).setCellValue(cleanser.getProductName());
					elimination.createCell(2).setCellValue(cleanser.getProductCategory());
					elimination.createCell(3).setCellValue(cleanser.getProductSubCategory());
					elimination.createCell(4).setCellValue(cleanser.getProductWhenToUse());
					elimination.createCell(5).setCellValue(cleanser.getReason());
					elimination.createCell(6).setCellValue(cleanser.getEliminatedIngrName());
				}
			}
			// Resize all columns to fit the content size
			for (int i = 0; i < eliminationHeader.length; i++) {
				eliminationSheet.autoSizeColumn(i);
			}
			// Closing the workbook
		    response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=Product_Scores.xlsx");
			ServletOutputStream out = response.getOutputStream();
			workbook.write(out);
			out.flush();
			out.close();

			target = "success";			
		} catch (Exception e) {
			e.printStackTrace();
			target="failure";
			return (mapping.findForward(target));
		}
		return null;
	}

	private ArrayList getProductArray(CustomerProfileVO cp, ArrayList eliminatedProductList, Hashtable eliminatedRuleProducts, Hashtable master) {
	
		ProductVO[] eliminatedProducts = new ProductVO[0];		
		int count = 0;
		
		Map<String, String> eliminationRuleMap = rec.getEliminationRuleProducts();
		Enumeration en = eliminatedRuleProducts.keys();
		eliminatedProducts = new ProductVO[eliminatedRuleProducts.size()];
		while (en.hasMoreElements()) {
			ProductVO pr = (ProductVO) eliminatedRuleProducts.get(en.nextElement());
			pr.setReason(ConstantDeclarations.ELIMINATION_RULE);
			String reason = "";
			String eliminationReason = "";
			for (String attribute : eliminationRuleMap.keySet()) {
				if (eliminationRuleMap.get(attribute).contains(pr.getUpc())) {
					// Change made by Anish Khandelwal 12-04-2022
					//Till now this logic is checking single attribute at once. But with this it will check multiple attribute in AND condition.
					String regex = ",";
					System.out.println(attribute.contains(regex));
					if(attribute.contains(regex)) {
						String proAttribute[] = attribute.split(",");
						int attributeFlag = 0;
						reason = "";
						for(int i = 0; i < proAttribute.length; i++) {
							String altReason = "";
							altReason = (altReason == "") ? (String) master.get(proAttribute[i]): altReason.concat(" or ").concat((String) master.get(proAttribute[i]));
							if(reason != null  && reason != "" && !altReason.equalsIgnoreCase(reason) && !reason.contains(altReason)) {
								reason = reason +" or " + altReason;
							}
							if(reason == null || reason == "") {
								reason = altReason;
							}
						}
					}
					else {
					reason = (reason == "") ? (String) master.get(attribute): reason.concat(" or ").concat((String) master.get(attribute));
					}
				}
			}
			
			// Exception rules - Shivam - 21/02/2025 - Start
			if (reason.trim().isEmpty()) {
				Map<String, Set<String>> exceptionRuleProductsMap = rec.getExceptionRuleProductsMap(true);

				if (exceptionRuleProductsMap.containsKey(pr.getProdId())) {
					reason = exceptionRuleProductsMap.get(pr.getProdId()).stream()
							.map(attribute -> (String) master.get(attribute))
							.collect(Collectors.joining(" or "));
				}

			}
			// Exception rules - Shivam - 21/02/2025 - End
			
			eliminationReason = reason.concat(" is not selected by user");
			pr.setEliminatedIngrName(eliminationReason);
			eliminatedProducts[count++] = pr;
		}
		
		for (int i = 0; i < eliminatedProducts.length; i++) {
			eliminatedProductList.add(eliminatedProducts[i]);
		}
		return eliminatedProductList;
	}
}
