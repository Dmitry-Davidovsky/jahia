<%@ tag body-content="empty" description="Renders page selection control." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided." %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for the page path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean" description="Initial value for the include children field." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. [jnt:page,jnt:virtualsite,jnt:navMenuText]" %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. [jnt:page,jnt:navMenuText]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="viewId" value="src_pagePath_valueView"/>
<c:set var="valueId" value="src_pagePath_value"/>
<c:if test="${not empty attributes.id}">
    <c:set var="viewId" value="${attributes.id}"/>
    <c:set var="valueId" value="${attributes.id}Value"/>
</c:if>

<c:set target="${attributes}" property="type" value="hidden"/>
<c:set target="${attributes}" property="name" value="src_pagePath.value"/>
<c:set target="${attributes}" property="id" value="${valueId}"/>
<%-- by default set includeChildren to 'true' to search in subpages --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<c:if test="${display}">
    <c:set var="value" value="${functions:default(param['src_pagePath.value'], value)}"/>
    <%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
    <c:set var="includeChildren"
           value="${functions:default(param['src_pagePath.includeChildren'], empty paramValues['src_pagePath.value'] ? includeChildren : 'false')}"/>
</c:if>
<input ${functions:attributes(attributes)} value="${fn:escapeXml(value)}"/>

<c:if test="${display}">
    <c:set target="${attributes}" property="type" value="text"/>
    <c:set target="${attributes}" property="name" value="src_pagePath.valueView"/>
    <c:set target="${attributes}" property="id" value="${viewId}"/>
    <c:if test="${not empty value}">
        <jcr:node path="${value}" var="pageNode"/>
        <c:if test="${not empty pageNode}">
            <jcr:nodeProperty node="${pageNode}" name="jcr:title" var="title"/>
            <c:set var="pageTitle" value="${not empty title ? title.string : ''}"/>
        </c:if>
        <c:if test="${empty pageTitle}">
            <c:set var="pageTitle"><fmt:message key="searchForm.pagePicker.noTitle"/></c:set>
        </c:if>
    </c:if>
    <input ${functions:attributes(attributes)} value="${fn:escapeXml(pageTitle)}"/>
    <uiComponents:pageSelector fieldId="${valueId}" displayFieldId="${attributes.id}" fieldIdIncludeChildren="src_pagePath.includeChildren" includeChildren="${includeChildren}"
                               nodeTypes="${functions:default(nodeTypes, 'jnt:page,jnt:virtualsite,jnt:navMenuText')}" selectableNodeTypes="${functions:default(selectableNodeTypes, 'jnt:page,jnt:navMenuText')}"/>
</c:if>

<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_pagePath.includeChildren" value="true"/>
</c:if>