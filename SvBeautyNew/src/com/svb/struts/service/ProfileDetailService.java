package com.svb.struts.service;

import java.util.ArrayList;

import com.svb.struts.DAL.ProfileDetailDB;

public class ProfileDetailService {
	
public ArrayList getProfileDetails() {
		
		
		ArrayList profiles= new ArrayList();
		
	   ProfileDetailDB profileDb= new ProfileDetailDB();
	   
	   profiles = profileDb.getProfileList();
	   
	   
		
		return profiles;

		
	}

public ArrayList getProfileDisc(String profilevalue) {
	
	
	ArrayList profiles= new ArrayList();
	
   ProfileDetailDB profileDb= new ProfileDetailDB();
   
   profiles = profileDb.getProfileDisc(profilevalue);
   
   
	
	return profiles;

	
}
	

}
