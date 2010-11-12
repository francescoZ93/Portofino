<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/crud/searchButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><s:property value="searchTitle"/></h1>
        <s:if test="!searchForm.isEmpty()">
            <div class="search_form">
                <mdes:write value="searchForm"/>
                <s:submit method="search" value="Search"/>
                <s:reset value="Reset form"/>
            </div>
        </s:if>
        <mdes:write value="tableForm"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:set name="cancelReturnUrl"
               value="%{pkHelper.generateSearchUrl(searchString)}"/>
        <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/crud/searchButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>