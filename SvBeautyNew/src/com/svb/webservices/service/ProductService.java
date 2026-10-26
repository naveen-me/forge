package com.svb.webservices.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import com.svb.common.valueobjects.ProductVO;
import com.svb.webservices.model.ErrorDetails;
import com.svb.webservices.model.ProductModel;
import com.svb.webservices.model.SingleProductScoreResponseModel;
import com.svb.webservices.model.RecProductAvailibility;
import com.svb.webservices.model.RecomondationProductModel;
import com.svb.webservices.model.RecomondedProductResponseModel;
import com.svb.webservices.model.SingleProductScoreModel;

public interface ProductService {

	RecomondedProductResponseModel getProductRecomondationList(RecomondationProductModel productModel)
			throws IllegalAccessException, InvocationTargetException;

	List<ErrorDetails> updateProductAvailabilityList(RecProductAvailibility productStatusModel) throws SQLException;
	
	SingleProductScoreResponseModel singleProductScore(SingleProductScoreModel singleProductScoreModel) throws Exception;

}
