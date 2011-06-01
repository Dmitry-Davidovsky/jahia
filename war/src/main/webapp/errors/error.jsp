<%@page language="java" contentType="text/html; charset=UTF-8"
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

%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.io.PrintWriter,java.util.Date,org.jahia.bin.errors.ErrorFileDumper"%>
<%@ page import="org.jahia.settings.SettingsBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<% pageContext.setAttribute("devMode", Boolean.valueOf(SettingsBean.getInstance().isDevelopmentMode())); %>
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-1.0.css" type="text/css"/>
    <title><fmt:message key="label.error"/></title>
</head>
<body class="login" onLoad="if (history.length > 1) { document.getElementById('backLink').style.display=''; }">
    <div id="adminLogin">
    <h2 class="loginlogo"></h2>
            <br class="clearFloat" />
            <h3 class="loginIcon"><fmt:message key="label.error"/></h3>
        <p><fmt:message key="label.error.500.description"/></p>
        <p id="backLink" style="display:none"><fmt:message key="label.error.backLink.1"/>&nbsp;<a href="javascript:history.back()"><fmt:message key="label.error.backLink.2"/></a>&nbsp;<fmt:message key="label.error.backLink.3"/></p>
        <p><fmt:message key="label.error.homeLink"/>:&nbsp;<a href="<c:url value='/'/>"><fmt:message key="label.homepage"/></a></p>
            <br class="clearFloat" />
    </div>
<c:if test="${devMode && (not empty requestScope['org.jahia.exception'] || not empty requestScope['javax.servlet.error.exception'])}">
<div style="display:none">
<pre>
Error: <c:out value="${not empty requestScope['org.jahia.exception'] ? requestScope['org.jahia.exception'].message : requestScope['javax.servlet.error.exception'].message}"/>

<c:out value="${not empty requestScope['org.jahia.exception.trace'] ? requestScope['org.jahia.exception.trace'] : requestScope['javax.servlet.error.exception']}"/>
</pre>
<strong>System Status Information at <%= new Date() %></strong>
<pre>
    <% ErrorFileDumper.outputSystemInfoAll(new PrintWriter(pageContext.getOut())); %>
</pre>
</div>
</c:if>
</body>
</html>