<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js,jquery.fancybox.js"/>
<template:addResources type="css" resources="contribute-toolbar.css,jquery.fancybox.css"/>
<fmt:message key="label.noSelection" var="i18nNoSelection"/>
<c:set var="i18nNoSelection" value="${functions:escapeJavaScript(i18nNoSelection)}"/>
<template:addResources>
    <script>
        var contributeParams = new Array();
        $.ajaxSetup({
            accepts: {
                script: "application/json"
            },
            cache:false
        })
        /*
         $("#delete-${currentNode.identifier}").button();
         $("#copy-${currentNode.identifier}").button();
         $("#paste-${currentNode.identifier}").button();
         */
        function getUuids() {
            var uuids = new Array();
            var i = 0;
            $("input:checked").each(function(index) {
                uuids[i++] = $(this).attr("name");
            });
            return uuids;
        }

        function reload() {
            for(var i=0; i < contributeParams.length; i++ ){

                $("#" + contributeParams[i].contributeReplaceTarget).load(contributeParams[i].contributeReplaceUrl, '', null);
            }
        }

        function deleteNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.deleteNodes.do'/>", {"uuids": uuids}, function(result) {
                    reload();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function copyNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.copy.do'/>", {"uuids": uuids}, function(result) {
                    showClipboard();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function cutNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.cut.do'/>", {"uuids": uuids}, function(result) {
                    showClipboard();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function publishNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.publishNodes.do'/>", {"uuids": uuids}, function(result) {
                	<fmt:message key="label.workflow.started" var="i18nWorkflowStarted"/>
                    window.alert("${functions:escapeJavaScript(i18nWorkflowStarted)}");
                    reload();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function pasteNodes(contributeParams) {
            $.post("<c:url value='${url.base}'/>"+contributeParams.contributeTarget+".paste.do", {}, function(result) {
                reload();
                hideClipboard();
            }, "json");
        }

        function emptyClipboard() {
            $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.emptyclipboard.do'/>", {}, function(result) {
                hideClipboard();
            }, "json");
        }

        function showClipboard() {
            $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.checkclipboard.do'/>", {}, function(data) {
                if (data != null && data.size > 0) {
                    $(".titleaddnewcontent").show();
                    $(".pastelink").show();
                    $("#empty-${currentNode.identifier}").show();
                    $("#clipboard-${currentNode.identifier}").html("<fmt:message key="label.clipboard.contains"/> " + data.size +
                            ' element(s)</span></a>');
                    $("#clipboard-${currentNode.identifier}").show();
                    $("#clipboardpreview-${currentNode.identifier}").empty();
                    var paths = data.paths;
                    for (var i = 0; i < paths.length; i++) {
                        $.get("<c:url value='${url.base}'/>" + paths[i] + ".html.ajax", {}, function(result) {
                            $("#clipboardpreview-${currentNode.identifier}").append("<div style='border:thin'>");
                            $("#clipboardpreview-${currentNode.identifier}").append(result);
                            $("#clipboardpreview-${currentNode.identifier}").append("</div>");
                        }, "html")
                    }
                    $("#clipboard-${currentNode.identifier}").fancybox();
                }
            }, "json");
        }

        function hideClipboard() {
            $(".titleaddnewcontent").hide();
            $(".pastelink").hide();
            $("#empty-${currentNode.identifier}").hide();
            $("#clipboard-${currentNode.identifier}").hide();
        }

        function onresizewindow() {
            h = document.documentElement.clientHeight - $("#contributeToolbar").height();
            $("#contributewrapper").attr("style","position:relative; overflow:auto; height:"+ h +"px");
        }

        $(document).ready(function() {
            $(".fancylink").fancybox({
                'titleShow' : false,
                'autoDimensions' : false,
                'width' : 800,
                'height' : 600,
                'onComplete' : function() {
                    animatedcollapse.init();
                }
            });
        });

    </script>
