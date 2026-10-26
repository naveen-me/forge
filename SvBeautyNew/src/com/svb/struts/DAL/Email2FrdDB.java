package com.svb.struts.DAL;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
//import javax.mail.util.ByteArrayDataSource;

import com.svb.common.dba.SVDBConnection;
import com.svb.struts.bean.BrandDetailsBean;
//import com.svb.struts.bean.Email2FrdBean;

public class Email2FrdDB {
	
	String query;
	SVDBConnection svConnection;
	
	String  d_password  = null;
    String doc_path1 = "F://one.pdf";
	String doc_path = null;
    String  m_subject = "Testing";
    String  m_text = "";
    String d_email=null;
    String customername="";
    String friendname="";
    
    
    
    public boolean Email2FrdDB4(String to_email,String from_email,String password,String filepath,String cusmsg){
		System.out.println("****filepath***"+filepath);
		boolean flagpdf=false;
		try{
			System.out.println("hello___from"+from_email);
			d_email=from_email;
			d_password=password;
			System.out.println("%%%%%before%%%"+filepath);
			String spath=filepath.replace('\\','/');
			File f=new File(spath);
			doc_path=f.getPath();
			String strpath=f.getAbsolutePath();
			System.out.println("%%%%%after%%%"+doc_path);
			System.out.println("%%%%%after%%%"+strpath);
//			 body message
	        BodyPart messageBodyPart = new MimeBodyPart();
			String htmlText = cusmsg ;
			
			messageBodyPart.setContent(htmlText, "text/html");
	        
	        // end body message
			MimeMultipart mimeMultipart = new MimeMultipart("related");
	        //mimeMultipart.setSubType("mixed");
	        mimeMultipart.addBodyPart(messageBodyPart);

	        File f2=new File(doc_path);
	        
	        
	        // Attach with PDF to E-Mail message.
	        FileDataSource file = new FileDataSource(doc_path);
	        DataHandler pdfDH = new DataHandler(file);
	        MimeBodyPart pdfPart = new MimeBodyPart();
	        pdfPart.setDataHandler(pdfDH);
	       
//	       pdfPart.setHeader("Content-Type","application/pdf; ");
//	       pdfPart.setDisposition("inline; filename=\"doc.pdf\"");
	       //pdfPart.setText(m_text);
	       pdfPart.setDisposition("inline; filename="+f2.getName()+"");
	      
	        mimeMultipart.addBodyPart(pdfPart);
	        System.out.println("mime body part:"+mimeMultipart.getContentType());

	     // Create an e-mail message object (MimeMessage)
	        Properties props = new Properties();
	        props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.host", "465");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.starttls.enable","true");
	        props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", "465");
	        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        props.put("mail.smtp.socketFactory.fallback", "false");

			SecurityManager security = System.getSecurityManager();
			
			
			
			Authenticator auth=new SMTPAuthenticator();
			Session session = Session.getInstance(props, auth);
			session.setDebug(true);
			
			
			
			
	        MimeMessage email = new MimeMessage(session);
//	        email.setDataHandler(new DataHandler(
//	        		new ByteArrayDataSource(m_text, "text/html")));
	        email.setFrom(new InternetAddress(from_email));
	        InternetAddress[] address ={new InternetAddress(to_email)};
	        //email.setRecipients(Message.RecipientType.TO, address);
            email.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));
            

            //email.setSubject("JavaMail APIs Multipart/Related Test");
            email.setSubject(m_subject);
             email.setSentDate(new java.util.Date());
            

            // Set the contents of the e-mail message to the MimeMultipart
            
            email.setContent(mimeMultipart);
           
            // Send the e-mail message
            Transport.send(email);
            System.out.println("Message with pdf attachment Sent Ok:");
            flagpdf=true;
            if(flagpdf){
            	
            	if(updateCust(from_email,to_email,"Y",cusmsg))
					System.out.println("record updated sucessfull with pdf");
				else
					System.out.println("problem getting to  record update with pdf");
            }
            

		}catch(Exception e){
			e.printStackTrace();
		}
		 return flagpdf;
	}
	
	public  boolean  Email2FrdDB3(String to_email,String from_email,String password,String cusmsg){
		
		boolean flagsent=false;
		try{
			
			System.out.println("hello___from  3 paraneters"+from_email);
			d_email=from_email;
			d_password=password;
			 
			BodyPart messageBodyPart = new MimeBodyPart();
			String htmlText = cusmsg ;
			
			messageBodyPart.setContent(htmlText, "text/html");
	        
	        // end body message
			MimeMultipart mimeMultipart = new MimeMultipart("related");
	        //mimeMultipart.setSubType("mixed");
	        mimeMultipart.addBodyPart(messageBodyPart);
				
			Properties props = new Properties();
	        props.put("mail.smtp.user", from_email);
	        props.put("mail.smtp.host", "smtp.gmail.com");
	        props.put("mail.smtp.port","465");
	        props.put("mail.smtp.starttls.enable","true");
	        props.put("mail.smtp.auth", "true");
	        //props.put("mail.smtp.debug", "true");
	        props.put("mail.smtp.socketFactory.port", "465");
	        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        props.put("mail.smtp.socketFactory.fallback", "false");
	        
	        
			Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
           // session.setDebug(true);
           //System.out.println("session:"+session.g);
            
            MimeMessage msg = new MimeMessage(session);
  //          msg.setDataHandler(new DataHandler(
	//        		new ByteArrayDataSource(m_text, "text/html")));
            msg.setText(cusmsg);
            msg.setSubject(m_subject);
            msg.setFrom(new InternetAddress(from_email));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));
            msg.setContent(mimeMultipart);
            Transport.send(msg);
			System.out.println("Message Sent Ok");
			flagsent=true;

			
			if(flagsent){
				if(updateCust(from_email,to_email,"N",cusmsg))
					System.out.println("record updated sucessfull without pdf");
				else
					System.out.println("problem getting to  record update without pdf");
			/* *******End of updating ******** */
			}
						
		}catch(Exception e){
			e.printStackTrace();
		}
		return flagsent;

	}
	
	public boolean updateCust(String from_email,String to_email,String attachedpdf,String custmsg )
	{
		boolean updateflag=false;
		svConnection = new SVDBConnection();
		String senddate="";
		String query1="select TO_CHAR(sysdate,'dd-Mon-yyyy') from dual";
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs=null;
		int j=0;
		try{
			connection = svConnection.getConnection();
			
			
			try{
			stmt = connection.createStatement();
			rs=stmt.executeQuery(query1);
			
			while(rs.next()){
				
				senddate=rs.getString(1);
			}
			}catch(SQLException e){
				
				System.out.println("exception to get date from dump"+e);
			}
			rs.close();
			stmt.close();
			System.out.println("senddate:"+senddate);
			String query2="update customer_emails set Email_sent='"+senddate+"',Email_text='"+custmsg+"',Attched_pdf='"+attachedpdf+"'where Customer_emal='"+from_email+"'and Friends_email='"+to_email+"'";
			System.out.println("query:"+query2);
			try{
			stmt = connection.createStatement();
			j=stmt.executeUpdate(query2);
			if(j==1)
				updateflag=true;
			}
            catch(SQLException e){
				
				System.out.println("exception to get date from dump"+e);
			}
			
		}
		catch(Exception e){
			
			
		}
				
		return updateflag;
		
		
	}
	
	
	public Email2FrdDB() {
		// TODO Auto-generated constructor stub
	}
	private class SMTPAuthenticator extends javax.mail.Authenticator
    {
        public PasswordAuthentication getPasswordAuthentication()
       {
            return new PasswordAuthentication(d_email, d_password);
        }
    }
	
	public boolean getEmailList(String to_email,String from_email){
		svConnection = new SVDBConnection();
		//efb = new Email2FrdBean();
	//	ArrayList  emails= new ArrayList();
		System.out.println("from/to in db:"+from_email+""+to_email);
		
		query="select Friends_email_flag   from customer_emails where Customer_emal='"+to_email+"'and Friends_email='"+from_email+"'";
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String flagstring="";
		boolean flag=false;
		
		try{
			connection = svConnection.getConnection();
			
			System.out.println("connection in email DB:"+connection+"query:"+query);
			stmt = connection.createStatement();
			
			rs = stmt.executeQuery(query);
			
			
			while(rs.next()){
			flagstring=rs.getString(1);
			System.out.println("******"+rs.getString(1));
			System.out.println("******"+flagstring);
			
//						emails.add(efb);
			}
			if(flagstring.equalsIgnoreCase("Y"))
			{
				flag=true;
			}
			
			
		}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					rs.close();
					stmt.close();
					connection.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		System.out.println("return from rs:"+flag);
			return flag;
			
	}
	
	public boolean OptedEmail2Frd(String from_name,String from_email,String to_name,String to_email){
		
		boolean flagOpt=false,result=false;
		String flag="";
		svConnection = new SVDBConnection();
		query="select Friends_email_flag   from customer_emails where Customer_emal='"+to_email+"'and Friends_email='"+from_email+"'";
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			connection = svConnection.getConnection();
			
			System.out.println("connection in email DB:"+connection+"query:"+query);
			stmt = connection.createStatement();
			
			rs = stmt.executeQuery(query);
			
			
			while(rs.next()){
				flag=rs.getString(1);
				System.out.println("flag of rs:"+flag);
				result=true;
				
			
			}
			
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					rs.close();
					stmt.close();
					connection.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(result)
			{
				
				  String query1="update customer_emails set Friends_email_flag='N' where Customer_emal='"+from_email+"'and Friends_email='"+to_email+"'";
				  
				  int k=0;
				try{
					connection = svConnection.getConnection();
					
					System.out.println("connection in email DB:"+connection+"query:"+query1);
					stmt = connection.createStatement();
					
					 k = stmt.executeUpdate(query1);
					
					
					if(k==1){
						System.out.println("update the record to optem");
						flagOpt=true;
					}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						try{
							
							stmt.close();
							connection.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				
			}
			else
			{
				String query1="insert into customer_emails ( CUSTOMER_NAME,CUSTOMER_EMAL,FRIENDS_NAME,FRIENDS_EMAIL,FRIENDS_EMAIL_FLAG) VALUES('"+from_name+"','"+from_email+"','"+to_name+"','"+to_email+"','N')";
				  
				  int k=0;
				try{
					connection = svConnection.getConnection();
					
					System.out.println("connection in email DB:"+connection+"query:"+query1);
					stmt = connection.createStatement();
					
					 k = stmt.executeUpdate(query1);
					
					
					if(k==1){
						System.out.println("insert as a new record");
						flagOpt=true;
					}
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						try{
							
							stmt.close();
							connection.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				
			}
		
		
		return flagOpt;
	}
	
	public boolean updateCust(){
		
		
		boolean updateflag=false;
		return updateflag;
	}

}
