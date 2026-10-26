package com.svb.webservices.serviceHelper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ConstantNameAndType;

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.webservices.model.IngredientNameModel;
import com.svb.webservices.model.NotHaveTheseBadIngredientsListModel;
import com.svb.webservices.model.NotHaveTheseBadIngredientsModel;

public class NotHaveTheseBadIngredientsSection {

	StringBuilder headerText = new StringBuilder();
	

	/**
	 * This method Calculates and returns not have these bad ingredient data for product
	 * @param ingredientsIdList 
	 * @param allergyList 
	 */
	public NotHaveTheseBadIngredientsModel getNotHaveTheseBadIngredients(PopupRatings popRate,
			List<String> ingredientsIdList, Hashtable<String, String> master, List<String> bodyAttributeList,
			ProductVO product, List<String> allergyList) {
		NotHaveTheseBadIngredientsModel notHaveTheseBadIngredients = new NotHaveTheseBadIngredientsModel();
		List<NotHaveTheseBadIngredientsListModel> notHaveTheseBadIngredientsList = new ArrayList<>();
		List<String> ingredientGroupFunction = new ArrayList<>();
		Hashtable<String,String> groupFunctionAttributes = new Hashtable<>();
		Set<String> groupfunctionIds = new HashSet<>();
		Hashtable<String, Hashtable<String, String>> groupFunctionTexts = new Hashtable<String, Hashtable<String,String>>();
		Hashtable<String,Boolean> groupFunctionFlagList = popRate.getGroupFunctionFlag(product.getMasterProdType(),bodyAttributeList,groupfunctionIds,groupFunctionTexts);
		Hashtable<String, String> groupFunctionText = getGroupFunctionText(groupFunctionAttributes,groupFunctionTexts,bodyAttributeList);
		Hashtable<String, List<IngredientNameModel>> groupFunctionIngredients = popRate.getGroupFunctionIngredients(ingredientGroupFunction);
		Hashtable<String, NotHaveTheseBadIngredientsListModel> groupFunctionAllDetails = popRate.getAllGroupFunctionDetailsWithFlag(groupFunctionIngredients,groupFunctionFlagList,groupfunctionIds,groupFunctionText);
		
		List<String> groupFunctionIdList = new ArrayList<>(groupFunctionFlagList.keySet());
		
		for(String ingredientId:ingredientsIdList) {
			for(String grpFuncId:groupFunctionIdList) {
				if(ingredientGroupFunction.contains(ingredientId+grpFuncId)) {
					groupFunctionFlagList.put(grpFuncId, true);
				}
			}
		}
		
		notHaveTheseBadIngredientsList = getNotHaveTheseBadIngredientsList(notHaveTheseBadIngredientsList,
				groupFunctionAllDetails, groupFunctionFlagList, groupFunctionAttributes, master,
				bodyAttributeList.get(0));
		if(headerText.length() != 0) {
			headerText.insert(0, "YAY! It does NOT HAVE ANY of these types of ingredients known to be 'bad' for ");
		}
		
		if(headerText.lastIndexOf(",") != -1) {
			int index = headerText.lastIndexOf(",");
			headerText.replace(index, index+2, ":");
		}
		
		if(headerText.lastIndexOf(",") != -1) {
			int index = headerText.lastIndexOf(",");
			headerText.replace(index, index+1, " or");
		}
		
		//Added by Shivam for new GOOD NOT TO HAVE Text - 25-08-2023 - Start
		/*String headerTextString = "";
		if(!allergyList.isEmpty() && !allergyList.contains(ConstantDeclarations.NO_ALLERGY_PREFERENCE)) {
			StringBuilder allergies = new StringBuilder(master.get(allergyList.get(0)));
			
			for(int index = 1; index < allergyList.size(); index++) {
				if(index == allergyList.size()-1) {
					allergies.append(" or "+master.get(allergyList.get(index))) ;
				}else {
					allergies.append(", "+master.get(allergyList.get(index)));
				}
			}
			headerTextString = "You indicated you prefer that your skincare products not have "+allergies.toString().toLowerCase()+". We are happy to report this product does not contain "+allergies.toString().toLowerCase()+".";
		}else {
			headerTextString = headerText.toString();
		}*/
		//Added by Shivam for new GOOD NOT TO HAVE Text - 25-08-2023 - End
		
		if(!notHaveTheseBadIngredientsList.isEmpty() && (notHaveTheseBadIngredientsList != null)) {
			notHaveTheseBadIngredients.setHeaderText(headerText.toString()); //commented by Shivam
//			notHaveTheseBadIngredients.setHeaderText(headerTextString); //Added By Shivam
			notHaveTheseBadIngredients.setNotHaveTheseBadIngredientsList(notHaveTheseBadIngredientsList);
		}
		//Added to add Does Not Contain Text - Shivam - 07/12/2023 - Start
		if(Objects.nonNull(allergyList) 
				&& !allergyList.isEmpty() 
				&& !allergyList.contains(ConstantDeclarations.NO_ALLERGY_PREFERENCE)
				&& Objects.nonNull(master)) {
			StringBuilder doesNotContainText = new StringBuilder("As you requested, this product DOES NOT CONTAIN: <br /><ul>");
			for(String allergy : allergyList) {
				if(Objects.nonNull(master.get(allergy))) {
					doesNotContainText.append("<li>"+master.get(allergy)+"</li>");
				}
			}
			doesNotContainText.append("</ul>");
			notHaveTheseBadIngredients.setDoesNotContainText(doesNotContainText.toString());
		}
		//Added to add Does Not Contain Text - Shivam - 07/12/2023 - End
		return notHaveTheseBadIngredients;
	
	}

	
	private Hashtable<String, String> getGroupFunctionText(Hashtable<String, String> groupFunctionAttributes,
			Hashtable<String, Hashtable<String, String>> groupFunctionTexts, List<String> bodyAttributeList) {
		
		Hashtable<String, String> groupFunctionText = new Hashtable<String, String>();
		 List<String> groupfunctionList = new ArrayList<>(groupFunctionTexts.keySet());
		 
		 for(String grpfunc:groupfunctionList) {
			 Hashtable<String, String> grpFunctionText = groupFunctionTexts.get(grpfunc);
			 
			 Set<Entry<String, String>> entrySet = grpFunctionText.entrySet();
			 String skintype = bodyAttributeList.get(0);
			 String combination = bodyAttributeList.get(0);
			 String combinationtemp = bodyAttributeList.get(0);
			 Hashtable<String, String> attributeGrpFunctionText = new Hashtable<String, String>();
			 
			 for(int i=1;i<bodyAttributeList.size();i++) {
				 	combination = combinationtemp;
		    	    combination += bodyAttributeList.get(i);
		    		Hashtable<String,String> attriCombinationtext = new Hashtable<>();
		    		for (Entry<String, String> entry : entrySet) {
		    			if ((entry.getKey()).startsWith(combination)) {
		    				attriCombinationtext.put(entry.getKey(), entry.getValue());
		    		    }
		            }

		    		if(attriCombinationtext.size() == 1 && Objects.nonNull(attriCombinationtext.get(combination))) {
		    			groupFunctionText.put(grpfunc, attriCombinationtext.get(combination));
	    				groupFunctionAttributes.put(grpfunc,combination);
	    				break;
	    			}
		    		
		    		if(!(attriCombinationtext.isEmpty() || attriCombinationtext==null)) {
		    				attributeGrpFunctionText = new Hashtable<>(attriCombinationtext);
			    			entrySet = attriCombinationtext.entrySet();
							combinationtemp = combination;
		    		}
		    		
		    		if((i == bodyAttributeList.size()-1) && !attributeGrpFunctionText.isEmpty() 
		    				&& Objects.nonNull(attributeGrpFunctionText.get(combinationtemp))) {
		    				groupFunctionText.put(grpfunc, attributeGrpFunctionText.get(combinationtemp));
		    				groupFunctionAttributes.put(grpfunc,combinationtemp);
		    				break;
		    		}
			 }
			 
				if(skintype.equals(combinationtemp) && (grpFunctionText.get(skintype+"NONE") != null)) {
					 groupFunctionText.put(grpfunc, grpFunctionText.get(skintype+"NONE"));
					 groupFunctionAttributes.put(grpfunc,skintype);
				 }
		 }
		 
		return groupFunctionText;
	}


