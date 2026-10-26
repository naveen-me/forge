
isOP = (navigator.userAgent.indexOf("Opera")!=-1 && document.getElementById);
isIE = document.all;
isNN = document.layers;
isFF = !document.all && document.getElementById;

posx=0;
posy=0;

function hideMe(){
  if(isIE)
  {
	  document.all.prLayer.style.visibility = "hidden";
	  document.all.pr_1_layer.style.visibility = "hidden";
  }else if(isNN)
  {
	  document.prLayer.visibility = "hidden";
	  document.pr_1_layer.visibility = "hidden";
  }else if(isFF || isOP)
  {
	  document.getElementById("prLayer").style.visibility = "hidden";
	  document.getElementById("pr_1_layer").style.visibility = "hidden";
  }
}

function showMeProduct(productName,productId,bid,e,crev)
{
  try{
  var popUpWidth = 600;
  var popUpHeight = 500;
  var width = 0;
  var height = 0;
  var docHeight = document.body.offsetHeight;

  ev =(!e)?window.event:e;
  if (ev.pageY)
  {
	posy = ev.pageY;
  }
  else 
  {
 	posy = ev.clientY + document.documentElement.scrollTop;
  }

   if (typeof(window.innerWidth) == 'number') {
       width = window.innerWidth;
   } else if(document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)){
       width = document.documentElement.clientWidth;
   } else if(document.body && (document.body.clientWidth || document.body.clientHeight)){
       width = document.body.clientWidth;
   }
  
   posx = (width / 2) - (popUpWidth / 2);
   if(posx < 0) posx = 0;

   posy = posy - (popUpHeight/2);
   if(posy < 0) posy = 0;
   if((posy + popUpHeight) >= docHeight)
		posy = docHeight - popUpHeight;

  if(isIE)
  {
    if (crev == "yes")
    {
	  document.all.prLayer.style.visibility = "visible";
	  document.all.prLayer.style.top = posy+"px";
	  document.all.prLayer.style.left = posx+"px";
	}
	else
	{
	  document.all.pr_1_layer.style.visibility = "visible";
	  document.all.pr_1_layer.style.top = posy+"px";
	  document.all.pr_1_layer.style.left = posx+"px";
	}
  }else if(isNN)
  {
    if (crev == "yes")
    {
	  document.prLayer.visibility = "show";
	  document.prLayer.top=posy+"px";
	  document.prLayer.left = posx+"px";
	}
	else
	{
	  document.pr_1_layer.visibility = "show";
	  document.pr_1_layer.top=posy+"px";
	  document.pr_1_layer.left = posx+"px";
	}
  }else if(isFF || isOP)
  {
    if (crev == "yes")
    {
	  document.getElementById("prLayer").style.top = posy+"px";
  	  document.getElementById("prLayer").style.left = posx+"px";
      document.getElementById("prLayer").style.visibility = "visible";
    }
    else
    {
	  document.getElementById("pr_1_layer").style.top = posy+"px";
  	  document.getElementById("pr_1_layer").style.left = posx+"px";
      document.getElementById("pr_1_layer").style.visibility = "visible";
    }
  }

    if (crev == "yes")
    {
      MakeTabActive("Promos", "images/white-arrow-gray.gif", true);
      MakeTabActive("ingredients", "images/arrow_right.gif", false);
      MakeTabActive("Reviews", "images/arrow_right.gif", false);
      MakeTabActive("Directions", "images/arrow_right.gif", false);
      document.getElementById("productId").value = productId;
      document.getElementById("pageDiv").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src='productDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>";
      document.getElementById("brandDiv").innerHTML = bid;
      document.getElementById("prNameDiv").innerHTML = productName;
    }
    else
    {
      MakeTabActive("Promos1", "images/white-arrow-gray.gif", true);
      MakeTabActive("ingredients1", "images/arrow_right.gif", false);
      MakeTabActive("Directions1", "images/arrow_right.gif", false);
      document.getElementById("product1Id").value = productId;
      document.getElementById("page1Div").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src='productDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>";
      document.getElementById("brand1Div").innerHTML = bid;
      document.getElementById("prName1Div").innerHTML = productName;
    }
  
  }catch(e){alert(e)}
}


