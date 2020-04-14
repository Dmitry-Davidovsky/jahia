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
package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreatePortletInitBean;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.allen_sauer.gwt.log.client.Log;

import java.util.*;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:06
 */
public class PortletFormCard extends PortletWizardCard {
    private PropertiesEditor pe;

    public PortletFormCard() {
        super(Messages.get("label.parameters","Parameters"), Messages.get("org.jahia.engines.PortletsManager.wizard.parameters.edit.label","Edit parameters"));
    }

    public void createUI() {
        removeAll();
        super.createUI();        
        createUIAsync();

    }

    public void next() {
        Log.debug("update properties");

        if (pe != null) {
            getGwtJahiaNewPortletInstance().setProperties(pe.getProperties());
        }
    }

    // laod form asyn
    private void createUIAsync() {
        JahiaContentManagementService.App.getInstance().initializeCreatePortletEngine(getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getPortletType(), getParentNode().getPath(), new BaseAsyncCallback<GWTJahiaCreatePortletInitBean>() {
            public void onSuccess(GWTJahiaCreatePortletInitBean result) {
                List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>(1);
                list.add(result.getNodeType());
                Map<String, GWTJahiaNodeProperty> defaultValues = new HashMap<String, GWTJahiaNodeProperty>();

                GWTJahiaPortletDefinition definition = getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition();
                GWTJahiaNodePropertyValue value = new GWTJahiaNodePropertyValue(definition.getContextName()+"!"+definition.getDefinitionName(), GWTJahiaNodePropertyType.STRING);
                defaultValues.put("j:definition", new GWTJahiaNodeProperty("j:definition", value));
                if (definition.getExpirationTime() != null) {
                    value = new GWTJahiaNodePropertyValue("" + definition.getExpirationTime(), GWTJahiaNodePropertyType.LONG);
                    defaultValues.put("j:expirationTime", new GWTJahiaNodeProperty("j:expirationTime", value));
                }
                if (definition.getCacheScope() != null) {
                    value = new GWTJahiaNodePropertyValue(definition.getCacheScope(), GWTJahiaNodePropertyType.STRING);
                    defaultValues.put("j:cacheScope", new GWTJahiaNodeProperty("j:cacheScope", value));
                }

                pe = new PropertiesEditor(list, defaultValues, Arrays.asList(GWTJahiaItemDefinition.CONTENT));
                pe.setMixin(result.getMixin());
                pe.setExcludedTypes(Arrays.asList("jnt:portlet", "mix:lastModified", "mix:created", "jmix:lastPublished"));
                pe.renderNewFormPanel();
                pe.setChoiceListInitializersValues(result.getChoiceListInitializersValues());
                setFormPanel(pe);
                layout();
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("error", caught);
            }

        });
    }
}