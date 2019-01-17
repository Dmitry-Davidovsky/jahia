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
package org.jahia.test.services.acl;

import static org.assertj.core.api.Assertions.*;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.jahia.test.services.content.*;
import org.junit.*;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AclTest {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ContentTest.class);

    private static final String TESTSITE_NAME = "aclTestSite";

    private static JCRUserNode user1;
    private static JCRUserNode user2;
    private static JCRUserNode user3;
    private static JCRUserNode user4;

    private static JCRGroupNode group1;
    private static JCRGroupNode group2;
    public static final String HOMEPATH = "/sites/"+TESTSITE_NAME+"/home";

    public static JCRPublicationService jcrService;

    private static JCRNodeWrapper home;
    private static JCRNodeWrapper content1;
    private static JCRNodeWrapper content11;
    private static JCRNodeWrapper content12;
    private static JCRNodeWrapper content2;
    private static JCRNodeWrapper content21;
    private static JCRNodeWrapper content22;
    private static String homeIdentifier;
    private JCRSessionWrapper session;
    static String content1Identifier;
    private static String content11Identifier;
    private static String content12Identifier;
    private static String content2Identifier;
    private static String content21Identifier;
    private static String content22Identifier;

    private static JahiaGroupManagerService groupService;
    private static JahiaUserManagerService userService;

    private static void assertRole(JCRNodeWrapper node, String principal, String grantType, String role) {
        Map<String, List<String[]>> aclEntries = node.getAclEntries();
        String path = node.getPath();
        assertThat(aclEntries)
                .as("ACL entries for node %s should contain %s for role for principal %s", path, grantType, role, principal)
                .containsKey(principal);
        assertThat(aclEntries.get(principal).get(0))
        .as("ACL entries for node %s should contain %s for role for principal %s", path, grantType, role, principal)
                .containsExactly(path, grantType, role);
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, TestHelper.DX_BASE_DEMO_TEMPLATES);

        jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();

        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

        home = session.getNode(HOMEPATH);
        homeIdentifier = home.getIdentifier();
        content1 = home.addNode("content1", "jnt:contentList");
        content1Identifier = content1.getIdentifier();
        content11 = content1.addNode("content1.1", "jnt:contentList");
        content11Identifier = content11.getIdentifier();
        content12 = content1.addNode("content1.2", "jnt:contentList");
        content12Identifier = content12.getIdentifier();
        content2 = home.addNode("content2", "jnt:contentList");
        content2Identifier = content2.getIdentifier();
        content21 = content2.addNode("content2.1", "jnt:contentList");
        content21Identifier = content21.getIdentifier();
        content22 = content2.addNode("content2.2", "jnt:contentList");
        content22Identifier = content22.getIdentifier();
        session.save();

        user1 = userService.createUser("user1", "password", new Properties(), session);
        user2 = userService.createUser("user2", "password", new Properties(), session);
        user3 = userService.createUser("user3", "password", new Properties(), session);
        user4 = userService.createUser("user4", "password", new Properties(), session);

        group1 = groupService.createGroup(site.getSiteKey(), "group1", new Properties(), false, session);
        group2 = groupService.createGroup(site.getSiteKey(), "group2", new Properties(), false, session);

        group1.addMember(user1);
        group1.addMember(user2);

        group2.addMember(user3);
        group2.addMember(user4);

        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists("/sites/"+TESTSITE_NAME)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }

            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
            userManager.deleteUser(user1.getPath(), session);
            userManager.deleteUser(user2.getPath(), session);
            userManager.deleteUser(user3.getPath(), session);
            userManager.deleteUser(user4.getPath(), session);
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        home = session.getNodeByIdentifier(homeIdentifier);
        home.getAclEntries();
        content1 = session.getNodeByIdentifier(content1Identifier);
        content1.getAclEntries();
        content11 = session.getNodeByIdentifier(content11Identifier);
        content11.getAclEntries();
        content12 = session.getNodeByIdentifier(content12Identifier);
        content12.getAclEntries();
        content2 = session.getNodeByIdentifier(content2Identifier);
        content2.getAclEntries();
        content21 = session.getNodeByIdentifier(content21Identifier);
        content21.getAclEntries();
        content22 = session.getNodeByIdentifier(content22Identifier);
        content22.getAclEntries();
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        home.revokeAllRoles();
        content1.revokeAllRoles();
        content11.revokeAllRoles();
        content12.revokeAllRoles();
        content2.revokeAllRoles();
        content21.revokeAllRoles();
        content21.revokeAllRoles();
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDefaultReadRight() throws Exception {
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(HOMEPATH, "jcr:read"))));
    }

    @Test
    public void testGrantUser() throws Exception {
        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user2", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testGrantGroup() throws Exception {
        content11.grantRoles("g:group1", Collections.singleton("owner"));

        assertRole(content11, "g:group1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user2", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user3", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user4", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testDenyUser() throws Exception {
        content1.grantRoles("u:user1", Collections.singleton("owner"));
        content11.denyRoles("u:user1", Collections.singleton("owner"));
        assertRole(content1, "u:user1", "GRANT", "owner");
        assertRole(content11, "u:user1", "DENY", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testAclBreak() throws Exception {
        assertThat(content1.getAclEntries()).as("ACL entries for node %s should NOT be empty", content1.getPath()).isNotEmpty();

        content1.setAclInheritanceBreak(true);

        assertThat(content1.getAclEntries()).as("ACL entries for node %s should be empty", content1.getPath()).isEmpty();

        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");
        assertThat(content11.getAclEntries()).as("ACL entries for node %s should contains %s role for user %s", content11.getPath(),
                "owner", "user1").containsOnlyKeys("u:user1");

        session.save();
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(home.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:read"))));
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content12.getPath(), "jcr:read"))));
    }

    @Test
    public void testRevokeRoles() throws Exception {
        content11.grantRoles("u:user1", Collections.singleton("owner"));
        content11.grantRoles("u:user2", Collections.singleton("owner"));
        assertRole(content11, "u:user1", "GRANT", "owner");
        assertRole(content11, "u:user2", "GRANT", "owner");
        session.save();

        content11.revokeRolesForPrincipal("u:user2");
        assertRole(content11, "u:user1", "GRANT", "owner");
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user2")
                .doesNotContainKey("u:user2");

        session.save();

        content11.revokeAllRoles();
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user1")
                .doesNotContainKey("u:user1");
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user2")
                .doesNotContainKey("u:user2");

        session.save();
    }

    @Test
    // Test case for the https://jira.jahia.org/browse/QA-9762
    public void testPrivilegedAccess() throws Exception {

        assertAccess(ImmutableMap.of("user1", false, "user3", false));

        // grant group1 an editor role on home page
        home.grantRoles("g:group1", Collections.singleton("editor"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user3", false));

        // revoke an editor role on home page from group1 and grant it to user1 directly
        home.revokeRolesForPrincipal("g:group1");
        home.grantRoles("u:user1", Collections.singleton("editor"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user2", false, "user3", false));

        // revoke an editor role on home page from user1 and grant her editor-in-chief role
        home.revokeRolesForPrincipal("u:user1");
        home.grantRoles("u:user1", Collections.singleton("editor-in-chief"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user2", false, "user3", false));

        // revoke all roles on home page from user1
        home.revokeRolesForPrincipal("u:user1");
        session.save();

        assertAccess(ImmutableMap.of("user1", false, "user2", false, "user3", false));

        home.grantRoles("g:group1", Collections.singleton("editor"));
        home.revokeRolesForPrincipal("g:group1");
        session.save();

        assertAccess(ImmutableMap.of("user1", false, "user2", false, "user3", false));
    }

    private static void assertAccess(ImmutableMap<String, Boolean> expectations) throws Exception {
        for (Map.Entry<String, Boolean> expectationEntry : expectations.entrySet()) {

            String principal = expectationEntry.getKey();
            Boolean shouldHaveAccess = expectationEntry.getValue();

            assertThat(isUserPrivileged(principal))
                    .as("%s should %sbe in privileged group", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);

            assertThat(nodeExists(home.getPath(), principal))
                    .as("%s should %shave access to home page in edit mode", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);
            assertThat(nodeExists(home.getParent().getPath(), principal))
                    .as("%s should %shave access to site in edit mode", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);
        }
    }

    private static boolean isUserPrivileged(String user) throws Exception {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            return groupService.lookupGroup(TESTSITE_NAME, JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, session)
                    .isMember(userService.lookupUser(user, session));
        });
    }

    private static boolean nodeExists(String path, String user) throws Exception {
        return doInJcrAsUser(user, session -> {
            try {
                session.getNode(path);
                return Boolean.TRUE;
            } catch (PathNotFoundException e) {
                return Boolean.FALSE;
            }
        });
    }

    private static <T> T doInJcrAsUser(String user, JCRCallback<T> callback) throws Exception {
        return JCRTemplate.getInstance().doExecute(user, null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, callback);
    }

    private static class CheckPermission implements JCRCallback<Boolean> {

        private String path;
        private String permission;

        CheckPermission(String path, String permission) {
            this.path = path;
            this.permission = permission;
        }

        @Override
        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
            try {
                return session.getNode(path).hasPermission(permission);
            } catch (PathNotFoundException e) {
                return false;
            }
        }
    }
}
