/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 4/28/11
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class ClearAllLocksActionItem extends BaseActionItem {

    private boolean doSubNodes = false;

    public void setDoSubNodes(boolean doSubNodes) {
        this.doSubNodes = doSubNodes;
    }

    public void onComponentSelection() {
        ContentActions.lock(false, linker);
        String selectedPaths = linker.getSelectionContext().getSingleSelection().getPath();
        JahiaContentManagementService.App.getInstance().clearAllLocks(selectedPaths, doSubNodes, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error", "Error"), throwable.getLocalizedMessage(), null);
                linker.loaded();
                linker.refresh(Linker.REFRESH_MAIN);
            }

            public void onSuccess(Object o) {
                linker.loaded();
                linker.refresh(Linker.REFRESH_MAIN);
            }
        });


    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection!=null && singleSelection.isLockable() &&
                PermissionsUtils.isPermitted("jcr:lockManagement", lh.getSelectionPermissions()) && singleSelection.getLockInfos() != null &&
                !lh.getSingleSelection().getLockInfos().isEmpty() && !lh.isSecondarySelection());
    }
}
