<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<stripes:layout-render name="/skins/${skin}/common-with-navigation.jsp">
    <stripes:layout-component name="content">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.AbstractActionBean"/>
        <stripes:form action="${actionBean.dispatch.absoluteOriginalPath}" method="post"
                      enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
                <div class="portletPageHeader">
                    <c:if test="${empty actionBean.returnToParentTarget}">
                        <stripes:submit id="Table_returnToParent" name="returnToParent" value="<< Return to search"/>
                    </c:if><c:if test="${not empty actionBean.returnToParentTarget}">
                        <stripes:submit id="Table_returnToParent" name="returnToParent" value="<< Return to ${actionBean.returnToParentTarget}"/>
                    </c:if>
                    <div class="breadcrumbs">
                        <div class="inner">
                            <mde:write name="breadcrumbs"/>
                        </div>
                    </div>
                </div>
                <div id="portletPageBody">
                    <c:forEach var="portlet" items="${actionBean.portlets}" varStatus="status">
                        <div class="portletWrapper ${status.first ? 'first' : ''}">
                            <jsp:include page="${portlet}" />
                        </div>
                    </c:forEach>
                </div>
                <div class="portletPageFooter">
                    <!-- TODO -->
                </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>