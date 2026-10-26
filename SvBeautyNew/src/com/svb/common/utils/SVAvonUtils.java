/*
 * Created on Apr 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author mahdav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVAvonUtils {

	public String formatPhone(String phoneNumber){
		if(phoneNumber!=null && !phoneNumber.trim().equals(""))
			phoneNumber = "("+phoneNumber.substring(0,3)+")"+phoneNumber.substring(3,6)+"-"+phoneNumber.substring(6,phoneNumber.length());
		return phoneNumber;
	}

	public boolean isEmailValid(String email) {

		char at = '@';
		char dot = '.';
		int lat = email.indexOf(at);
		int lstr=email.length();
		int ldot=email.indexOf(dot);
		if (email.indexOf(at)==-1){
			return false;//Invalid E-mail ID
		}

		if (email.indexOf(at)==-1 || email.indexOf(at)==0 || email.indexOf(at)==lstr){
			return false;
		}

		if (email.indexOf(dot)==-1 || email.indexOf(dot)==0 || email.indexOf(dot)==lstr){
			return false;
		}

        if (email.indexOf(at,(lat+1))!=-1){
		 	return false;
		}

		 if (email.substring(lat-1,lat).equals(".") || email.substring(lat+1,lat+2).equals(".")){
		 	return false;
		 }

		 if (email.indexOf(dot,(lat+2))==-1){
		 	return false;
		 }

		 if (email.indexOf(" ")!=-1){
		 	return false;
		 }

		 if(email.indexOf(".") == lstr-1) {
		 	return false;
		 }

 		 return true;//Valid E-mail ID
	}

	public boolean isString(String s){
		int i;
	    for (i = 0; i < s.length(); i++){
	        // Check that current character is in [a-z][A-Z].
	        char ch = s.charAt(i);
	        int charVal = ch;
	        if (!((charVal > 64 && charVal < 91) || (charVal > 96 && charVal <123))) return false;
	    }
	    // All are characters.
	    return true;
	}

	public boolean isValidName(String s){
		int i;
		boolean flag = false;
	    for (i = 0; i < s.length(); i++){
	        // Check that current character is in [a-z][A-Z].
	        char ch = s.charAt(i);
	        int charVal = ch;
	        if(i==0) {
	        	if (!((charVal > 64 && charVal < 91) || (charVal > 96 && charVal <123))) return false;
	        }
	        else {
	        	if(charVal == 39)
	        		if(flag == false)
	        			flag = true;
	        		else
	        			return false;
	        	if (!((charVal > 64 && charVal < 91) || (charVal > 96 && charVal <123) || (charVal == 32) || (charVal == 39) || (charVal == 45))) return false;
	        }
	    }
	    // All are characters.
	    return true;
	}

	public boolean isCity(String s){
		int i;
	    for (i = 0; i < s.length(); i++){
	        // Check that current character is in [a-z][A-Z].
	        char ch = s.charAt(i);
	        int charVal = ch;
	        if (!((charVal > 64 && charVal < 91) || (charVal > 96 && charVal <123) || (charVal == 32))) return false;
	    }
	    // All are characters.
	    return true;
	}

	public String validateDate(String strMonth, String strDay, String strYear){
		String errorMsg = "";
		try{
		if(strYear==null){
			strYear = "";
		}
		String strYr = strYear;
		Hashtable daysInMonth = new Hashtable();
		daysInMonth = DaysArray(12);

		if (strDay.charAt(0)== 0 && strDay.length()>1) strDay=strDay.substring(1);
		if (strMonth.charAt(0)== 0 && strMonth.length()>1) strMonth=strMonth.substring(1);
		for (int i = 1; i <= 3; i++) {
			if (strYr.charAt(0)== 0 && strYr.length()>1) strYr=strYr.substring(1);
		}
		int month = Integer.parseInt(strMonth);
		int day = Integer.parseInt(strDay);
		int year = Integer.parseInt(strYr);
		int minYear=1900;
	    int maxYear=2100;
	    int daysinmonth;
	    String numdaysinmonth = "";
	    if((String)daysInMonth.get(new Integer(month))!= null)
	    	numdaysinmonth = (String)daysInMonth.get(new Integer(month));
	    if(numdaysinmonth.equals("29")) daysinmonth = 29;
	    else if(numdaysinmonth.equals("30")) daysinmonth = 30;
	    else if(numdaysinmonth.equals("31")) daysinmonth = 31;
	    else daysinmonth = 0;


		if (strMonth.length() <1 || month<1 || month>12){
			errorMsg = "error.date.invalid";
		}
		else if (strDay.length() <1 || day<1 || day>31 || (month==2 && day>daysInFebruary(year)) || day > daysinmonth){
			errorMsg = "error.date.invalid";
		}
		else if (strYear.length() != 4 || year==0 || year<minYear || year>maxYear){
			errorMsg = "error.date.invalid";
		}
		}catch(Exception e){
			errorMsg = "error.date.invalid";
		}
		return errorMsg;
	}



	public Hashtable DaysArray(int n) {
		Hashtable list = new Hashtable();
		for (int i = 1; i <= n; i++) {
			if (i==4 || i==6 || i==9 || i==11) {list.put(new Integer(i),"30");}
			else if (i==2) {list.put(new Integer(i),"29");}
			else {list.put(new Integer(i),"31");}
	   }
	   return list;
	}

	public int daysInFebruary (int year){
		// February has 29 days in any year evenly divisible by four,
	    // EXCEPT for centurial years which are not also divisible by 400.
	    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
	}

	public boolean isInteger(String s){
		int i;
	    for (i = 0; i < s.length(); i++){
	        // Check that current character is number.
	        char c = s.charAt(i);
	        if (((c < '0') || (c > '9'))) return false;
	    }
	    // All characters are numbers.
	    return true;
	}

	public String insertBackSlash(String str)
	{
		String returnStr = "" ;
		if (!str.equals("")) {
			int i;
		    //check if there is "'" and insert back slash before it
		    i = str.indexOf("'");
		    if(i!=-1) {
		    	returnStr = str.substring(0,i);
		    	returnStr = returnStr + "\\'";
		    	returnStr = returnStr + str.substring(i+1,str.length());
			}
		    else returnStr = str;
		}
		return returnStr;
	}

	public String insertSingleQuote(String str)
	{
		String returnStr = "" ;
		if (!str.equals("")) {
			int i;
		    //check if there is "'" and insert another "'" before to it
		    i = str.indexOf("'");
		    if(i!=-1) {
		    	returnStr = str.substring(0,i);
		    	returnStr = returnStr + "''";
		    	returnStr = returnStr + str.substring(i+1,str.length());
			}
		    else returnStr = str;
		}
		return returnStr;
	}

	public StringBuffer readEmailFile(String fileName) {

        DataInputStream dis = null;
        String record = null;
        int recCount = 0;
        StringBuffer data = new StringBuffer();
        try {

           File f = new File(fileName);
           FileInputStream fis = new FileInputStream(f);
           BufferedInputStream bis = new BufferedInputStream(fis);
           dis = new DataInputStream(bis);

           while ( (record=dis.readLine()) != null ) {
            data.append(record);
           }

        } catch (IOException e) {
           // catch io errors from FileInputStream or readLine()
           //logger.log(Level.DEBUG,"Uh oh, got an IOException error!" + e.getMessage());
           e.printStackTrace();
        } finally {
           // if the file opened okay, make sure we close it
           if (dis != null) {
	      try {
                 dis.close();
	      } catch (IOException ioe) {
	      	ioe.printStackTrace();
	      }
           }
       }
       return data;
	}

	public String formatName(String name) {

		//remove extra space
		name.replaceAll("  "," ");

		//capitalize initial letters of the name

		String[] n = name.split(" ");
		name = "";

		for (int i=0; i<n.length; i++) {
			//capitalize letter after ' - Eg: O'Reilly
			String[] n1 = n[i].split("'");
			if(n1.length > 1) {
				String name1 = "";
				for(int j=0; j<n1.length; j++) {
					if(name1.equals(""))
						name1 = (n1[j].substring(0,1).toUpperCase())+(n1[j].substring(1,n1[j].length()).toLowerCase());
					else
						name1 = name1+"'"+(n1[j].substring(0,1).toUpperCase())+(n1[j].substring(1,n1[j].length()).toLowerCase());

				}
				if(name.equals(""))
					name = name1;
				else
					name = name+" "+name1;
			}
			else {
				if(name.equals(""))
					name = (n[i].substring(0,1).toUpperCase())+(n[i].substring(1,n[i].length()).toLowerCase());
				else
					name = name+" "+(n[i].substring(0,1).toUpperCase())+(n[i].substring(1,n[i].length()).toLowerCase());
			}
		}

		return name;
	}
}





