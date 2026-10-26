package com.svb.documents.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.InputSource;

import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.svb.common.dba.SVDBConnection;
import com.svb.common.valueobjects.ProductVO;
import com.svb.documents.pdf.RegimenDetailsBean;


public class RegimenDescriptionPdf {

	String pdfFilesPath;
	int fourthPageStatus = 0;
	String logoSrc = "<img src=\"http://65.74.148.56/svbeauty/img/production/BCOM_logo_StyleValet.jpg\" class=\"logo\"/>";
	String bottomSrc = "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace\"/><img src=\"http://65.74.148.56/svbeauty/images/sv_skin_brochure_bottom.gif\" class=\"logo2\"/>";
	String graphicSrc = "<img src=\"http://65.74.148.56/svbeauty/images/brochure_showcase.jpg\" class=\"graphics\"/>";
	//"<?xml version='1.0' encoding='UTF-8'?> "+ 
	//"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
	String head = "<html><head>"+
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Page 1</title>";

	String css = "<style>"+
				"body{font-size: 83.125%;}"+
				"@page {margin-top: -8px;margin-right: 32px;margin-bottom: 32px;margin-left: 32px;}"+
				//"body {margin-top: -8px;margin-right: 32px;margin-bottom: 32px;margin-left: 32px;}"+
				"img.logo {width: 16.4em;height: 6.4em;}"+
				"img.graphics {width:46.3em;height:4.2em;margin-top:3.4em;}"+
				"img.logo2 {width:46.3em;height:4.2em;margin-top:3.0em;margin-bottom:0.0em;margin-left:1.0em;}"+
				"img.logospace {width: 16.5em;height: 1.0em;}"+
				"img.logospace2 {margin-right: 449px;height: 0.0em;}"+
				"table.regimenDetail img {width: 10em;height: 10em;}"+
				"body {font-family:Arial, Helvetica, sans-serif;color: #333333;	width: 53.0em;}"+
				"h1 {font-size: 1.5em;color: #ec1862;font-weight: normal;margin: 0 0 .25em 0;}"+
				"body#page1 h1 {margin-top: 1em;}"+
				"h2 {font-size: 1.2em;margin: 0 0 .25em 0;color: #333333;}"+
				"div#am, div#pm {padding: 0 1em 0 1em;background-color: #e9e8ea;}"+
				"div#am {border-top-width: 2px;border-right-width: 2px;border-bottom-width: 2px;border-left-width: 2px;	border-top-style: solid;border-right-style: solid;border-bottom-style: dotted;border-left-style: solid;	border-top-color: #757575;border-right-color: #757575;border-bottom-color: #757575;border-left-color: #757575;}"+
				"div#pm {border-right-width: 2px;border-bottom-width: 2px;border-left-width: 2px;border-right-style: solid;border-bottom-style: solid;border-left-style: solid;border-right-color: #757575;border-bottom-color: #757575;	border-left-color: #757575;margin-bottom: 1em;}"+
				"div#am div.header, div#pm div.header {margin: 0 -1em .75em -1em;padding: 0 1em 0 1em;background-color: #c7c9ca;}"+
				"div#am h2, div#pm h2 {font-size: 1.4em;	color: #333333;	font-weight: normal;padding: .25em 0 .25em 0;}"+
				"h3 {font-size: 0.9em;margin: 0;}"+
				"a:link {text-decoration: none;color: #cc66cc;}"+
				"a:visited {text-decoration: none;color: #990099;}"+
				"a:hover {text-decoration: underline;}"+
				"a:active {color: #cc66cc;}"+
				"p {margin-top: 0;line-height: 1.25em;}"+
				"ul {margin-left: 5em;padding-left: 0;}"+
				".clear {clear: both;}"+
				".center {text-align: center;}"+
				"div.border {clear: both;border-top: dotted 1px #666666;margin: 0 -1.4em 0 -1.4em;}"+
				".floatLeft {float: left;}"+
				"img.logo {width: 16.4em;height: 6.4em;}"+
				"table.regimenDetail {width: 53.0em;height: 12.0em;min-height: 12.0em;border: solid 2px #999;margin: 0 0 0.8em 0;vertical-align: top;}"+
				"table.regimenDetail td, table.regimenDetail th {vertical-align: top;}"+
				"table.regimenDetail td.detail {width: 16.5em;min-width: 16.5em;}"+
				"table.regimenDetail img {float: left;margin-right: 1em;	width: 10em;height: 10em;}"+
				"table.regimenDetail p.detail {font-size: 1.0em;	color: #888;}"+
				"table.regimenDetail p.productBrand {font-weight: bold;text-transform: uppercase;margin: .125em 0 0 0;color: #333333;}"+
				"table.regimenDetail p.productDetail {margin: 0;}"+
				"table.regimenDetail p.beautyDetail {margin: 0;text-align: right;}"+
				"table.regimenDetail p.price {margin: 0;	color: #333333;}"+
				"</style>";
	
