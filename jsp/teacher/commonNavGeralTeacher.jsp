<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page import="ServidorApresentacao.Action.sop.utils.SessionConstants" %>
<bean:define id="userView" name="<%= SessionConstants.U_VIEW %>" scope="session"/>
<table width="50%" border="0" align="center" cellpadding="0" cellspacing="2">
	  <tr>
<logic:iterate id="role" name="userView" property="roles">
	<bean:define id="bundleKeyPageName"><bean:write name="role" property="pageNameProperty"/>.name</bean:define>
	<bean:define id="link"><%= request.getContextPath() %>/dotIstPortal.do?prefix=<bean:write name="role" property="portalSubApplication"/>&amp;page=<bean:write name="role" property="page"/></bean:define>
	    <td width="20%" nowrap class="navopgeral-td">
	    	<html:link href='<%= link %>'><bean:message name="bundleKeyPageName" bundle="PORTAL_RESOURCES"/></html:link>
	    </td>
</logic:iterate>	
<td width="20%" nowrap class="centerContent"><html:link forward="logoff"><span class="logout">Sair </span><img alt="" border="0" src="<%= request.getContextPath() %>/images/dotist_logout.gif"></html:link></td>
  </tr>
</table>	

