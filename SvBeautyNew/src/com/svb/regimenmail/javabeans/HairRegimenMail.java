/*
 * Created on May 2, 2005
 *
 */
package com.svb.regimenmail.javabeans;

import java.util.*;
import java.io.File;

import javax.servlet.http.HttpServletRequest;

import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.svb.common.ImageName;
import com.svb.common.mail.EmailMessage;
import com.svb.common.mail.MessageSenderQueue;
import com.svb.common.mail.SendMessage;
import com.svb.customerregistration.dal.CustomerRegistrationDAL;

/**
 * @author Neelima
 *
 */
public class HairRegimenMail {

	public static final String hostName = "http://sv-beauty.com/svbeauty";

	public void sendEmail(String custId,String custName,String email,String content,String salutation,String filename,String attachpdf, String toEmail, HttpServletRequest request) throws Exception{

		String pbsemail = "";
        System.out.println("vbbbb 1 "+custId) ;
        System.out.println("vbbbb 2 "+custName) ;
        System.out.println("vbbbb 3 "+email) ;
        System.out.println("vbbbb 4 "+attachpdf) ;
        System.out.println("vbbbb 5 "+filename) ;
		EmailMessage msg = new EmailMessage();
		
	    msg.setAttachPdf(attachpdf) ;
		if (attachpdf.equals("yes"))
		{
		   msg.setBody(content);
		   msg.setFileName(filename) ;
		}
		
		ArrayList list = new ArrayList();
		ArrayList nameList = new ArrayList();
//		String content2 = getBodyContents(salutation,content);
		String option = "" ;
		String content2 = getBodyContents(salutation,content,option,custName) ;
//		msg.setMailId(mailId);
		//msg.setFrom(email);
		//msg.setReplyTo(email);
        System.out.println("vbbbb 4 "+toEmail) ;
		list.add(toEmail);
		msg.setFrom(email);
		msg.setReplyTo(email);
		msg.setGroupTo(list);
		msg.setMessageType("text/html");
		//msg.setMessageType("text/plain");
		//msg.setSubject(custName+", here is the Product Regimen");
		msg.setSubject("Your personalized hair care regimen");
		
	//2vb	content = salutation+"\n\n\n\n"+content ;
		if (attachpdf.equals("yes"))
		{
		   msg.setBody(content);
		   msg.setFileName(filename) ;
		}
		else
		{
		  msg.setBody(content2);
		}
		//msg.setBody(content);

      //  System.out.println("vbbbb 4 "+content) ;
		nameList.add(custName);
		msg.setNames(nameList);

		//MessageSenderQueue queue = MessageSenderQueue.getInstance();
		//remove this code during production time
			//queue.addMessageToSend(msg);

		SendMessage sendMessage = new SendMessage();
		ArrayList list2 = new ArrayList();
		list2.add(msg);
		sendMessage.sendMessage(list2);

	}

	public String getBodyContents(String toname,String message,String option,String custName) throws Exception{

		StringBuffer emailString = new StringBuffer();
		emailString.append(getRegimenEmailHTML(toname,message,option,custName));
		//emailString.append(getRegimenEmailText(custId,custName,custEmail,salutation,personalMsg,productMsg,closing,signature,email,phoneNum,prodLine,regimenusage));

	    return emailString.toString();
	}

	public String getRegimenEmailHTMLVB(String toname,String message){
           System.out.println("I am in HTML content "+toname) ;
		   String result = "";
		   result = "<table bgcolor=\"#ffffff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"+
			 "<tr>"+
			 "<td width=\"463\" height=\"42\" style=\"padding-top:30px;\"><img "+ 
			 "src=\"http://65.74.148.55:8080/svbeauty/images/brochure_showcase.jpg\" border=\"0\" width=\"463\" height=\"42\"></td>"+
			 "</tr>"+
			 "<tr>"+
			 "<td width=\"463\" style=\"font-family:Arial, Helvetica, sans-serif;"+ 
			 "font-size:11px; padding-top:20px; padding-bottom:50px;\">Dear <span style=\"font-family:Arial,"+ 
			 "Arial, Helvetica, sans-serif; font-size:11px; \">"+toname+" </span>,"+
			 "<br><br>"+message+""+ 
			 "<br><br>Take a look!"+
			 "<br>"+
			 "<br><a href=\"http://www.Beauty.com/SkinAnalysis\" style=\"font-family:Arial, Helvetica,"+ 
			 "sans-serif; font-size:11px; color:#cb66cc;\">www.Beauty.com/SkinAnalysis</a>"+
			 "<br><br>"+
			// "+<span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px;"+ 
			// "color:#67cc64;\">[included only if customer checked the box to give friend their"+ 
			// "regimen]</span>"+	
			// "<br>"+	
			// "If you're interested in learning more, my personalized regimen is attached."+ 
		//	 "<br><span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px;"+ 
	//		 "color:#67cc64;\">[PDF here]</span></td>"+
			 "</tr>"+
			 "</table>";

    	return result;
	}

