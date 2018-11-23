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
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.*;

/**
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:55:28 PM
 *
 */
public class CreateContentEngine extends AbstractContentEngine {

    protected GWTJahiaNodeType type = null;
    protected String targetName = null;
    protected boolean createInParentAndMoveBefore = false;

    private int childCount;
    private int listLimit;

    /**
     * Open Edit content engine for a new node creation
     *  @param configuration
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param props                       initial values for properties
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public CreateContentEngine(GWTEngineConfiguration configuration, Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore, EngineContainer engineContainer, boolean skipRefreshOnSave) {
        super(configuration, linker, createInParentAndMoveBefore ? parent.getPath().substring(0, parent.getPath().lastIndexOf('/')) : parent.getPath(), skipRefreshOnSave);
        this.existingNode = false;
        this.targetNode = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;
        this.setId("JahiaGxtCreateContentEngine");
        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        presetProperties = new HashMap<String, GWTJahiaNodeProperty>(props);
        heading = Messages.get("label.add", "Add") + ": " + type.getLabel();
        init(engineContainer);
        addStyleName("create-content-engine");
        loadEngine();

    }

    @Override
    public void close() {
        super.close();
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : config.getEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.showInEngine() && (tabConfig.getRequiredPermission() == null ||
                    PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), JahiaGWTParameters.getSiteNode()))) {
                if (tabItem.isHandleCreate() &&
                        (tabItem.getHideForTypes().isEmpty() || !tabItem.getHideForTypes().contains(type.getName())) &&
                        (tabItem.getShowForTypes().isEmpty() || tabItem.getShowForTypes().contains(type.getName()))) {
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
        for (ButtonItem buttonItem : config.getCreationButtons()) {
            BoxComponent button = buttonItem.create(this);
            buttons.add(button);
            buttonBar.add(button);
        }
        for (ButtonItem buttonItem : config.getCommonButtons()) {
            buttonBar.add(buttonItem.create(this));
        }

        List<Module> parentModules = ModuleHelper.getModulesByPath() != null ? ModuleHelper.getModulesByPath().get(parentPath) : null;
        if (parentModules != null && parentModules.size() == 1) {
            Module module = parentModules.get(0);
            childCount = module.getChildCount();
            listLimit = module.getListLimit();
        } else {
            childCount = 0;
            listLimit = -1;
        }

        setButtonsEnabled(false);
    }

    /**
     * on language chnage, fill currentAzble
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
        fillCurrentTab();
    }


    /**
     * load mixin
     */
    private void loadEngine() {
        JahiaContentManagementService.App.getInstance().initializeCreateEngine(nodeTypes.iterator().next().getName(), parentPath, targetName, new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {

            @Override
            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                if (closed) {
                    return;
                }
                mixin = result.getMixin();
                choiceListInitializersValues = result.getChoiceListInitializersValues();
                defaultValues = result.getDefaultValues();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                acl = result.getAcl();
                final List<GWTJahiaLanguage> languages = result.getLanguages();
                setAvailableLanguages(languages);
                setButtonsEnabled(true);

                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {

                    @Override
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                nodeName = result.getDefaultName();

                fillCurrentTab();
                updateWipControls();

                loaded();
            }

            @Override
            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to load avalibale mixin", caught);
            }
        });
    }

    @Override
    public void setButtonsEnabled(final boolean enabled) {
        for (BoxComponent button : buttons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    protected void prepareSave() {
        cleanPrepareSaveData();

        getNewNodeACL().setAce(new ArrayList<GWTJahiaNodeACE>());
        for (TabItem tab : this.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    nodeName = ((ContentTabItem) item).getName().getValue();
                }
                final List<CheckBox> values = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (values != null) {
                    // Checkboxes are not null so they are displayed, if list is empty this means that this
                    // content is not visible in any language
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    if (values.size() != siteLanguages.size()) {
                        List<String> strings = new ArrayList<String>(siteLanguages.size());
                        for (GWTJahiaLanguage siteLanguage : siteLanguages) {
                            strings.add(siteLanguage.getLanguage());
                        }
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName("j:invalidLanguages");
                        gwtJahiaNodeProperty.setMultiple(true);
                        for (CheckBox value : values) {
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
                        }
                    }
                }
            }
            item.doSave(this.getNode(), this.getChangedProperties(), this.getChangedI18NProperties(), getAddedTypes(),
                    new HashSet<String>(), getChildren(), getNewNodeACL());
        }
    }

    @Override
    protected void cancelAndClose() {
        confirmCancel();
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isCreateInParentAndMoveBefore() {
        return createInParentAndMoveBefore;
    }

    public GWTJahiaNodeType getType() {
        return type;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getListLimit() {
        return listLimit;
    }

    @Override
    protected boolean isNodeOfJmixLastPublishedType() {
        return super.isNodeOfJmixLastPublishedType() || this.getType().getSuperTypes().contains("jmix:lastPublished");
    }
}
