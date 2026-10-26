<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import ="java.util.*,java.io.*"%>
<%
  String xu = "" ;
  String catid = "" ;
  String mid = "" ;
  String mode = "" ;
  mid = request.getParameter("mid");
  mode = request.getParameter("mode");
  catid = request.getParameter("catid") ;
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /> <title>Offline</title> <link href="css/SkinStyle.css" rel="stylesheet" type="text/css" /> <script type="text/javascript">
<!--
function checkPageHeight()
{
 var ht = document.documentElement.scrollHeight ;
// alert(ht);
// parent.location = "http://www.drugstore.com/partners/sv/recommender.asp?catid=<%=catid%>#"+ht
;

}
//-->
</script>
</head>
<body onLoad="javascript:checkPageHeight()">
<div id="wrapper">
  <table id="offline">
    <tr>
      <td><p class="header">We're sorry; the Skin Care Analysis feature is taking a break.</p>
        <p>Please check back with us soon to get personalized skin care recommendations just for you!</p>
        <p><a href="http://beauty.com" target="_top">Continue shopping &gt;</a></p></td>
    </tr>
  </table>
</div>
</body>
</html>

