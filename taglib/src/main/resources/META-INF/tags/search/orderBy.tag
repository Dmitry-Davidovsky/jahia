<%@ tag body-content="empty" dynamic-attributes="attributes" description="Specifies ordering for search results" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>

<%@ attribute name="operand" required="false" type="java.lang.String" description="The initial operand to order by [score]." %>
<%@ attribute name="propertyName" required="false" type="java.lang.String" description="If operand is 'property', then this attribute defines the property name to order by. []" %>
<%@ attribute name="order" required="false" type="java.lang.String" description="The order direction. [descending]" %>
<%@ attribute name="normalize" required="false" type="java.lang.Boolean" description="Defines whether umlaut, accent,... characters should be normalized before sorting. [false]" %>
<%@ attribute name="caseConversion" required="false" type="java.lang.String" description="Defines whether all characters should be set lower/upper case before sorting. []" %>
            

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="display" value="${functions:default(display, true)}"/>
<c:set var="order" value="${functions:default(order, 'descending')}"/>
<c:set var="normalize" value="${functions:default(normalize, false)}"/>

<c:set var="formId" value='<%= request.getAttribute("org.jahia.tags.search.form.formId") %>'/>
<c:set var="orderingIndex" value="${searchOrderingIndexes[formId]}"/>

<c:set var="operandKey" value="src_orderings[${orderingIndex}].operand"/>
<c:set var="propertyNameKey" value="src_orderings[${orderingIndex}].propertyName"/>
<c:set var="orderKey" value="src_orderings[${orderingIndex}].order"/>
<c:set var="normalizeKey" value="src_orderings[${orderingIndex}].normalize"/>
<c:set var="caseConversionKey" value="src_orderings[${orderingIndex}].caseConversion"/>

<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="propertyName" value="${functions:default(param[propertyNameKey], propertyName)}"/>

<input type="hidden" name="${operandKey}" value="${fn:escapeXml(functions:default(param[operandKey], empty operand ? 'score' : operand))}"/>

<c:choose>
  <c:when test="${!display}">
    <input type="hidden" name="${propertyNameKey}" value="${propertyName}"/>
    <input type="hidden" name="${orderKey}" value="${fn:escapeXml(functions:default(param[orderKey], order))}"/>    
    <input type="hidden" name="${normalizeKey}" value="${fn:escapeXml(functions:default(param[normalizeKey], normalize))}"/>
    <c:if test="${not empty caseConversion or not empty param[caseConversionKey]}">
        <input type="hidden" name="${caseConversionKey}" value="${fn:escapeXml(functions:default(param[caseConversionKey], caseConversion))}"/>
    </c:if>            
  </c:when>
  <c:otherwise>
    <c:if test="${empty requestScope['org.apache.jsp.tag.web.search.orderByTag.included']}">
        <c:set var="org.apache.jsp.tag.web.search.orderByTag.included"
            value="true" scope="request" />
        <template:addResources type="javascript" resources="${pageContext.request.contextPath}/javascript/search.js"/>
    </c:if>
    <select ${functions:attributes(attributes)} name="${propertyNameKey}" onchange="searchOrderByToggle(this, document.getElementsByName('${operandKey}')[0]);">
        <option value="score" ${propertyName == 'score' ? 'selected="selected"' : ''}><fmt:message key="searchForm.orderBy.score"/></option>
        <option value="jcr:lastModified" ${propertyName == 'jcr:lastModified' ? 'selected="selected"' : ''}><fmt:message key="searchForm.orderBy.date"/></option>            
    </select>  
  </c:otherwise>
</c:choose>  
<c:set target="${searchOrderingIndexes}" property="${formId}" value="${searchOrderingIndexes[formId] + 1}"/>