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
package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Helper class for various column renderers and configurations.
 *
 * @author ktlili
 */
@SuppressWarnings("serial")
public class NodeColumnConfigList extends ArrayList<ColumnConfig> {
    private List<GWTColumn> columnList;
    private String autoExpand;

    public static final GridCellRenderer<GWTJahiaNode> ICON_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> LOCKED_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            Map<String,List<String>> infos = modelData.getLockInfos();
            columnData.css = columnData.css
                    + (modelData.isMarkedForDeletion() ? " marked-for-deletion" : "")
                    + (modelData.isMarkedForDeletionRoot() ? " marked-for-deletion-root" : "");

            if (infos != null && (infos.containsKey(null) && (infos.size() == 1 || infos.containsKey(JahiaGWTParameters.getLanguage())))) {
                return StandardIconsProvider.STANDARD_ICONS.lock().getHTML();
            } else if (infos != null && infos.size() > 1) {
                return StandardIconsProvider.STANDARD_ICONS.lockLanguage().getHTML();
            } else {
                return "";
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> SIZE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            if (modelData.getSize() != null) {
                long size = modelData.getSize().longValue();
                return Formatter.getFormattedSize(size);
            } else {
                return "-";
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> DATE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            Date d = modelData.get(s);
            if (d != null) {
                return new DateTimePropertyEditor(DateTimeFormat.getFormat(
                        CalendarField.DEFAULT_DATE_FORMAT)).getStringValue(d);
            } else {
                return "-";
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> PUBLICATION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            final GWTJahiaPublicationInfo info = node.getAggregatedPublicationInfo();
            if (info != null) {
                HorizontalPanel p = new HorizontalPanel();
                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info);
                p.add(res);
                return p;
            }
            return "";
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> SCM_STATUS_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            String scmStatus = (String) node.getProperties().get("scmStatus");
            if (scmStatus == null) {
                return "";
            }
            Image img = ToolbarIconProvider.getInstance().getIcon("scmStatus/" + scmStatus).createImage();
            img.setTitle(Messages.get("label.scmStatus." + scmStatus, scmStatus));
            return img;
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> QUICK_PUBLICATION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            final GWTJahiaPublicationInfo info = node.getQuickPublicationInfo();
            if (info != null) {
                if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED
                        || info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                    HorizontalPanel p = new HorizontalPanel();
                    Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info);
                    p.add(res);
                    return p;
                } else {
                    return "";
                }
            }
            return "";
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> VERSION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
            List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
            if (versions != null) {
                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(true);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                for (GWTJahiaNodeVersion version : versions) {
                    String value = Messages.get("label.version", "Version") + " ";
                    if (version.getLabel() != null && !"".equals(version.getLabel())) {
                        String[] strings = version.getLabel().split("_at_");
                        if (strings.length == 2) {
                            String s1;
                            if (strings[0].contains("published")) {
                                s1 = Messages.get("label.version.published", "published at");
                            } else {
                                s1 = Messages.get("label.version.uploaded", "uploaded at");
                            }
                            value = value + s1 + " " + DateTimeFormat.getMediumDateTimeFormat().format(
                                    DateTimeFormat.getFormat("yyyy_MM_dd_HH_mm_ss").parse(strings[1]));
                        }
                    }
                    combo.add(value + " (" + version.getVersionNumber() + ")");
                }
                final String s2 = "Always Latest Version";
                combo.add(s2);
                combo.setSimpleValue(s2);
                combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                        SimpleComboValue<String> value = simpleComboValueSelectionChangedEvent.getSelectedItem();
                        String value1 = value.getValue();
                        if (!s2.equals(value1)) {
                            gwtJahiaNode.setSelectedVersion(value1.split("\\(")[0].trim());
                        }
                    }
                });
                combo.setDeferHeight(true);
                return combo;
            } else {
                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(false);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                combo.add("No version");
                combo.setSimpleValue("No version");
                combo.setEnabled(false);
                combo.setDeferHeight(true);
                return combo;
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> NAME_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            Object v = node.get(property);
            if (v != null) {
                v = SafeHtmlUtils.htmlEscape(v.toString());
            }
            if (node.isMarkedForDeletion()) {
                v = "<span class=\"markedForDeletion\">" + v + "</span>";
            }
            return v;
        }
    };

    public static final TreeGridCellRenderer<GWTJahiaNode> NAME_TREEGRID_RENDERER = new TreeGridCellRenderer<GWTJahiaNode>() {
        @Override
        protected String getText(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode node, String property, int rowIndex, int colIndex) {
            String v = super.getText(gwtJahiaNodeTreeGrid, node, property, rowIndex, colIndex);
            if (v != null) {
                v = SafeHtmlUtils.htmlEscape(v);
            }
            String classes = "";
            if (node.isMarkedForDeletion()) {
                classes += "markedForDeletion ";
            }
            if (node.getQuickPublicationInfo() != null &&
                    (node.getQuickPublicationInfo().getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED ||
                            node.getQuickPublicationInfo().getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED
                            || node.getQuickPublicationInfo().getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE)) {
                classes += "notPublished ";
            }
            if (!PermissionsUtils.isPermitted("jcr:write_default", node)) {
                classes += "readOnly ";
            }
            if (classes.length() > 0) {
                v = "<span class=\""+classes+"\">" + v + "</span>";
            }
            return v;
        }
    };


    public NodeColumnConfigList(List<GWTColumn> columnList) {
        this(columnList, false);
    }

    public NodeColumnConfigList(List<GWTColumn> columnList, boolean init) {
        this.columnList = columnList;
        if (init) {
            init();
        }
    }

    public String getAutoExpand() {
        return autoExpand;
    }

    /**
     * @return
     */
    public void init() {
        List<GWTColumn> columns = columnList == null ? new ArrayList<GWTColumn>() : new ArrayList<GWTColumn>(
                columnList);

        for (GWTColumn column : columns) {
            int i = column.getSize();
            if (i == -1) {
                autoExpand = column.getKey();
                i = 250;
            }
            ColumnConfig col = new ColumnConfig(column.getKey(), column.getTitle(), i);
            col.setResizable(column.isResizable());
            col.setSortable(column.isSortable());
            col.setMenuDisabled(true);
            if ("icon".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setSortable(false);
                col.setRenderer(ICON_RENDERER);
            } else if ("locked".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(LOCKED_RENDERER);
            } else if ("path".equals(column.getKey())) {
                col.setHidden(true);
            } else if ("pathVisible".equals(column.getKey())) {
                col.setId("path");
            } else if ("size".equals(column.getKey())) {
                col.setRenderer(SIZE_RENDERER);
            } else if ("publicationInfo".equals(column.getKey())) {
                col.setRenderer(PUBLICATION_RENDERER);
            } else if ("scmStatus".equals(column.getKey())) {
                col.setRenderer(SCM_STATUS_RENDERER);
            } else if ("quickPublicationInfo".equals(column.getKey())) {
                col.setRenderer(QUICK_PUBLICATION_RENDERER);
            } else if ("version".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(VERSION_RENDERER);
            } else if ("jcr:created".equals(column.getKey())) {
                col.setRenderer(DATE_RENDERER);
            } else if ("jcr:lastModified".equals(column.getKey())) {
                col.setRenderer(DATE_RENDERER);
            } else if ("index".equals(column.getKey())) {
                col.setHeaderHtml("");
                col.setResizable(false);
                col.setFixed(true);
                col.setId("numberer");
                col.setDataIndex("index");
            } else if ("name".equals(column.getKey()) || "displayName".equals(column.getKey())) {
                col.setRenderer(NAME_RENDERER);
            }
            add(col);
        }

    }

}