function hideRecPopUp(id){
  if(id=="1"){
    if(isIE)
    {
	    document.all.whyRec.style.visibility = "hidden";
    }else if(isNN)
    {
        document.whyRec.visibility = "hidden";
    }else if(isFF || isOP)
    {
        document.getElementById("whyRec").style.visibility = "hidden";
    }
  }else if(id=="2"){
    if(isIE)
    {
	    document.all.activeIngRec.style.visibility = "hidden";
    }else if(isNN)
    {
        document.activeIngRec.visibility = "hidden";
    }else if(isFF || isOP)
    {
        document.getElementById("activeIngRec").style.visibility = "hidden";
    }
  }
}

function showRecPopUp(bid,productName,cid,pdid,blank,e,status)
{
    var width = 0;
    var height = 0;
    var popUpWidth = 650;
    var popUpHeight = 500;
	var docHeight = document.body.offsetHeight;

  	try{
	if(status)
  	{
 		ev =(!e)?window.event:e;
  		if (ev.pageY)
		{
			posy = ev.pageY;
		}
		else 
		{
		 	posy = ev.clientY + document.documentElement.scrollTop;
		}
  	 }
	 
   if (typeof(window.innerWidth) == 'number') {
       width = window.innerWidth;
   } else if(document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)){
       width = document.documentElement.clientWidth;
   } else if(document.body && (document.body.clientWidth || document.body.clientHeight)){
       width = document.body.clientWidth;
   }

	 posx = (width / 2) - (popUpWidth / 2);
	 if(posx < 0) posx = 0;
	 posy = posy - (popUpHeight/2);
	 if(posy < 0) posy = 0;
	 if((posy + popUpHeight) >= docHeight)
		posy = docHeight - popUpHeight;

	  if(isIE)
	  { 
		  document.all.whyRec.style.top = posy+"px";
		  document.all.whyRec.style.left = posx+"px";
		  document.all.whyRec.style.visibility = 'visible';
	  }else if(isNN)
	  {
		  document.whyRec.top = posy+"px";
		  document.whyRec.left = posx+"px";
		  document.whyRec.visibility = "visible";
	  }else if(isFF || isOP)
	  {
		  document.getElementById("whyRec").style.top = posy+"px";
	  	  document.getElementById("whyRec").style.left = posx+"px";
		  document.getElementById("whyRec").style.visibility = "visible";
	  }
    document.getElementById("whyDiv").innerHTML = "<iframe style='width:650px;height:300px;margin:0px;padding:0px;' frameborder='NO' src='productanalysis.do?customerId="+cid+"&productId="+pdid+"'></iframe>";
    document.getElementById("brand2Div").innerHTML = bid;
    document.getElementById("prName2Div").innerHTML = productName;
	}catch(e){}
}

function MakeTabActive(key, image, visible) 
{ 
   // alert(key+" "+image+" "+visible) ;
    try 
    {
        var tabButton, imgButton, tab;
	    tabButton = document.getElementById('btn'+key);
	    imgButton = document.getElementById('img'+key);
	    tab = document.getElementById('td'+key);	    
	    if(visible)
	    {
	        tabButton.className = 'TblPDetailsSelectedBtn';
		    imgButton.src = image;	
		    imgButton.alt = key;
		    tab.className='selectedTab';		    
	    } 
	    else 
	    {
		    tabButton.className = 'TblPDetailsBtn';
		    imgButton.src = image;
		    imgButton.alt = key;
		    tab.className='hiddenTab';		    
	    }    
    } 
    catch(e) {}    
}


function move(site){
  window.location = site; 
}

function selectBrand(){
  document.pdet.cleanseSkip.value  = "" ;
  document.pdet.moisturizeSkip.value  = "" ;
  document.pdet.treatSkip.value  = "" ;
  document.pdet.etreatSkip.value  = "" ;
  document.pdet.cleanseprod.value  = "" ;
  document.pdet.moisturizeprod.value  = "" ;
  document.pdet.treatprod.value  = "" ;
  document.pdet.etreatprod.value  = "" ;
  
  if(isIE)
  {
	  document.all.brLayer.style.visibility = "visible";
  }else if(isNN)
  {
	  document.brLayer.visibility = "visible";
  }else if(isFF || isOP)
  {
	  document.getElementById("brLayer").style.visibility = "visible";
  }
}

