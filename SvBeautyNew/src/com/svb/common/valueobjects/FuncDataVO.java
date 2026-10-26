package com.svb.common.valueobjects;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */


/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class FuncDataVO {
	String desc = "";

	String ingrnames = "";

	int count = 0;

	public void setCountIngr(int count) {
		this.count = count;
	}

	public int getCountIngr() {
		return this.count;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setIngrNames(String ingrname) {
		if (ingrnames.trim().length() == 0)
			ingrnames = ingrname;
		else
			ingrnames = ingrnames + ", " + ingrname;

	}

	public String getDesc() {
		return desc;
	}

	public String getIngrNames() {
		return ingrnames;
	}

}