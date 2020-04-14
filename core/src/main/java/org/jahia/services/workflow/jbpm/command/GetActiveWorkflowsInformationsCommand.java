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
package org.jahia.services.workflow.jbpm.command;

import com.google.common.base.Joiner;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
* Get processes based on list of ids
*/
public class GetActiveWorkflowsInformationsCommand extends BaseCommand<List<Workflow>> {

	private transient static Logger logger = LoggerFactory.getLogger(GetActiveWorkflowsInformationsCommand.class);
	
    private final List<String> processIds;
    private final Locale uiLocale;

    public GetActiveWorkflowsInformationsCommand(List<String> processIds, Locale uiLocale) {
        this.processIds = processIds;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<Workflow> execute() {
        List<Workflow> activeWorkflows = new ArrayList<Workflow>();
        for (String processId : processIds) {
        	ProcessInstance processInstance = getKieSession().getProcessInstance(Long.parseLong(processId));
        	if (processInstance != null) {
                activeWorkflows.add(convertToWorkflow(processInstance, uiLocale, getKieSession(), getTaskService(), getLogService()));
        	} else {
        		logger.debug("Retrieving process instance with ID {} returned null while getting active workflows", processId);
        	}
        }
        return activeWorkflows;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n processIds: %s", Joiner.on(",").join(processIds)) +
                String.format("%n uiLocale: %s", uiLocale);
    }
}
