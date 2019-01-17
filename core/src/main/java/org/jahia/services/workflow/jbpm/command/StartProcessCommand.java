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

import com.google.common.base.Joiner;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.Map;

/**
* Start a new process
*/
public class StartProcessCommand extends BaseCommand<String> {
    private final String processKey;
    private final Map<String, Object> args;

    public StartProcessCommand(String processKey, Map<String, Object> args) {
        this.processKey = processKey;
        this.args = args;
    }

    @Override
    public String execute() {
        ProcessInstance processInstance = getKieSession().startProcess(JBPM6WorkflowProvider.getEncodedProcessKey(processKey), args);
        return Long.toString(processInstance.getId());
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n processKey: %s", processKey) +
                String.format("%n args: %s", Joiner.on(",").withKeyValueSeparator("=").join(args));
    }
}
