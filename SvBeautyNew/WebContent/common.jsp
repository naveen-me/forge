 <%
	session = request.getSession(false);
     
	if(session==null || session.getAttribute("repid") == null){
		if (session.getAttribute("HeaderRequestObjectValues") == null)
		{
			request.setAttribute("sessionMessage","Sorry, Your session expired!");
	   		if(request.getRequestURL().toString().indexOf("avoncustomerbeauty.com")> 0 ){
			      request.getRequestDispatcher("/customerregistration.jsp").forward(request,response);
			 } else if(request.getRequestURL().toString().indexOf("avonvirtualgrandopening.com")> 0 ){
				  request.getRequestDispatcher("/vgregistration.jsp").forward(request,response);
			 } else {	
			      request.getRequestDispatcher("/avonpersonalbeauty.jsp").forward(request,response);
			 }
				

		}
	}
%>
