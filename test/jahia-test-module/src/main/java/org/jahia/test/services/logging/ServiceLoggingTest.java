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
package org.jahia.test.services.logging;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.tags.TaggingService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StopWatch;
import org.springframework.util.StopWatch.TaskInfo;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author : rincevent
 * @since JAHIA 6.5 Created : 27 nov. 2009
 */
public class ServiceLoggingTest {
    private static final Logger logger = Logger.getLogger(ServiceLoggingTest.class);
    private static final int TAGS_TO_CREATE = 30;
    private static final float ACCEPTABLE_DEVIATION = 10;

    private int counter = 0;

    private TaggingService service;
    private final static String TESTSITE_NAME = "serviceLoggingTest";

    private String tagPrefix;

    private String generateTagName() {
        return tagPrefix + counter++;
    }

    @Before
    public void setUp() {
        try {
            TestHelper.createSite(TESTSITE_NAME);
            tagPrefix = "test-" + System.currentTimeMillis() + "-";
            service = (TaggingService) SpringContextSingleton
                    .getBean("org.jahia.services.tags.TaggingService");
        } catch (Exception e) {
            logger.error("Error setting up ServiceLoggingTest environment", e);
            fail();
        }
    }

    @After
    public void tearDown() {
        try {
            deleteAllTags();
        } catch (Exception e) {
            logger.error("Error tearing down ServiceLoggingTest environment", e);
        }
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.error("Error tearing down ServiceLoggingTest environment", e);
        }        
        tagPrefix = null;
        counter = 0;
        service = null;
    }

    private void deleteAllTags() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                NodeIterator nodeIterator = session
                        .getWorkspace()
                        .getQueryManager()
                        .createQuery(
                                "select * from [jnt:tag] " + "where ischildnode([/sites/"
                                        + TESTSITE_NAME + "/tags]) and localname() like '" + tagPrefix
                                        + "%'", Query.JCR_SQL2).execute().getNodes();
                while (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    try {
                        session.checkout(node);
                        node.remove();
                    } catch (PathNotFoundException e) {
                        // ignore -> it is a bug in Jackrabbit that produces
                        // duplicate results
                    }
                }
                try {
                    session.getNode("/sites/" + TESTSITE_NAME + "/tags-content").remove();
                } catch (PathNotFoundException e) {
                    // ignore it
                }
                session.save();
                return null;
            }
        });
    }
    
    @Test
    public void testCreateMultipleTags() throws RepositoryException {
        for (int i = 0; i < 3; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        
        Logger metricsLogger = Logger.getLogger("loggingService");
        Logger profilerMetricsLogger = Logger.getLogger("profilerLoggingService");
        profilerMetricsLogger.setLevel(Level.OFF);
        
        metricsLogger.setLevel(Level.OFF);
        StopWatch stopWatch = new StopWatch();
        
        System.gc();
        
        for (int i = 0; i < TAGS_TO_CREATE; i++) {
            stopWatch.start("Create task " + i + " without logs");            
            service.createTag(generateTagName(), TESTSITE_NAME);
            stopWatch.stop();            
        }
        final long withoutLogs = calculateTime(stopWatch.getTaskInfo());
        
        deleteAllTags();
        
        for (int i = 0; i < 3; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        
        metricsLogger.setLevel(Level.TRACE);
        
        StopWatch logStopWatch = new StopWatch();        
        System.gc();        
        
        for (int i = 0; i < TAGS_TO_CREATE; i++) {
            logStopWatch.start("Create task " + i + " with logs");
            service.createTag(generateTagName(), TESTSITE_NAME);
            logStopWatch.stop();
        }
        final long withLogs = calculateTime(logStopWatch.getTaskInfo());
       
        if (withLogs > withoutLogs) {
            float percentage = ((Math.abs(logStopWatch.getTotalTimeMillis()
                    - stopWatch.getTotalTimeMillis()) / (float) stopWatch
                    .getTotalTimeMillis()) * 100);
            if (percentage >= ACCEPTABLE_DEVIATION) {
                percentage = ((Math.abs(withLogs - withoutLogs) / (float)withoutLogs) * 100);    
            }
            if (percentage >= ACCEPTABLE_DEVIATION) {
                logger.warn("Without logging 80% iterations (" + withoutLogs
                        + "ms): " + stopWatch.prettyPrint());
                logger.warn("With logging 80% iterations (" + withLogs
                        + "ms): " + logStopWatch.prettyPrint());            
            }
            assertThat("Logs has more than " + ACCEPTABLE_DEVIATION + "% impact on peformance",
                    percentage, lessThan(ACCEPTABLE_DEVIATION));
        }
    }
    
    private long calculateTime (TaskInfo[] tasks) {
        Arrays.sort(tasks, new Comparator<TaskInfo>() {
            public int compare(TaskInfo o1, TaskInfo o2) {
                return (o1.getTimeMillis() < o2.getTimeMillis()) ? -1 : ((o1
                        .getTimeMillis() == o2.getTimeMillis()) ? 0 : 1);
            }
        });
        int i = 0;
        long time = 0;
        int reducedLoop = (int)((TAGS_TO_CREATE/(float)100) * 80);
        for (TaskInfo taskInfo : tasks) {
            time += taskInfo.getTimeMillis();
            if (++i == reducedLoop) {
                break;
            }
        }
        return time;
    }
}
