/***************************/
//@Author: Adrian "yEnS" Mato Gondelle
//@website: www.yensdesign.com
//@email: yensamg@gmail.com
//@license: Feel free to use it, but keep this credits please!					
/***************************/

//SETTING UP OUR POPUP
//0 means disabled; 1 means enabled;
var popupStatus = 0;

//loading popup with jQuery magic!
function loadPopup($target){
	//loads popup only if it is disabled
	if(popupStatus==0){
		$("#backgroundPopup").css({
			"opacity": "0.7"
           });
		$("#backgroundPopup").fadeIn("slow");
		$target.fadeIn("slow");
		popupStatus = 1;
	}
}

//disabling popup with jQuery magic!
function disablePopup($target,e){
	
	//disables popup only if it is enabled
	if(popupStatus==1){
		$("#backgroundPopup").fadeOut("slow");
		$target.fadeOut("slow");
		popupStatus = 0;
	}
}

//centering popup
function centerPopup($target,e){
	var leftVal='';
	//request data for centering
	var windowWidth = document.documentElement.clientWidth;
	var windowHeight =parent.document.body.clientHeight;
	var popupHeight = $target.height();
	var popupWidth = $target.width();
	
	
    var topVal=e.pageY-(popupHeight/2)+"px";
	
    $target.css({
		"position": "absolute",
		"z-index" : "100",
		"top": topVal,
		"left": windowWidth/2-popupWidth/2 
	});
	//only need force for IE6
	
	$("#backgroundPopup").css({
		"height": windowHeight
	});
	
}

