package com.svb.webservices.controller;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svb.webservices.model.ErrorDetails;
import com.svb.webservices.model.RecProductAvailibility;
import com.svb.webservices.model.RecomondationProductModel;
import com.svb.webservices.model.RecomondedProductResponseModel;
import com.svb.webservices.model.SingleProductScoreModel;
import com.svb.webservices.model.SingleProductScoreResponseModel;
import com.svb.webservices.service.ProductService;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */

@RestController
public class ProductRecommendationController {

	private static Logger log = Logger.getLogger(ProductRecommendationController.class.getName());

	@Autowired
	ProductService productService;

	@Autowired
	private MessageSource messageSource;

	@PostMapping("/productRecommendation")
	public ResponseEntity<Object> getProductList(@Valid @RequestBody RecomondationProductModel productModel,
			BindingResult bindingResult) throws IllegalAccessException, InvocationTargetException {

		if (bindingResult.hasErrors()) {
			List<FieldError> errors = bindingResult.getFieldErrors();
			List<String> message = new ArrayList<>();
			for (FieldError e : errors) {
				if (!message.isEmpty()) {
					message.add(
							messageSource.getMessage(e.getDefaultMessage(), new Object[] { e.getField() }, Locale.US));
				}
			}
			return new ResponseEntity<>(ErrorDetails.build(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					 message.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try

		{
			
			long startTime = System.nanoTime();
			
			RecomondedProductResponseModel result = productService.getProductRecomondationList(productModel);
			
			long stopTime = System.nanoTime();
			System.out.println("TOTAL TIME TO CALL METHOD :: " + (stopTime - startTime));
			
			// START: ADDED by Haresh:SYNOPTEK for JSON Request-Response File Write
			ObjectMapper Obj = new ObjectMapper();
			String ReqJsonStr = Obj.writeValueAsString(productModel);
			String ResJsonStr = Obj.writeValueAsString(result);

//            log.debug("jsonStr: "+jsonStr);
			log.log(Level.INFO,
					"\n  *************************************************  JSON Request  ******************************************************************* \n"
							+ ReqJsonStr);

			log.log(Level.INFO,
					"\n  *************************************************  JSON Response  ****************************************************************** \n"
							+ ResJsonStr);

			// END: ADDED by Haresh:SYNOPTEK for JSON Request-Response File Write

			return new ResponseEntity<Object>(result, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(ErrorDetails.build(500, e.toString()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/updateProductAvailibility")
	public ResponseEntity<Object> updateProductAvailability(
			@Valid @RequestBody RecProductAvailibility productStatusModel) {

		List<ErrorDetails> resultList = new ArrayList<>();
		try {
			resultList = productService.updateProductAvailabilityList(productStatusModel);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (resultList.get(0).getResponseMessage().equalsIgnoreCase("Product status updated successfully"))
			return new ResponseEntity<Object>(resultList, HttpStatus.CREATED);
		else
			return new ResponseEntity<Object>(resultList, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@PostMapping("/singleProductScore/{upc}")
	public ResponseEntity<Object> singleProductScore(@PathVariable String upc,
			@RequestBody SingleProductScoreModel singleProductScoreModel) {
		long start = System.currentTimeMillis();
		long end = 0L;
		log.info("API CALLED");
		singleProductScoreModel.setUpc(upc);
		log.info("API :: " + singleProductScoreModel);
		SingleProductScoreResponseModel resultList = new SingleProductScoreResponseModel();
		try {
			resultList = productService.singleProductScore(singleProductScoreModel);
			end = System.currentTimeMillis();
			System.out.println("TOTAL SECONDS TAKEN BY API :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
			return new ResponseEntity<Object>(resultList, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(resultList, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
//		if (resultList.get(0).getResponseMessage().equalsIgnoreCase("Product status updated successfully"))
//			return new ResponseEntity<Object>(resultList, HttpStatus.CREATED);
//		else
//			return new ResponseEntity<Object>(resultList, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
