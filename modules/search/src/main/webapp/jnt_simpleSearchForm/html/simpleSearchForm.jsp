<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="jquery.autocomplete.css" />
<template:addResources type="css" resources="thickbox.css" />
<template:addResources type="css" resources="simplesearchform.css" />

<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />
<template:addResources>
<c:url var="findUrl" value="${url.find}"/>
<script type="text/javascript">
    $(document).ready(function() {

        /**
         * As any property can match the query, we try to intelligently display properties that either matched or make
         * sense to display.
         * @param node
         */
        function getText(node) {
            if (node.matchingProperties.length > 0) {
                var firstMatchingProperty = node.matchingProperties[0];
                return node[firstMatchingProperty];
            }
            if (node["jcr:title"] != null) {
                return node["jcr:title"];
            } else if (node["text"] != null) {
                return node["text"];
            } else if (node["j:nodename"] != null) {
                return node["j:nodename"];
            }
        }

        function format(result) {
            return getText(result["node"]);
        }

        $("#searchTerm").autocomplete("${findUrl}", {
            dataType: "json",
            selectFirst: false,
            cacheLength: 1,
            parse: function parse(data) {
                return $.map(data, function(row) {
				    return {
					    data: row,
					    value: getText(row["node"]),
					    result: getText(row["node"])
				    }
			    });
            },
            formatItem: function(item) {
			    return format(item);
		    },
            extraParams: {
                query : "/jcr:root${renderContext.site.path}//element(*, nt:base)[jcr:contains(.,'{$q}*')]",
                language : "xpath",
                propertyMatchRegexp : "{$q}.*",
                removeDuplicatePropValues : "true"
            }
        });
    });
</script>
</template:addResources>
<template:addCacheDependency uuid="${currentNode.properties.result.string}"/>
<c:if test="${not empty currentNode.properties.result.node}">
<c:url value='${url.base}${currentNode.properties.result.node.path}.html' var="searchUrl"/>
<s:form method="post" class="simplesearchform" action="${searchUrl}">
		<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
		<c:if test="${not empty title.string}">
		<label for="searchTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
		</c:if>
		<fmt:message key='search.startSearching' var="startSearching"/>
       	<s:term match="all_words" id="searchTerm" value="${startSearching}" searchIn="siteContent,tags" onfocus="if(this.value==this.defaultValue)this.value='';" onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
       	<s:site value="${renderContext.site.name}" display="false"/>
       	<s:language value="${renderContext.mainResource.locale}" display="false" />
    	<input class="searchsubmit" type="submit"  title="<fmt:message key='search.submit'/>" value=""/>

</s:form><br class="clear"/>
</c:if>