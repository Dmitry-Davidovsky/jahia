/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.util;

import com.google.gwt.core.client.JsArray;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * User: ktlili
 * Date: Aug 21, 2009
 * Time: 2:01:49 PM
 *
 */
public class ContentHelper {

    public static List<GWTJahiaNode> getSelectedContentNodesFromHTML() {
        List<GWTJahiaNode> selectedContentNodes = new ArrayList<GWTJahiaNode>();
        JsArray<ContentNodeJavaScriptObject> contentNodeJavaScriptObjectJsArray = getContentNodeOverlayTypes();

        if (contentNodeJavaScriptObjectJsArray != null) {
            for (int i = 0; i < contentNodeJavaScriptObjectJsArray.length(); i++) {
                ContentNodeJavaScriptObject contentNodeJavaScriptObject = contentNodeJavaScriptObjectJsArray.get(i);
                GWTJahiaNode gwtJahiaNode = new GWTJahiaNode();
                gwtJahiaNode.setUUID(contentNodeJavaScriptObject.getUUID());
                gwtJahiaNode.setPath(contentNodeJavaScriptObject.getPath());
                gwtJahiaNode.setName(contentNodeJavaScriptObject.getName());
                gwtJahiaNode.setDisplayName(contentNodeJavaScriptObject.getDisplayName());
                selectedContentNodes.add(gwtJahiaNode);
            }
        }
        return selectedContentNodes;
    }

    public static native String getContentNodeLocale() /*-{
        return $wnd.sLocale;
    }-*/;

    public static native String getAutoSelectParent() /*-{
        return $wnd.sAutoSelectParent;
    }-*/;

    private static native JsArray<ContentNodeJavaScriptObject> getContentNodeOverlayTypes() /*-{
        // Get a reference to the first customer in the JSON array from earlier
        return $wnd.sContentNodes;
    }-*/;

    public static native void sendContentModificationEvent(String nodeUuid, String nodePath, String nodeName, String operation, String nodeType) /*-{
        if ($wnd.contentModificationEventHandlers) {
            $wnd.contentModificationEventHandlers.forEach(function(func) {
                func.call(null, nodeUuid, nodePath, nodeName, operation, nodeType);
            });
        }
    }-*/;
}
