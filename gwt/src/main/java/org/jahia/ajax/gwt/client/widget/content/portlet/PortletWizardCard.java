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

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;

/**
 * User: ktlili
 * Date: 4 d�c. 2008
 * Time: 11:09:55
 */
public abstract class PortletWizardCard extends WizardCard {

    public PortletWizardCard(String cardtitle, String text) {
        super(cardtitle, text);
        setLayout(new FitLayout());
    }

    public PortletWizardWindow getPortletWizardWindow() {
        return (PortletWizardWindow) getWizardWindow();
    }

    public GWTJahiaNewPortletInstance getGwtJahiaNewPortletInstance() {
        if (getPortletWizardWindow() == null) {
            return null;
        }
        return getPortletWizardWindow().getGwtJahiaNewPortletInstance();
    }

    public void setGwtPortletInstanceWizard(GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance) {
        if (getPortletWizardWindow() == null) {
            return;
        }
        getPortletWizardWindow().setGwtPortletInstanceWizard(gwtJahiaNewPortletInstance);
    }

    public String getJahiNodeType() {
        if (getGwtJahiaNewPortletInstance() == null) {
            return null;
        }
        return getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getPortletType();
    }

    public Linker getLinker() {
        if (getPortletWizardWindow() == null) {
            return null;
        }
        return getPortletWizardWindow().getLinker();
    }

    public GWTJahiaNode getParentNode() {
        if (getPortletWizardWindow() == null) {
            return null;
        }
        return getPortletWizardWindow().getParentNode();
    }

    public void updateHtmlText() {
        String name = "";
        if (getGwtJahiaNewPortletInstance() != null && getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition() != null) {
            name = getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getDisplayName();

        }
        super.setHtmlText("[" + name + "] " + getHtmltext());
    }

    @Override
    public void createUI() {
        updateHtmlText(); 
    }
    
}