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
package org.jahia.test.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.filter.cache.AreaResourceCacheKeyPartGenerator;
import org.jahia.services.render.filter.cache.CacheFilter;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * New implementation of CacheFilter specific unit tests
 */
public class NewCacheFilterHttpTest extends CacheFilterHttpTest {

    protected static void switchCacheImplem() {
        ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(false);
        ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(false);
        ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(true);
        ((AreaResourceCacheKeyPartGenerator) SpringContextSingleton.getBean("areaResourceCacheKeyPartGenerator")).setDisabled(false);
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterHttpTest.oneTimeSetUp();
        switchCacheImplem();
    }
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterHttpTest.oneTimeTearDown();
    }

    @Test
    public void testModuleError() throws Exception {
        String s = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error1");
        assertTrue(s.contains("<!-- Module error :"));
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error2");
        // All served from cache
        // No request go after cache filter, everything is served by the cache
        assertNull(getCheckFilter("CacheHttpTestRenderFilter2").getData("error2"));
        Thread.sleep(5000);
        // Error should be flushed, and only this fragment should be regenerate
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error3");
        CacheFilterCheckFilter.RequestData data = getCheckFilter("CacheHttpTestRenderFilter2").getData("error3");
        assertEquals(1, data.getCount());
        assertEquals("/sites/cachetest/home/error/main/simple-text.error.html", data.getRenderCalled().toArray()[0]);
    }

    @Test
    public void testModuleWait() throws Exception {

        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();

        try {

            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);
            URL url = getUrl(SITECONTENT_ROOT_NODE + "/home/long");
            HttpThread t1 = new HttpThread(url, "root", "root1234", "testModuleWait1");
            t1.start();
            Thread.sleep(1000);

            HttpThread t2 = new HttpThread(url, "root", "root1234", "testModuleWait2");
            t2.start();
            t2.join();

            String content = getContent(url, "root", "root1234", "testModuleWait3");

            t1.join();

            String content1 = getContent(url, "root", "root1234", "testModuleWait4");

            // Long module is left blank
            assertFalse(t2.getResult().contains(LONG_CREATED_ELEMENT_TEXT));
            assertTrue("Page title " + LONG_PAGE_TITLE + " not found in result: " + t2.getResult(), t2.getResult().contains(LONG_PAGE_TITLE));
            assertTrue("Second thread did not spend correct time", getCheckFilter("CacheHttpTestRenderFilter1").getData("testModuleWait2").getTime() >= 1000);

            // Entry is cached without the long module
            assertFalse(content.contains(LONG_CREATED_ELEMENT_TEXT));
            assertTrue("Page title " + LONG_PAGE_TITLE + " not found in result: " + content, content.contains(LONG_PAGE_TITLE));
            assertNull(getCheckFilter("CacheHttpTestRenderFilter2").getData("testModuleWait3"));

            assertTrue(t1.getResult().contains(LONG_CREATED_ELEMENT_TEXT));
            assertTrue("Page title " + LONG_PAGE_TITLE + " not found in result: " + t1.getResult(), t1.getResult().contains(LONG_PAGE_TITLE));
            assertTrue("First thread did not spend correct time", getCheckFilter("CacheHttpTestRenderFilter1").getData("testModuleWait1").getTime() >= 15000);

            // Entry is now cached with the long module
            assertTrue(content1.contains(LONG_CREATED_ELEMENT_TEXT));
            assertTrue("Page title " + LONG_PAGE_TITLE + " not found in result: " + content1, content1.contains(LONG_PAGE_TITLE));
            assertNull(getCheckFilter("CacheHttpTestRenderFilter2").getData("testModuleWait4"));
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }
}
