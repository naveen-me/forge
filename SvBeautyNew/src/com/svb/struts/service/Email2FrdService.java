package com.svb.struts.service;

import java.util.ArrayList;

import com.svb.struts.DAL.BrandDetailsDB;
import com.svb.struts.DAL.Email2FrdDB;;

public class Email2FrdService {
	
	Email2FrdDB emailDb;
	boolean flag=false;
	
	public boolean getCustomer(String toname,String fromname)
	{
		
		System.out.println("to/from in service:"+toname+"from name:"+fromname);
		
		emailDb = new Email2FrdDB();
		flag= emailDb.getEmailList(toname,fromname);
		return flag;
		
	}
	
public boolean getEmailDetails(String to,String from, String pass,String filepath,String cusmsg) {
			
		
		System.out.println("****to1111111111***"+to);
		emailDb=new Email2FrdDB();
		boolean flagpdf=false;
		
		if(emailDb.Email2FrdDB4(to,from,pass,filepath,cusmsg))
		{
			flagpdf=true;
		}
			
	   System.out.println("in service return flag with pdf:"+flagpdf);
	   	return	flagpdf;
	
	   		
	}
public boolean getEmailDetails(String to,String from, String pass,String cusmsg) {
	
	Email2FrdDB emailDb;
	System.out.println("****to2222222222222***"+to+"*****from*****"+from+"****pass*******"+pass);
	boolean falgservice=false;
	emailDb=new Email2FrdDB();
	//System.out.println("in service return the db :"+emailDb.Email2FrdDB3(to, from, pass,cusmsg));
	if(emailDb.Email2FrdDB3(to, from, pass,cusmsg))
	{
		falgservice=true;
	}
		
   System.out.println("in service return flag:"+falgservice);
   	return	falgservice;
}

public boolean setOptedEmail(String from_name,String from_email,String to_name,String to_email)
{
      boolean optedflag=false;
      emailDb = new Email2FrdDB();
      optedflag=emailDb.OptedEmail2Frd(from_name, from_email, to_name, to_email);
      
      return optedflag;

}

}
