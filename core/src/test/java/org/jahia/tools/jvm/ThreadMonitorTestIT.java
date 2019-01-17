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
package org.jahia.tools.jvm;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.settings.SettingsBean;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit test for ThreadMonitor class
 */
public class ThreadMonitorTestIT extends AbstractJUnitTest {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ThreadMonitorTestIT.class);

    private final long THREAD_COUNT = 500L;
    private final long LOOP_COUNT = 10L;

    private static File todaysDirectory;
    private Set<Thread> threadSet = new HashSet<Thread>();

    private boolean enabledDebugLogging = false;
    private long minimalIntervalBetweenDumps = 20;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        Date now = new Date();
        todaysDirectory = new File(SettingsBean.getThreadDir(), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.deleteDirectory(todaysDirectory);
    }

    @Test
    public void testMonitorActivation() throws InterruptedException {
        logger.info("Starting testMonitorActivation test...");

        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeActiveMonitor = (files == null ? 0 : files.length);

        ThreadMonitor.getInstance().setActivated(true);
        ThreadMonitor.getInstance().dumpThreadInfo(false, true);
        Thread.sleep(minimalIntervalBetweenDumps * 2);
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval(false, true, 2, 1);
        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }
        String deadLocks = ThreadMonitor.getInstance().findDeadlock();
        Thread.sleep(minimalIntervalBetweenDumps * 2);
        StringWriter stringWriter = new StringWriter();
        ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
        Thread.sleep(minimalIntervalBetweenDumps * 2);

        files = todaysDirectory.listFiles();
        int fileCountAfterActiveMonitor = (files == null ? 0 : files.length);
        Assert.assertEquals("File difference is not as expected with active monitor", 2, fileCountAfterActiveMonitor - fileCountBeforeActiveMonitor);
        Assert.assertNotSame("Value for dead lock is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, deadLocks);
        Assert.assertNotSame("Value for generated thread info is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, stringWriter.toString());

        ThreadMonitor.getInstance().setActivated(false);
        Thread.sleep(minimalIntervalBetweenDumps * 2);
        ThreadMonitor.getInstance().dumpThreadInfo(true, true);
        Thread.sleep(minimalIntervalBetweenDumps * 2);
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval(true, true, 2, 1);
        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }
        deadLocks = ThreadMonitor.getInstance().findDeadlock();
        Thread.sleep(minimalIntervalBetweenDumps * 2);
        stringWriter = new StringWriter();
        ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
        Thread.sleep(minimalIntervalBetweenDumps * 2);

        files = todaysDirectory.listFiles();
        int fileCountAfterInactiveMonitor = (files == null ? 0 : files.length);
        Assert.assertEquals("File difference is not as expected with inactive monitor", 0, fileCountAfterInactiveMonitor - fileCountAfterActiveMonitor);
        Assert.assertEquals("Value for dead lock is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, deadLocks);
        Assert.assertEquals("Value for generated thread info is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, stringWriter.toString());

        ThreadMonitor.getInstance().setActivated(true);
        Thread.sleep(minimalIntervalBetweenDumps * 2);
    }

    @Test
    public void testDumpThreadInfo() throws InterruptedException {
        logger.info("Starting testDumpThreadInfo test...");

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeTest = (files == null ? 0 : files.length);

        runParallelTest("testDumpThreadInfo", new Runnable() {
            public void run() {
                for (int i=0; i < LOOP_COUNT; i++) {
                    ThreadMonitor.getInstance().dumpThreadInfo(false, true);
                }
            }
        });

        assertFileCount(fileCountBeforeTest);
    }


    @Test
    public void testDumpThreadInfoWithInterval() throws InterruptedException {
        logger.info("Starting testDumpThreadInfoWithInterval test...");

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeTest = (files == null ? 0 : files.length);

        runParallelTest("testDumpThreadInfoWithInterval", new Runnable() {
            public void run() {
                for (int i=0; i < LOOP_COUNT; i++) {
                    ThreadMonitor.getInstance().dumpThreadInfoWithInterval(false, true, 5, 1);
                }
            }
        });

        assertFileCount(fileCountBeforeTest);
    }

    @Test
    public void testFindDeadLock() throws InterruptedException {
        logger.info("Starting testFindDeadLock test...");

        runParallelTest("testFindDeadLock", new Runnable() {

            public void run() {
                for (int i=0; i < LOOP_COUNT; i++) {
                    ThreadMonitor.getInstance().findDeadlock();
                }
            }
        });

    }

    @Test
    public void testGenerateThreadInfo() throws InterruptedException {
        logger.info("Starting testGenerateThreadInfo test...");

        runParallelTest("testGenerateThreadInfo", new Runnable() {

            public void run() {
                for (int i=0; i < LOOP_COUNT; i++) {
                    StringWriter stringWriter = new StringWriter();
                    ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
                    stringWriter.toString();
                    stringWriter = null;
                }
            }
        });

    }

    private void runParallelTest(String testName, Runnable runnable) throws InterruptedException {

        StopWatch stopWatch = new StopWatch(testName);
        stopWatch.start(Thread.currentThread().getName() + " dumping thread info");

        threadSet.clear();
        ThreadMonitor.getInstance().setActivated(true);
        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(runnable, testName + i);
            threadSet.add(newThread);
            Thread.yield();
            Thread.sleep(50);
            newThread.start();
        }

        logger.info("Waiting for test completion...");

        Thread.yield();
        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ThreadMonitor.shutdownInstance();

        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());

        Thread.sleep(minimalIntervalBetweenDumps * 2);
    }

    private void assertFileCount(int fileCountBeforeTest) {
        File[] files;
        Assert.assertTrue("Thread dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Thread dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

        files = todaysDirectory.listFiles();
        int fileCountAfterTest = (files == null ? 0 : files.length);

        Assert.assertFalse("File count should not be the same after the test (before=" + fileCountBeforeTest + ",after=" + fileCountAfterTest + ")!", fileCountBeforeTest == fileCountAfterTest);
    }

}