function selectBrandDetail(brandId){
  tmpSite = "brandDisc.do?brandparam="+brandId;
  if(isIE)
  {
	  document.all.brDetailLayer.style.visibility = "visible";
  }else if(isNN)
  {
	  document.brDetailLayer.visibility = "visible";
  }else if(isFF || isOP)
  {
	  document.getElementById("brDetailLayer").style.visibility = "visible";
  }
  document.getElementById("brDetDiv").innerHTML = "<iframe frameborder='NO' style='width:600px;height:300px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";
}

function hideMeBrand(){
	if(isIE)
    {
	    document.all.brLayer.style.visibility = "hidden";
    }else if(isNN)
    {
        document.brLayer.visibility = "hidden";
    }else if(isFF || isOP)
    {
        document.getElementById("brLayer").style.visibility = "hidden";
    }
}

function hideMeBrandDetail(){
	if(isIE)
    {
	    document.all.brDetailLayer.style.visibility = "hidden";
    }else if(isNN)
    {
        document.brDetailLayer.visibility = "hidden";
    }else if(isFF || isOP)
    {
        document.getElementById("brDetailLayer").style.visibility = "hidden";
    }
}

function move(site, key,crev){
	if (crev == "yes")
	{
	tmpSite = "productDetails.do?productId="+document.getElementById("productId").value+site;
	try{
		var tabIds = new Array("Promos", "ingredients", "Reviews", "Directions");
		var hiddenTabImgURL = "images/arrow_right.gif";
		var activeTabImgURL = "images/white-arrow-gray.gif";
		for(i=0;i<tabIds.length;i++)
		{
	       if(key != tabIds[i])
	       {
	          MakeTabActive(tabIds[i],hiddenTabImgURL,false);
	       }
	       else
	       {
	          MakeTabActive(tabIds[i],activeTabImgURL,true);
		   }
	    }
	}catch(e){}
  	   document.getElementById("pageDiv").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";
  	}
  	else
  	{
	tmpSite = "productDetails.do?productId="+document.getElementById("product1Id").value+site;
	try{
		var tabIds = new Array("Promos1", "ingredients1",  "Directions1");
		var hiddenTabImgURL = "images/arrow_right.gif";
		var activeTabImgURL = "images/white-arrow-gray.gif";
		for(i=0;i<tabIds.length;i++)
		{
	       if(key != tabIds[i])
	       {
	          MakeTabActive(tabIds[i],hiddenTabImgURL,false);
	       }
	       else
	       {
	          MakeTabActive(tabIds[i],activeTabImgURL,true);
		   }
	    }
	}catch(e){}
  	   document.getElementById("page1Div").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";
  	}
}

