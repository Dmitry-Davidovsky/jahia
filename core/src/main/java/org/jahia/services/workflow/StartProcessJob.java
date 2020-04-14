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
package org.jahia.services.workflow;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * Assign and complete task
 */
public class StartProcessJob extends BackgroundJob {
    public static final String NODE_IDS = "nodeIds";
    public static final String PROVIDER = "provider";
    public static final String PROCESS_KEY = "processKey";
    public static final String MAP = "map";
    public static final String COMMENTS = "comments";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<String> nodesIds = (List<String>) jobDataMap.get(NODE_IDS);
        String provider = (String) jobDataMap.get(PROVIDER);
        String processKey = (String) jobDataMap.get(PROCESS_KEY);
        Map<String,Object> map = (Map<String, Object>) jobDataMap.get(MAP);
        List<String> comments = (List<String>) jobDataMap.get(COMMENTS);
        String locale = (String) jobDataMap.get(JOB_CURRENT_LOCALE);

        WorkflowService.getInstance().startProcess(nodesIds, JCRSessionFactory.getInstance().getCurrentUserSession(null, LanguageCodeConverters.languageCodeToLocale(locale)),
                processKey, provider, map, comments);
    }
}
