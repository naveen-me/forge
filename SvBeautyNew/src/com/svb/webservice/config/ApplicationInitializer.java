package com.svb.webservice.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	 protected Class < ? > [] getRootConfigClasses() {
	  return new Class[] {
	   ApplicationConfiguration.class
	  };
	 }
	 @Override
	 protected Class < ? > [] getServletConfigClasses() {
	  return null;
	 }
	 @Override
	 protected String[] getServletMappings() {
	  return new String[] {
	  };
	 }

}
