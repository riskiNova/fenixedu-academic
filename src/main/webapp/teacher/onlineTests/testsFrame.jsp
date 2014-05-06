<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:xhtml/>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="row">
	<nav class="col-lg-2" id="context">
		<ul class="nav nav-pills nav-stacked">
			<li>
				<html:link page="/manageExecutionCourse.do?method=instructions" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="label.back"/>
				</html:link>
			</li>
			<li>
				<html:link page="/testsManagement.do?method=testsFirstPage" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.home"/>
				</html:link>
			</li>
		</ul>
		<ul class="nav nav-pills nav-stacked">
			<li class="navheader">
				<strong><bean:message key="title.tests"/></strong>
			</li>
			<li>
				<html:link page="/testsManagement.do?method=prepareCreateTest" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.createTest"/>
				</html:link>
			</li>
			<li>
				<html:link page="/testsManagement.do?method=showTests" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.showTests"/>
				</html:link>
			</li>
			<li>
				<html:link page="/testsManagement.do?method=showDistributedTests" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.showDistributedTests"/>
				</html:link>
			</li>
		</ul>
		<ul class="nav nav-pills nav-stacked">
			<li class="navheader">
				<strong><bean:message key="title.exercises"/></strong>
			</li>
			<li>
				<html:link page="/exercisesManagement.do?method=exercisesFirstPage" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.showExercises"/>
				</html:link>
			</li>
			<li>
				<html:link page="/exercisesManagement.do?method=chooseNewExercise" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.createExercise"/>
				</html:link>
			</li>
			<li>
				<html:link page="/exercisesManagement.do?method=insertNewExercise" paramId="executionCourseID" paramName="executionCourseID">
					<bean:message key="link.importExercise"/>
				</html:link>
			</li>
		</ul>
	</nav>
	<main class="col-lg-10">
		<ol class="breadcrumb">
			<em>${executionCourse.name} - ${executionCourse.executionPeriod.qualifiedName}
				(<c:forEach var="degree" items="${executionCourse.degreesSortedByDegreeName}"> ${degree.sigla} </c:forEach>)
			</em>
		</ol>
		<jsp:include page="${teacher$actual$page}" />
	</main>
</div>

<style>

.gen-button {
	margin: 0;
	padding: 0 5px 0;
}

</style>