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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.dnd.DND;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeGridDragSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:23:39 PM
 * 
 */
public class DisplayGridDragSource extends EditModeGridDragSource {
    public DisplayGridDragSource(Grid<GWTJahiaNode> grid) {
        super(grid);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
        nodes.add((GWTJahiaNode) grid.getSelectionModel().getSelectedItem());
        e.setData(nodes);
        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>(1);
        List<ModelData> l = grid.getSelectionModel().getSelectedItems();
        for (ModelData modelData : l) {
            list.add((GWTJahiaNode) modelData);
        }
        e.getStatus().setData("size", list.size());
        e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
        e.setOperation(DND.Operation.COPY);
        super.onDragStart(e);
    }
}
