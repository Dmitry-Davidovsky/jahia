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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

/**
 * File and folder picker control.
 *
 * @author rfelden
 */
public class ContentPickerViewport extends Viewport {
    private PickedContentView pickedContent;
    public static final int BUTTON_HEIGHT = 24;


    public ContentPickerViewport(final String jahiaContextPath, final String jahiaServletPath, final String filesServletPath,
                                 final String selectionLabel, final Map<String, String> selectorOptions, final List<GWTJahiaNode> selectedNodes,
                                 final List<String> filters, final List<String> mimeTypes, final GWTManagerConfiguration config,
                                 final boolean multiple, final String callback) {
        setLayout(new FitLayout());
        final ContentPicker picker =
                new ContentPicker(selectorOptions, selectedNodes, null, filters, mimeTypes, config, multiple);

        // buttom component
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button ok = new Button(Messages.get("label.save"));
        ok.addStyleName("button-save");
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                List<String[]> selectedNodes = getSelectedNodePathes(jahiaContextPath, jahiaServletPath, filesServletPath);
                if (selectedNodes != null && !selectedNodes.isEmpty()) {
                    callback(callback, selectedNodes.get(0)[0], selectedNodes.get(0)[1]);
                    WindowUtil.close();
                }
            }
        });

        buttonBar.add(ok);

        if (selectedNodes == null || selectedNodes.size() ==0) {
            ok.setEnabled(false);
        }
        picker.setSaveButton(ok);

        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                WindowUtil.close();
            }
        });
        cancel.addStyleName("button-cancel");
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        final Component bar = buttonsPanel;
        picker.setBottomComponent(bar);

        add(picker);

        pickedContent = (PickedContentView) picker.getLinker().getBottomRightObject();
    }

    /**
     * Get selected node
     *
     * @return
     */
    public List<GWTJahiaNode> getSelectedNodes() {
        return pickedContent.getSelectedContent();
    }

    /**
     * Get selectedNode pathes
     *
     * @return
     */
    public List<String[]> getSelectedNodePathes(final String jahiaContextPath, final String jahiaServletPath,
                                              final String filesServletPath) {
        return pickedContent.getSelectedContentPath(jahiaContextPath,jahiaServletPath, filesServletPath);
    }

    private native void callback(String callback, String url, String title)/*-{
        if (typeof title == 'undefined') {
            $wnd.opener.CKEDITOR.tools.callFunction(callback, url, "");
        } else {
            $wnd.opener.CKEDITOR.tools.callFunction(callback, url, function () {
                var dlg = this.getDialog();
                var trgt = this.filebrowser.target || null;
                if (trgt) {
                    var advTitle = trgt == 'info:url' ? dlg.getContentElement('info', 'advTitle') : (trgt == 'info:txtUrl' ? dlg.getContentElement('info', 'txtAlt') : null);
                    if (advTitle) {
                        advTitle.setValue(title);
                    }
                }
                return true;
            });
        }
      }-*/;


}