function selectProfileDesc(attribId,attribName,e){
  tmpSite = "SvProfileDescription.jsp?profparam="+attribId;
  try{
  var popUpWidth = 400;
  var popUpHeight = 300;
  var width = 0;
  var docWidth = document.body.offsetWidth;

  ev =(!e)?window.event:e;
  if (ev.pageY)
  {
	posx = ev.pageX;
	posy = ev.pageY;
  }
  else 
  {
	posx = ev.clientX;
 	posy = ev.clientY + document.documentElement.scrollTop;
  }
  
  if(posx < 0) posx = 0;
  if((posx+popUpWidth) >= docWidth)
	  posx = docWidth - popUpWidth;
  if(posy < 0) posy = 0;

  if(isIE)
  {
	  document.all.prDetailLayer.style.visibility = "visible";
	  document.all.prDetailLayer.style.top = posy+"px";
	  document.all.prDetailLayer.style.left = posx+"px";
  }else if(isNN)
  {
	  document.prDetailLayer.visibility = "show";
	  document.prDetailLayer.top=posy+"px";
	  document.prDetailLayer.left = posx+"px";
  }else if(isFF || isOP)
  {
	  document.getElementById("prDetailLayer").style.top = posy+"px";
  	  document.getElementById("prDetailLayer").style.left = posx+"px";
      document.getElementById("prDetailLayer").style.visibility = "visible";
  }
  document.getElementById("attribDiv").innerHTML = attribName;
  document.getElementById("prDetDiv").innerHTML = "<iframe frameborder='NO' style='width:300px;height:200px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";

  }catch(e){}
  
//  if (isIE||isNN) prDetailLayer.style.visibility="visible";
//  else if (isN4) document.prDetailLayer.visibility="show";
}
function hideMeProfileDetail(){
//  if (isIE||isNN) prDetailLayer.style.visibility="hidden";
//  else if (isN4) document.prDetailLayer.visibility="hide";
  if(isIE)
  {
	  document.all.prDetailLayer.style.visibility = "hidden";
  }else if(isNN)
  {
	  document.prDetailLayer.visibility = "hidden";
  }else if(isFF || isOP)
  {
	  document.getElementById("prDetailLayer").style.visibility = "hidden";
  }

}
function selectHairBrand(){
  document.pdet.shampooSkip.value  = "" ;
  document.pdet.conditionerSkip.value  = "" ;
  document.pdet.htreatSkip.value  = "" ;
  document.pdet.stylingSkip.value  = "" ;
  document.pdet.shampooprod.value  = "" ;
  document.pdet.conditionerprod.value  = "" ;
  document.pdet.htreatprod.value  = "" ;
  document.pdet.stylingprod.value  = "" ;
  
  if(isIE)
  {
	  document.all.brLayer.style.visibility = "visible";
  }else if(isNN)
  {
	  document.brLayer.visibility = "visible";
  }else if(isFF || isOP)
  {
	  document.getElementById("brLayer").style.visibility = "visible";
  }
}
function showMeHairProduct(productName,productId,bid,e,crev)
{
	
  try{
  var popUpWidth = 600;
  var popUpHeight = 500;
  var width = 0;
  var height = 0;
  var docHeight = document.body.offsetHeight;

  ev =(!e)?window.event:e;
  if (ev.pageY)
  {
	posy = ev.pageY;
  }
  else 
  {
 	posy = ev.clientY + document.documentElement.scrollTop;
  }

   if (typeof(window.innerWidth) == 'number') {
       width = window.innerWidth;
   } else if(document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)){
       width = document.documentElement.clientWidth;
   } else if(document.body && (document.body.clientWidth || document.body.clientHeight)){
       width = document.body.clientWidth;
   }
  
   posx = (width / 2) - (popUpWidth / 2);
   if(posx < 0) posx = 0;

   posy = posy - (popUpHeight/2);
   if(posy < 0) posy = 0;
   if((posy + popUpHeight) >= docHeight)
		posy = docHeight - popUpHeight;

  if(isIE)
  {
    if (crev == "yes")
    {
	  document.all.prLayer.style.visibility = "visible";
	  document.all.prLayer.style.top = posy+"px";
	  document.all.prLayer.style.left = posx+"px";
	}
	else
	{
	  document.all.pr_1_layer.style.visibility = "visible";
	  document.all.pr_1_layer.style.top = posy+"px";
	  document.all.pr_1_layer.style.left = posx+"px";
	}
  }else if(isNN)
  {
    if (crev == "yes")
    {
	  document.prLayer.visibility = "show";
	  document.prLayer.top=posy+"px";
	  document.prLayer.left = posx+"px";
	}
	else
	{
	  document.pr_1_layer.visibility = "show";
	  document.pr_1_layer.top=posy+"px";
	  document.pr_1_layer.left = posx+"px";
	}
  }else if(isFF || isOP)
  {
    if (crev == "yes")
    {
	  document.getElementById("prLayer").style.top = posy+"px";
  	  document.getElementById("prLayer").style.left = posx+"px";
      document.getElementById("prLayer").style.visibility = "visible";
    }
    else
    {
	  document.getElementById("pr_1_layer").style.top = posy+"px";
  	  document.getElementById("pr_1_layer").style.left = posx+"px";
      document.getElementById("pr_1_layer").style.visibility = "visible";
    }
  }

    if (crev == "yes")
    {
    	
      MakeTabActive("Promos", "images/white-arrow-gray.gif", true);
      MakeTabActive("ingredients", "images/arrow_right.gif", false);
      MakeTabActive("Reviews", "images/arrow_right.gif", false);
      MakeTabActive("Directions", "images/arrow_right.gif", false);
      document.getElementById("productId").value = productId;
      document.getElementById("pageDiv").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>";
      document.getElementById("brandDiv").innerHTML = bid;
      document.getElementById("prNameDiv").innerHTML = productName;
    }
    else
    {
    	
      MakeTabActive("Promos1", "images/white-arrow-gray.gif", true);
      MakeTabActive("ingredients1", "images/arrow_right.gif", false);
      MakeTabActive("Directions1", "images/arrow_right.gif", false);
      document.getElementById("product1Id").value = productId;
      document.getElementById("page1Div").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src='hairProductDetails.do?productId="+productId+"&popup=y&desc=y&direc=n&ingred=n&glos=n'></iframe>";
      document.getElementById("brand1Div").innerHTML = bid;
      document.getElementById("prName1Div").innerHTML = productName;
    }

  
  }catch(e){alert(e)}
}
function hairmove(site, key,crev){
	if (crev == "yes")
	{
	tmpSite = "hairProductDetails.do?productId="+document.getElementById("productId").value+site;
	try{
		var tabIds = new Array("Promos", "ingredients", "Reviews", "Directions");
		var hiddenTabImgURL = "images/arrow_right.gif";
		var activeTabImgURL = "images/white-arrow-gray.gif";
		for(i=0;i<tabIds.length;i++)
		{
	       if(key != tabIds[i])
	       {
	          MakeTabActive(tabIds[i],hiddenTabImgURL,false);
	       }
	       else
	       {
	          MakeTabActive(tabIds[i],activeTabImgURL,true);
		   }
	    }
	}catch(e){}
  	   document.getElementById("pageDiv").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";
  	}
  	else
  	{
	tmpSite = "hairProductDetails.do?productId="+document.getElementById("product1Id").value+site;
	try{
		var tabIds = new Array("Promos1", "ingredients1",  "Directions1");
		var hiddenTabImgURL = "images/arrow_right.gif";
		var activeTabImgURL = "images/white-arrow-gray.gif";
		for(i=0;i<tabIds.length;i++)
		{
	       if(key != tabIds[i])
	       {
	          MakeTabActive(tabIds[i],hiddenTabImgURL,false);
	       }
	       else
	       {
	          MakeTabActive(tabIds[i],activeTabImgURL,true);
		   }
	    }
	}catch(e){}
  	   document.getElementById("page1Div").innerHTML = "<iframe frameborder='1' style='width:600px;height:300px;margin:0px;padding:0px;' src="+tmpSite+"></iframe>";
  	}
}
function showHairRecPopUp(bid,productName,cid,pdid,blank,e,status)
{
    var width = 0;
    var height = 0;
    var popUpWidth = 650;
    var popUpHeight = 500;
	var docHeight = document.body.offsetHeight;

  	try{
	if(status)
  	{
 		ev =(!e)?window.event:e;
  		if (ev.pageY)
		{
			posy = ev.pageY;
		}
		else 
		{
		 	posy = ev.clientY + document.documentElement.scrollTop;
		}
  	 }
	 
   if (typeof(window.innerWidth) == 'number') {
       width = window.innerWidth;
   } else if(document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)){
       width = document.documentElement.clientWidth;
   } else if(document.body && (document.body.clientWidth || document.body.clientHeight)){
       width = document.body.clientWidth;
   }

	 posx = (width / 2) - (popUpWidth / 2);
	 if(posx < 0) posx = 0;
	 posy = posy - (popUpHeight/2);
	 if(posy < 0) posy = 0;
	 if((posy + popUpHeight) >= docHeight)
		posy = docHeight - popUpHeight;

	  if(isIE)
	  { 
		  document.all.whyRec.style.top = posy+"px";
		  document.all.whyRec.style.left = posx+"px";
		  document.all.whyRec.style.visibility = 'visible';
	  }else if(isNN)
	  {
		  document.whyRec.top = posy+"px";
		  document.whyRec.left = posx+"px";
		  document.whyRec.visibility = "visible";
	  }else if(isFF || isOP)
	  {
		  document.getElementById("whyRec").style.top = posy+"px";
	  	  document.getElementById("whyRec").style.left = posx+"px";
		  document.getElementById("whyRec").style.visibility = "visible";
	  }
    document.getElementById("whyDiv").innerHTML = "<iframe style='width:650px;height:300px;margin:0px;padding:0px;' frameborder='NO' src='hairproductanalysis.do?customerId="+cid+"&productId="+pdid+"'></iframe>";
    document.getElementById("brand2Div").innerHTML = bid;
    document.getElementById("prName2Div").innerHTML = productName;
	}catch(e){}
}