	String impNotice = "<p><strong>Important Notice:</strong><br/>"+
					"  <em>Information on this "+
					"site is not meant to be a substitute for the advice from a "+
					"dermatologist or other medical professional. Prior to starting a new "+
					"skin care regimen, you should consult your own physician, particularly "+
					"if you are currently using any medications. Always read the complete "+
					"manufacturer's instructions for each product.</em></p>";
		
	String aboutUs = "<h1>ABOUT US</h1>"+
					"<p><em><strong>Beauty.com</strong> has the best beauty products from "+
					"more than 200 brands, plus tons of perks. Join our beauty community to "+
					"get exclusive newsletter offers, see the latest beauty trends, read and "+
					"write product reviews and get expert advice. </em></p>"+
					"<p><em>You'll also get Free Shipping, earn 5% back on every purchase, free samples with your order, and a little something<br/>"+
					"  extra with our gifts with purchase. How beautiful is that?</em></p>"+
					"<p><em><strong>Style Valet Inc.</strong> is a leading provider of "+
					"product recommendation solutions for the beauty industry. The company’s team of beauty "+
					"specialists incorporate decades of dermatological skin care expertise with current research; "+
					"utilize a highly sophisticated computer analysis system to pinpoint the best skin care products for your skin, "+
					"and  quite literally, analyze thousands of ingredients in the process of identifying the ideal skin care regimen to meet your beauty needs. "+
					"The result is that you save time, money and get what you want the first time!</em></p>";
	
	public String skinCareAgeDetails(int skinCareAgeGroup) {

		switch(skinCareAgeGroup){
			case 1:
				return
				"<h1>Skin Care at Any Age</h1>"+
				"<p>No matter what your age, there are important steps you can take to nourish, protect, and ensure great skin. Over time, make these essentials part of your regimen; they can be the key to having your very best skin.</p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from free radicals (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet) </li>"+
				"  <li>AHAs (alpha hydroxy acids) to exfoliate and strengthen the skin</li>"+
				"  <li>A broad spectrum SPF of 15 or higher to protect your skin from the #1 sign of aging – the sun</li>"+
				"</ul>";
			case 2:
				return 
				"<h1>Skin Care in Your 20s &amp; 30s</h1>"+
				"<p>Starting an anti-aging skin care regimen in your 20s will help greatly reduce signs of aging later, but natural elements will still start to have an impact on your skin as you move into your 30s. To help diminish fine lines and wrinkles and prevent future damage, your skin care regimen should include the following:  </p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from free radicals (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet)  </li>"+
				"  <li>Humectants and emollients to keep your skin hydrated and moisturized</li>"+
				"  <li>A daily cleanser to gently exfoliate, encouraging cell turnover for vibrant, radiant skin</li>"+
				"  <li>AHAs (alpha hydroxy acids) to help renew skin cells</li>"+
				"  <li>Ingredients to boost natural collagen production</li>"+
				"  <li>Most importantly, a broad spectrum SPF of 15 or higher to protect your skin from the #1 sign of aging - the sun</li>"+
				"</ul>";
			case 3:
				return
				"<h1>Skin Care in Your 30s &amp; 40s</h1>"+
				"<p>In your 30s, natural elements start to have a larger impact on your complexion. And as you move into your 40s, you may need to take a few extra steps to fight the appearance of fine lines and wrinkles. To keep your skin looking youthful, your skin care regimen should include the following: </p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from free radicals (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet) </li>"+
				"  <li>AHAs (alpha hydroxy acids) to help rejuvenate skin cells (they can even add thickness to the thin, delicate skin around your eyes)</li>"+
				"  <li>Ingredients to boost natural collagen production </li>"+
				"  <li>Humectants and emollients to keep your skin hydrated and moisturized</li>"+
				"  <li>Most importantly, a broad spectrum SPF of 15 or higher to protect your skin from the #1 sign of aging - the sun.</li>"+
				"</ul>";
			case 4:
				return
				"<h1>Skin Care in Your 40s &amp; 50s</h1>"+
				"<p>Women in their 40s may need to take a few extra steps to fight fine lines and wrinkles. And as you move into your 50s, it’s more important than ever to keep your skin moisturized, in addition to using a good eye and overall face treatment. Your skin care regimen should include: </p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from free radicals (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet)  </li>"+
				"  <li>AHAs (alpha hydroxy acids) to help rejuvenate skin cells (they can even add thickness to the thin, delicate skin around your eyes)</li>"+
				"  <li>Humectants and emollients are critical to keep your skin hydrated and moisturized</li>"+
				"  <li>Collagen-inducing ingredients that help keep your skin supple</li>"+
				"  <li>Essential vitamin and mineral complexes to restore strength and vibrance</li>"+
				"  <li>Most importantly, be sure to use a broad spectrum sunscreen with SPF of 15 or higher everyday on your face, neck, hands and arms to control age spots and maintain an even skin tone.</li>"+
				"</ul>";
			case 5:
				return
				"<h1>Skin Care in Your 50s &amp; 60s</h1>"+
				"<p>In your 50s, it’s more important than ever to keep your skin moisturized, in addition to using a good eye and overall face treatment. As you move into your 60s, skin will start to lose elasticity and even more moisture. To help keep skin supple and hydrated, your regimen should include:</p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from future free radical damage (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet) </li>"+
				"  <li>AHAs (alpha hydroxy acids) to exfoliate and strengthen the skin</li>"+
				"  <li>Collagen-supporting ingredients to help keep your skin more supple</li>"+
				"  <li>Essential vitamin and mineral complexes to restore strength and vibrance</li>"+
				"  <li>Most importantly, be sure to use a broad spectrum sunscreen with SPF of 15 or higher everyday on your face, neck, hands and arms to control age spots and maintain an even skin tone.</li>"+
				"</ul>";
			case 6:
				return
				"<h1>SKIN CARE IN YOUR 60s &amp; 70s</h1>"+
				"<p>Skin starts to lose elasticity and even more moisture during these years. To help keep it supple and hydrated, apply moisturizers morning and night – and even in between as needed. Pay special attention to the skin on your neck. Your skin care regimen should include:</p>"+
				"<ul>"+
				"  <li>Antioxidants to help protect your skin from future free radical damage (electrically-charged molecules that are caused by internal and external forces such as pollution, UV exposure, stress and diet)  </li>"+
				"  <li>AHAs (alpha hydroxy acids) to exfoliate and strengthen the skin</li>"+
				"  <li>Collagen-supporting ingredients to help keep your skin more supple</li>"+
				"  <li>Essential vitamin and mineral complexes to restore strength and vibrancy</li>"+
				"  <li>Most importantly, be sure to use a broad spectrum sunscreen with SPF of 15 or higher everyday on your face, neck, hands and arms to control age spots and maintain an even skin tone.</li>"+
				"</ul>";
		}
		return "";
	}
	
