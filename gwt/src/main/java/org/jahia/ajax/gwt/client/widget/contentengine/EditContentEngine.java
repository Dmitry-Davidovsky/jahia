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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode.WipStatus;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jahia.ajax.gwt.client.widget.contentengine.SeoTabItem.VANITY_MAPPINGS;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends AbstractContentEngine {

    private String contentPath;
    private HandlerRegistration handlerRegistration;

    private Map<String, GWTJahiaGetPropertiesResult> langCodeGWTJahiaGetPropertiesResultMap =
            new HashMap<String, GWTJahiaGetPropertiesResult>();
    private boolean hasOrderableChildNodes;

    private int originalAclHash;

    /**
     * Initializes an instance of this class.
     *  @param configuration
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTEngineConfiguration configuration, GWTJahiaNode node, Linker linker, EngineContainer engineContainer, boolean skipRefreshOnSave) {
        super(configuration, linker, node.getPath().substring(0, node.getPath().lastIndexOf('/')), skipRefreshOnSave);
        contentPath = node.getPath();
        nodeName = node.getName();
        this.node = node;
        init(engineContainer);
        loadEngine();

        addStyleName("edit-content-engine");

        handlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {

            @Override
            public void onClose(CloseEvent<Window> event) {
                close();
            }
        });
        //setTopComponent(toolBar);
    }

    @Override
    public void close() {
        super.close();
        JahiaContentManagementService.App.getInstance().closeEditEngine(contentPath, new BaseAsyncCallback<Object>() {

            @Override
            public void onSuccess(Object result) {
            }
        });
        closeEngine();
    }

    protected void closeEngine() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }

        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        // container ID, concatenated to each tab's ID
        tabs.setId("JahiaGxtEditEngineTabs");
        for (GWTEngineTab tabConfig : config.getEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.showInEngine() && (tabConfig.getRequiredPermission() == null || PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), JahiaGWTParameters.getSiteNode()))) {
                if ((tabItem.getHideForTypes().isEmpty() || !node.isNodeType(tabItem.getHideForTypes())) &&
                        ((hasOrderableChildNodes && tabItem.isOrderableTab()) || (!tabItem.isOrderableTab() && (tabItem.getShowForTypes().isEmpty() || node.isNodeType(tabItem.getShowForTypes()))))) {
                    tabs.add(tabItem.create(tabConfig, this));
                }
            }
        }
        tabs.setSelection(tabs.getItem(0));
    }


    /**
     * init buttons
     */
    @Override
    protected void initFooter() {
        for (ButtonItem buttonItem : config.getEditionButtons()) {
            BoxComponent button = buttonItem.create(this);
            buttons.add(button);
            buttonBar.add(button);
        }
        for (ButtonItem buttonItem : config.getCommonButtons()) {
            buttonBar.add(buttonItem.create(this));
        }

        setButtonsEnabled(false);
    }

    /**
     * load node
     */
    private void loadProperties() {
        JahiaContentManagementService.App.getInstance().getProperties(contentPath, getSelectedLanguage(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {

            @Override
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            @Override
            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                fillCurrentTab();
            }
        });

    }

    /**
     * load node
     */
    private void loadEngine() {

        JahiaContentManagementService.App.getInstance().initializeEditEngine(contentPath, true, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {

            @Override
            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                if (closed) {
                    return;
                }
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                hasOrderableChildNodes = result.hasOrderableChildNodes();
                langCodeGWTJahiaGetPropertiesResultMap.put(currentLanguageBean.getLanguage(), result);
                acl = result.getAcl();
                originalAclHash = result.getAcl().hashCode();
                referencesWarnings = result.getReferencesWarnings();
                if (!PermissionsUtils.isPermitted("jcr:modifyProperties", node)) {
                    heading = Messages.getWithArgs("label.edit.engine.heading.read.only", "Read {0} ({1})", new String[]{nodeName, nodeTypes.get(0).getLabel()});
                } else {
                    heading = Messages.getWithArgs("label.edit.engine.heading.edit", "Edit {0} ({1})", new String[]{nodeName, nodeTypes.get(0).getLabel()});
                }
                Element headerElement = container.getPanel().getHeader().getElement();
                for (int i = 0; i < headerElement.getChildNodes().getLength(); i++) {
                    if (headerElement.getChild(i) instanceof Element) {
                        Element el = (Element) headerElement.getChild(i);
                        if (el.getClassName().contains("-header-text")) {
                            el.setAttribute("data-friendly-name", node.getDisplayName());
                            Boolean isLocked = node.isLocked() || node.getLockInfos() != null && !node.getLockInfos().isEmpty();
                            el.setAttribute("data-content-locked", isLocked.toString());
                        }
                    }
                }
                container.getPanel().setHeadingHtml(heading);
                if (node.isLocked()) {
                    StringBuilder infos = new StringBuilder();
                    if (node.getLockInfos().containsKey(null) && node.getLockInfos().size() == 1) {
                        for (String s : node.getLockInfos().get(null)) {
                            infos.append(Formatter.getLockLabel(s));
                        }
                    } else {
                        for (Map.Entry<String, List<String>> entry : node.getLockInfos().entrySet()) {
                            if (entry.getKey() != null) {
                                if (infos.length() > 0) {
                                    infos.append("; ");
                                }
                                infos.append(entry.getKey()).append(" : ");
                                int i = 0;
                                for (String s : entry.getValue()) {
                                    if (i > 0) {
                                        infos.append(", ");
                                    }
                                    infos.append(Formatter.getLockLabel(s));
                                    i++;
                                }
                            }
                        }
                    }
                    heading = heading + "&nbsp;" + Messages.getWithArgs("label.edit.engine.heading.locked.by", "[ locked by {0} ]", new String[]{infos.toString()});
                    container.getPanel().setHeadingHtml(heading);
                } else if (node.getLockInfos() != null && !node.getLockInfos().isEmpty()) {
                    heading = heading + "&nbsp;" + Messages.get("label.edit.engine.heading.locked.by.you", "[ locked by you ]");
                    container.getPanel().setHeadingHtml(heading);
                }

                if (node.getWorkInProgressStatus() != null) {
                    WipStatus status = WipStatus.valueOf(node.getWorkInProgressStatus());
                    // set languages
                    if (node.getWorkInProgressLanguages() != null) {
                        for (String lang : node.getWorkInProgressLanguages()) {
                            for (GWTJahiaLanguage l : result.getAvailabledLanguages()) {
                                if (l.getLanguage().equals(lang)) {
                                    workInProgressLanguages.add(lang);
                                }
                            }
                        }
                    }
                    // if no language set and the state is LANGUAGES, we change it to DISABLED
                    wipStatus = status == WipStatus.LANGUAGES && workInProgressLanguages.isEmpty() ? WipStatus.DISABLED : status;

                    // update button
                    updateWipControls();
                }
                setAvailableLanguages(result.getAvailabledLanguages());

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                mixin = result.getMixin();
                choiceListInitializersValues = result.getInitializersValues();
                defaultValues = result.getDefaultValues();
                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {

                    @Override
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                fillCurrentTab();

                if (PermissionsUtils.isPermitted("jcr:modifyProperties", node) && !node.isLocked()) {
                    setButtonsEnabled(true);
                }

                loaded();
            }

            @Override
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
                Info.display(Messages.get("label.error", "Error"), throwable.getLocalizedMessage());
                closeEngine();
            }

            @Override
            public void onSessionExpired() {
                closeEngine();
            }
        });

    }

    /**
     * on language chnage, reload the node
     *
     * @param previous
     */
    @Override
    protected void onLanguageChange(GWTJahiaLanguage previous) {
        if (previous != null) {
            final String lang = previous.getLanguage();
            for (TabItem item : tabs.getItems()) {
                if (!changedI18NProperties.containsKey(lang)) {
                    changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                }
                Object itemData = item.getData("item");
                if (itemData instanceof EditEngineTabItem) {
                    ((EditEngineTabItem) itemData).onLanguageChange(getSelectedLanguage(), item);
                }
            }
        }
        GWTJahiaGetPropertiesResult result = langCodeGWTJahiaGetPropertiesResultMap.get(getSelectedLanguage());
        if (result == null) {
            loadProperties();
        } else {
            node = result.getNode();
            nodeTypes = result.getNodeTypes();
            properties = result.getProperties();
            currentLanguageBean = result.getCurrentLocale();
            fillCurrentTab();
        }
    }

    @Override
    public void setButtonsEnabled(final boolean enabled) {
        for (BoxComponent button : buttons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public String toString() {
        return node.getPath();
    }

    // return true if modifications has been done in the edit engine / and the opposite

    @Override
    protected void cancelAndClose() {
        if (hasChanges()) {
            confirmCancel();
        } else {
            close();
        }
    }

    @Override
    protected void prepareSave() {
        cleanPrepareSaveData();

        for (TabItem tab : getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            // case of contentTabItem
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    Field<String> name = ((ContentTabItem) item).getName();
                    setNodeName(name.getValue());
                    getNode().setName(getNodeName());
                }
                final List<CheckBox> validLanguagesChecked = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (validLanguagesChecked != null) {
                    // Checkboxes are not null so they are displayed, if list is empty this means that this
                    // content is not visible in any language
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    List<String> invalidLanguages = getNode().getInvalidLanguages();
                    List<String> newInvalidLanguages = new ArrayList<String>();
                    for (GWTJahiaLanguage language : siteLanguages) {
                        boolean found = false;
                        for (CheckBox validLang : validLanguagesChecked) {
                            if (language.getLanguage().equals(validLang.getValueAttribute())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newInvalidLanguages.add(language.getLanguage());
                        }
                    }
                    boolean hasChanged = newInvalidLanguages.size() != invalidLanguages.size();
                    if (!hasChanged) {
                        for (String lang : newInvalidLanguages) {
                            if (!invalidLanguages.contains(lang)) {
                                hasChanged = true;
                                break;
                            }
                        }
                    }
                    if (hasChanged) {
                        List<String> strings = new ArrayList<String>(siteLanguages.size());
                        for (GWTJahiaLanguage siteLanguage : siteLanguages) {
                            strings.add(siteLanguage.getLanguage());
                        }
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName("j:invalidLanguages");
                        gwtJahiaNodeProperty.setMultiple(true);
                        for (CheckBox value : validLanguagesChecked) {
                            if (value.getValue()) {
                                strings.remove(value.getValueAttribute());
                            }
                        }
                        if (strings.size() > 0) {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            for (String string : strings) {
                                gwtJahiaNodeProperty.getValues().add(new GWTJahiaNodePropertyValue(string));
                            }
                        }
                        final List<GWTJahiaNodePropertyValue> gwtJahiaNodePropertyValues = gwtJahiaNodeProperty.getValues();
                        if (gwtJahiaNodePropertyValues != null && gwtJahiaNodePropertyValues.size() > 0) {
                            getChangedProperties().add(gwtJahiaNodeProperty);
                            getAddedTypes().add("jmix:i18n");
                        } else {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            getChangedProperties().add(gwtJahiaNodeProperty);
                        }
                    }
                }
            }

            // case of right tab
            item.doSave(getNode(), getChangedProperties(), getChangedI18NProperties(), getAddedTypes(), getRemovedTypes(), null,
                    getAcl());
        }
    }

    private boolean hasChanges() {
        prepareSave();

        if (getChangedProperties().size() > 0 || originalAclHash != getAcl().hashCode()) {
            return true;
        }

        for (Map.Entry<String, List<GWTJahiaNodeProperty>> entry : getChangedI18NProperties().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return true;
            }
        }

        // Check system name changed
        if (!contentPath.endsWith("/" + getNodeName())) {
            return true;
        }

        // Check for mixin removal
        if (!getRemovedTypes().isEmpty()) {
            return true;
        }

        // Check SEO
        if (node.get(VANITY_MAPPINGS) != null) {
            return true;
        }
        return false;
    }

}