	public StringBuffer getRegimenEmailText(String custId, String custName, String custEmail, String salutation, String personalMsg, String productMsg, String closing, String signature, String email, String phoneNum, String prodLine, String regimenusage){

		StringBuffer result = new StringBuffer();
		String prodImage="guideline-face-email.gif";
		String newline = System.getProperty("line.separator");
		personalMsg = personalMsg.replaceAll("''","'");
		productMsg = productMsg.replaceAll("<BR><BR>",newline+newline);
		custName = custName.replaceAll(" ","%20");
		result.append(salutation);
		result.append(newline+newline);
		result.append(personalMsg);
		result.append(newline+newline);
		result.append("Copy and paste this link in your browser or just click on the link below to view your personal Regimen and Application guidelines,");
		result.append(newline);
		result.append(hostName+"applicationGuidelines.jsp?cusId="+custId+"&dname="+custName+"&prodLine="+prodLine+"&productusage="+regimenusage);
		result.append(newline+newline);
		result.append(productMsg);
		result.append(newline);
		result.append(closing);
		result.append(newline+newline);
		result.append(signature);
		result.append(newline);
		result.append(email);
		result.append(newline);
		result.append(phoneNum);
		result.append(newline+newline);
		result.append(getCustomerUnSubScribeText(custEmail));

    	return result;
	}

	public void sendColorRegimenEmail(String custId,String custName,String toEmail,String repId,String salutation,String personalMsg,String closing,String signature,String email,String phoneNum,String productMsg,String prodLine,String regimenusage, String mailId) throws Exception{

		CustomerRegistrationDAL cusRegDAL = new CustomerRegistrationDAL();
		String repdet[][] = cusRegDAL.getrepname(repId);
		String pbsemail = "";
		RegimenMailDAL rad = new RegimenMailDAL();
		pbsemail = rad.getRepPBSEmail(repId);

		EmailMessage msg = new EmailMessage();
		ArrayList list = new ArrayList();
		ArrayList nameList = new ArrayList();
		String content = getBodyContentsForColorRegimen(custId,custName,toEmail,repId,salutation,personalMsg,closing,signature,email,phoneNum,productMsg,prodLine,regimenusage);
		list.add(toEmail);
		msg.setMailId(mailId);
		//msg.setFrom(email);
		//msg.setReplyTo(email);
		msg.setFrom(pbsemail);
		msg.setReplyTo(pbsemail);
		msg.setGroupTo(list);
		msg.setMessageType("text/html");
		//msg.setMessageType("text/plain");
		//msg.setSubject(custName+", here is the Product Regimen");
		msg.setSubject("Your personalized color regimen");
		msg.setBody(content);

		nameList.add(repdet[0][0]+ " "+repdet[0][1]);
		msg.setNames(nameList);

		//MessageSenderQueue queue = MessageSenderQueue.getInstance();
		//remove this code during production time
			//queue.addMessageToSend(msg);

		SendMessage sendMessage = new SendMessage();
		ArrayList list2 = new ArrayList();
		list2.add(msg);
		sendMessage.sendMessage(list2);

	}

	public String getBodyContentsForColorRegimen(String custId,String custName,String custEmail,String repId,String salutation,String personalMsg,String closing,String signature,String email,String phoneNum,String productMsg,String prodLine,String regimenusage) throws Exception{

		StringBuffer emailString = new StringBuffer();
		emailString.append(getColorRegimenEmailHTML(custId,custName,custEmail,salutation,personalMsg,productMsg,closing,signature,email,phoneNum,prodLine,regimenusage));
		//emailString.append(getRegimenEmailText(custId,custName,custEmail,salutation,personalMsg,productMsg,closing,signature,email,phoneNum,prodLine,regimenusage));

	    return emailString.toString();
	}