	/**
	 * This method calculates and returns list of not have these bad ingredients for product 
	 */
	private List<NotHaveTheseBadIngredientsListModel> getNotHaveTheseBadIngredientsList(
			List<NotHaveTheseBadIngredientsListModel> notHaveTheseBadIngredientsList, Hashtable<String, NotHaveTheseBadIngredientsListModel> groupFunctionDetails, 
			Hashtable<String, Boolean> groupFunctionFlagList, Hashtable<String,String> groupFunctionAttributes, Hashtable<String, String> master, String skinType) {

		Enumeration<String> en = groupFunctionDetails.keys();
		while (en.hasMoreElements()) {
			String groupFunctionId = en.nextElement();
			if(!groupFunctionFlagList.get(groupFunctionId) && Objects.nonNull(groupFunctionDetails.get(groupFunctionId))
					&& Objects.nonNull(groupFunctionAttributes.get(groupFunctionId))) {
				notHaveTheseBadIngredientsList.add(groupFunctionDetails.get(groupFunctionId));
				 Matcher m = Pattern.compile("[A-Z]{1}\\d{3}").matcher(groupFunctionAttributes.get(groupFunctionId));
	    		 while (m.find()) {
	    			 String skin = m.group();
	    			 String skinString = master.get(skin).toLowerCase();
	    			 if(headerText.indexOf(skinString) == -1) {
							if(skin.equals(skinType)) {
								headerText.insert(0, skinString+", ");
							}
							else {
								headerText.append(skinString+", ");
							}
						}
	    		 }
			}
		}
		
		return notHaveTheseBadIngredientsList;
	}
	
	
}
