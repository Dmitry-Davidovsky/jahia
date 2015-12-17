/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeTreeGridDragSource;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Selection;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class PagesTabItem extends SidePanelTabItem {
    protected List<String> folderTypes = new ArrayList<String>();
    private List<String> paths = new ArrayList<String>();

    protected transient TreeGrid<GWTJahiaNode> pageTree;
    protected transient GWTJahiaNodeTreeFactory pageFactory;
    protected transient String path;
    protected transient SelectMainNodeTreeLoadListener selectMainNodeTreeLoadListener;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        tab.setLayout(new FitLayout());
        tab.setId("JahiaGxtPagesTab");
        return tab;
    }

    private void initPageTree() {
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(paths);
        factory.setNodeTypes(folderTypes);
        for (GWTColumn c : config.getTreeColumns()) {
            c.setSortable(false);
        }
        List<String> fields = new ArrayList<String>(config.getTreeColumnKeys());
        if (!fields.contains(GWTJahiaNode.QUICK_PUBLICATION_INFO) && !fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
            fields.add(GWTJahiaNode.QUICK_PUBLICATION_INFO);
        }
        factory.setFields(fields);

        this.pageFactory = factory;
        this.pageFactory.setSelectedPath(path);

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);

        pageTree = factory.getTreeGrid(new ColumnModel(columns));

        pageTree.setAutoExpandColumn(columns.getAutoExpand());
        pageTree.getTreeView().setRowHeight(25);
        pageTree.getTreeView().setForceFit(true);
        pageTree.setHeight("100%");
        pageTree.setIconProvider(ContentModelIconProvider.getInstance());
        pageTree.getTreeStore().setStoreSorter(new StoreSorter<GWTJahiaNode>() {
            @Override public int compare(Store<GWTJahiaNode> gwtJahiaNodeStore, GWTJahiaNode m1, GWTJahiaNode m2,
                                         String property) {
                if (!m1.getInheritedNodeTypes().contains("jmix:navMenuItem") && m2.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                    return 1;
                } else if (!m2.getInheritedNodeTypes().contains("jmix:navMenuItem") && m1.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                    return -1;
                } else {
                    return gwtJahiaNodeStore.getModels().indexOf(m2) - gwtJahiaNodeStore.getModels().indexOf(m1);
                }
            }
        });
        pageTree.setSelectionModel(new TreeGridClickSelectionModel());
        this.pageTree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                final GWTJahiaNode node = se.getSelectedItem();
                if (node != null && !node.getPath().equals(editLinker.getMainModule().getPath()) &&
                    !node.getNodeTypes().contains("jnt:virtualsite") && !node.getNodeTypes().contains("jnt:navMenuText") &&
                        !node.getInheritedNodeTypes().contains("jmix:link")
                        ) {
                    MainModule.staticGoTo(node.getPath(), null);
                }
            }
        });
        this.pageTree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        
        pageTree.setContextMenu(createContextMenu(config.getTreeContextMenu(), pageTree.getSelectionModel()));
        selectMainNodeTreeLoadListener = new SelectMainNodeTreeLoadListener(pageTree);
        tab.add(pageTree);
    }

    @Override
    public void handleNewMainSelection(String path) {
        selectMainNodeTreeLoadListener.handleNewMainSelection(path);
        editLinker.getSelectionContext().setSelectedNodes(Arrays.asList(MainModule.getInstance().getNode()));
        editLinker.getSelectionContext().refresh(LinkerSelectionContext.BOTH);
        super.handleNewMainSelection(path);
    }

    private void initDND() {
        EditModeTreeGridDragSource source = new PageTreeGridDragSource();
        TreeGridDropTarget target = new PageTreeGridDropTarget();
        target.setAllowDropOnLeaf(true);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
        target.setFeedback(DND.Feedback.BOTH);

        source.addDNDListener(editLinker.getDndListener());
        target.addDNDListener(editLinker.getDndListener());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();
        initPageTree();
        if (linker.getConfig().isEnableDragAndDrop()) {
        initDND();
        }
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.isPage() || node.getNodeTypes().contains("jnt:externalLink")
                    || node.getNodeTypes().contains("jnt:nodeLink")
                    || node.getNodeTypes().contains("jnt:template") || node.getInheritedNodeTypes().contains("jnt:template")
                    || node.getInheritedNodeTypes().contains("jmix:visibleInPagesTree")) {
                return true;
            }
        }
        if (data.containsKey("event")
                && ("languageChanged".equals(data.get("event"))
                || "workflowStarted".equals(data.get("event"))
                || "workflowExecuted".equals(data.get("event"))
                || "publicationSuccess".equals(data.get("event"))
                || "unpublicationSuccess".equals(data.get("event")))) {
            return true;
        }
        return false;
    }

    @Override
    public void doRefresh() {
        List<String> selectedPath = pageFactory.getSelectedPath();
        pageFactory.getStore().removeAll();
        pageFactory.setSelectedPath(selectedPath);
        pageFactory.getLoader().load();
    }

    public void addOpenPath(String path) {
        pageFactory.setOpenPath(path);
    }

    public class PageTreeGridDropTarget extends TreeGridDropTarget {
        public PageTreeGridDropTarget() {
            super(PagesTabItem.this.pageTree);
        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            List<GWTJahiaNode> nodes = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
            e.getStatus().setData("type", status);
            boolean isAllowed = false;
            for (String f : folderTypes) {
                isAllowed |= nodes.get(0).getInheritedNodeTypes().contains(f);
            }
            if (activeItem != null && isAllowed) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                isAllowed = PermissionsUtils.isPermitted("editModeAccess", JahiaGWTParameters.getSiteNode()) && PermissionsUtils.isPermitted(
                        "jcr:write_default", activeNode) && !activeNode.isLocked();
                if (isAllowed) {
                    GWTJahiaNode parent = pageTree.getTreeStore().getParent(activeNode);
                    e.getStatus().setData(EditModeDNDListener.TARGET_NODE, activeNode);
                    e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, parent);
                    e.getStatus().setData(EditModeDNDListener.TARGET_PATH, activeNode.get("path"));

                    if (status == 1 && activeItem.isExpanded() && activeItem.getItemCount() > 0) {
                        List<GWTJahiaNode> children = pageTree.getTreeStore().getChildren(activeNode);
                        GWTJahiaNode n = children.get(0);
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, n);
                        e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, activeNode);
                    } else if (status == 1) {
                        List<GWTJahiaNode> children = pageTree.getTreeStore().getChildren(parent);
                        int next = children.indexOf(activeNode) + 1;
                        if (next < children.size()) {
                            GWTJahiaNode n = children.get(next);
                            e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, n);
                        } else {
                            e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, null);
                        }
                    }

                    if (activeNode.getInheritedNodeTypes().contains("jmix:navMenuItem")) {
                        e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                    } else if (activeNode.getNodeTypes().contains("jnt:templatesFolder") &&
                               EditModeDNDListener.PAGETREE_TYPE.equals(e.getStatus().getData(
                                       EditModeDNDListener.SOURCE_TYPE))) {
                        e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.TEMPLATETREE_TYPE);
                    } else {
                        e.getStatus().setStatus(false);
                        e.setCancelled(true);
                    }
                    return;
                }
            }
            e.getStatus().setData(EditModeDNDListener.TARGET_NODE, null);
            e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, null);
            e.getStatus().setData(EditModeDNDListener.TARGET_PATH, null);
            e.getStatus().setStatus(false);
            e.setCancelled(true);

        }

        @Override
        protected void onDragDrop(DNDEvent event) {
        }

        public AsyncCallback<Map<String, Object>> getCallback() {
            AsyncCallback<Map<String, Object>> callback = new BaseAsyncCallback<Map<String, Object>>() {
                public void onSuccess(Map<String, Object> data) {
                    editLinker.refresh(data);
                }

                public void onApplicationFailure(Throwable throwable) {
                    Window.alert("Failed : "+throwable);
                }
            };
            return callback;
        }
    }

    private class PageTreeGridDragSource extends EditModeTreeGridDragSource {
        public PageTreeGridDragSource() {
            super(PagesTabItem.this.pageTree);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            super.onDragStart(e);
            for (Selection s : editLinker.getMainModule().getSelections().values()) {
                s.hide();
            }
            editLinker.getMainModule().getSelections().clear();
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            final GWTJahiaNode node = PagesTabItem.this.pageTree.getSelectionModel().getSelectedItem();
            if (node.getInheritedNodeTypes().contains("jmix:navMenuItem") && PermissionsUtils.isPermitted("jcr:removeNode", node) && !node.isLocked()) {
                l.add(node);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
            } else {
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, null);
                e.getStatus().setStatus(false);
                e.setCancelled(true);
            }
        }


        @Override
        protected void onDragDrop(DNDEvent event) {
            // do nothing
        }
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }


}
