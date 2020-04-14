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
package org.jahia.registries;

import org.apache.commons.collections.FastHashMap;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.applications.DispatchingService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.mail.MailService;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.query.QueryService;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.SearchService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * The ServicesRegistry class that give a unique access point to Jahia Services.
 * Services are instantiated and put in an Map.<br>
 * Services are loaded from properties file.<br>
 *
 * @author Eric Vassalli
 * @author Khue Nguyen
 * @author Fulco Houkes
 */
public class ServicesRegistry {

    private transient static Logger logger = LoggerFactory.getLogger(ServicesRegistry.class);
    /**
     * It's a Singleton
     */
    private static ServicesRegistry theObject = new ServicesRegistry();

    // This map is an optimization to avoid synchronization issues.
    private FastHashMap servicesCache;

    {
    	 servicesCache = new FastHashMap();
    	 servicesCache.setFast(true);
    }

    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static ServicesRegistry getInstance() {
        return theObject;
    }

    /**
     * Initialize the Services registry.
     *
     * @throws JahiaException when a service could not be loaded and instantiated.
     */
    public void init() throws JahiaException {
        getServiceInstances();
    }

    public Collection<? extends JahiaService> getServiceInstances() {
        return SpringContextSingleton.getInstance().getContext().getBeansOfType(JahiaService.class).values();
    }

    // @author NK 21.12.2000
    /**
     * Retrieve the service instance associated to the service name <code>serviceName</code>.
     *
     * @param serviceName the service name
     * @return the service instance
     */
    public JahiaService getService(String serviceName) {
        JahiaService jahiaService = (JahiaService) servicesCache.get(serviceName);
        if (jahiaService != null) {
            return jahiaService;
        }
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        if (context != null) {
            jahiaService = (JahiaService) context.getBean(serviceName);
            servicesCache.put(serviceName, jahiaService);
        } else {
            logger.warn("Application context is not (yet) available when trying to retrieve service " + serviceName
                    + ", will return null !");
        }
        return jahiaService;
    } // end getService

    // @author NK 21.12.2000
    public DispatchingService getApplicationsDispatchService() {
        return (DispatchingService) getService(
                "DispatchingService");
    }

    /**
     * Return a reference on the DB User Manager service
     */
    public JahiaUserManagerService getJahiaUserManagerService() {
        return (JahiaUserManagerService) getService("JahiaUserManagerService");
    }

    /**
     * Return a reference on the DB User Manager service
     */
    public JahiaGroupManagerService getJahiaGroupManagerService() {
        return (JahiaGroupManagerService) getService("JahiaGroupManagerService");
    }

    /**
     * NK 12.01.2001
     */
    public JahiaFileWatcherService getJahiaFileWatcherService() {
        return (JahiaFileWatcherService) getService("JahiaFileWatcherService");
    }

    /**
     * NK 13.02.2001
     */
    public ApplicationsManagerService getApplicationsManagerService() {
        return (ApplicationsManagerService) getService(
                "ApplicationsManagerService");
    }

    /**
     * NK 12.03.2001
     */
    public JahiaSitesService getJahiaSitesService() {
        return (JahiaSitesService) getService("JahiaSitesService");
    }

    /**
     * Return a reference to the Jahia Cache Factory service
     */
    public CacheService getCacheService() {
        return (CacheService) getService("JahiaCacheService");
    }

    public MailService getMailService() {
        return (MailService) getService("MailService");
    }

    public CategoryService getCategoryService() {
        return (CategoryService) getService("CategoryService");
    }

    public SchedulerService getSchedulerService() {
        return (SchedulerService) getService("SchedulerService");
    }

    public JCRStoreService getJCRStoreService() {
        return (JCRStoreService) getService("JCRStoreService");
    }

    public JCRPublicationService getJCRPublicationService() {
        return (JCRPublicationService) getService("jcrPublicationService");
    }

    public JCRVersionService getJCRVersionService() {
        return (JCRVersionService) getService("jcrVersionService");
    }

    public ImportExportBaseService getImportExportService() {
        return (ImportExportBaseService) getService("ImportExportService");
    }

    public JahiaPreferencesService getJahiaPreferencesService() {
        return (JahiaPreferencesService) getService("JahiaPreferencesService");
    }

    public JahiaPasswordPolicyService getJahiaPasswordPolicyService() {
        return (JahiaPasswordPolicyService) getService("JahiaPasswordPolicyService");
    }

    public JahiaTemplateManagerService getJahiaTemplateManagerService() {
        return (JahiaTemplateManagerService) getService("JahiaTemplateManagerService");
    }

    public QueryService getQueryService() {
        return (QueryService) getService("QueryService");
    }

    public SearchService getSearchService() {
        return (SearchService) getService("SearchService");
    }

    /**
     * Default constructor, creates a new <code>ServiceRegistry</code> instance.
     */
    private ServicesRegistry() {
        super();
    }

}
