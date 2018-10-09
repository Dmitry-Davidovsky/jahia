/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.List;

/**
 * User: rfelden
 * Date: 9 sept. 2008 - 17:49:57
 */
public class ThumbView extends AbstractView {

    private ContentPanel m_component;
    private ThumbsListView view;
    private ComboBox<ModelData> sort;
    private ToggleButton sortOrder ;

    public ThumbView(final GWTManagerConfiguration config, boolean detailed) {
        super(config);

        m_component = new ContentPanel(new FitLayout());
        m_component.setHeaderVisible(false);
        m_component.setScrollMode(Style.Scroll.AUTO);
        m_component.setId("images-view");
        m_component.setBorders(true);
        m_component.setBodyBorder(false);
        if (GXT.isIE) {
            m_component.setHeight(400); // media gallery fix
        }

        StoreFilterField<GWTJahiaNode> field = new StoreFilterField<GWTJahiaNode>() {
            @Override
            protected boolean doSelect(Store<GWTJahiaNode> store, GWTJahiaNode parent, GWTJahiaNode record, String property, String filter) {
                String name = record.getName().toLowerCase();
                return name.indexOf(filter.toLowerCase()) != -1;
            }

            @Override
            protected void onFilter() {
                super.onFilter();
//                view.getSelectionModel().select(0);
            }
        };
        field.setWidth(150);
        field.bind(store);

        ToolBar bar = new ToolBar();
        bar.add(new LabelToolItem(Messages.get("thumbFilter.label")));

        bar.add(field);
        bar.add(new SeparatorToolItem());
        bar.add(new LabelToolItem(Messages.get("thumbSort.label")));

        // please keep same order as in sort() method
        ListStore<ModelData> sorts = new ListStore<ModelData>();

        for (GWTColumn column : config.getTableColumns()) {
            if (column.isSortable()) {
                ModelData d = new BaseModelData();
                d.set("key", column.getKey());
                d.set("title", column.getTitle());
                sorts.add(d);
            }
        }

        sort = new ComboBox<ModelData>();
        sort.setDisplayField("title");
        sort.setTriggerAction(ComboBox.TriggerAction.ALL);
        sort.setEditable(false);
        sort.setForceSelection(true);
        sort.setStore(sorts);
        sort.setValue(sorts.getAt(0));
        sort.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent>() {
            public void handleEvent(SelectionChangedEvent be) {
                sort();
            }
        });
        sortOrder = new ToggleButton(Messages.get("invertSort.label"));
        sortOrder.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent componentEvent) {
                sort();
            }
        });

        bar.add(sort);
        bar.add(sortOrder);

        bar.add(new FillToolItem());

        final Slider slider = new Slider();
        slider.setMinValue(32);
        slider.setMaxValue(150);
        slider.setValue(100);
        slider.setWidth(50);
        slider.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                view.setSize(slider.getValue());
            }
        });
        bar.add(slider);

        m_component.setTopComponent(bar);

        view = new ThumbsListView(detailed, config.isUseLargeThumbnails());
        view.setStore(store);

        selectionModel = view.getSelectionModel();
        view.addListener(Events.DoubleClick, new Listener<ListViewEvent>() {
            public void handleEvent(ListViewEvent event) {
                List<GWTJahiaNode> selection = (List<GWTJahiaNode>) getLinker().getTableSelection();
                if (selection != null && selection.size() > 0) {
                    GWTJahiaNode selected = selection.get(0);
                    BottomRightComponent buttonBar = getLinker().getBottomRightObject();
                    if (buttonBar instanceof PickedContentView) {
                        ((PickedContentView) buttonBar).setSelection(selection);
                        ((PickedContentView) buttonBar).getSaveButton().fireEvent(Events.Select);
                        return;
                    }
                    if (selected.isFile()) {
                        if (config.isEnableFileDoubleClick()) {
                            if (selected.isDisplayable()) {
                                ImagePopup.popImage(selected, getLinker());
                            } else {
                                ContentActions.download(getLinker());
                            }
                        }
                    } else {
                        if (!config.isHideLeftPanel()) {
                            getLinker().onTableItemDoubleClicked(selected);
                        } else {
                            Log.debug("Double click disabled if the hide panel is hidden");
                        }
                    }
                }
            }
        });
        /*view.getSelectionModel().addListener(Events.OnDoubleClick, new Listener<SelectionEvent<GWTJahiaNode>>() {
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                getLinker().onTableItemDoubleClicked(be.model);
            }
        });*/
        m_component.add(view);
    }

    private void sort() {
        ModelData col = sort.getValue();
        store.sort((String) col.get("key"), sortOrder.isPressed() ? Style.SortDir.DESC : Style.SortDir.ASC);
    }

    public void setContextMenu(Menu menu) {
        view.setContextMenu(menu);
    }

    public void setProcessedContent(Object content, ContentSource source) {
        super.setProcessedContent(content, source);
        if (content != null) {
            sort();
        }
    }

    public Component getComponent() {
        return m_component;
    }

}
