<%@page import="java.util.*"%>
<%@page import="DefaultNamespace.DominoSoapBindingStub"%>
<%@page import="DefaultNamespace.WS_EKP_SSOServiceLocator"%>
<%@page import="java.net.URL"%>
<%@page import="DefaultNamespace.WS_EKP_SSO"%><HTML>
<HEAD>
<%@ page 
contentType="text/html; charset=GB2312"
pageEncoding="GB2312"
%>
<META http-equiv="Content-Type" content="text/html; charset=GB18030">
<META name="GENERATOR" content="IBM WebSphere Studio">
<META http-equiv="Content-Style-Type" content="text/css">
<LINK href="css/index.css" rel="stylesheet" type="text/css">
<TITLE>ϵͳ</TITLE>
</HEAD>
<BODY>
<P align="left">
<%
	String errorPage = "http://10.7.2.81:8090/LamsSznyIF/page/error.jsp";
	String EKP_LOGIN_PAGE = "http://10.7.2.81:8090/LamsSznyIF/sso?Ltpatoken=";
	String Ltpatoken = request.getParameter("Ltpatoken");
	Ltpatoken = new String(Ltpatoken.getBytes("iso-8859-1"),"utf-8");
	if(Ltpatoken==null||Ltpatoken.equals("")||Ltpatoken.equalsIgnoreCase("NULL")){
			response.sendRedirect(errorPage);
			return;
	}
	String usercode = null;
	try{
		WS_EKP_SSO sso = new WS_EKP_SSOServiceLocator().getDomino();
		//System.out.println(request.getServerName()+" #$%^&");
		//System.out.println("Ltpatoken------"+Ltpatoken);
		usercode = sso.getUserName("oa.sec.com.cn",Ltpatoken);
		System.out.println("usercode:"+usercode);
		if(usercode==null||usercode.equals("")){
			response.sendRedirect(errorPage);
			return;
		}else{
			response.sendRedirect(EKP_LOGIN_PAGE + usercode);
		}
	}catch(Exception e){
		e.printStackTrace();
	}
%></P>
</BODY>
</HTML>