	public StringBuffer getColorRegimenEmailHTML(String custId, String custName, String custEmail, String salutation, String personalMsg, String productMsg, String closing, String signature, String email, String phoneNum, String prodLine, String regimenusage){

		StringBuffer result = new StringBuffer();
		//String prodImage="guideline-face-email.gif";
		personalMsg = personalMsg.replaceAll("''","'");

		if(phoneNum != null) {
			if(phoneNum.startsWith("null"))
				phoneNum = "";
			else if(!phoneNum.equals("")) {
				if(phoneNum.length() == 10)
					phoneNum = "("+phoneNum.substring(0,3)+") "+phoneNum.substring(3,6)+"-"+phoneNum.substring(6,10);
			}
		} else
			phoneNum = "";

		//result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"  \"http://www.w3.org/TR/html4/loose.dtd\">");
		result.append("<html><head>");
		result.append("<title> Color Regimen Email</title>");
		result.append("<STYLE TYPE=\"text/css\">");
		result.append("<!--");
		result.append(".emailbotm { color: black; font-size: 12px; font-family: Helvetica, Geneva, Arial, SunSans-Regular, sans-serif; padding: 10px;}");
		result.append(".bodytextgrey1 { color: #666; font-size: 12px; font-family: Helvetica, Geneva, Arial, SunSans-Regular, sans-serif; padding-top: 10px; padding-bottom: 10px }");
		result.append("-->");
		result.append("</STYLE>");
		result.append("</head><body>");
		result.append("<table width=\"562\" border=0>");
		result.append("<tr>");
		result.append("<td class=\"emailbotm\">");
		result.append(salutation);
		result.append("<p>");
		result.append(personalMsg);
		result.append("</p>");
		//result.append("<p>");
		//result.append("<a href=\""+hostName+"applicationGuidelines.jsp?cusId="+custId+"&dname="+custName+"&prodLine="+prodLine+"&productusage="+regimenusage+"\"><img src=\""+hostName+"images/"+prodImage+"\" alt=\"Product Display\" width=\"336\" height=\"54\" border=\"0\"></a>");
		//result.append("</p>");
		result.append("<p><span class=\"bodytextgrey1\">");
		result.append(productMsg);
		result.append("</span><br>");
		result.append(closing);
		result.append("<br>");
		result.append(signature);
		result.append("<br>");
		result.append(email);
		result.append("<br>");
		result.append(phoneNum);
		result.append("<br>");
		result.append("</p>");
		result.append(getCustomerUnSubScribeHTML(custEmail));
		result.append("</td></tr>");
		result.append("</table>");
		result.append("</body></html>");

    	return result;
	}

	public String getCustomerUnSubScribeHTML(String email){
		String customerunsubscribe = "<p><font face=\"arial, helvetica\" size=\"1\">Unsubscribe Instructions:<br>This email was sent from AvonBeauty@avonpersonalbeauty.com by your Avon Representative. If you feel you are receiving this email in error or you would like to opt-out of receiving emails, please <a href=\"http://www.avonpersonalbeauty.com/webunsubcustomer.do?customerEmail="+email+"\">click here</a>.</font></p>";

		return customerunsubscribe;
	}

	public String getCustomerUnSubScribeText(String email){
		String customerunsubscribe = "\n\nUnsubscribe Instructions: \nThis email was sent from AvonBeauty@avonpersonalbeauty.com by your Avon Representative. If you feel you are receiving this email in error or you would like to opt-out of receiving emails, please go to http://www.avonpersonalbeauty.com/webunsubcustomer.do?customerEmail="+email+".";
		return customerunsubscribe;
	}

	public String getCustomerUnSubScribePrv(String email){
		String customerunsubscribe = "<font face=\"arial, helvetica\" size=\"1\">Unsubscribe Instructions:<br>This email was sent from AvonBeauty@avonpersonalbeauty.com by your Avon Representative. If you feel you are receiving this email in error or you would like to opt-out of receiving emails, please <a href=\"http://www.avonpersonalbeauty.com/webunsubcustomer.do?customerEmail="+email+"\">click here</a>.</font>";
		return customerunsubscribe;
	}
	
