/*
 * Created on Apr 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerregistration.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @author vbrao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RepRegistrationFormBean extends ActionForm{

	String repFirstName;
	String repMiddleName;
	String repLastName;
	String repEmail;
	String user_id;
	String pass;
	String userType;
	String title;
	String district;
	String division;
	String repAccountNum;
	String subject;
	String comments;

	public String getRepFirstName() {
		return repFirstName;
		}

	public void setRepFirstName(String repFirstName) {
			this.repFirstName = repFirstName;
		}

	public String getRepMiddleName() {
			return repMiddleName;
		}

	public void setRepMiddleName(String repMiddleName) {
				this.repMiddleName = repMiddleName;
		}


		public String getRepLastName() {
			return repLastName;
		}

		public void setRepLastName(String repLastName) {
			this.repLastName = repLastName;
		}

		public String getRepEmail() {
			return repEmail;
		}

		public void setRepEmail(String repEmail) {
			this.repEmail = repEmail;
		}

		public String getUser_id() {
			return user_id;
		}

		public void setUser_id(String user_id) {
			this.user_id = user_id;
		}

		public String getPass() {
			return pass;
		}

		public void setPass(String pass) {
			this.pass = pass;
		}

		public String getUserType() {
			return userType;
			}

		public void setUserType(String userType) {
				this.userType = userType;
			}
		public String getTitle() {
			return title;
			}

		public void setTitle(String title) {
				this.title = title;
			}
		public String getDistrict() {
			return district;
			}

		public void setDistrict(String district) {
				this.district = district;
			}
		public String getDivision() {
			return division;
			}

		public void setDivision(String division) {
				this.division = division;
			}
		public String getRepAccountNum() {
			return repAccountNum;
			}

		public void setRepAccountNum(String repAccountNum) {
				this.repAccountNum = repAccountNum;
			}

		public String getSubject() {
			return subject;
			}

		public void setSubject(String subject) {
				this.subject = subject;
			}

		public String getComments() {
			return comments;
			}

		public void setComments(String comments) {
				this.comments = comments;
			}
}
