/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

import java.util.ArrayList;

/**
 * @author Neelima
 *
**/

public class EditProfileVO {

	private String[][] crec = new String[20][20] ;
	private String dname = "" ;
	private ArrayList vtb = new ArrayList() ;
	private String select1 = "" ;
    private String select2 = "" ;
    private String se1 = "" ;
    private String se2 = "" ;
    private String se3 = "" ;
    private String se4 = "" ;
    private String se5 = "" ;
    private String se6 = "" ;
    private String se7 = "" ;
    private String[] selected_spline = new String[0] ;
	private String[] selected_shline = new String[0] ;
	private ArrayList plineidvector = new ArrayList() ;
	private ArrayList plinenamevector = new ArrayList() ;

	public String[][] getCrec() {
		return crec;
	}

	public void setCrec(String[][] crec) {
		this.crec = crec;
	}

	public String getDname() {
		return dname;
	}

	public void setDname(String dname) {
		this.dname = dname;
	}

	public void setVtb(ArrayList vtb) {
		this.vtb = vtb;
	}

	public ArrayList getVtb() {
		return vtb;
	}

	public String getSelect1() {
		return select1;
	}

	public void setSelect1(String select1) {
		this.select1 = select1;
	}

	public String getSelect2() {
		return select2;
	}

	public void setSelect2(String select2) {
		this.select2 = select2;
	}

	public String getSe1() {
		return se1;
	}

	public void setSe1(String se1) {
		this.se1 = se1;
	}

	public String getSe2() {
		return se2;
	}

	public void setSe2(String se2) {
		this.se2 = se2;
	}

	public String getSe3() {
		return se3;
	}

	public void setSe3(String se3) {
		this.se3 = se3;
	}

	public String getSe4() {
		return se4;
	}

	public void setSe4(String se4) {
		this.se4 = se4;
	}

	public String getSe5() {
		return se5;
	}

	public void setSe5(String se5) {
		this.se5 = se5;
	}

	public String getSe6() {
		return se6;
	}

	public void setSe6(String se6) {
		this.se6 = se6;
	}

	public String getSe7() {
		return se7;
	}

	public void setSe7(String se7) {
		this.se7 = se7;
	}

	public String[] getSelected_spline() {
		return selected_spline;
	}

	public void setSelected_spline(String[] selected_spline) {
		this.selected_spline = selected_spline;
	}

	public String[] getSelected_shline() {
		return selected_shline;
	}

	public void setSelected_shline(String[] selected_shline) {
		this.selected_shline = selected_shline;
	}

	public ArrayList getPlineidvector() {
		return plineidvector;
	}

	public void setPlineidvector(ArrayList plineidvector) {
		this.plineidvector = plineidvector;
	}

	public ArrayList getPlinenamevector() {
		return plinenamevector;
	}

	public void setPlinenamevector(ArrayList plinenamevector) {
		this.plinenamevector = plinenamevector;
	}

}
