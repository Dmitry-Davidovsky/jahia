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
package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.HistoryWorkflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jbpm.process.audit.VariableInstanceLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
* Get all history process for a node id
*/
public class GetHistoryWorkflowsForNodeCommand extends BaseCommand<List<HistoryWorkflow>> {
    private final String nodeId;
    private final Locale uiLocale;

    public GetHistoryWorkflowsForNodeCommand(String nodeId, Locale uiLocale) {
        this.nodeId = nodeId;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<HistoryWorkflow> execute() {
        @SuppressWarnings("unchecked")
        List<VariableInstanceLog> result = getEm()
                .createQuery("FROM VariableInstanceLog v WHERE v.variableId = :variableId AND v.value = :variableValue")
                .setParameter("variableId", "nodeId")
                .setParameter("variableValue", nodeId).getResultList();

        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> l = new ArrayList<String>();
        for (VariableInstanceLog log : result) {
            l.add(Long.toString(log.getProcessInstanceId()));
        }

        return getHistoryWorkflows(l, uiLocale);
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n nodeId: %s", nodeId) +
                String.format("%n uiLocale: %s", uiLocale);
    }
}
