/*
 * Created on Mar 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerprofile.formbeans;

import java.util.ArrayList;
import org.apache.struts.action.ActionForm;
/**
 * @author David  murphy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProfileSummaryFormBean extends ActionForm
{
	String mid = "" ;
	String catid = "" ;
	String mode = "" ;
	String cusid;
	String dname = "";
	String scrname;
	String add_modify;
	String repid;
	String noprods;
	String profileMethodToCall;

	String ctype;
	String cperiod;
	String cptype;
	String contdate;
	String contmail;
	String remindernotes;
	String comments;
	String pstatus;

	String environments[] = new String[0];
	String environmentw[] = new String[0];
	String exposure[] = new String[0];

	String skin_type;
	String skin_concern[]=new String[0];
	String eye_concern[]=new String[0];
	String skin_concern_new[] = new String[0];
	String skin_condition[] = new String[0];
	String allergy[] = new String[0];

	String natural;
	String age;
	String ethnicity;
	String specification[] = new String[0]; 
	/**
	 * @return Returns the hair_Color.
	 */
	public String getHair_Color() {
		return hair_Color;
	}
	/**
	 * @param hair_Color The hair_Color to set.
	 */
	public void setHair_Color(String hair_Color) {
		this.hair_Color = hair_Color;
	}
	/**
	 * @return Returns the hair_Conditions.
	 */
	public String[] getHair_Conditions() {
		return hair_Conditions;
	}
	/**
	 * @param hair_Conditions The hair_Conditions to set.
	 */
	public void setHair_Conditions(String[] hair_Conditions) {
		this.hair_Conditions = hair_Conditions;
	}
	/**
	 * @return Returns the hair_Finish.
	 */
	public String getHair_Finish() {
		return hair_Finish;
	}
	/**
	 * @param hair_Finish The hair_Finish to set.
	 */
	public void setHair_Finish(String hair_Finish) {
		this.hair_Finish = hair_Finish;
	}
	/**
	 * @return Returns the hair_Process.
	 */
	public String[] getHair_Process() {
		return hair_Process;
	}
	/**
	 * @param hair_Process The hair_Process to set.
	 */
	public void setHair_Process(String[] hair_Process) {
		this.hair_Process = hair_Process;
	}
	/**
	 * @return Returns the hair_Styles.
	 */
	public String[] getHair_Styles() {
		return hair_Styles;
	}
	/**
	 * @param hair_Styles The hair_Styles to set.
	 */
	public void setHair_Styles(String[] hair_Styles) {
		this.hair_Styles = hair_Styles;
	}
	/**
	 * @return Returns the hair_Texture.
	 */
	public String getHair_Texture() {
		return hair_Texture;
	}
	/**
	 * @param hair_Texture The hair_Texture to set.
	 */
	public void setHair_Texture(String hair_Texture) {
		this.hair_Texture = hair_Texture;
	}
	/**
	 * @return Returns the hair_Thickness.
	 */
	public String getHair_Thickness() {
		return hair_Thickness;
	}
	/**
	 * @param hair_Thickness The hair_Thickness to set.
	 */
	public void setHair_Thickness(String hair_Thickness) {
		this.hair_Thickness = hair_Thickness;
	}
	
	
	String hair_Color = "" ;
	String hair_Process[] = new String[0]; ;
	String hair_Thickness = "" ;
	String hair_Texture = "" ;
	String hair_Conditions[] = new String[0] ;
	String hair_Styles[] = new String[0] ;
	String hair_Finish = "" ;
	
 	
	String fname="";
	String mname="";
	String lname="";

	String hphone1="";
	String hphone2="";
	String hphone3="";

	String wphone1="";
	String wphone2="";
	String wphone3="";

	String fphone1="";
	String fphone2="";
	String fphone3="";

	String mphone1="";
	String mphone2="";
	String mphone3="";

	String email="";
	String addr1 = "";
	String addr2 = "";
	String city = "";
	String state = "";
	String zip = "";
	String gender = "";
	String status = "";
	String bdate = "";
	String bday_mm = "";
	String bday_dd = "";
	String bday_yy = "";
	String adate = "";
	String anday_mm = "";
	String anday_dd = "";
	String anday_yy = "";

	int pagenum;


	private ArrayList vtb = new ArrayList() ;

    String profilestatus = "" ;
    String prodrecflag = "" ;
    String selfprofileflag = "" ;
    String selfprofilesummary = "";
	private String inviteType;

	
    public String getSelfprofilesummary() {
		return selfprofilesummary;
	}
	public void setSelfprofilesummary(String selfprofilesummary) {
		this.selfprofilesummary = selfprofilesummary;
	}

    public String getSelfprofileflag() {
		return selfprofileflag;
	}
	public void setSelfprofileflag(String selfprofileflag) {
		this.selfprofileflag = selfprofileflag;
	}
	public String getProdrecflag() {
		return prodrecflag;
	}
	public void setProdrecflag(String prodrecflag) {
		this.prodrecflag = prodrecflag;
	}
	public String getProfileStatus() {
		return profilestatus;
	}
	public void setProfileStatus(String profilestatus) {
		this.profilestatus = profilestatus;
	}

	public String getCusid() {
		//logger.log(Level.DEBUG,cusid);
		return cusid;
	}
	public void setCusid(String cusid) {
		this.cusid = cusid;
		//logger.log(Level.DEBUG,this.cusid);
	}

	public String getDname() {
		//logger.log(Level.DEBUG,dname);
		return dname;
	}

	public void setDname(String dname) {
		this.dname = dname;
	}

	public void setOption1(String dname) {
		this.dname = dname;
		//logger.log(Level.DEBUG,this.dname);
	}

	public String getScrname() {
		return scrname;
	}
	public void setScrname(String scrname) {
		this.scrname = scrname;
		//logger.log(Level.DEBUG,this.scrname);
	}

	public String getAdd_modify() {
		//logger.log(Level.DEBUG,add_modify);
		return add_modify;
	}
	public void setAdd_modify(String add_modify) {
		this.add_modify = add_modify;
		//logger.log(Level.DEBUG,this.add_modify);
	}

	public String getRepid() {
		//logger.log(Level.DEBUG,repid);
		return repid;
	}
	public void setRepid(String repid) {
		this.repid = repid;
		//logger.log(Level.DEBUG,this.repid);
	}

	public String getNoprods() {
		return noprods;
	}

	public void setNoprods(String noprods) {
		this.noprods = noprods;
	}

	public String getProfileMethodToCall() {
		return profileMethodToCall;
	}

	public void setProfileMethodToCall(String profileMethodToCall) {
		this.profileMethodToCall = profileMethodToCall;
	}

	public String getCtype() {
		//logger.log(Level.DEBUG,ctype);
		return ctype;
	}
	public void setCtype(String ctype) {
		this.ctype = ctype;
		//logger.log(Level.DEBUG,this.ctype);
	}

	public String getCperiod() {
		//logger.log(Level.DEBUG,cperiod);
		return cperiod;
	}
	public void setCperiod(String cperiod) {
		this.cperiod = cperiod;
		//logger.log(Level.DEBUG,this.cperiod);
	}

	public String getCptype() {
		//logger.log(Level.DEBUG,cptype);
		return cptype;
	}
	public void setCptype(String cptype) {
		this.cptype = cptype;
		//logger.log(Level.DEBUG,this.cptype);
	}

	public String getContdate() {
		//logger.log(Level.DEBUG,contdate);
		return contdate;
	}
	public void setContdate(String contdate) {
		this.contdate = contdate;
		//logger.log(Level.DEBUG,this.contdate);
	}

	public String getContmail() {
		//logger.log(Level.DEBUG,contmail);
		return contmail;
	}
	public void setContmail(String contmail) {
		this.contmail = contmail;
		//logger.log(Level.DEBUG,this.contmail);
	}

	public String getRemindernotes() {
		//logger.log(Level.DEBUG,remindernotes);
		return remindernotes;
	}
	public void setRemindernotes(String remindernotes) {
		this.remindernotes = remindernotes;
		//logger.log(Level.DEBUG,this.remindernotes);
	}

	public String getComments() {
		//logger.log(Level.DEBUG,comments);
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
		//logger.log(Level.DEBUG,this.comments);
	}

	public String getPstatus(){
		return pstatus;
	}
	public void setPstatus(String pstatus){
		this.pstatus=pstatus;
	}

	public String[] getEnvironments(){
		return environments;
	}
	public void setEnvironments(String[] environments){
		this.environments = environments;
	}

	public String[] getEnvironmentw(){
		return environmentw;
	}
	public void setEnvironmentw(String[] environmentw){
		this.environmentw = environmentw;
	}

	public String[] getExposure(){
		return exposure;
	}
	public void setExposure(String[] exposure){
		this.exposure = exposure;
	}

	public String getSkin_type() {

		return skin_type;
	}
	public void setSkin_type(String skin_type) {
		this.skin_type = skin_type;
	}

	public String[] getSkin_condition() {
		return skin_condition;
	}
	public void setSkin_condition(String[] skin_condition) {
		this.skin_condition= skin_condition;
	}

	public String[] getSkin_concern() {
		return skin_concern;
	}
	public void setSkin_concern(String[] skin_concern) {
		this.skin_concern = skin_concern;
	}


	public String[] getSkin_concern_new() {
		return skin_concern_new;
	}
	public void setSkin_concern_new(String[] skin_concern_new) {
		this.skin_concern_new = skin_concern_new;
	}

	public String[] getAllergy() {
		return allergy;
	}
	public void setAllergy(String[] allergy) {
		this.allergy = allergy;
	}


	public String getNatural(){
		return natural;
	}
	public void setNatural(String natural){
		this.natural=natural;
	}
	
	public String getAge(){
		return age;
	}
	public void setAge(String age){
		this.age=age;
	}

	public String getEthnicity(){
		return ethnicity;
	}
	public void setEthnicity(String ethnicity){
		this.ethnicity=ethnicity;
	}


	public String getFname() {
		return fname;
		}

		public void setFname(String fname) {
			this.fname = fname;
		}

		public String getMname() {
			return mname;
		}

		public void setMname(String mname) {
			this.mname = mname;
		}

		public String getLname() {
			return lname;
		}

		public void setLname(String lname) {
			this.lname = lname;
		}

		public String getHphone1() {
			return hphone1;
		}

		public void setHphone1(String hphone1) {
			this.hphone1 = hphone1;
		}

		public String getHphone2() {
			return hphone2;
		}

		public void setHphone2(String hphone2) {
			this.hphone2 = hphone2;
		}

		public String getHphone3() {
			return hphone3;
		}

		public void setHphone3(String hphone3) {
			this.hphone3 = hphone3;
		}

		public String getWphone1() {
			return wphone1;
		}

		public void setWphone1(String wphone1) {
			this.wphone1 = wphone1;
		}

		public String getWphone2() {
			return wphone2;
		}

		public void setWphone2(String wphone2) {
			this.wphone2 = wphone2;
		}

		public String getWphone3() {
			return wphone3;
		}

		public void setWphone3(String wphone3) {
			this.wphone3 = wphone3;
		}

		public String getFphone1() {
			return fphone1;
		}

		public void setFphone1(String fphone1) {
			this.fphone1 = fphone1;
		}

		public String getFphone2() {
			return fphone2;
		}

		public void setFphone2(String fphone2) {
			this.fphone2 = fphone2;
		}

		public String getFphone3() {
			return fphone3;
		}

		public void setFphone3(String fphone3) {
			this.fphone3 = fphone3;
		}

		public String getMphone1() {
			return mphone1;
		}

		public void setMphone1(String mphone1) {
			this.mphone1 = mphone1;
		}

		public String getMphone2() {
			return mphone2;
		}

		public void setMphone2(String mphone2) {
			this.mphone2 = mphone2;
		}

		public String getMphone3() {
			return mphone3;
		}

		public void setMphone3(String mphone3) {
			this.mphone3 = mphone3;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getAddr1() {
			return addr1;
		}

		public void setAddr1(String addr1) {
			this.addr1 = addr1;
		}

		public String getAddr2() {
			return addr2;
		}

		public void setAddr2(String addr2) {
			this.addr2 = addr2;
		}


		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getZip() {
			return zip;
		}

		public void setZip(String zip) {
			this.zip = zip;
		}

		public String getBdate() {
			return bdate;
		}

		public void setBdate(String bdate) {
			this.bdate = bdate;
		}

		public String getBday_mm() {
			return bday_mm;
		}

		public void setBday_mm(String bday_mm) {
			this.bday_mm = bday_mm;
		}

		public String getBday_dd() {
			return bday_dd;
		}

		public void setBday_dd(String bday_dd) {
			this.bday_dd = bday_dd;
		}

		public String getBday_yy() {
			return bday_yy;
		}

		public void setBday_yy(String bday_yy) {
			this.bday_yy = bday_yy;
		}

		public String getAnday_mm() {
			return anday_mm;
		}

		public void setAnday_mm(String anday_mm) {
			this.anday_mm = anday_mm;
		}

		public String getAnday_dd() {
			return anday_dd;
		}

		public void setAnday_dd(String anday_dd) {
			this.anday_dd = anday_dd;
		}

		public String getAnday_yy() {
			return anday_yy;
		}

		public void setAnday_yy(String anday_yy) {
			this.anday_yy = anday_yy;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getAdate() {
			return adate;
		}

		public void setAdate(String adate) {
			this.adate = adate;
		}


		public void setVtb(ArrayList vtb) {
			this.vtb = vtb;
		}

		public ArrayList getVtb() {
			return vtb;
		}

		public int getPagenum() {
			//logger.log(Level.DEBUG,pagenum);
			return pagenum;
		}
		public void setPagenum(int pagenum) {
			this.pagenum = pagenum;
			//logger.log(Level.DEBUG,this.pagenum);
		}
		/**
		 * @return
		 */
		public String  getInviteType() {
			// TODO Auto-generated method stub
			return inviteType;
		}

	/**
	 * @param inviteType The inviteType to set.
	 */
	public void setInviteType(String inviteType) {
		this.inviteType = inviteType;
	}

	public String[] getEye_concern() {
		// TODO Auto-generated method stub
		return eye_concern;
	}
	public void setEye_concern(String[] eye_concern) {
		this.eye_concern = eye_concern;
	}
	/**
	 * @return Returns the mid.
	 */
	public String getMid() {
		return mid;
	}
	/**
	 * @param mid The mid to set.
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}
	/**
	 * @return Returns the mode.
	 */
	public String getMode() {
		return mode;
	}
	/**
	 * @param mode The mode to set.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	/**
	 * @return Returns the catid.
	 */
	public String getCatid() {
		return catid;
	}
	/**
	 * @param catid The catid to set.
	 */
	public void setCatid(String catid) {
		this.catid = catid;
	}
	/**
	 * @return the specification
	 */
	public String[] getSpecification() {
		return specification;
	}
	/**
	 * @param specification the specification to set
	 */
	public void setSpecification(String[] specification) {
		this.specification = specification;
	}
	
}
