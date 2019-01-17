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
package org.jahia.services.content.rules;

import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Background job that executes a predefined action. 
 */
public class ActionJob extends BackgroundJob {
	
    public static final String NAME_PREFIX = "ACTION_JOB_"; 
    
    private static transient Logger logger = LoggerFactory.getLogger(ActionJob.class);

    public static final String JOB_ACTION_TO_EXECUTE = "actionToExecute";
    public static final String JOB_NODE_UUID = "node";
    public static final String JOB_WORKSPACE = "workspace";
    
    public static final String getJobGroup(String actionName) {
    	return BackgroundJob.getGroupName(ActionJob.class) + "." + actionName;
    }

    public static final String getJobName(String actionName, String nodeIdentifier) {
    	return actionName + "-" + nodeIdentifier;
    }

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            String actionName = map.getString(JOB_ACTION_TO_EXECUTE);
			final BackgroundAction action = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getBackgroundActions().get(
                    actionName);
            if (action != null) {
                BackgroundAction backgroundAction = (BackgroundAction) action;
                final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
                final JCRSessionWrapper jcrSessionWrapper = sessionFactory.getCurrentUserSession(map.getString(ActionJob.JOB_WORKSPACE),
                        map.getString(ActionJob.JOB_CURRENT_LOCALE) != null ? LanguageCodeConverters.getLocaleFromCode(map.getString(ActionJob.JOB_CURRENT_LOCALE)): null);
                try {
                    JCRNodeWrapper node = jcrSessionWrapper.getNodeByUUID(map.getString(JOB_NODE_UUID));
                    backgroundAction.executeBackgroundAction(node);
                } catch (ItemNotFoundException e) {
                    logger.warn("The node with UUID {} cannot be found in the repository. Skip executing background action.", map.getString(JOB_NODE_UUID));
                    throw new JobExecutionException(e);
                }
            } else {
                throw new JobExecutionException("Background action with the name " + actionName + " is not found in the registry."
                        + " Skip executing action.");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}