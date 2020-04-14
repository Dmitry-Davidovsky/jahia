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
package org.jahia.ajax.gwt.client.widget.content.compare;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.ui.Frame;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.toolbar.action.PublishActionItem;

import java.util.Date;

/**
 *
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:32:54 AM
 */
public class VersionViewer extends ContentPanel {
    private Linker linker = null;
    private String locale;
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private final String uuid;
    private String workspace = "default";
    private Frame currentFrame;
    private CalendarField versionComboBox;
    private boolean addButtons;
    private Button restoreButton;
    private boolean displayHighLigthButton;
    private final CompareEngine compareEngine;
    private String versionLabel = null;
    private Date[] previousValue;
    private Date versionDate = null;

    /**
     * Constructor
     *
     * @param node
     * @param linker
     * @param addButtons
     */
    public VersionViewer(GWTJahiaNode node, String locale, Linker linker, String workspace, boolean addButtons,
                         boolean displayVersionSelector, CompareEngine compareEngine) {
        super();
        this.linker = linker;
        this.workspace = workspace;
        this.locale = locale;
        this.addButtons = addButtons;
        this.displayHighLigthButton = addButtons;
        this.compareEngine = compareEngine;
        this.uuid = node.getUUID();
        init(displayVersionSelector);
    }

    public VersionViewer(String uuid, String locale, String workspace, boolean displayHighLigthButton,
                         boolean displayVersionSelector, Date versionDate, CompareEngine compareEngine, String versionLabel) {
        super();
        this.uuid = uuid;
        this.workspace = workspace;
        this.locale = locale;
        this.displayHighLigthButton = displayHighLigthButton;
        this.versionDate = versionDate;
        this.compareEngine = compareEngine;
        this.versionLabel = versionLabel;
        init(displayVersionSelector);
    }

