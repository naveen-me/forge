package com.svb.common.javabean;
import java.io.File;

import javax.servlet.http.HttpServletRequest;

public class ImageName {
	public String garmentarrayImage(String imgname,String prodline) {
		String tempimgfile = "";
		String tempimgfile0 = "";
		String tempimgfile1 = "";
		String tempimgfile2 = "";
		String parentdir = "svdemos2z";
		String imgdir = "jcpimages";
		//logger.log(Level.DEBUG,"prod line is " + prodline + "  ff " + imgname);
		if (imgname != null && imgname.trim().length() != 0) {
			tempimgfile0 = imgname.substring(0, 5);
			tempimgfile0 = tempimgfile0.toUpperCase();
			//logger.log(Level.DEBUG,"tempimgfile0  " + tempimgfile0);
			tempimgfile1 = imgname.substring(5, imgname.length() - 6) + "SM";
			tempimgfile2 = imgname.substring(imgname.length() - 4, imgname
					.length());
			parentdir = "avon";
			imgdir = "avonimages";
			//logger.log(Level.DEBUG,"imgdir  " + imgdir);
			String separator = File.separator;
			tempimgfile = tempimgfile0 + tempimgfile1 + tempimgfile2;

		}
		return tempimgfile;
	}

	public String garmentdetailImage(String imgname, String prodline) {
		//logger.log(Level.DEBUG,"Image name ------- is" + imgname);
		String tempimgfile = "";
		String tempimgfile0 = "";
		String tempimgfile1 = "";
		String tempimgfile2 = "";
		String parentdir = "svdemos2z";
		String imgdir = "jcpimages";
		//logger.log(Level.DEBUG,"prod line is " + prodline + "  ff " + imgname);
		if (imgname != null && imgname.trim().length() != 0) {
			tempimgfile0 = imgname.substring(0, 5);
			tempimgfile0 = tempimgfile0.toUpperCase();
			//logger.log(Level.DEBUG,"tempimgfile0  " + tempimgfile0);
			tempimgfile1 = imgname.substring(5, imgname.length() - 6) + "LG";
			//logger.log(Level.DEBUG,"tempimgfile1  " + tempimgfile1);
			tempimgfile2 = imgname.substring(imgname.length() - 4, imgname
					.length());
			parentdir = "avon";
			imgdir = "avonimages";
			//logger.log(Level.DEBUG,"imgdir  " + imgdir);
			String separator = File.separator;
			tempimgfile = imgdir + separator + tempimgfile0 + tempimgfile1
					+ tempimgfile2;

		}
		return tempimgfile;
	}

}