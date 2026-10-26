package com.svb.dataentry;

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

import java.util.StringTokenizer;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class ReplaceCharacter {
	public String addquote(String str) {
		StringTokenizer stk = new StringTokenizer(str, ",");
		String tmp = "";
		while (stk.hasMoreTokens()) {
			if (tmp.trim().length() != 0)
				tmp = tmp + ",'" + stk.nextToken() + "'";
			else
				tmp = "'" + stk.nextToken() + "'";

		}
		return tmp;
	}

}