<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<html:messages id="message" message="true" bundle="MESSAGING_RESOURCES">
	<span class="error">
		<bean:write name="message"/>
	</span>
</html:messages>

<logic:present name="forum">
	<h2><bean:message bundle="MESSAGING_RESOURCES" key="label.viewForum.title"/></h2>
	
	<!-- Forum Details -->
	<fr:view name="forum" schema="forum.view-full">
		<fr:layout name="tabular">
	        <fr:property name="classes" value="style1"/>
	        <fr:property name="columnClasses" value="listClasses,"/>
	    </fr:layout>
	</fr:view>

	<h2><bean:message bundle="MESSAGING_RESOURCES" key="label.viewForum.threads"/></h2>
	
	<!-- Conversation Threads -->
	<html:link action="/messaging/forunsManagement.do?method=prepareCreateThreadAndMessage" paramId="forumId" paramName="forum" paramProperty="idInternal">
		<bean:message bundle="MESSAGING_RESOURCES" key="link.viewForum.createThread"/>
	</html:link>
	<br/><br/>
	
	<strong><bean:message bundle="MESSAGING_RESOURCES" key="label.viewForum.threads"/>:</strong><br/>
	<logic:empty name="conversationThreads">
		<span class="error"><bean:message bundle="MESSAGING_RESOURCES" key="label.viewForum.noThreads"/></span>
	</logic:empty>
	<logic:notEmpty name="conversationThreads">
		<fr:view name="conversationThreads" schema="conversationThread.view-full">
			<fr:layout name="tabular">
				<fr:property name="style" value="width:100%"/>
		        <fr:property name="classes" value="style1"/>
		        <fr:property name="columnClasses" value="listClasses,"/>
       			<fr:property name="link(view)" value="/messaging/forunsManagement.do?method=viewThread"/>
				<fr:property name="param(view)" value="forum.idInternal/forumId,idInternal/threadId"/>
				<fr:property name="key(view)" value="label.viewForum.viewThread"/>
				<fr:property name="bundle(view)" value="MESSAGING_RESOURCES"/>
		    </fr:layout>
		</fr:view>

		<strong><bean:message bundle="MESSAGING_RESOURCES" key="label.viewForum.page"/></strong>&nbsp;
		<bean:define id="currentPageNumberString"><bean:write name="currentPageNumber"/></bean:define>
		<logic:iterate id="pageNumber" name="pageNumbers" type="java.lang.Integer">
			<logic:equal name="currentPageNumberString" value="<%=pageNumber.toString()%>">
				<bean:write name="pageNumber"/>
			</logic:equal>
			<logic:notEqual name="currentPageNumber" value="<%=pageNumber.toString()%>">
				<bean:define id="forumId" name="forum" property="idInternal" />
				<html:link action="<%="/messaging/forunsManagement.do?method=viewForum&forumId=" + forumId.toString() + "&pageNumber=" + pageNumber%>">
					<bean:write name="pageNumber"/>
				</html:link>			
			</logic:notEqual>
		</logic:iterate>
	</logic:notEmpty>
	
	<br/><br/>
			
</logic:present>