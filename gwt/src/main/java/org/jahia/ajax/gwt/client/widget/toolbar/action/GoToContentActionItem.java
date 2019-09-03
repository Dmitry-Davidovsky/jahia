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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 * UI action item that "navigates" to the currently selected content (the displayable node, which corresponds to it) in main area of edit
 * mode.
 *
 * @author Sergiy Shyrkov
 */
public class GoToContentActionItem extends BaseActionItem {

    private static final long serialVersionUID = 3945583396729003576L;

    @Override
    public void handleNewLinkerSelection() {
        setEnabled(isActionAvailable());
    }

    private boolean isActionAvailable() {
        if (!isMainModuleAvailable()) {
            return false;
        }
        GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        return (selectedNode != null && !isOutOfContextContent(selectedNode));
    }

    private boolean isMainModuleAvailable() {
        MainModule mainModule = MainModule.getInstance();
        return (mainModule != null && mainModule.getEditLinker() != null);
    }

    private boolean isOutOfContextContent(GWTJahiaNode selectedNode) {
        return selectedNode.getPath().startsWith(JahiaGWTParameters.getSiteNode().getPath() + "/contents/") || selectedNode.isFile();
    }

    @Override
    public void onComponentSelection() {
        if (!isActionAvailable()) {
            return;
        }
        GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        JahiaContentManagementService.App.getInstance().getDisplayableNodePath(selectedNode.getPath(), false,
                new BaseAsyncCallback<String>() {
                    @Override
                    public void onSuccess(String path) {
                        if (path != null) {
                            MainModule.staticGoTo(path, null);
                        }
                    }
                });
    }
}