	public String page1BodyContent(ArrayList profdetails){

		String profilestring = "" ;
		String warnings = "" ;
		String age = "" ;
		
		int skinCareAgeGroup = 0 ;
		String commontext = "Test a small amount of each new product on a hidden area (such as behind your ear) to make sure that your skin does not have any adverse reactions.  Always follow the recommended application quantity and frequency for each product.  Consult your physician before using these products if you are pregnant or nursing." ;
		
		ArrayList dynamic = new ArrayList() ;
		
		try
		{
			
		  profilestring = (String) profdetails.get(0) ;
		  age = (String) profdetails.get(1) ;
		  dynamic = (ArrayList) profdetails.get(2) ;
		  
		  if (age.equals("G004"))
		  	skinCareAgeGroup = 2 ;
		  if (age.equals("G001"))
		  	skinCareAgeGroup = 2 ;
		  if (age.equals("G002"))
		  	skinCareAgeGroup = 3 ;
		  if (age.equals("G003"))
		  	skinCareAgeGroup = 4 ;
		  if (age.equals("G005"))
		  	skinCareAgeGroup = 5 ;
		  if (age.equals("G006"))
		  	skinCareAgeGroup = 6 ;
		  if (age.equals("G007"))
		  	skinCareAgeGroup = 6 ;
		  if (age.equals("G999"))
		  	skinCareAgeGroup = 1 ;
		  
          if (dynamic.size() > 0)
          {
               warnings = (String) dynamic.get(0) ;
              // commontext = (String) dynamic.get(1) ;
          }

		  //System.out.println("vb 10 "+profilestring) ;
		}
		catch (Exception ex)
		{
			ex.printStackTrace() ;
		}
		String bodyContent = "<body id=\"page1\">"+
					"<p>"+logoSrc+"</p>"+
					"<p>"+graphicSrc+"</p>"+
					"<h1>YOUR PERSONAL SKIN CARE REGIMEN</h1>"+
					""+profilestring+""+
					"<p> </p>"+
					skinCareAgeDetails(skinCareAgeGroup)+
					"<p> </p>"+
					"<p> </p><br/>"+
					"<p><strong>Please note:</strong><br/>"+
					"<em>"+commontext+"</em><br/><br/>"+
					"<em>"+warnings+"</em></p>"+
					"</body></html>";
		return bodyContent;
	}
	
