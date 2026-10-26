package com.svb.webservices.controller;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
@RestController
@RequestMapping("/SVBRecommendation")
public class SVBRecommendation {

	@Autowired
	private MessageSource messageSource;

	
	@GetMapping()
	public String getMsg() {
		return messageSource.getMessage("welcome.message", new Object[] { "InSpaEngine" }, Locale.US);
	}
}
