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
import com.svb.documents.pdf.HairRegimenDetailsBean;


public class HairRegimenDescriptionPdf {

	String pdfFilesPath;
	int fourthPageStatus = 0;
	String logoSrc = "<img src=\"http://65.74.148.56/svbeauty/img/production/BCOM_logo_StyleValet.jpg\" class=\"logo\"/>";
	String bottomSrc = "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace\"/><img src=\"http://65.74.148.56/svbeauty/images/sv_hair_brochure_bottom.gif\" class=\"logo2\"/>";
	String graphicSrc = "<img src=\"http://65.74.148.56/svbeauty/images/sv_hair_header.gif\" class=\"graphics\"/>";
	
	//"<?xml version='1.0' encoding='UTF-8'?> "+ 
	//"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
	String head = "<html><head>"+
	"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Page 1</title>";

String css = "<style>"+
		"body{font-size: 83.125%;}"+
		"@page {margin-top: -8px;margin-right: 32px;margin-bottom: 32px;margin-left: 32px;}"+
		//"body {margin-top: -8px;margin-right: 32px;margin-bottom: 32px;margin-left: 32px;}"+
		"img.logo {width: 16.4em;height: 6.4em;}"+
		"img.graphics {width:46.3em;height:4.2em;margin-top:1.7em;}"+
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
		"table.regimenDetail p.beautyDetail {text-align: right;}"+
		"table.regimenDetail p.price {margin: 0;	color: #333333;}"+
		"</style>";

String impNotice = "<p><strong>Important Notice:</strong><br/>"+
			"  <em>Information on this "+
			"site is not meant to be a substitute for the advice of a "+
			"hair care or medical professional. Always read the complete manufacturer’s instructions for each "+
			"product. If at any time excessive or persistent irritation, hair loss or other side effect occurs, "+
			"discontinue use and seek medical attention. Consult with your physician before "+
			"using these products if you are pregnant, nursing, or have allergies or other sensitivities.</em></p>";

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

