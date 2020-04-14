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
package org.jahia.test.services.feedimporter;

import java.io.IOException;
import java.net.URL;

import javax.jcr.RepositoryException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for feed importer action.
 *
 * @author loom
 *         Date: May 21, 2010
 *         Time: 3:22:09 PM
 */
public class GetFeedActionTest extends JahiaTestCase {

    private final static String TESTSITE_NAME = "jcrFeedImportTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;    

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        TestHelper.createSite(TESTSITE_NAME);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Test    
    public void testGetFeedAction() throws Exception {
        /*JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/contents");
        session.checkout(node);
        node.addNode("feeds","jnt:contentList");
        session.save();

        JCRPublicationService.getInstance().publishByMainId(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                true, null);

        testFeed("testSDAFeed", "res:feedimporter/newsml/newsml_1_2_sda", "2_textwithphotoreference.xml");
        testFeed("testKoreanPicturesFeed", "res:feedimporter/newsml/koreanpictures_iptc", "2002-09-23T000051Z_01_BER04D_RTRIDSP_0_GERMANY.XML");
        testFeed("testAFPBasicFeed", "res:feedimporter/newsml/newsml_1_2_afp_basicsample", "NewsML-AFP-mmd-sample.xml");
        testFeed("testAFPLongFeed", "res:feedimporter/newsml/newsml_1_2_afp_longsample", "index.xml");
        // testFeed("testReutersFeed", "res:feedimporter/newsml/reuters_246_samples", "2002-09-23T100332Z_01_LA391519_RTRIDST_0_SPORT-CRICKET-CHAMPIONS-UPDATE-2.XML");

        session.save();*/
    }
   
    private void testFeed(String nodeName, String feedURL, String testNodeName) throws RepositoryException, IOException, JSONException {
        JCRSessionWrapper baseSession = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRSiteNode site = (JCRSiteNode) baseSession.getNode(SITECONTENT_ROOT_NODE);        

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/contents/feeds");

        session.checkout(node);

        JCRNodeWrapper sdaFeedNode = node.addNode(nodeName, "jnt:feed");

        sdaFeedNode.setProperty("url", feedURL);

        session.save();
        JCRPublicationService.getInstance().publishByMainId(sdaFeedNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                true, null);

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        String baseurl = getBaseServerURL() + Jahia.getContextPath() + "/cms";
        final URL url = new URL(baseurl + "/render/default/en" + sdaFeedNode.getPath() + ".getfeed.do");

        Credentials defaultcreds = new UsernamePasswordCredentials("root", "root1234");
        client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);

        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        PostMethod getFeedAction = new PostMethod(url.toExternalForm());
        try {
            getFeedAction.addRequestHeader(new Header("accept",
                    "application/json"));

            client.executeMethod(getFeedAction);
            assertEquals("Bad result code", 200, getFeedAction.getStatusCode());

            JSONObject response = new JSONObject(
                    getFeedAction.getResponseBodyAsString());
        } finally {
            getFeedAction.releaseConnection();
        }

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.LIVE_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper target = liveSession.getNode("/sites/"+TESTSITE_NAME+ "/contents/feeds/"+nodeName+"/" + testNodeName);
        // assertNotNull("Feed should have some childs", target); deactivated because we load content in a single language.
    }


}
