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
package org.jahia.test.bin;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jahia.bin.FindPrincipal;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.*;

/**
 * Test of the find principal servlet.
 *
 * @author loom
 *         Date: Jun 16, 2010
 *         Time: 12:03:19 PM
 */
public class FindPrincipalTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FindPrincipalTest.class);

    private HttpClient client;
    private final static String TESTSITE_NAME = "findPrincipalTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                         TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.WEB_TEMPLATES);
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    session.save();
                    return null;
                }
            });

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Before
    public void setUp() throws Exception {

        // Create an instance of HttpClient.
        client = new HttpClient();

        // todo we should really insert content to test the find.

        PostMethod loginMethod = new PostMethod(getLoginServletURL());
        try {
            loginMethod.addParameter("username", "root");
            loginMethod.addParameter("password", "root1234");
            loginMethod.addParameter("redirectActive", "false");
            // the next parameter is required to properly activate the valve check.
            loginMethod.addParameter(
                    LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1");
            // Provide custom retry handler is necessary
            loginMethod.getParams().setParameter(
                    HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            int statusCode = client.executeMethod(loginMethod);
            assertEquals("Method failed: " + loginMethod.getStatusLine(),
                    HttpStatus.SC_OK, statusCode);
        } finally {
            loginMethod.releaseConnection();
        }
    }

    @After
    public void tearDown() throws Exception {

        PostMethod logoutMethod = new PostMethod(getLogoutServletURL());
        try {
            logoutMethod.addParameter("redirectActive", "false");
            // Provide custom retry handler is necessary
            logoutMethod.getParams().setParameter(
                    HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            int statusCode = client.executeMethod(logoutMethod);
            assertEquals("Method failed: " + logoutMethod.getStatusLine(),
                    HttpStatus.SC_OK, statusCode);
        } finally {
            logoutMethod.releaseConnection();
        }
    }

    @Test
    public void testFindUsers() throws IOException, JSONException, JahiaException {

        PostMethod method = new PostMethod(getFindPrincipalServletURL());
        try {
            method.addParameter("principalType", "users");
            method.addParameter("wildcardTerm", "*root*");

            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            // Execute the method.
            int statusCode = client.executeMethod(method);

            assertEquals("Method failed: " + method.getStatusLine(),
                    HttpStatus.SC_OK, statusCode);

            // Read the response body.
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[")
                    .append(method.getResponseBodyAsString()).append("]");
            String responseBody = responseBodyBuilder.toString();

            JSONArray jsonResults = new JSONArray(responseBody);

            assertNotNull(
                    "A proper JSONObject instance was expected, got null instead",
                    jsonResults);
        } finally {
            method.releaseConnection();
        }

        // @todo we need to add more tests to validate results.

    }

    @Test
    public void testFindGroups() throws IOException, JSONException {

        PostMethod method = new PostMethod(getFindPrincipalServletURL());
        try {
            method.addParameter("principalType", "groups");
            method.addParameter("siteKey", TESTSITE_NAME);
            method.addParameter("wildcardTerm", "*administrators*");

            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            // Execute the method.
            int statusCode = client.executeMethod(method);

            assertEquals("Method failed: " + method.getStatusLine(),
                    HttpStatus.SC_OK, statusCode);

            // Read the response body.
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[")
                    .append(method.getResponseBodyAsString()).append("]");
            String responseBody = responseBodyBuilder.toString();

            JSONArray jsonResults = new JSONArray(responseBody);

            assertNotNull(
                    "A proper JSONObject instance was expected, got null instead",
                    jsonResults);
        } finally {
            method.releaseConnection();
        }

        // @todo we need to add more tests to validate results.

    }

    private String getLoginServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/cms/login";
    }

    private String getLogoutServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/cms/logout";
    }

    private String getFindPrincipalServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + FindPrincipal.getFindPrincipalServletPath();
    }

}
