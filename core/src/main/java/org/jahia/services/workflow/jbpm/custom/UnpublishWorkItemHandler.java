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
package org.jahia.services.workflow.jbpm.custom;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import java.util.Date;
import java.util.List;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class UnpublishWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {
    private transient static Logger logger = LoggerFactory.getLogger(UnpublishWorkItemHandler.class);

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) workItem.getParameter("nodeIds");

        String userKey = (String) workItem.getParameter("user");
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRUserNode user = JahiaUserManagerService.getInstance().lookupUserByPath(userKey);
        JahiaUser currentUser = sessionFactory.getCurrentUser();
        if (user != null) {
            sessionFactory.setCurrentUser(user.getJahiaUser());
        }
        try {
            if (logger.isDebugEnabled()) {
                for (String id : ids) {
                    JCRNodeWrapper node = sessionFactory.getCurrentUserSession().getNodeByUUID(id);
                    logger.debug("Launching unpublication of node " + node.getPath() + " at " + (new Date()).toString());
                }
            }
            JCRPublicationService.getInstance().unpublish(ids, false);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        sessionFactory.setCurrentUser(currentUser);
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}