	public String hairThicknessDetails(int hairThickness) {

		switch(hairThickness){
			case 1:
			   return
//				 "<h1>Hair Care for Your Hair Type &amp; Texture</h1>"+
//				 "<p>Regardless of your age, your hair thickness and texture will be a key factor in the selection of your best shampoos, conditioners and treatment products. Because your hair is <strong>thick and/or coarse,</strong> you are able to use products rich in emollients, moisturizers and conditioning agents. </p>"+
//				 "<p>When it comes to styling products, you can use the most luscious products to achieve a smooth rich shine for your straight hair or to retain soft and rich curls throughout the day.</p>"+
//				 "<p>Your cleansing and condition products provide high levels of moisturization and protection - designed to strengthen and protect the hair’s cuticle - the outside layer of the hair.  Chemical processes, heavy styling (lots of heat to the hair), sun exposure and other daily stresses can damage the hair’s cuticle. In effect, this damage puts “cracks” in the cuticle, which can make your hair look like it has “the frizzies”. Recommended conditioners and treatment products have special smoothing ingredients that help to “fill in” the damaged cuticle which helps your hair lay flat, look smoother to better reflect light, all for a great overall shine.</p>"+
//				 "<p>Your scalp deserves special attention too; a dry scalp can be itchy and irritated but will respond well to moisturizers, anti-inflammatory ingredients and skin soothers. Oily scalps do well with astringents and toners, skin healers and other ingredients that help manage active oil production. Dry or oily scalp, almost all hair types and textures will find that the ends of their hair are dry and in need of emollients and conditioners applied as a treatment or daily conditioner. Tip # 1: Focus your conditioners and treatment on the hair beginning from the middle of the hair strands to the furthermost ends of the hair; Tip #2; when shampooing, massage the scalp as opposed to rubbing hair strand against hair strand; friction on the hair causes cracks and breakage; massaging the scalp gets better cleansing results and the motion encourages blood circulation which in turn encourages hair growth and vitality.</p>";
			   "<h1>Tips &amp; Suggestions for Thin or Fine Hair:</h1>"+
//			   "<br/>"+
			   "<p>You may have thin hair, which means you have fewer strands of hair than average, or you may have plenty of hair, but your hair is somewhat (or very) fine; even if your thin hair can tend to be a little coarse now and again, you should use hair products that will help give your thin hair lift, strength and volume.</p>"+
//			   "<br/>"+
			   "<p>Selecting products specifically formulated to address thin or fine hair needing volume will provide your hair with: <br/>"+
			   "<ol><li>	Proteins, amino acids and water-binding ingredients to \"plump\" your hair, giving it a thicker  appearance </li>"+
			   "<li>	Special ingredients that \"stiffen\" your hair by adding a very fine coating  (film formers; plasticizing agents often found in gels and sprays);</li>"+
			   "<li>	Antioxidants, which encourage more vibrant hair and a healthier scalp, and protect hair from future damage. </li></ol></p>"+
//			   "<br/>"+
			   "<strong>Shampoo Tip:</strong><br/>"+
			   "<p>Massage the scalp while you shampoo to increase your natural circulation and restore hair’s vibrancy.  Avoid rubbing hair against hair – it increases breakage. When towel-drying, blot, don’t rub.</p>"+
			   "<br/>"+
			   "<strong>Condition &amp; Treatment Tip:</strong><br/>"+
			   "<p>Your own natural oil is the furthest away from where you need it most, so make sure you apply more conditioner to the ends. Leaving conditioners and treatments on the hair for a longer period of time increases the impact of the product.</p>"+
//			   "<br/>"+
			   "<strong>To Prevent Damage:</strong><br/>"+
			   "<p>Use a wide-toothed comb after washing; as your hair is far more fragile when it is wet than dry. Make sure you towel-dry before blow-drying to remove excess moisture, and keep your blow dryer 3 to 6 inches away while drying hair. </p>"+
			   "" ;
			case 2:
				return 
				"<h1>Tips &amp; Suggestions for Hair of Average Thickness: </h1>"+
//				"<br/>"+
				"<p>Selecting products specifically formulated to address normal hair will provide your hair with a balanced combination of ingredients for optimum results: "+
//				"<br/>"+
				"<ol><li>	Humectants - to draw moisture from the outside</li>"+
				"<li>	Moisturizing and protection agents  - to keep the moisture in</li>"+
				"<li>	Emollients  - to keep hair soft and manageable</li>"+
				"<li>	Clarifying and toning ingredients - to prepare the hair for conditioning and treatments</li>"+
				"<li>	Silicones to add shine and silkiness</li>"+
				"<li>	Protective agents and antioxidants - to prevent future damage</li></ol></p>"+
//				"<br/>"+
				"<strong>Shampoo Tip:  </strong>"+
//				"<br/>"+
				"<p>Massage the scalp while you shampoo to increase your natural circulation and restore hair’s vibrancy.  Avoid rubbing hair against hair - it increases breakage. When towel-drying, blot, don’t rub.</p>"+
				"<strong>Condition &amp; Treatment Tip:</strong>"+
//				"<br/>"+
				"<p>Your own natural oil is the furthest away from where you need it most, so make sure you apply more conditioner to the ends. Leaving conditioners and treatments on the hair for a longer period of time increases the impact of the product.</p>"+
				"<strong>To Prevent Damage:</strong>"+
//				"<br/>"+
				"<p>Use a wide-toothed comb after washing; as your hair is far more fragile when it is wet than dry. Make sure you towel-dry before blow-drying to remove excess moisture, and keep your blow dryer 3 to 6 inches away while drying hair. </p>"+
				"";
				
			case 3:
				return
				"<h1>Tips &amp; Suggestions for Thick or Coarse Hair: </h1>"+
//				"<br/>"+
				"<p>You have thick hair, which means you have more strands of hair than average with a texture ranging from fine to average to coarse; for thick hair you should use hair products that will help give you more manageability.</p>"+
//				"<br/>"+
				"<p>Selecting products specifically formulated to address thick or coarse hair will provide your hair with: <br/>"+
				"<ol><li>	Silicones to add shine and silkiness</li>"+
				"<li>	Moisturizers to help soften hair</li>"+
				"<li>	Detanglers that add a coating to make hair strands slide away from each other</li>"+
				"<li>	Smoothening agents to \"fill in\"  cracks in the outside layer of your hair (the cuticle), giving hair a smoother, shinier finish</li>"+
				"<li>	Antioxidants, which encourage more vibrant hair and a healthier scalp, and protect hair from future damage.</li></ol> </p>"+
//				"<br/>"+
				"<strong>Shampoo Tip:  </strong>"+
//				"<br/>"+
				"<p>Massage the scalp while you shampoo to increase your natural circulation and restore hair’s vibrancy.  Avoid rubbing hair against hair – it increases breakage. When towel-drying, blot, don’t rub.</p>"+
				"<strong>Condition &amp; Treatment Tip:</strong>"+
//				"<br/>"+
				"<p>Your own natural oil is the furthest away from where you need it most, so make sure you apply more conditioner to the ends. Leaving conditioners and treatments on the hair for a longer period of time increases the impact of the product.</p>"+
				"<strong>To Prevent Damage:</strong>"+
				"<p>Use a wide-toothed comb after washing; as your hair is far more fragile when it is wet than dry. Make sure you towel-dry before blow-drying to remove excess moisture, and keep your blow dryer 3 to 6 inches away while drying hair. </p>"+
				"";

		}
		return "";
	}
	
