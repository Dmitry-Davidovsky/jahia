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
package org.jahia.ajax.gwt.client.data.toolbar;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Apr 6, 2010
 * Time: 7:27:40 PM
 * 
 */
public class GWTSidePanelTab  implements Serializable {
    private String name;
    private String tooltip;

    private SidePanelTabItem tabItem;



    private GWTJahiaToolbar treeContextMenu;
    private GWTJahiaToolbar tableContextMenu;

    private List<GWTColumn> treeColumns;
    private List<String> treeColumnKeys;

    private List<GWTColumn> tableColumns;
    private List<String> tableColumnKeys;
    private String icon;

    private String requiredPermission;

    public GWTSidePanelTab() {
        treeColumns = new ArrayList<GWTColumn>();
        treeColumnKeys = new ArrayList<String>();
        tableColumns = new ArrayList<GWTColumn>();
        tableColumnKeys = new ArrayList<String>();
    }


    public GWTSidePanelTab(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public SidePanelTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(SidePanelTabItem tabItem) {
        this.tabItem = tabItem;
    }

    public GWTJahiaToolbar getTreeContextMenu() {
        return treeContextMenu;
    }

    public void setTreeContextMenu(GWTJahiaToolbar treeContextMenu) {
        this.treeContextMenu = treeContextMenu;
    }

    public GWTJahiaToolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(GWTJahiaToolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public void addTableColumn(GWTColumn col) {
        tableColumns.add(col);
        tableColumnKeys.add(col.getKey());
    }

    public void addTreeColumn(GWTColumn col) {
        treeColumns.add(col);
        treeColumnKeys.add(col.getKey());
    }

    public List<GWTColumn> getTableColumns() {
        if (tableColumns.isEmpty()) {
            addTableColumn(new GWTColumn("name", Messages.get("label.name"),-1));
        }
        return tableColumns;
    }

    public List<String> getTableColumnKeys() {
        if (tableColumns.isEmpty()) {
            addTableColumn(new GWTColumn("name", Messages.get("label.name"),-1));
        }
        return tableColumnKeys;
    }

    public List<GWTColumn> getTreeColumns() {
        if (treeColumns.isEmpty()) {
            addTreeColumn(new GWTColumn("name", Messages.get("label.name"),-1));
        }
        return treeColumns;
    }

    public List<String> getTreeColumnKeys() {
        if (treeColumns.isEmpty()) {
            addTreeColumn(new GWTColumn("name", Messages.get("label.name"),-1));
        }
        return treeColumnKeys;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }
}
