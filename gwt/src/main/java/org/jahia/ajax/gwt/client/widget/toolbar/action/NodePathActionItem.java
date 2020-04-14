/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 * Displays the currently selected node path in the main module toolbar.
 */
@SuppressWarnings("serial")
public class NodePathActionItem extends BaseActionItem {
    private transient Text text;
    private transient HtmlContainer container;

    @Override
    public Component getCustomItem() {
        text = new Text("");
        text.setTagName("span");;
        container = new HtmlContainer("<span class='node-path-title'>" +
                Messages.get("label.currentPagePath", "Current page path") +
                ": " +
                "</span><span class='x-current-page-path node-path-text'></span>");
        container.setStyleName("node-path-container");
        container.addStyleName(getGwtToolbarItem().getClassName());
        container.addStyleName("action-bar-menu-item");
        container.add(text, ".node-path-text");
        return container;
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        String path = node.getPath();
        if (path.startsWith("/sites/"+node.getSiteKey())) {
            path = path.substring(node.getSiteKey().length()+8);
        }
        text.addStyleName("node-path-text-inner");
        text.setStyleAttribute("color","");
        text.setText(path);
        if (container.isRendered()) {
            container.getElement().setAttribute("data-nodedisplayname", node.getDisplayName());
            container.getElement().setAttribute("data-nodepath", node.getPath());
        }
        if (linker instanceof EditLinker) {
            MainModule mainModule = ((EditLinker) linker).getMainModule();
            HTML overlayLabel = mainModule.getOverlayLabel();
            if (overlayLabel != null) {
                text.setStyleAttribute("color", mainModule.getOverlayColorText());
                text.setText(text.getText() + " (" + overlayLabel.getText() + ")");
            }
        }
    }
}