	public String hairStyleDetails(int hairStyle) {

		switch(hairStyle){
			case 1:
			   return
			   "<strong><u>Styling Tip: If you have curly hair and want to have a straight hair style:</u></strong>"+
			   "<br/>"+
			   "<p>Start by conditioning to soften hair; using a highly emollient conditioner for thick or coarse hair (leave-in conditioners are good), or a light rinse-out conditioner for thin or fine hair. </p>"+
			   "<p>After towel-drying or partially drying with a blow dryer, add serums, creams or lotions with emollients to the length of your hair. Avoid adding product to the roots, as it can make your hair look heavy or oily.</p>"+
			   "<p>Continue drying to work products in further. For extremely curly hair, you may need to use a stronger straightening product and a straightening iron. Dry the hair fully, then add a finishing product such as wax, pomade or spay for extra shine and hold.</p>"+
			   "" ;
			case 2:
				return 
				   "<strong><u>Styling Tip: If you have straight or wavy hair and want curls:</u></strong>"+
				   "<br/>"+
				   "<p>Start by conditioning to soften hair; using highly emollient conditioners for thick or coarse hair (leave-in conditioners are best for thick hair) or light rinse-out conditioners for thin or fine hair.</p>"+
				   "<p>If your hair is straight and you want curl, use a curling iron or hot rollers on completely dry hair; finish with a holding spray.</p>"+
				   "<p>If your hair has a natural wave, use a product designed to enhance curl. Apply a styling gel, spray or mousse to damp or dry hair, following the product’s directions. If your hair has some natural curl you can apply a serum or spray. Use a blow dryer with a diffuser while forming curls with your fingers or by scrunching the hair in place. Lift hair from the root while drying to gain volume.</p>"+
				   "<p>When the hair is completely dry, add a spray or finishing gel for extra shine and hold.</p>"+
				   "";
				

		}
		return "";
	}
	public String page1BodyContent(ArrayList profdetails){

		String profilestring = "" ;
		String warnings = "" ;
		String thickness = "" ;
		String texture = "" ;
		String chairstyle = "" ;
		
		int hairThickness = 0 ;
		int hairStyle = 0 ;
		String commontext = "Test a small amount of each new product on a hidden area (such as behind your ear) to make sure that your skin does not have any adverse reactions.  Always follow the recommended application quantity and frequency for each product.  Consult your physician before using these products if you are pregnant or nursing." ;
		
		//ArrayList dynamic = new ArrayList() ;
		
		try
		{
			
		  profilestring = (String) profdetails.get(0) ;
		  thickness = (String) profdetails.get(1) ;
		  texture = (String) profdetails.get(2) ;
		  chairstyle = (String) profdetails.get(5) ;
		  //dynamic = (ArrayList) profdetails.get(2) ;
		  
		  if (thickness.equals("I004") || texture.equals("X004"))
		  	hairThickness = 1 ;
		  if (thickness.equals("I005") || texture.equals("X005"))
		  	hairThickness = 1 ;
		  if (thickness.equals("I003") || texture.equals("X003"))
		  	hairThickness = 2 ;
		  if (thickness.equals("I001") || texture.equals("X001"))
		  	hairThickness = 3 ;
		  if (thickness.equals("I002") || texture.equals("X002"))
		  	hairThickness = 3 ;

		  if (chairstyle.equals("S004"))
		  	hairStyle = 1 ;
		  if (chairstyle.equals("S005"))
		  	hairStyle = 2 ;
		  
//          if (dynamic.size() > 0)
  //        {
    //           warnings = (String) dynamic.get(0) ;
              // commontext = (String) dynamic.get(1) ;
      //    }

		  //System.out.println("vb 10 "+profilestring) ;
		}
		catch (Exception ex)
		{
			ex.printStackTrace() ;
		}
		String bodyContent = "<body id=\"page1\">"+
					"<p>"+logoSrc+"</p>"+
					"<p>"+graphicSrc+"</p>"+
					"<h1>YOUR PERSONAL HAIR CARE REGIMEN</h1>"+
					""+profilestring+""+
					hairThicknessDetails(hairThickness)+
				//	"<p><strong>Please note:</strong><br/>"+
				//	"<em>"+commontext+"</em><br/><br/>"+
				//	"<em>"+warnings+"</em></p>"+
					hairStyleDetails(hairStyle)+
				//	"<p>"+bottomSrc+"</p>"+
					"</body></html>";
		//System.out.println("page1 "+bodyContent) ;
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
		
		String shampooprod = "" ;
		String conditionerprod = "" ;
		String htreatprod = "" ;
		String stylingprod = "" ;
		String finishprod = "" ;
		String shampooimage = "" ;
		String conditionerimage = "" ;
		String htreatimage = "" ;
		String stylingimage = "" ;
		String finishimage = "" ;
		String shampooname = "" ;
		String conditionername = "" ;
		String htreatname = "" ;
		String stylingname = "" ;
		String finishname = "" ;
		String shampooprodline = "" ;
		String conditionerprodline = "" ;
		String htreatprodline = "" ;
		String stylingprodline = "" ;
		String finishprodline = "" ;
		ArrayList shampooarray = new ArrayList() ;
		ArrayList conditionerarray = new ArrayList() ;
		ArrayList htreatarray = new ArrayList() ;
		ArrayList stylingarray = new ArrayList() ;
		ArrayList finisharray = new ArrayList() ;
		boolean shampooflag = false ;
		boolean conditionerflag = false ;
		boolean htreatflag = false ;
		boolean stylingflag = false ;
		boolean finishflag = false ;

		Hashtable products = (Hashtable) products0.get("products") ;
		Enumeration en = products.keys() ;
		while (en.hasMoreElements())
		{
			String prodid = (String) en.nextElement() ;
			ProductVO product = (ProductVO) products.get(prodid) ;
			String prodimage = (String) product.getImage().replaceAll("&", "&amp;") ; 
			String prodname  = (String) product.getProdName().replaceAll("&", "&amp;") ;
			
			if (product.getMasterProdType().equals("1001"))
			{
			     shampooprod = prodid ;
			     shampooimage = prodimage ;
			     shampooname = prodname ;
			     shampooarray = (ArrayList) products0.get(prodid) ;
			     shampooprodline  = (String) product.getProductLinePrime() ;
			     shampooflag = true ;
			  //   System.out.println("shampoo array "+shampooarray.size()) ;
			}
			if (product.getMasterProdType().equals("1003"))
			{
				  conditionerprod = prodid ;
			      conditionerimage = prodimage ;
				  conditionername = prodname ;
			      conditionerprodline  = (String) product.getProductLinePrime() ;
			      conditionerarray = (ArrayList) products0.get(prodid) ;
			      conditionerflag = true ;
			}
			if (product.getMasterProdType().equals("1005"))
			{
				  htreatprod = prodid ;
				  htreatimage = prodimage ;
				  htreatname = prodname ;
			      htreatprodline  = (String) product.getProductLinePrime() ;
			      htreatarray = (ArrayList) products0.get(prodid) ;
			      htreatflag = true ;
			}
			if (product.getMasterProdType().equals("1004"))
			{
				  stylingprod = prodid ;
				  stylingimage = prodimage ;
				  stylingname = prodname ;
			      stylingprodline  = (String) product.getProductLinePrime() ;
				  stylingarray = (ArrayList) products0.get(prodid) ;
				  stylingflag = true ;
			}
			if (product.getMasterProdType().equals("1009"))
			{
				  finishprod = prodid ;
				  finishimage = prodimage ;
				  finishname = prodname ;
			      finishprodline  = (String) product.getProductLinePrime() ;
			      finisharray = (ArrayList) products0.get(prodid) ;
			      finishflag  = true ;
			}
		}
		String bodyContent = "<body id=\"page2\">"+
							"<p>"+logoSrc+"</p>"+
							"<h1>YOUR REGIMEN DETAILS</h1>";
		
		if(shampooflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Shampoo</h2>"+
							"      <p class=\"detail\">" ;
			       if (shampooarray.size() > 0 && shampooarray.get(0) != null)
			       	 bodyContent +=	"        "+shampooarray.get(0)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 1 && shampooarray.get(1) != null)
			       	 bodyContent +=	"        "+shampooarray.get(1)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 2 && shampooarray.get(2) != null)
			       	 bodyContent +=	"        "+shampooarray.get(2)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 3 && shampooarray.get(3) != null)
			       	 bodyContent +=	"        "+shampooarray.get(3)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 4 && shampooarray.get(4) != null)
			       	 bodyContent +=	"        "+shampooarray.get(4)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 5 && shampooarray.get(5) != null)
			       	 bodyContent +=	"        "+shampooarray.get(5)+"<br/>";
			       else
			       	 bodyContent +=	"        <br/>";
			       if (shampooarray.size() > 6 && shampooarray.get(6) != null)
			       	 bodyContent +=	"        "+shampooarray.get(6) ;
			       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+shampooimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+shampooprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+shampooprod+"\">"+shampooname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(conditionerflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Conditioner</h2>"+
							"      <p class=\"detail\">";
						       if (conditionerarray.size() > 0 && conditionerarray.get(0) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(0)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 1 && conditionerarray.get(1) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(1)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 2 && conditionerarray.get(2) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(2)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 3 && conditionerarray.get(3) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(3)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 4 && conditionerarray.get(4) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(4)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 5 && conditionerarray.get(5) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(5)+"<br/>";
						       else
						       	 bodyContent +=	"        <br/>";
						       if (conditionerarray.size() > 6 && conditionerarray.get(6) != null)
						       	 bodyContent +=	"        "+conditionerarray.get(6) ;
						       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+conditionerimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+conditionerprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+conditionerprod+"\">"+conditionername+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(htreatflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Treat</h2>"+
							"      <p class=\"detail\">";
		       if (htreatarray.size() > 0 && htreatarray.get(0) != null)
		       	 bodyContent +=	"        "+htreatarray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 1 && htreatarray.get(1) != null)
		       	 bodyContent +=	"        "+htreatarray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 2 && htreatarray.get(2) != null)
		       	 bodyContent +=	"        "+htreatarray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 3 && htreatarray.get(3) != null)
		       	 bodyContent +=	"        "+htreatarray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 4 && htreatarray.get(4) != null)
		       	 bodyContent +=	"        "+htreatarray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 5 && htreatarray.get(5) != null)
		       	 bodyContent +=	"        "+htreatarray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (htreatarray.size() > 6 && htreatarray.get(6) != null)
		       	 bodyContent +=	"        "+htreatarray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+htreatimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+htreatprodline+"</p>";
			bodyContent += "      <p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+htreatprod+"\">"+htreatname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(stylingflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>Styling</h2>"+
							"      <p class=\"detail\">";
		       if (stylingarray.size() > 0 && stylingarray.get(0) != null)
		       	 bodyContent +=	"        "+stylingarray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 1 && stylingarray.get(1) != null)
		       	 bodyContent +=	"        "+stylingarray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 2 && stylingarray.get(2) != null)
		       	 bodyContent +=	"        "+stylingarray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 3 && stylingarray.get(3) != null)
		       	 bodyContent +=	"        "+stylingarray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 4 && stylingarray.get(4) != null)
		       	 bodyContent +=	"        "+stylingarray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 5 && stylingarray.get(5) != null)
		       	 bodyContent +=	"        "+stylingarray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (stylingarray.size() > 6 && stylingarray.get(6) != null)
		       	 bodyContent +=	"        "+stylingarray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+stylingimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+stylingprodline+"</p>";
			bodyContent += "      <p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+stylingprod+"\">"+stylingname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		if(finishflag){
			bodyContent +=  "<table class=\"regimenDetail\">"+
							"  <tbody><tr>"+
							"    <td class=\"detail\"><h2>The Finishing Touch </h2>"+
							"      <p class=\"detail\">";
		       if (finisharray.size() > 0 && finisharray.get(0) != null)
		       	 bodyContent +=	"        "+finisharray.get(0)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 1 && finisharray.get(1) != null)
		       	 bodyContent +=	"        "+finisharray.get(1)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 2 && finisharray.get(2) != null)
		       	 bodyContent +=	"        "+finisharray.get(2)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 3 && finisharray.get(3) != null)
		       	 bodyContent +=	"        "+finisharray.get(3)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 4 && finisharray.get(4) != null)
		       	 bodyContent +=	"        "+finisharray.get(4)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 5 && finisharray.get(5) != null)
		       	 bodyContent +=	"        "+finisharray.get(5)+"<br/>";
		       else
		       	 bodyContent +=	"        <br/>";
		       if (finisharray.size() > 6 && finisharray.get(6) != null)
		       	 bodyContent +=	"        "+finisharray.get(6) ;
		       bodyContent   +=	"</p></td>";
			bodyContent += "<td><img src=\""+finishimage+"\"/>";
			bodyContent += "<p class=\"productDetail\">"+finishprodline+"</p>";
			bodyContent += "<p class=\"productDetail\"><a href=\"http://www.drugstore.com/products/prod.asp?pid="+finishprod+"\">"+finishname+"</a></p>";
			bodyContent +="</td></tr></tbody></table>";
		}
		bodyContent += "</body></html>";
		//System.out.println("pag 2bodycontent "+bodyContent) ;
		return bodyContent;
	}
	
	public String page3BodyContent(Hashtable products0,Hashtable productusage){
		
		String shampooprod = "" ;
		String conditionerprod = "" ;
		String htreatprod = "" ;
		String stylingprod = "" ;
		String finishprod = "" ;
		String shampoousage = "" ;
		String conditionerusage = "" ;
		String htreatusage = "" ;
		String stylingusage = "" ;
		String finishusage = "" ;
		String shampooname = "" ;
		String conditionername = "" ;
		String htreatname = "" ;
		String stylingname = "" ;
		String finishname = "" ;
		String shampoodirections = "" ;
		String conditionerdirections = "" ;
		String htreatdirections = "" ;
		String stylingdirections = "" ;
		String finishdirections = "" ;
		boolean shampooflag = false ;
		boolean stylingflag = false ;
		boolean finishflag = false ;
		boolean shampoodayflag = false ;
		boolean conditionerflag = false ;
		boolean htreatflag = false ;
		boolean stylingdayflag = false ;
		boolean finishdayflag = false ;
		boolean shampoonightflag = false ;
		boolean conditionernightflag = false ;
		boolean htreatnightflag = false ;
		boolean stylingnightflag = false ;
		boolean finishnightflag = false ;

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
			if (productusage.get(prodid.trim()) != null)
			{
			   produsage1 = (String) productusage.get(prodid.trim()) ;
		//	   System.out.println("produsage 1xxx "+prodid+"  "+produsage1);
			}
			if (product.getMasterProdType().equals("1001"))
			{
			     shampooprod = prodid ;
			     shampooname = prodname ;
			     shampooflag = true ;
			     shampoodirections = directions ;
			}
			if (product.getMasterProdType().equals("1003"))
			{
				  conditionerprod = prodid ;
				  conditionername = prodname ;
				  conditionerflag = true ;
				  conditionerdirections = directions ;
			}
			if (product.getMasterProdType().equals("1005"))
			{
				  htreatprod = prodid ;
				  htreatname = prodname ;
			      htreatflag = true ;
			      htreatdirections = directions ;
			}
			if (product.getMasterProdType().equals("1004"))
			{
				  stylingprod = prodid ;
				  stylingname = prodname ;
				  stylingflag = true ;
			      stylingdirections = directions ;
			}
			if (product.getMasterProdType().equals("1009"))
			{
				  finishprod = prodid ;
				  finishname = prodname ;
				  finishflag = true ;
			      finishdirections = directions ;
			}
		}
		int amstep = 0;
		int pmstep = 0;
		String bodyContent = "<body id=\"page2\">"+
							"<p>"+logoSrc+"</p>"+
							"<h1>HOW TO USE YOUR PRODUCTS</h1>"+
							"<div id=\"am\">"+