	public String addPage2Content(HttpServletRequest request, String productId){
		SVDBConnection svConnection;
		svConnection = new SVDBConnection();
		Connection connection=null;
		Statement stmt=null;
		ResultSet rs=null;
		String content="";
		String query = "select productname, imageurlmedium from product where productid="+productId;
		try{
		connection = svConnection.getConnection();
		stmt = connection.createStatement();
		rs = stmt.executeQuery(query);
		while(rs.next()){
			String imageUrl = rs.getString("imageurlmedium").replaceAll("&", "&amp;");
			content += "<td><img src=\""+imageUrl+"\"/>";
			content += "      <p class=\"productDetail\"><a href=\"#\">"+rs.getString("productname")+"</a></p>";
		}
		}catch(Exception e){
		}finally{
			try{
				rs.close();
				stmt.close();
				svConnection.free(connection);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return content;
	}
	
	public String page2BodyContent(Hashtable products0){
		
		String cleanseprod = "" ;
		String moisturizeprod = "" ;
		String treatprod = "" ;
		String etreatprod = "" ;
		String spfprod = "" ;
		String cleanseimage = "" ;
		String moisturizeimage = "" ;
		String treatimage = "" ;
		String etreatimage = "" ;
		String spfimage = "" ;
		String cleansename = "" ;
		String moisturizename = "" ;
		String treatname = "" ;
		String etreatname = "" ;
		String spfname = "" ;
		String cleanseprodline = "" ;
		String moisturizeprodline = "" ;
		String treatprodline = "" ;
		String etreatprodline = "" ;
		String spfprodline = "" ;
		ArrayList cleansearray = new ArrayList() ;
		ArrayList moisturizearray = new ArrayList() ;
		ArrayList treatarray = new ArrayList() ;
		ArrayList etreatarray = new ArrayList() ;
		ArrayList spfarray = new ArrayList() ;
		boolean cleanseflag = false ;
		boolean moisturizeflag = false ;
		boolean treatflag = false ;
		boolean etreatflag = false ;
		boolean spfflag = false ;

		Hashtable products = (Hashtable) products0.get("products") ;
		Enumeration en = products.keys() ;
		while (en.hasMoreElements())
		{
			String prodid = (String) en.nextElement() ;
			ProductVO product = (ProductVO) products.get(prodid) ;
			String prodimage = (String) product.getImage().replaceAll("&", "&amp;") ;
			String prodname  = (String) product.getProdName().replaceAll("&", "&amp;") ;
			
			if (product.getMasterProdType().equals("0001"))
			{
			     cleanseprod = prodid ;
			     cleanseimage = prodimage ;
			     cleansename = prodname ;
			     cleansearray = (ArrayList) products0.get(prodid) ;
			     cleanseprodline  = (String) product.getProductLinePrime() ;
			     cleanseflag = true ;
			 //    System.out.println("cleanse array "+cleansearray.size()) ;
			}
			if (product.getMasterProdType().equals("0003"))
			{
				  moisturizeprod = prodid ;
			      moisturizeimage = prodimage ;
				  moisturizename = prodname ;
			      moisturizeprodline  = (String) product.getProductLinePrime() ;
			      moisturizearray = (ArrayList) products0.get(prodid) ;
			      moisturizeflag = true ;
			}
			if (product.getMasterProdType().equals("0005"))
			{
				  treatprod = prodid ;
				  treatimage = prodimage ;
				  treatname = prodname ;
			      treatprodline  = (String) product.getProductLinePrime() ;
			      treatarray = (ArrayList) products0.get(prodid) ;
			      treatflag = true ;
			}
			if (product.getMasterProdType().equals("0004"))
			{
				  etreatprod = prodid ;
				  etreatimage = prodimage ;
				  etreatname = prodname ;
			      etreatprodline  = (String) product.getProductLinePrime() ;
				  etreatarray = (ArrayList) products0.get(prodid) ;
				  etreatflag = true ;
			}
			if (product.getMasterProdType().equals("0009"))
			{
				  spfprod = prodid ;
				  spfimage = prodimage ;
				  spfname = prodname ;
			      spfprodline  = (String) product.getProductLinePrime() ;
			      spfarray = (ArrayList) products0.get(prodid) ;
			      spfflag  = true ;
			}
		}
		String bodyContent = "<body id=\"page2\">"+
							"<p>"+logoSrc+"</p>"+
							"<h1>YOUR REGIMEN DETAILS</h1>";
		
		if(cleanseflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Cleanse</h2>"+
							"      <p class=\"detail\">" ;
			       if (cleansearray.size() > 0 && cleansearray.get(0) != null)
			       	 bodyContent +=	"        "+cleansearray.get(0)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 1 && cleansearray.get(1) != null)
			       	 bodyContent +=	"        "+cleansearray.get(1)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 2 && cleansearray.get(2) != null)
			       	 bodyContent +=	"        "+cleansearray.get(2)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 3 && cleansearray.get(3) != null)
			       	 bodyContent +=	"        "+cleansearray.get(3)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 4 && cleansearray.get(4) != null)
			       	 bodyContent +=	"        "+cleansearray.get(4)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 5 && cleansearray.get(5) != null)
			       	 bodyContent +=	"        "+cleansearray.get(5)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (cleansearray.size() > 6 && cleansearray.get(6) != null)
			       	 bodyContent +=	"        "+cleansearray.get(6) ;
			       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+cleanseimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+cleanseprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+cleanseprod+"\">"+cleansename+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(moisturizeflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Moisturize</h2>"+
							"      <p class=\"detail\">";
						       if (moisturizearray.size() > 0 && moisturizearray.get(0) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(0)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 1 && moisturizearray.get(1) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(1)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 2 && moisturizearray.get(2) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(2)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 3 && moisturizearray.get(3) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(3)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 4 && moisturizearray.get(4) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(4)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 5 && moisturizearray.get(5) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(5)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (moisturizearray.size() > 6 && moisturizearray.get(6) != null)
						       	 bodyContent +=	"        "+moisturizearray.get(6) ;
						       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+moisturizeimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+moisturizeprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+moisturizeprod+"\">"+moisturizename+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(treatflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Treat</h2>"+
							"      <p class=\"detail\">";
		       if (treatarray.size() > 0 && treatarray.get(0) != null)
		       	 bodyContent +=	"        "+treatarray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 1 && treatarray.get(1) != null)
		       	 bodyContent +=	"        "+treatarray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 2 && treatarray.get(2) != null)
		       	 bodyContent +=	"        "+treatarray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 3 && treatarray.get(3) != null)
		       	 bodyContent +=	"        "+treatarray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 4 && treatarray.get(4) != null)
		       	 bodyContent +=	"        "+treatarray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 5 && treatarray.get(5) != null)
		       	 bodyContent +=	"        "+treatarray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (treatarray.size() > 6 && treatarray.get(6) != null)
		       	 bodyContent +=	"        "+treatarray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+treatimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+treatprodline+"</p>";
			bodyContent += "      <p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+treatprod+"\">"+treatname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(etreatflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Eye</h2>"+
							"      <p class=\"detail\">";
		       if (etreatarray.size() > 0 && etreatarray.get(0) != null)
		       	 bodyContent +=	"        "+etreatarray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 1 && etreatarray.get(1) != null)
		       	 bodyContent +=	"        "+etreatarray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 2 && etreatarray.get(2) != null)
		       	 bodyContent +=	"        "+etreatarray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 3 && etreatarray.get(3) != null)
		       	 bodyContent +=	"        "+etreatarray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 4 && etreatarray.get(4) != null)
		       	 bodyContent +=	"        "+etreatarray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 5 && etreatarray.get(5) != null)
		       	 bodyContent +=	"        "+etreatarray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (etreatarray.size() > 6 && etreatarray.get(6) != null)
		       	 bodyContent +=	"        "+etreatarray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+etreatimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+etreatprodline+"</p>";
			bodyContent += "      <p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+etreatprod+"\">"+etreatname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(spfflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Stay safe from the sun</h2>"+
							"      <p class=\"detail\">";
		       if (spfarray.size() > 0 && spfarray.get(0) != null)
		       	 bodyContent +=	"        "+spfarray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 1 && spfarray.get(1) != null)
		       	 bodyContent +=	"        "+spfarray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 2 && spfarray.get(2) != null)
		       	 bodyContent +=	"        "+spfarray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 3 && spfarray.get(3) != null)
		       	 bodyContent +=	"        "+spfarray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 4 && spfarray.get(4) != null)
		       	 bodyContent +=	"        "+spfarray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 5 && spfarray.get(5) != null)
		       	 bodyContent +=	"        "+spfarray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (spfarray.size() > 6 && spfarray.get(6) != null)
		       	 bodyContent +=	"        "+spfarray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+spfimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+spfprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+spfprod+"\">"+spfname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		bodyContent += "</body></html>";