    /**
     * init component
     *
     * @param displayVersionSelector
     */
    private void init(final boolean displayVersionSelector) {

        final Label[] label = {null};

        label[0] = new Label(("live".equals(workspace) ? Messages.get("label.live.version", "Live version") : Messages.get("label.staging.version", "Staging version"))+ " ");

        // combo box that allows to select the version
        if (displayVersionSelector) {
            Date startDate = new Date();
            previousValue = new Date[]{startDate};
            versionComboBox = new CalendarField(startDate);
            versionComboBox.setWidth(150);
            versionComboBox.addListener(Events.Change, new Listener<FieldEvent>() {

                @Override
                public void handleEvent(FieldEvent be) {
                    if (!be.getField().getValue().equals(previousValue[0])) {
                        previousValue[0] = (Date) be.getField().getValue();
                        refresh();
                    }
                }
            });
        } else {
            previousValue = new Date[]{versionDate};
        }

        if (versionDate != null) {
            restoreButton = new Button(Messages.get("label.restore", "Restore"));
            restoreButton.addStyleName("button-restore");
            restoreButton.setEnabled(false);
            restoreButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    mask(Messages.get("label.restoring", "Restoring") + "...", "x-mask-loading");
                    contentService.restoreNodeByIdentifierAndDate(uuid, versionDate, (versionLabel != null && !versionLabel.contains("live") ? versionLabel : null), false,
                            new BaseAsyncCallback<Void>() {

                                @Override
                                public void onSuccess(Void result) {
                                    unmask();
                                    compareEngine.setRefreshOpener(true);
                                    if (compareEngine.getRightPanel() != null) {
                                        compareEngine.getRightPanel().refresh();
                                    }
                                }
                            });
                }
            });
        }

        final Button refresh = new Button();
        refresh.setIcon(StandardIconsProvider.STANDARD_ICONS.refresh());
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent componentEvent) {
                refresh();
            }
        });

        final ToggleButton hButton = new ToggleButton(Messages.get("label.highlight", "Highligthing"));
        hButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent componentEvent) {
                if (hButton.isPressed()) {
                    displayHighLigth();
                } else {
                    refresh();
                }
            }
        });

        // case of preview or edit: no version
        if (addButtons && !workspace.equals("live")) {
            // add in the toolbar
            ToolBar headerToolBar = new ToolBar();
            headerToolBar.add(label[0]);
            if (displayVersionSelector) {
                headerToolBar.add(versionComboBox);
            }
            headerToolBar.add(refresh);
            headerToolBar.add(hButton);
            if (displayVersionSelector) {
                headerToolBar.add(restoreButton);
            }

            GWTJahiaToolbarItem gwtJahiaToolbarItem = new GWTJahiaToolbarItem();
            PublishActionItem actionItem = new PublishActionItem();
            gwtJahiaToolbarItem.setId("VersionViewer.Toolbar.Item.Publish");
            gwtJahiaToolbarItem.setActionItem(actionItem);
            gwtJahiaToolbarItem.setTitle(Messages.get("label.publish"));
            gwtJahiaToolbarItem.setLayout(Constants.LAYOUT_BUTTON_LABEL);
            gwtJahiaToolbarItem.setDisplayTitle(true);
            gwtJahiaToolbarItem.setIcon("publish");
            actionItem.init(gwtJahiaToolbarItem, linker);
            headerToolBar.add(actionItem.getTextToolItem());
            actionItem.handleNewLinkerSelection();

            gwtJahiaToolbarItem = new GWTJahiaToolbarItem();
            actionItem = new PublishActionItem();
            actionItem.setAllSubTree(true);
            gwtJahiaToolbarItem.setId("VersionViewer.Toolbar.Item.PublishAll");
            gwtJahiaToolbarItem.setActionItem(actionItem);
            gwtJahiaToolbarItem.setTitle(Messages.get("label.publishall"));
            gwtJahiaToolbarItem.setLayout(Constants.LAYOUT_BUTTON_LABEL);
            gwtJahiaToolbarItem.setDisplayTitle(true);
            gwtJahiaToolbarItem.setIcon("publishAll");
            actionItem.init(gwtJahiaToolbarItem, linker);
            headerToolBar.add(actionItem.getTextToolItem());
            actionItem.handleNewLinkerSelection();

            setTopComponent(headerToolBar);
        } else {

            // case of th live mode
            ToolBar headerToolBar = new ToolBar();
            headerToolBar.add(label[0]);
            if (displayVersionSelector) {
                headerToolBar.add(versionComboBox);
            }
            headerToolBar.add(refresh);
            if (displayHighLigthButton) {
                headerToolBar.add(hButton);
            }
            if (versionDate != null) {
                headerToolBar.add(restoreButton);
            }
            setTopComponent(headerToolBar);
        }
        load();
    }


    /**
     * refresh
     */
    protected void refresh() {
        load();
    }

    /**
     * Render widget
     */
    private void load() {
        if (uuid != null) {
            mask(Messages.get("label.loading", "Loading"), "x-mask-loading");
            // version is not specified. Current.
            contentService.getNodeURLByIdentifier(null, uuid, previousValue[0], null, workspace, locale,
                    new BaseAsyncCallback<String>() {

                        @Override
                        public void onSuccess(String url) {
                            currentFrame = setUrl(url);
                            setHeadingHtml(url);
                            unmask();
                            if (restoreButton != null) {
                                restoreButton.setEnabled(true);
                            }
                        }

                        @Override
                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("", throwable);
                            unmask();
                        }
                    });

        }
    }

    /**
     * Get html
     *
     * @return
     */
    public String getInnerHTML() {
        IFrameElement frameElement = IFrameElement.as(currentFrame.getElement());
        Document document = frameElement.getContentDocument();
        BodyElement ele = document.getBody();
        if (ele != null) {
            return ele.getInnerHTML();
        }

        // it may happens if the iframe is not yet loaded
        return null;

    }

    /**
     * Compare version
     */
    public void displayHighLigth() {
        contentService.getHighlighted(getInnerHTML(), getCompareWith(), new BaseAsyncCallback<String>() {

            @Override
            public void onSuccess(String s) {
                IFrameElement frameElement = IFrameElement.as(currentFrame.getElement());
                Document document = frameElement.getContentDocument();
                BodyElement ele = document.getBody();
                if (ele != null) {
                    ele.setInnerHTML(s);
                }
            }

            @Override
            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error when triing to display higthligthing", throwable);
            }
        });

    }


    /**
     * Override this method to compare with another html
     *
     * @return
     */
    public String getCompareWith() {
        return getInnerHTML();
    }


}