//							"  <div class=\"header\">"+
//							"    <h2>Directions</h2>"+
//							"  </div>";
							"" ;
		                    if (shampooflag)
		                    {
		                      amstep++;
							  bodyContent+="  <p><strong>Shampoo: "+shampooname+"</strong><br/>";
//							  bodyContent+="    Use daily. </p>";
							  bodyContent+="    "+shampoodirections+"</p>";
		                    }
		                    if (conditionerflag)
		                    {
		                    	amstep++;
		                    	bodyContent+="  <p><strong>Conditioner: "+conditionername+"</strong><br/>";
//		                    	bodyContent+="    Use daily.</p>";
							    bodyContent+="    "+conditionerdirections+"</p>";
		                    }
		                    if (htreatflag)
		                    {
		                     amstep++;
		                     bodyContent+="  <p><strong>Treatment: "+htreatname+"</strong><br/>";
//		                     bodyContent+="    Use daily.</p>";
							 bodyContent+="    "+htreatdirections+"</p>";
		                    }
		                    if (stylingflag)
		                    {
		                      amstep++;
		                      bodyContent+="  <p><strong>Styling: "+stylingname+"</strong><br/>";
//							  bodyContent+="     Use daily.</p>";
							  bodyContent+="    "+stylingdirections+"</p>";
		                    }
		                    if (finishflag)
		                    {
		                    	amstep++;
		                    	bodyContent+="  <p><strong>The finishing touch: "+finishname+"</strong><br/>";
//		                    	bodyContent+="    Use daily.</p>";
							    bodyContent+="    "+finishdirections+"</p>";
		                    }
		                    bodyContent+="</div><br/>" ;
		if(fourthPageStatus == 0)
		{
			bodyContent += impNotice + "";
			bodyContent += ""+bottomSrc+"";
			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12875\">Shop Hair Care at Beauty.com</a><br/><br/>";
			bodyContent += aboutUs + "</body></html>";
		}
		else if(fourthPageStatus == 1)
		{
			bodyContent += impNotice + "";
			bodyContent += ""+bottomSrc+"";
			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12875\">Shop Hair Care at Beauty.com</a><br/><br/>";
			bodyContent += "</body></html>";
		}
		else if(fourthPageStatus == 2)
		{
			bodyContent += "</body></html>";
		}
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
			bodyContent += "<img src=\"http://65.74.148.56/svbeauty/images/shared/trans.gif\" class=\"logospace2\"/><a href=\"http://www.drugstore.com/templates/stdcat/default.asp?catid=12875\">Shop Hair Care at Beauty.com</a><br/><br/>";
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
		//	System.out.println("pageNumber "+pageNumber) ;
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
		
		HairRegimenDetailsBean rdb = new HairRegimenDetailsBean() ;
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

	