	public StringBuffer getRegimenEmailHTML(String toname,String message, String option,String custName){
	
	String hostname = "http://sv-beauty.com/svbeauty/images/" ;
	StringBuffer result = new StringBuffer();
	
	result.append("<html><head></head>") ;
	result.append("<body bgcolor=\"#ffffff\">") ;
	result.append("<table width=\"700\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:#999999 1px solid; bgcolor:#FFFFFF\">") ;
	result.append("<tr valign=\"top\"><td>") ;
	result.append("	<table cellpadding=\"0\" cellspacing=\"0\"><tr>") ;
	result.append("	<td valign=\"top\" width=\"100\" height=\"40\" style=\"background-color:#f2f2f4;\"></td>") ;
	result.append("	<td valign=\"top\" width=\"163\" bgcolor=\"#333333\"><a href=\"http://www.drugstore.com/category.asp?catid=9730\" style=\"color:#333333;\"><img src=\""+hostname+"black33.bmp\" width=\"163\" height=\"38\" border=\"0\" alt=\"\"></a></td>") ;
	result.append("	<td valign=\"top\" width=\"425\" style=\"background-color:#f2f2f4; padding-left:5px; padding-right:5px;\"></td></tr></table>") ;
	result.append("</td></tr>") ;
	result.append("<tr><td>") ;
	result.append("	<table cellpadding=\"0\" cellspacing=\"0\"><tr>") ;
	result.append("	<td valign=\"top\" width=\"100\" height=\"16\"></td>") ;
	result.append("	<td valign=\"top\" width=\"163\" bgcolor=\"#333333\"></td>") ;
	result.append("	<td valign=\"top\" width=\"435\"></td>") ;
	result.append("	</tr></table>") ;
	result.append("</td></tr>") ;
	result.append("<tr><td>") ;
	result.append("	<table cellpadding=\"0\" cellspacing=\"0\"><tr>") ;
	result.append("	<td width=\"100\" height=\"40\"></td>") ;
	result.append("	<td valign=\"top\" width=\"400\" bgcolor=\"#ffffff\"><a href=\"http://www.drugstore.com/category.asp?catid=9730\"><img src=\""+hostname+"07bblogo.gif\" width=\"400\" height=\"34\" alt=\"\" border=\"0\"></a></td>") ;
	result.append("	</tr></table>") ;
	result.append("</td></tr>") ;
	result.append("<tr><td>") ;
	result.append("	<table width=\"698\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-bottom:#dddddd 1px solid;\"><tr valign=\"top\">") ;
	result.append("	<td width=\"60\" height=\"14\" style=\"background-color:#6e6565;\"> </td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"56\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;TEXT-DECORATION:none;COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=12873%\">SKIN CARE</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF;background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"43\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"vertical-align:middle; font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none; COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=12871\">MAKEUP</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF; background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"65\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none; COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=12872\">FRAGRANCE</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF; background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"70\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none; COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=12874\">BATH + BODY</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF; background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"57\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none; COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=12875\">HAIR CARE</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF; background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"44\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none;COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=21616\">BRANDS</a></td>") ;
	result.append("	<td align=\"center\" valign=\"bottom\" width=\"30\" style=\"font-size:11px;color:#FFFFFF; background-color:#6e6565;\">|</td>") ;
	result.append("	<td align=\"center\" valign=\"middle\" width=\"65\" style=\"background-color:#6e6565;\">") ;
	result.append("		<a style=\"font-family:Arial, Helvetica, sans-serif; font-size:10px;TEXT-DECORATION:none;COLOR:#ffffff;\" href=\"http://www.drugstore.com/category.asp?catid=56189\">SEE MORE &gt;</a></td>") ;
	result.append("	<td width=\"60\" height=\"14\" style=\"background-color:#6e6565;\"></td>") ;
	result.append("	</tr></table>") ;
	result.append("</td></tr>") ;
	result.append("") ;
	result.append("<tr><td>") ;
	result.append("	<table width=\"698\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr valign=\"top\">") ;
	result.append("   	<td width=\"355\" height=\"22\" style=\"background-color:#333333;\"></td>") ;
	result.append("		<td width=\"45\" style=\"vertical-align:middle; color:#FFFFFF; background-color:#333333; font-family:Arial, Helvetica, sans-serif; font-size:11px; font-weight:bold;\">Search:</td>") ;
	result.append("		<td width=\"298\" style=\"vertical-align:middle; background-color:#333333;\">") ;
	result.append("			<form name=\"FRM_SEARCH_BTY_EMAIL\" method=\"get\" action=\"http://www.beauty.com/search/search.asp?srchtree=8&search=%%search%%\" style=\"margin:0;padding:0;\">") ;
	result.append("			<input type=\"text\" style=\"vertical-align:middle; border:#9da2a8 1px inset; font-size:9px; height:16px; width:209px\" maxlength=\"50\" name=\"search\" value=\"\" size=\"20\">") ;
	result.append("			<input type=\"image\" name=\"btnEmailGo\" style=\"vertical-align:middle; font-size:9px; background-color:#eb1963; color:#FFFFFF;\" src=\""+hostname+"b_go.gif\" alt=\"GO\" border=\"0\" width=\"23\" height=\"15\">") ;
	result.append("			</form>") ;
	result.append("		</td>") ;
	result.append("	</tr></table>") ;
	result.append("</td></tr>") ;
	result.append("") ;
	result.append("<tr valign=\"top\"><td width=\"698\" align=\"center\">") ;
	result.append("	<table bgcolor=\"#ffffff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">") ;
	result.append("    	<tr>") ;
	result.append("			<td width=\"463\" height=\"42\" style=\"padding-top:30px;\"><img src=\""+hostname+"sv_hair_header.gif\" border=\"0\" width=\"463\" height=\"42\"></td>") ;
	result.append("	</tr>") ;
	result.append("        <tr>") ;
	result.append("			<td width=\"463\" style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; padding-top:20px; padding-bottom:50px;\">Dear <span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; \">"+toname+"</span>,") ;
	result.append("") ;
	result.append("<br><br>"+message+"") ;
	result.append("<br>") ;
	result.append("<br><a href=\"http://www.Beauty.com/HairConsultation\" style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; color:#cb66cc;\">www.Beauty.com/HairConsultation</a>") ;
	result.append("") ;
	if (option.equals("checked"))
	{
	result.append("<br><br><span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; color:#67cc64;\">[included only if customer checked the box to give friend their regimen]</span>") ;
	result.append("<br>") ;
	  result.append("If you're interested in learning more, my personalized regimen is attached. ") ;
	  result.append("<br><span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; color:#67cc64;\">[PDF here]</span></td>") ;
	}
	result.append("		</tr>") ;
	result.append("     </table>") ;
	result.append("      ") ;
	result.append("     ") ;
	result.append("</td></tr>") ;
	result.append("") ;
	result.append("") ;
	result.append("") ;
	result.append("") ;
	result.append("</table>") ;
	result.append("") ;
	result.append("<table width=\"700\" cellpadding=\"0\" cellspacing=\"0\" style=\"bgcolor:#FFFFFF\">") ;
	result.append("<tr><td height=\"10\"></td></tr><tr>") ;
	result.append("<td width=\"698\" valign=\"top\" style=\"border-top:#999999 1px solid; border-bottom:#999999 1px solid; padding:10px; background-color:#f2f2f4; color:#666666; font-family:Arial, Helvetica, sans-serif; font-size:11px;\">") ;
	result.append("	Your friend <span style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; \">"+custName+"</span> asked us to share this email with you. If you have questions about Beauty.com, please visit our <a href=\"http://www.drugstore.com/category.asp?catid=114973\" style=\"COLOR:#cb66cc;\">website</a>.<br>") ;
	result.append("	<BR>") ;
	result.append("	") ;
	result.append("	(c) 2007 Beauty.com, inc. All rights reserved. Beauty.com is a trademark of drugstore.com, inc.<br>") ;
	result.append("	<br>") ;
	result.append("	Questions or concerns? Contact us at:<br>") ;
	result.append("	Beauty.com<br>") ;
	result.append("	attn: Customer Care<br>") ;
	result.append("	411 108th Avenue NE, Suite 1400<br>") ;
	result.append("	Bellevue, WA 98004<br>") ;
	result.append("	<br>") ;
	result.append("</td></tr>") ;
	result.append("</TABLE>") ;
	result.append("") ;
	result.append("</body>") ;
	result.append("</html>") ;
     
	return result ;
  }

}
