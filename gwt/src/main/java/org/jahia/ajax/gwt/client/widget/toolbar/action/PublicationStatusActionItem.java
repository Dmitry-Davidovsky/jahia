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
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.List;

public class PublicationStatusActionItem extends BaseActionItem {

    private transient HorizontalPanel panel;

    @Override
    public void handleNewMainNodeLoaded(final GWTJahiaNode node) {
        final GWTJahiaPublicationInfo info = node.getAggregatedPublicationInfo();
        if (info != null) {
            displayInfo(info);
        } else {
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(node.getPath()), Arrays.asList(GWTJahiaNode.PUBLICATION_INFO), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> result) {
                    final GWTJahiaPublicationInfo info = result.get(0).getAggregatedPublicationInfo();
                    if (info != null) {
                        displayInfo(info);
                    }
                }
            });
        }
    }

    private void displayInfo(GWTJahiaPublicationInfo info) {
        Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info);
        panel.removeAll();
        if (getGwtToolbarItem().getTitle() != null) {
            panel.add(new Html(getGwtToolbarItem().getTitle() + "&nbsp;:&nbsp;"));
        }
        panel.add(res);
        panel.layout();
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        panel = new HorizontalPanel();
        panel.addStyleName(getGwtToolbarItem().getClassName());
        panel.addStyleName("action-bar-menu-item");
    }

    @Override
    public Component getCustomItem() {
        return panel;
    }

}
