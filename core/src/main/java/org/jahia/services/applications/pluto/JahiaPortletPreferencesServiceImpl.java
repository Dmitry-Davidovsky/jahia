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
package org.jahia.services.applications.pluto;
import org.apache.pluto.container.PortletPreference;
import org.apache.pluto.container.impl.PortletPreferenceImpl;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.driver.container.DefaultPortletPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesQueryHelper;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.usermanager.JahiaUser;
import javax.portlet.PortletRequest;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * 
 * User: loom
 * Date: Nov 18, 2008
 * Time: 2:24:14 PM
 */
public class JahiaPortletPreferencesServiceImpl extends DefaultPortletPreferencesService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPortletPreferencesServiceImpl.class);

    private JahiaPreferencesService jahiaPreferencesService;

    public JahiaPreferencesService getJahiaPreferencesService() {
        return jahiaPreferencesService;
    }

    public void setJahiaPreferencesService(JahiaPreferencesService jahiaPreferencesService) {
        this.jahiaPreferencesService = jahiaPreferencesService;
    }


    /**
     * Returns the stored portlet preferences array. The preferences managed by
     * this service should be protected from being directly accessed, so this
     * method returns a cloned copy of the stored preferences.
     *
     * @param portletWindow the portlet window.
     * @param request       the portlet request from which the remote user is retrieved.
     * @return a copy of the stored portlet preferences array.
     * @throws PortletContainerException
     */
    public Map<String,PortletPreference> getStoredPreferences(PortletWindow portletWindow, PortletRequest request) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            JahiaUser jahiaUser = (JahiaUser) request.getUserPrincipal();
            String portletName = portletWindow.getPortletDefinition().getApplication().getContextPath() + "." + portletWindow.getPortletDefinition().getPortletName();
            List<JahiaPreference> foundPreferences = portletPreferenceProvider.findJahiaPreferences(jahiaUser, JahiaPreferencesQueryHelper.getPortletSQL(portletName));
            if (foundPreferences == null) {
                return  new HashMap<String,PortletPreference>();
            }
            Map<String,PortletPreference> portletPreferences = new HashMap<String,PortletPreference>();

            for (JahiaPreference currentPreference : foundPreferences) {
                JahiaPortletPreference curPortletPreference = (JahiaPortletPreference) currentPreference.getNode();
                PortletPreferenceImpl portletPreferenceImpl = new PortletPreferenceImpl(curPortletPreference.getPrefName(), curPortletPreference.getValues(), curPortletPreference.getReadOnly());
                portletPreferences.put(portletPreferenceImpl.getName(),portletPreferenceImpl);
            }
            return portletPreferences;
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Error while retrieving portlet preferences", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving portlet preferences", e);
        }
        return null;
    }

    /**
     * Stores the portlet preferences to the in-memory storage. This method
     * should be invoked after the portlet preferences are validated by the
     * preference validator (if defined).
     * <p>
     * The preferences managed by this service should be protected from being
     * directly accessed, so this method clones the passed-in preferences array
     * and saves it.
     * </p>
     *
     * @param portletWindow the portlet window
     * @param request       the portlet request from which the remote user is retrieved.
     * @param stringPortletPreferenceMap   the portlet preferences to store.
     * @throws PortletContainerException
     * @see javax.portlet.PortletPreferences#store()
     */
    public void store(PortletWindow portletWindow, PortletRequest request, Map<String,PortletPreference> stringPortletPreferenceMap) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            Collection<PortletPreference > preferences = stringPortletPreferenceMap.values();
            for (PortletPreference curPlutoPreference : preferences) {
                String portletName = portletWindow.getPortletDefinition().getApplication().getContextPath() + "." + portletWindow.getPortletDefinition().getPortletName();
                JahiaPreference portletPreference = portletPreferenceProvider.getJahiaPreference(request.getUserPrincipal(), JahiaPreferencesQueryHelper.getPortletSQL(portletName, curPlutoPreference.getName()));
                if (portletPreference == null) {
                    portletPreference = portletPreferenceProvider.createJahiaPreferenceNode(request.getUserPrincipal());
                    JahiaPortletPreference node = (JahiaPortletPreference) portletPreference.getNode();
                    node.setPortletName(portletName);
                    node.setPrefName(curPlutoPreference.getName());
                } else {
                    // if values == null then delete the corresponding preference
                    if (curPlutoPreference.getValues() == null) {
                        portletPreferenceProvider.deleteJahiaPreference(portletPreference);
                    }
                }
                // if values == null the pref is not saved
                if (curPlutoPreference.getValues() != null) {
                    JahiaPortletPreference node = (JahiaPortletPreference) portletPreference.getNode();
                    node.setReadOnly(curPlutoPreference.isReadOnly());
                    node.setValues(curPlutoPreference.getValues());
                    portletPreferenceProvider.setJahiaPreference(portletPreference);
                }
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Error while storing portlet preferences", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving portlet preferences", e);
        }
    }
}