//		System.out.println("bodycontent "+bodyContent) ;
		return bodyContent;
	}
	
	public String page3BodyContent(Hashtable products0,Hashtable productusage){
		
		String cleanseprod = "" ;
		String moisturizeprod = "" ;
		String treatprod = "" ;
		String etreatprod = "" ;
		String spfprod = "" ;
		String cleanseusage = "" ;
		String moisturizeusage = "" ;
		String treatusage = "" ;
		String etreatusage = "" ;
		String spfusage = "" ;
		String cleansename = "" ;
		String moisturizename = "" ;
		String treatname = "" ;
		String etreatname = "" ;
		String spfname = "" ;
		String cleansedirections = "" ;
		String moisturizedirections = "" ;
		String treatdirections = "" ;
		String etreatdirections = "" ;
		String spfdirections = "" ;
		boolean cleanseflag = false ;
		boolean etreatflag = false ;
		boolean spfflag = false ;
		boolean cleansedayflag = false ;
		boolean moisturizedayflag = false ;
		boolean treatdayflag = false ;
		boolean etreatdayflag = false ;
		boolean spfdayflag = false ;
		boolean cleansenightflag = false ;
		boolean moisturizenightflag = false ;
		boolean treatnightflag = false ;
		boolean etreatnightflag = false ;
		boolean spfnightflag = false ;
	//	System.out.println("produsage size "+productusage.size());
    //    System.out.println("productusage size xxx2 "+productusage.size()) ;
        Enumeration en2 = productusage.keys() ;
        while (en2.hasMoreElements())
        {
        	String x = (String) en2.nextElement() ;
      //  	System.out.println("prod 33333 usage "+x+"  "+productusage.get(x)) ;
			
        }

		Hashtable products = (Hashtable) products0.get("products") ;
		Enumeration en = products.keys() ;
		while (en.hasMoreElements())
		{
			String prodid = (String) en.nextElement() ;
			ProductVO product = (ProductVO) products.get(prodid) ;
			String prodname  = (String) product.getProductLinePrime()+" "+(String) product.getProdName().replaceAll("&", "&amp;") ;
			String produsage1 = "" ;
			String directions = (String) product.getShortDesc();
			if (directions != null && directions.length() > 0)
			  directions = (String) preProcess(directions) ;
		    else
			  directions = "Use daily. " ;
	//    System.out.println("directions "+prodid+"  "+directions);
		if (productusage.get(prodid.trim()) != null)
			{
			   produsage1 = (String) productusage.get(prodid.trim()) ;
		//	   System.out.println("produsage 1xxx "+prodid+"  "+produsage1);
			}
		//	System.out.println("produsage 1 "+prodid+"  "+produsage1);
			if (product.getMasterProdType().equals("0001"))
			{
			     cleanseprod = prodid ;
			     cleansename = prodname ;
			     cleanseusage = (String) productusage.get(prodid.trim()) ;
			     cleanseflag = true ;
			     if (cleanseusage != null && cleanseusage.length() > 0 && cleanseusage.trim().equals("daily"))
			       cleansedayflag = true ;
			     if (cleanseusage != null && cleanseusage.length() > 0 && cleanseusage.trim().equals("nightly"))
				   cleansenightflag = true ;
			     if (cleanseusage != null && cleanseusage.length() > 0 && cleanseusage.trim().equals("day & night"))
			     {
				       cleansedayflag = true ;
					   cleansenightflag = true ;
			     }
			     if (cleanseusage != null && cleanseusage.length() > 0 && cleanseusage.trim().equals("weekly"))
					   cleansenightflag = true ;
			     if (cleanseusage != null && cleanseusage.length() > 0 && cleanseusage.trim().equals("occasional /as needed"))
					   cleansenightflag = true ;
			     cleansedirections = directions ;
			}
			if (product.getMasterProdType().equals("0003"))
			{
				  moisturizeprod = prodid ;
				  moisturizename = prodname ;
				  moisturizeusage = produsage1 ;
				  if (moisturizeusage != null && moisturizeusage.length() > 0 && moisturizeusage.trim().equals("daily"))
				       moisturizedayflag = true ;
				  if (moisturizeusage != null && moisturizeusage.length() > 0 && moisturizeusage.trim().equals("nightly"))
					   moisturizenightflag = true ;
				  if (moisturizeusage != null && moisturizeusage.length() > 0 && moisturizeusage.trim().equals("day & night"))
				  {
					       moisturizedayflag = true ;
						   moisturizenightflag = true ;
				  }
				  if (moisturizeusage != null && moisturizeusage.length() > 0 && moisturizeusage.trim().equals("weekly"))
						   moisturizenightflag = true ;
				  if (moisturizeusage != null && moisturizeusage.length() > 0 && moisturizeusage.trim().equals("occasional /as needed"))
						   moisturizedayflag = true ;
				  if (product.getSPFId() != null && !product.getSPFId().equals("U001"))
				  	moisturizenightflag = false ;
				  moisturizedirections = directions ;
			}
			if (product.getMasterProdType().equals("0005"))
			{
				  treatprod = prodid ;
				  treatname = prodname ;
				  treatusage = produsage1 ;
				  if (treatusage != null && treatusage.length() > 0 && treatusage.trim().equals("daily"))
				       treatdayflag = true ;
				  if (treatusage != null && treatusage.length() > 0 && treatusage.trim().equals("nightly"))
					   treatnightflag = true ;
				  if (treatusage != null && treatusage.length() > 0 && treatusage.trim().equals("day & night"))
				  {
					       treatdayflag = true ;
						   treatnightflag = true ;
				  }
				  if (treatusage != null && treatusage.length() > 0 && treatusage.trim().equals("weekly"))
						   treatnightflag = true ;
				  if (treatusage != null && treatusage.length() > 0 && treatusage.trim().equals("occasional /as needed"))
					       treatnightflag = true ;
			      treatdirections = directions ;
			}
			if (product.getMasterProdType().equals("0004"))
			{
				  etreatprod = prodid ;
				  etreatname = prodname ;
				  etreatusage = produsage1 ;
				  etreatflag = true ;
				  if (etreatusage != null && etreatusage.length() > 0 && etreatusage.trim().equals("daily"))
				       etreatdayflag = true ;
				  if (etreatusage != null && etreatusage.length() > 0 && etreatusage.trim().equals("nightly"))
					   etreatnightflag = true ;
				  if (etreatusage != null && etreatusage.length() > 0 && etreatusage.trim().equals("day & night"))
				  {
					       etreatdayflag = true ;
						   etreatnightflag = true ;
				  }
				  if (etreatusage != null && etreatusage.length() > 0 && etreatusage.trim().equals("weekly"))
						   etreatnightflag = true ;
				  if (etreatusage != null && etreatusage.length() > 0 && etreatusage.trim().equals("occasional /as needed"))
					       etreatnightflag = true ;
			      etreatdirections = directions ;
			}
			if (product.getMasterProdType().equals("0009"))
			{
				  spfprod = prodid ;
				  spfname = prodname ;
				  treatusage = produsage1 ;
				  spfflag = true ;
				  if (spfusage != null && spfusage.length() > 0 && spfusage.trim().equals("daily"))
				       spfdayflag = true ;
			      spfdirections = directions ;
			}
		}
		int amstep = 0;
		int pmstep = 0;
		String bodyContent = "<body id=\"page2\">"+
							"<p>"+logoSrc+"</p>"+
							"<h1>HOW AND WHEN TO USE YOUR PRODUCTS</h1>"+
							"<div id=\"am\">"+
							"  <div class=\"header\">"+
							"    <h2>In the A.M.</h2>"+
							"  </div>";
		                    if (cleanseflag)
		                    {
		                      amstep++;
							  bodyContent+="  <p><strong>Step "+amstep+": "+cleansename+"</strong><br/>";
//							  bodyContent+="    Use daily. </p>";
							  bodyContent+="    "+cleansedirections+"</p>";
		                    }
		                    if (etreatflag)
		                    {
		                      amstep++;
		                      bodyContent+="  <p><strong>Step "+amstep+": "+etreatname+"</strong><br/>";
//							  bodyContent+="     Use daily.</p>";
							  bodyContent+="    "+etreatdirections+"</p>";
		                    }
		                    if (treatdayflag)
		                    {
		                    	amstep++;
		                    	bodyContent+="  <p><strong>Step "+amstep+": "+treatname+"</strong><br/>";
//		                    	bodyContent+="    Use daily.</p>";
							    bodyContent+="    "+treatdirections+"</p>";
		                    }
		                    if (moisturizedayflag)
		                    {
		                    	amstep++;
		                    	bodyContent+="  <p><strong>Step "+amstep+": "+moisturizename+"</strong><br/>";
//		                    	bodyContent+="    Use daily.</p>";
							    bodyContent+="    "+moisturizedirections+"</p>";
		                    }
		                    if (spfflag)
		                    {
		                    	amstep++;
		                    	bodyContent+="  <p><strong>Step "+amstep+": "+spfname+"</strong><br/>";
//		                    	bodyContent+="    Use daily.</p>";
							    bodyContent+="    "+spfdirections+"</p>";
		                    }
		                    bodyContent+="</div>"+
							"<div id=\"pm\">"+
							"  <div class=\"header\">"+
							"    <h2>In the P.M.</h2>"+
							"  </div>";
		                    if (cleanseflag)
		                    {
		                    	pmstep++;
		                    	bodyContent+="  <p><strong>Step "+pmstep+": "+cleansename+"</strong><br/>";
//		                    	bodyContent+="    Use nightly. </p>";
							    bodyContent+="    "+cleansedirections+"</p>";
		                    }
		                    if (etreatflag)
		                    {
		                    	  pmstep++;
			                      bodyContent+="  <p><strong>Step "+pmstep+": "+etreatname+"</strong><br/>";
//							      bodyContent+=" Use nightly.</p>";
							      bodyContent+="    "+etreatdirections+"</p>";
		                    }
		                    if (treatnightflag)
		                    {
		                    	 pmstep++;
		                    	 bodyContent+="  <p><strong>Step "+pmstep+": "+treatname+"</strong><br/>";
//		                    	 bodyContent+="    Use nightly.</p>";
							     bodyContent+="    "+treatdirections+"</p>";
		                    }
		                    if (moisturizenightflag)
		                    {
		                    	  pmstep++;
		                    	  bodyContent+="  <p><strong>Step "+pmstep+": "+moisturizename+"</strong><br/>";
//		                    	  bodyContent+="    Use nightly.</p>";
							      bodyContent+="    "+moisturizedirections+"</p>";
		                    }
		                    bodyContent+="</div>";
		            		if(fourthPageStatus == 0)
		            		{
		            			bodyContent += impNotice + "";
		            			bodyContent += ""+bottomSrc+"";
		            			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12873\">Shop Skin Care at Beauty.com</a><br/><br/>";
		            			bodyContent += aboutUs + "</body></html>";
		            		}
		            		else if(fourthPageStatus == 1)
		            		{
		            			bodyContent += impNotice + "";
		            			bodyContent += ""+bottomSrc+"";
		            			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12873\">Shop Skin Care at Beauty.com</a><br/><br/>";
		            			bodyContent += "</body></html>";
		            		}
		            		else if(fourthPageStatus == 2)
		            		{
		            			bodyContent += "</body></html>";
		            		}
		            	//	System.out.println(bodyContent) ;
		            		return bodyContent;
		            	}
		            	
		            	public String page4BodyContent(){
		            			
		            		String bodyContent = "<body id=\"page1\">"+
		            							"<p>"+logoSrc+"</p>";
		            		if(fourthPageStatus == 1)
		            		{
		            			bodyContent += "";
		            			bodyContent += aboutUs + "</body></html>";
		            		}
		            		else if(fourthPageStatus == 2)
		            		{
		            			bodyContent += impNotice + "";
		            			bodyContent += ""+bottomSrc+"";
		            			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12873\">Shop Skin Care at Beauty.com</a><br/><br/>";
		            			bodyContent += aboutUs + "</body></html>";
		            		}
		            		return bodyContent;
		            	}
	
	public String preProcess(String newString){
		if (newString != null && newString.length() > 0)
		{
		 newString = newString.replaceAll("&amp;", "&"); 
    	 newString = newString.replaceAll("&","&amp;");
    	 newString = newString.replaceAll("&amp;nbsp;", "&nbsp;");
    	 newString = newString.replaceAll("&amp; 153;", "™");
    	 newString = newString.replaceAll("&amp;#153;", "™");
    	 newString = newString.replaceAll("&amp;reg;", "®");
    	 newString = newString.replaceAll("<LI>", " ");
    	 newString = newString.replaceAll("</LI>", " ");
    	 newString = newString.replaceAll("<UL>", " ");
    	 newString = newString.replaceAll("</UL>", " ");
    	 newString = newString.replaceAll("<OL>", " ");
    	 newString = newString.replaceAll("</OL>", " ");
    	 newString = newString.replaceAll("<BR>", "<BR/>");
    	 newString = newString.replaceAll("&nbsp;"," ");
    	 newString = newString.replaceAll("&deg;"," ");
    	 newString = newString.replaceAll("&amp;deg;"," ");
		}
	//	System.out.println("new string "+newString) ;
    	return newString;
    }
	
	public StringBuffer pageContent(int pageNumber, HttpServletRequest request, ArrayList profdetails){
		
		StringBuffer pageBuffer = new StringBuffer();
		pageBuffer.append(head);
		//pageBuffer.append(css+"</head>");
		pageBuffer.append(css+" <style>@page { @bottom-right { content: \"page "+pageNumber+"\";font-family:Arial;} }</style></head>");

		switch(pageNumber){
		case 1 : pageBuffer.append(page1BodyContent(profdetails));
				 break;
		case 2 : {
			       Hashtable products = (Hashtable) profdetails.get(3) ;
			       pageBuffer.append(page2BodyContent(products));
		           break;
		         }
		case 3 : {
		           Hashtable products = (Hashtable) profdetails.get(3) ;
		           Hashtable productusage = (Hashtable) profdetails.get(4) ;
			       pageBuffer.append(page3BodyContent(products,productusage));
		 		   break;
		         } 
		case 4 : pageBuffer.append(page4BodyContent());
		 		 break;
		}
		return pageBuffer;
	}
	
	public File createPdf(File page, int pageNumber, HttpServletRequest request, ArrayList profdetails, String regimenId){
		try {
			StringBuffer pageBuffer = pageContent(pageNumber, request, profdetails);
//		    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//		    Document doc = builder.parse(new StringBufferInputStream(pageBuffer.toString()));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( new InputSource(new StringReader(pageBuffer.toString())));

		    ITextRenderer renderer = new ITextRenderer();
		    renderer.setDocument(doc, null);

		    OutputStream os = new FileOutputStream(page);
		    renderer.layout();
		    renderer.createPDF(os);
		    os.close();
		    if(pageNumber == 3){
		    	PdfReader reader = new PdfReader(pdfFilesPath+"/"+regimenId+"page3.pdf");
		    	if(reader.getNumberOfPages() > 1){
		    		fourthPageStatus++;
		    		createPdf(page, 3, request, profdetails, regimenId);
		    	}
			}
		    return page;
		}catch(Exception e) {
			  e.printStackTrace();
		}
		return null;
	}
	
	public void pdfForRegimen(String regimenId, File pdfFile, File page1, File page2, File page3, File page4, String filePath, HttpServletRequest request){
		pdfFilesPath = filePath;
		
		RegimenDetailsBean rdb = new RegimenDetailsBean() ;
		ArrayList profdetails = new ArrayList() ;
		Hashtable products = new Hashtable() ;
		try
		{
		  profdetails = (ArrayList) rdb.getProductInfo(request) ;
		}
		catch (Exception ex)
		{
			ex.printStackTrace() ;
		}
		
		
		createPdf(page1, 1, request, profdetails, regimenId);
		createPdf(page2, 2, request, profdetails, regimenId);
		createPdf(page3, 3, request, profdetails, regimenId);
		if(fourthPageStatus != 0)
			createPdf(page4, 4, request, profdetails, regimenId);
		createRegimenPdf(regimenId, pdfFile, page1, page2, page3, page4);
	}
	
	public void createRegimenPdf(String regimenId, File pdfFile, File page1, File page2, File page3, File page4){
		try{
			PdfReader reader = new PdfReader(pdfFilesPath+"/"+regimenId+"page1.pdf");
			com.lowagie.text.Document document = new com.lowagie.text.Document(reader.getPageSizeWithRotation(1));
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(pdfFilesPath+"/"+regimenId+".pdf"));
			document.open();
			copy.addPage(copy.getImportedPage(reader, 1));
			reader = new PdfReader(pdfFilesPath+"/"+regimenId+"page2.pdf");
			copy.addPage(copy.getImportedPage(reader, 1));
			reader = new PdfReader(pdfFilesPath+"/"+regimenId+"page3.pdf");
			copy.addPage(copy.getImportedPage(reader, 1));
			if(fourthPageStatus != 0) {
				reader = new PdfReader(pdfFilesPath+"/"+regimenId+"page4.pdf");
				copy.addPage(copy.getImportedPage(reader, 1));
			}
			document.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

	
