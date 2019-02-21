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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;

import java.util.List;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:58 PM
 */
public class UnpublishActionItem extends PublishActionItem {
    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        allSubTree = false;
        checkForUnpublication = true;
        workflowType = "unpublish";
    }

    protected void callback(List<GWTJahiaPublicationInfo> result) {
        if (result.isEmpty()) {
            MessageBox.info(Messages.get("label.publish", "Publication"), Messages.get("label.publication.nothingToUnpublish", "Nothing to unpublish"), null);
        } else {
            PublicationWorkflow.create(result, linker, checkForUnpublication);
        }
    }

    @Override
    public void handleNewLinkerSelection() {
        super.handleNewLinkerSelection();

        if (allLanguages) {
            LinkerSelectionContext ctx = linker.getSelectionContext();

            if (ctx.getMultipleSelection() == null || ctx.getMultipleSelection().size() <= 1) {
                GWTJahiaNode gwtJahiaNode = ctx.getSingleSelection();
                if (gwtJahiaNode != null) {
                    updateTitle(Messages.getWithArgs("label.unPublish.languages", "Unpublish {0} in all languages", new String[]{gwtJahiaNode.getDisplayName()}));
                }
            }
        }
    }
}