</template:addResources>
<utils:setBundle basename="JahiaContributeMode" useUILocale="true" templateName="Jahia Contribute Mode"/>
<div id="contributeToolbar" >

    <div id="edit">
        <a href="<c:url value='${url.live}'/>" ><img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.live"/></a>
        <a href="<c:url value='${url.preview}'/>" ><img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.preview"/></a>
        <span> </span>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:removeChildNodes_default')}">
            <a href="#" id="delete-${currentNode.identifier}" onclick="deleteNodes();"><img src="<c:url value='/icons/delete.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.delete"/></a>
        </c:if>
        <a href="#" id="copy-${currentNode.identifier}" onclick="copyNodes();"><img src="<c:url value='/icons/copy.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.copy"/></a>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:removeChildNodes_default')}">
            <a href="#" id="cut-${currentNode.identifier}" onclick="cutNodes();"><img src="<c:url value='/icons/cut.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.cut"/></a>
        </c:if>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:write_default')}">
            <a href="#" id="publish-${currentNode.identifier}" onclick="publishNodes();"><img src="<c:url value='/icons/publish.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.requestPublication"/></a>
        </c:if>
        <a href="#" id="empty-${currentNode.identifier}" onclick="emptyClipboard();" style="display:none;"><img src="<c:url value='/icons/clipboard.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.clipboard.reset"/></a>
        <a href="#clipboardpreview-${currentNode.identifier}" id="clipboard-${currentNode.identifier}" style="display:none;"><img src="<c:url value='/icons/clipboard.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.clipboard.contains"/></a>
        <a href="<c:url value='${url.basePreview}${renderContext.user.localPath}.contributeTasklist.html.ajax'/>" class="fancylink"><img src="<c:url value='/icons/user.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.goto.myTasks"/></a>
        <c:choose>
            <c:when test="${jcr:isNodeType(currentNode, 'jnt:folder') || jcr:isNodeType(currentNode, 'nt:file')}">
                <c:if test="${jcr:hasPermission(currentNode,'fileManager')}">
                <c:url var="mgrUrl" value="/engines/manager.jsp">
                    <c:param name="conf" value="filemanager"/>
                    <c:param name="site" value="${renderContext.site.identifier}"/>
                    <c:param name="selectedPaths" value="${currentNode.path}"/>
                </c:url>
                <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/treepanel-files-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                        key="label.filemanager"/></a>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:set var="contentPath" value="${currentNode.resolveSite.path}/contents"/>
                <c:if test="${fn:startsWith(currentNode.path,contentPath) && jcr:hasPermission(currentNode,'editorialContentManager')}">
                <c:url var="mgrUrl" value="/engines/manager.jsp">
                    <c:param name="conf" value="editorialcontentmanager"/>
                    <c:param name="site" value="${renderContext.site.identifier}"/>
                    <c:param name="selectedPaths" value="${currentNode.path}"/>
                </c:url>
                    <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/treepanel-content-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                            key="label.contentmanager"/></a>
                </c:if>
            </c:otherwise>
        </c:choose>
        <span><fmt:message key="label.goto"/>: </span> <a href="<c:url value='${url.base}${currentNode.resolveSite.home.path}.html'/>"><img src="<c:url value='/icons/siteManager.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteHomepage"/></a>
        <a href="<c:url value='${url.base}${currentNode.resolveSite.path}/contents.html'/>"><img src="<c:url value='/icons/content-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteContent"/></a>
        <a href="<c:url value='${url.base}${currentNode.resolveSite.path}/files.html'/>"><img src="<c:url value='/icons/files-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteFiles"/></a>
        <a href="<c:url value='${url.logout}'/>"><img src="<c:url value='/icons/logout.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.logout"/></a>
    </div>
    <div style="display:none;">
        <div id="clipboardpreview-${currentNode.identifier}">
        </div>
    </div>
</div>

<div style="display:none;">
    <div id="tasks" >
        <%-- Just load the resources here ! --%>
        <template:module path="${renderContext.user.localPath}" view="contributeTasklist" var="temp"/>
    </div>
</div>
<div id="contributewrapper">