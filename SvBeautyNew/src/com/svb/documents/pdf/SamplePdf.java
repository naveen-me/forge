package com.svb.documents.pdf;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import com.svb.common.dba.SVDBConnection;
import com.svb.struts.bean.ProductDetailsBean;
import com.svb.struts.service.DetailsService;

public class SamplePdf {

	
	/*String description;
	String directions;
	String image;
	String activeIngredients;
	String inactiveIngredients;
	ArrayList ingredients;
	SVDBConnection svConnection;
	String name;*/
	
	StringReader reader;
	StyleSheet st;
	ArrayList list, list1;
	
	public void pdfGeneration(HttpServletResponse response, String regimenId, File pdfFile){
		
		Document document = new Document(PageSize.A4, 32, 32, 32, 32);
		document.setMarginMirroring(true);

		//String regimenId = "151354";
		String productId = "151354";
		st = new StyleSheet();
		list = new ArrayList();
		list1 = new ArrayList();
		
		DetailsService service = new DetailsService();
		ProductDetailsBean details = service.getProductDetails(productId);
		
		try {
			
			PdfWriter.getInstance(document, new FileOutputStream(pdfFile)); 
			document.open();
			Chunk newLine = new Chunk("\n");
			
			Paragraph heading = new Paragraph("SKINCARE PRODUCT CATALOG", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC, new Color(0, 0, 0)));
			heading.setAlignment(Element.ALIGN_CENTER);
			document.add(heading);
			document.add(newLine);
			
			Chunk productName = new Chunk(details.getProductName(), new Font(Font.HELVETICA, 10));
			document.add(productName);
			document.add(newLine);
			
			Image pdfImage = Image.getInstance(details.getMediumImage());
			pdfImage.setAlignment(Image.LEFT | Image.TEXTWRAP);
			document.add(pdfImage);
			document.add(newLine);
			
			Paragraph productDetails = new Paragraph("", FontFactory.getFont(FontFactory.HELVETICA, 8));
			
			Chunk descHead = new Chunk("Package Details", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, new Color(0, 0, 0)));
			descHead.setUnderline(0.2f, -2f);
						
			Chunk directionsHead = new Chunk("Directions", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, new Color(0, 0, 0)));
			directionsHead.setUnderline(0.2f, -2f);
			//Chunk productDirections = new Chunk(directions, FontFactory.getFont(FontFactory.HELVETICA, 8));
		
			Chunk ingredientsHead = new Chunk("Ingredients", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, new Color(0, 0, 0)));
			ingredientsHead.setUnderline(0.2f, -2f);
			
			/*Chunk activeHead = new Chunk("Active", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, new Color(0, 0, 0)));
			activeHead.setUnderline(0.2f, -2f);
			
			Chunk inactiveHead = new Chunk("Inactive", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, new Color(0, 0, 0)));
			inactiveHead.setUnderline(0.2f, -2f);*/
			
			/*Chunk active = new Chunk(activeIngredients, FontFactory.getFont(FontFactory.HELVETICA, 8));
			Chunk inactive = new Chunk(inactiveIngredients, FontFactory.getFont(FontFactory.HELVETICA, 8));*/
			
			document.add(descHead);
			document.add(newLine);

			reader = new StringReader(details.getProductDescription());

			list = HTMLWorker.parseToList(reader, st);
			for (int k = 0; k < list.size(); ++k)
			{
				if(list.get(k) instanceof List){
					productDetails.add(newLine);
					ArrayList list1 = ((List)list.get(k)).getChunks();
					Iterator i = list1.iterator();
					while(i.hasNext()){
						Chunk listDescription = new Chunk(i.next().toString(), FontFactory.getFont(FontFactory.HELVETICA, 8));//(Chunk)("->"+i.next().toString());
						productDetails.add(listDescription);
					}
				}else{
					Chunk listDescription = new Chunk(list.get(k).toString().substring(1, list.get(k).toString().length()-1), FontFactory.getFont(FontFactory.HELVETICA, 8));
					productDetails.add(listDescription);
				}
			}
			
			productDetails.add(newLine);
			productDetails.add(directionsHead);
			productDetails.add(newLine);
			
			reader = new StringReader(details.getProductDirections());
			list = HTMLWorker.parseToList(reader, st);
			for (int k = 0; k < list.size(); ++k)
			{
				if(list.get(k) instanceof List){
					productDetails.add(newLine);
					ArrayList list1 = ((List)list.get(k)).getChunks();
					Iterator i = list1.iterator();
					while(i.hasNext()){
						Chunk listDescription = new Chunk(i.next().toString(), FontFactory.getFont(FontFactory.HELVETICA, 8));//(Chunk)("->"+i.next().toString());
						productDetails.add(listDescription);
					}
				}else{
					Chunk listdescription = new Chunk(list.get(k).toString().substring(1, list.get(k).toString().length()-1), FontFactory.getFont(FontFactory.HELVETICA, 8));
					productDetails.add(listdescription);
				}
			}
			
			//productDetails.add(productDirections);
			productDetails.add(newLine);
			productDetails.add(ingredientsHead);
			productDetails.add(newLine);
			
			/*if(!activeIngredients.equals("")){
				productDetails.add(activeHead);
				productDetails.add(newLine);
				productDetails.add(active);
				productDetails.add(newLine);
			}
			productDetails.add(inactiveHead);
			productDetails.add(newLine);
			productDetails.add(inactive);
			productDetails.add(newLine);*/
			
			Iterator i = details.getIngredients().iterator();
			while(i.hasNext()){
				Chunk ingredientsList = new Chunk(i.next().toString(),  FontFactory.getFont(FontFactory.HELVETICA, 8));
				productDetails.add(ingredientsList);
			}
			document.add(productDetails);
			
		} catch (DocumentException de) {
			System.out.println("----------------");
			System.err.println(de.getMessage());
			de.printStackTrace();
		} catch (IOException ioe) {
			System.out.println("----------------");
			System.err.println(ioe.getMessage());
			ioe.printStackTrace();
		} finally{
			document.close();
		}

	}

	
}
