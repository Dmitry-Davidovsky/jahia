<%@ tag body-content="empty" description="Renders the label of a facet." %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ attribute name="display" required="false" type="java.lang.Boolean" description="Should we display the label or just return it in the parameter set by attribute var."%>
<%@ attribute name="var" required="false" type="java.lang.String" description="Request scoped attribute name for setting the label."%>
<%@ attribute name="currentFacetField" required="false" type="org.apache.solr.client.solrj.response.FacetField" description="Either the FacetField for the current facet." %>
<%@ attribute name="currentActiveFacet" required="false" type="java.lang.Object" description="Alternatively the Map.Entry with KeyValue from the active facet filters variable." %>
<%@ attribute name="facetLabels" required="true" type="java.util.Map" description="Mapping between facet name and label." %>
<%@ variable name-given="facetLabel" scope="AT_END"%>
<%@ variable name-given="mappedFacetLabel" scope="AT_END"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<c:set var="display" value="${functions:default(display, true)}"/>

<c:choose>
    <c:when test="${not empty currentFacetField}">
        <c:set var="currentFacetName" value="${currentFacetField.name}"/>
        <c:if test="${not empty facetLabels and (not empty facetLabels[currentFacetName])}">
            <c:set var="mappedFacetLabel" value="${facetLabels[currentFacetName]}"/>        
        </c:if>        
    </c:when>
    <c:otherwise>
        <c:set var="currentFacetName" value="${currentActiveFacet != null ? currentActiveFacet.key : ''}"/>
        <c:if test="${not empty facetLabels}">
            <c:forEach items="${facetLabels}" var="currentFacetLabel">
                <c:if test="${empty mappedFacetLabel and fn:endsWith(currentFacetName, currentFacetLabel.key)}">
                    <c:set var="mappedFacetLabel" value="${currentFacetLabel.value}"/>
                </c:if>
            </c:forEach>
        </c:if>        
    </c:otherwise>
</c:choose>  

<c:set var="facetLabel" value="${not empty mappedFacetLabel ? mappedFacetLabel : (not empty currentFacetField ? currentFacetName : '')}"/>
<c:if test="${display}">
    ${facetLabel}
</c:if>