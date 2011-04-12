<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<div class="backToParent">
   <c:if test="${!empty jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}">
		<c:url value='${url.base}${jcr:getParentOfType(renderContext.mainResource.node, "jnt:page").path}.html' var="action"/>
    </c:if>
    <c:if test="${empty jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}">
        <c:set var="action">javascript:history.back()</c:set>
    </c:if>
    <a class="returnLink" href="${action}" title='<fmt:message key="backToPreviousPage"/>'><fmt:message key='label.backToNewsList'/></a>
</div>