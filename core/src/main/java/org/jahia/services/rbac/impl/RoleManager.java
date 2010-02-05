/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.rbac.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

/**
 * Service for managing roles and permissions.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleManager {

    private static final List<PermissionImpl> EMPTY_PERMISSION_LIST = Collections.emptyList();

    private static final List<RoleImpl> EMPTY_ROLE_LIST = Collections.emptyList();

    public static final String JAHIANT_PERMISSION = "jnt:permission";

    public static final String JAHIANT_PERMISSION_GROUP = "jnt:permissionGroup";

    public static final String JAHIANT_PERMISSIONS = "jnt:permissions";

    public static final String JAHIANT_ROLE = "jnt:role";

    public static final String JAHIANT_ROLES = "jnt:roles";

    public static final String JMIX_ROLE_BASED_ACCESS_CONTROLLED = "jmix:roleBasedAccessControlled";

    private static Logger logger = Logger.getLogger(RoleManager.class);

    protected String defaultPermissionGroup = "global";

    protected JCRUserManagerProvider jcrUserManagerProvider;

    protected String permissionsNodeName = "permissions";

    protected String rolesNodeName = "roles";

    /**
     * Looks up the permission by its corresponding JCR node path. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param jcrPath the JCR path of the corresponding JCR node
     * @param session current JCR session
     * @return the permission by its corresponding JCR node path. Returns $
     *         {@code null} if the requested permission is not found
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl getPermission(String jcrPath, JCRSessionWrapper session) throws RepositoryException {
        PermissionImpl perm = null;
        try {
            perm = toPermission(session.getNode(jcrPath));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return perm;
    }

    /**
     * Looks up the permission with the requested name for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param name the name of the permission to look up
     * @param group the permission group name
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl getPermission(String name, String group, String site, JCRSessionWrapper session)
            throws RepositoryException {
        PermissionImpl perm = null;
        JCRNodeWrapper permissionsHome = getPermissionsHome(site, group, session);
        try {
            perm = toPermission(permissionsHome.getNode(name));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return perm;
    }

    /**
     * Returns a list of permissions, defined for the specified site. If the
     * specified site is ${@code null} returns global permissions for the
     * server.
     * 
     * @param site the site key to retrieve permissions for
     * @param session current JCR session
     * @return a list of permissions, defined for the specified site. If the
     *         specified site is ${@code null} returns global permissions for
     *         the server
     * @throws RepositoryException in case of an error
     */
    public List<PermissionImpl> getPermissions(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<PermissionImpl> permissions = new LinkedList<PermissionImpl>();
        JCRNodeWrapper permissionsHome = getPermissionsHome(site, session);
        for (NodeIterator groupIterator = permissionsHome.getNodes(); groupIterator.hasNext();) {
            Node groupNode = groupIterator.nextNode();
            if (groupNode.isNodeType(JAHIANT_PERMISSION_GROUP)) {
                for (NodeIterator permIterator = groupNode.getNodes(); permIterator.hasNext();) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) permIterator.nextNode();
                    if (permissionNode.isNodeType(JAHIANT_PERMISSION)) {
                        permissions.add(toPermission(permissionNode));
                    }
                }
            }
        }
        return permissions.isEmpty() ? EMPTY_PERMISSION_LIST : permissions;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified. This method
     * creates the requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified.
     *         This method creates the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPermissionsHome(String site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper permissionsNode = null;
        try {
            permissionsNode = session.getNode(site == null ? "/" + permissionsNodeName : "/sites/" + site + "/"
                    + permissionsNodeName);
        } catch (PathNotFoundException ex) {
            // create it
            JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
            session.checkout(parentNode);
            permissionsNode = parentNode.addNode(permissionsNodeName, JAHIANT_PERMISSIONS);
            session.save();
        }

        return permissionsNode;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified. The
     * permission group is considered in both cases. This method creates the
     * requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param group the permission group
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified. The
     *         permission group is considered in both cases. This method creates
     *         the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPermissionsHome(String site, String group, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(site, session);
        String permissionGroup = StringUtils.defaultIfEmpty(group, defaultPermissionGroup);
        try {
            permissionsNode = session.getNode(permissionGroup);
        } catch (PathNotFoundException ex) {
            // create it
            session.checkout(permissionsNode);
            permissionsNode = permissionsNode.addNode(permissionGroup, JAHIANT_PERMISSION_GROUP);
            session.save();
        }

        return permissionsNode;
    }

    /**
     * Retrieved a JCR node that corresponds to the specified principal.
     * 
     * @param principal the principal to look up
     * @param session current JCR session
     * @return a JCR node that corresponds to the specified principal or null if
     *         the node cannot be retrieved
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPrincipalNode(JahiaPrincipal principal, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper principalNode = null;
        if (principal instanceof JahiaUser) {
            if (principal instanceof JCRUser) {
                try {
                    principalNode = session.getNodeByIdentifier(((JCRUser) principal).getNodeUuid());
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find user node with identifier " + ((JCRUser) principal).getNodeUuid());
                }
            } else {
                JCRUser externalUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(principal.getName());
                if (externalUser != null) {
                    try {
                        principalNode = session.getNodeByIdentifier(externalUser.getNodeUuid());
                    } catch (ItemNotFoundException e) {
                        logger.warn("Unable to find user node with identifier " + externalUser.getNodeUuid());
                    }
                } else {
                    // TODO need to create a node for external user
                }
            }
        } else if (principal instanceof JahiaGroup) {
            if (principal instanceof JCRGroup) {
                try {
                    principalNode = session.getNodeByIdentifier(((JCRGroup) principal).getNodeUuid());
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find group node with identifier " + ((JCRGroup) principal).getNodeUuid());
                }
            } else {
                // TODO handle the case with external groups
            }

        }
        return principalNode;
    }

    /**
     * Looks up the role by its JCR path. Returns ${@code null} if the requested
     * role is not found.
     * 
     * @param jcrPath the JCR path of the corresponding node
     * @param session current JCR session
     * @return the role with the requested path. Returns ${@code null} if the
     *         requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public RoleImpl getRole(final String jcrPath, JCRSessionWrapper session) throws RepositoryException {
        RoleImpl role = null;
        try {
            role = toRole(session.getNode(jcrPath));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return role;
    }

    /**
     * Looks up the role with the requested name for the specified site. If site
     * is not specified considers it as a global role. Returns ${@code null} if
     * the requested role is not found.
     * 
     * @param name the name of the role to look up
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the role with the requested name for the specified site. If site
     *         is not specified considers it as a global role. Returns ${@code
     *         null} if the requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public RoleImpl getRole(final String name, final String site, JCRSessionWrapper session) throws RepositoryException {
        RoleImpl role = null;
        JCRNodeWrapper rolesHome = getRolesHome(site, session);
        try {
            role = toRole(rolesHome.getNode(name));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return role;
    }

    /**
     * Returns a list of roles, defined for the specified site. If the specified
     * site is ${@code null} returns global permissions for the server.
     * 
     * @param site the site key to retrieve roles for
     * @param session current JCR session
     * @return a list of roles, defined for the specified site. If the specified
     *         site is ${@code null} returns global permissions for the server.
     * @throws RepositoryException in case of an error
     */
    public List<RoleImpl> getRoles(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<RoleImpl> roles = new LinkedList<RoleImpl>();
        JCRNodeWrapper rolesHome = getRolesHome(site, session);
        for (NodeIterator iterator = rolesHome.getNodes(); iterator.hasNext();) {
            JCRNodeWrapper roleNode = (JCRNodeWrapper) iterator.nextNode();
            if (roleNode.isNodeType(JAHIANT_ROLE)) {
                roles.add(toRole(roleNode));
            }
        }
        return roles.isEmpty() ? EMPTY_ROLE_LIST : roles;
    }

    /**
     * Returns the node that corresponds to the roles of the specified site or
     * the global roles, if the site is not specified. This method creates the
     * requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global roles node is
     *            requested
     * @param session current JCR session
     * @return the node that corresponds to the roles of the specified site or
     *         the global roles, if the site is not specified. This method
     *         creates the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getRolesHome(String site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rolesNode = null;
        try {
            rolesNode = session.getNode(site == null ? "/" + rolesNodeName : "/sites/" + site + "/" + rolesNodeName);
        } catch (PathNotFoundException ex) {
            // create it
            JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
            session.checkout(parentNode);
            rolesNode = parentNode.addNode(rolesNodeName, JAHIANT_ROLES);
            session.save();
        }

        return rolesNode;
    }

    /**
     * Grants a permission to the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPath permission to be granted, defined as a JCR path
     *            of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantPermission(final String roleJcrPath, String permissionJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> granted = new LinkedList<String>();
        granted.add(permissionJcrPath);
        grantPermissions(roleJcrPath, granted, session);
    }

    /**
     * Grants permissions to the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPaths permissions to be granted, defined as a JCR
     *            path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantPermissions(final String roleJcrPath, List<String> permissionJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (permissionJcrPaths == null || permissionJcrPaths.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(roleJcrPath);
        Set<String> toBeGranted = new LinkedHashSet<String>(permissionJcrPaths.size());
        for (String permission : permissionJcrPaths) {
            try {
                JCRNodeWrapper permissionNode = session.getNode(permission);
                toBeGranted.add(permissionNode.getIdentifier());
            } catch (PathNotFoundException ex) {
                logger.warn("Unable to find a node that corresponds to a permission '" + permission);
            }
        }

        List<Value> newValues = new LinkedList<Value>();
        if (roleNode.hasProperty("j:permissions")) {
            Value[] oldValues = roleNode.getProperty("j:permissions").getValues();
            for (Value oldOne : oldValues) {
                newValues.add(oldOne);
                toBeGranted.remove(oldOne.getString());

            }
        }
        for (String granted : toBeGranted) {
            newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
        }
        session.checkout(roleNode);
        roleNode.setProperty("j:permissions", newValues.toArray(new Value[] {}));
        session.save();
    }

    /**
     * Grants a role to the specified principal.
     * 
     * @param principal principal to grant the role to
     * @param roleJcrPath the role to be granted, defined as a JCR path of the
     *            corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantRole(final JahiaPrincipal principal, String roleJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> granted = new LinkedList<String>();
        granted.add(roleJcrPath);
        grantRoles(principal, granted, session);
    }

    /**
     * Grants roles to the specified principal.
     * 
     * @param principal principal to grant roles to
     * @param roleJcrPaths the list of roles to be granted, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantRoles(final JahiaPrincipal principal, List<String> roleJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (roleJcrPaths == null || roleJcrPaths.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        if (principalNode != null) {
            session.checkout(principalNode);

            if (!principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)) {
                principalNode.addMixin(JMIX_ROLE_BASED_ACCESS_CONTROLLED);
            }

            Set<String> toBeGranted = new LinkedHashSet<String>(roleJcrPaths.size());
            for (String role : roleJcrPaths) {
                try {
                    toBeGranted.add(session.getNode(role).getIdentifier());
                } catch (PathNotFoundException ex) {
                    logger.warn("Unable to find a node that corresponds to a role '" + role);
                }
            }

            List<Value> newValues = new LinkedList<Value>();
            if (principalNode.hasProperty("j:roles")) {
                Value[] oldValues = principalNode.getProperty("j:roles").getValues();
                for (Value oldOne : oldValues) {
                    newValues.add(oldOne);
                    toBeGranted.remove(oldOne.getString());
                }
            }
            for (String granted : toBeGranted) {
                newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
            }
            principalNode.setProperty("j:roles", newValues.toArray(new Value[] {}));
            session.save();

            invalidateCache(principal);
        } else {
            logger.warn("Unable to find corresponding JCR node for principal " + principal + ". Skip granting roles.");
        }
    }

    protected void invalidateCache(JahiaPrincipal principal) {
        // TODO implement cache invalidation
    }

    /**
     * Revokes a permission from the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPath the permission to be removed, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokePermission(final String roleJcrPath, String permissionJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> toRevoke = new LinkedList<String>();
        toRevoke.add(permissionJcrPath);
        revokePermissions(roleJcrPath, toRevoke, session);
    }

    /**
     * Revokes permissions from the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPaths permissions to be removed, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokePermissions(final String roleJcrPath, List<String> permissionJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (permissionJcrPaths == null || permissionJcrPaths.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(roleJcrPath);
        Set<String> toRevoke = new HashSet<String>(permissionJcrPaths);
        if (roleNode.hasProperty("j:permissions")) {
            Value[] values = roleNode.getProperty("j:permissions").getValues();
            if (values != null) {
                List<Value> newValues = new LinkedList<Value>();
                for (Value value : values) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (permissionNode != null && !toRevoke.contains(permissionNode.getPath())) {
                        newValues.add(value);
                    }
                }
                if (values.length != newValues.size()) {
                    session.checkout(roleNode);
                    roleNode.setProperty("j:permissions", newValues.toArray(new Value[] {}));
                    session.save();
                }
            }
        }
    }

    /**
     * Revokes a role from the specified principal.
     * 
     * @param principal principal to revoke the role from
     * @param roleJcrPath the role to be revoked, defined as a JCR path of the
     *            corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeRole(final JahiaPrincipal principal, String roleJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> revoked = new LinkedList<String>();
        revoked.add(roleJcrPath);
        revokeRoles(principal, revoked, session);
    }

    /**
     * Revokes roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @param roleJcrPaths the list of roles to revoke, defined as a JCR path of
     *            the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeRoles(final JahiaPrincipal principal, List<String> roleJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (roleJcrPaths == null || roleJcrPaths.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        if (principalNode != null && principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)
                && principalNode.hasProperty("j:roles")) {
            Value[] values = principalNode.getProperty("j:roles").getValues();
            if (values != null) {
                Set<String> toRevoke = new HashSet<String>(roleJcrPaths);
                List<Value> newValues = new LinkedList<Value>();
                for (Value value : values) {
                    JCRNodeWrapper roleNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (roleNode != null && !toRevoke.contains(roleNode.getPath())) {
                        newValues.add(value);
                    }
                }
                if (values.length != newValues.size()) {
                    session.checkout(principalNode);
                    principalNode.setProperty("j:roles", newValues.toArray(new Value[] {}));
                    session.save();

                    invalidateCache(principal);
                }
            }
        } else {
            if (principalNode == null) {
                logger.warn("Unable to find corresponding JCR node for principal " + principal
                        + ". Skip revoking roles.");
            } else {
                logger.warn("Principal '" + principal.getName()
                        + "' does not have any roles assigned. Skip revoking roles.");
            }
        }
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     * 
     * @param permission the permission to be stored
     * @param session current JCR session
     * @return the corresponding permission node
     * @throws RepositoryException in case of an error
     */
    public JCRNodeWrapper savePermission(PermissionImpl permission, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(
                permission instanceof SitePermissionImpl ? ((SitePermissionImpl) permission).getSite() : null, session);
        JCRNodeWrapper target = null;
        try {
            String group = StringUtils.defaultIfEmpty(permission.getGroup(), defaultPermissionGroup);
            try {
                permissionsNode = permissionsNode.getNode(group);
            } catch (PathNotFoundException e) {
                // does not exist yet
                session.checkout(permissionsNode);
                permissionsNode = permissionsNode.addNode(group, JAHIANT_PERMISSION_GROUP);
            }
            permission.setGroup(group);
            target = permissionsNode.getNode(permission.getName());
            if (target.hasProperty("jcr:title")) {
                permission.setTitle(target.getProperty("jcr:title").getString());
            }
            if (target.hasProperty("jcr:description")) {
                permission.setDescription(target.getProperty("jcr:description").getString());
            }
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(permissionsNode);
            target = permissionsNode.addNode(permission.getName(), JAHIANT_PERMISSION);
        }

        permission.setPath(target.getPath());

        session.save();

        return target;
    }

    /**
     * Creates or updates the specified {@link RoleImpl}.
     * 
     * @param role the role to be stored
     * @param session current JCR session
     * @return the corresponding role node
     * @throws RepositoryException in case of an error
     */
    public JCRNodeWrapper saveRole(RoleImpl role, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rolesHome = getRolesHome(role instanceof SiteRoleImpl ? ((SiteRoleImpl) role).getSite() : null,
                session);
        JCRNodeWrapper roleNode = null;
        try {
            roleNode = rolesHome.getNode(role.getName());
            if (roleNode.hasProperty("jcr:title")) {
                role.setTitle(roleNode.getProperty("jcr:title").getString());
            }
            if (roleNode.hasProperty("jcr:description")) {
                role.setDescription(roleNode.getProperty("jcr:description").getString());
            }
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(rolesHome);
            roleNode = rolesHome.addNode(role.getName(), JAHIANT_ROLE);
        }

        role.setPath(roleNode.getPath());

        List<Value> values = new LinkedList<Value>();
        for (PermissionImpl permission : role.getPermissions()) {
            values.add(new ValueImpl(savePermission(permission, session).getIdentifier(), PropertyType.WEAKREFERENCE));
        }
        roleNode.setProperty("j:permissions", values.toArray(new Value[] {}));

        session.save();

        return roleNode;
    }

    /**
     * @param defaultPermissionGroup the defaultPermissionGroup to set
     */
    public void setDefaultPermissionGroup(String defaultPermissionGroup) {
        this.defaultPermissionGroup = defaultPermissionGroup;
    }

    /**
     * @param jcrUserManagerProvider the jcrUserManagerProvider to set
     */
    public void setJCRUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    /**
     * @param permissionsNodeName the permissionsNodeName to set
     */
    public void setPermissionsNodeName(String permissionsNodeName) {
        this.permissionsNodeName = permissionsNodeName;
    }

    /**
     * @param rolesNodeName the rolesNodeName to set
     */
    public void setRolesNodeName(String rolesNodeName) {
        this.rolesNodeName = rolesNodeName;
    }

    /**
     * Converts the provided JCR node to a {@link PermissionImpl} object
     * populating corresponding fields.
     * 
     * @param permissionNode the underlying JCR node
     * @return {@link PermissionImpl} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected PermissionImpl toPermission(JCRNodeWrapper permissionNode) throws RepositoryException {
        String site = permissionNode.getPath().startsWith("/sites/") ? StringUtils.substringBetween(permissionNode
                .getPath(), "/sites/", "/" + permissionsNodeName + "/") : null;
        String group = StringUtils.substringBetween(permissionNode.getPath(), "/" + permissionsNodeName + "/", "/");

        PermissionImpl perm = site != null ? new SitePermissionImpl(permissionNode.getName(), group, site)
                : new PermissionImpl(permissionNode.getName(), group);
        perm.setPath(permissionNode.getPath());
        if (permissionNode.hasProperty("jcr:title")) {
            perm.setTitle(permissionNode.getProperty("jcr:title").getString());
        }
        if (permissionNode.hasProperty("jcr:description")) {
            perm.setDescription(permissionNode.getProperty("jcr:description").getString());
        }

        return perm;
    }

    /**
     * Converts the provided JCR node to a {@link RoleImpl} object populating
     * corresponding fields.
     * 
     * @param roleNode the underlying JCR node
     * @return {@link RoleImpl} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected RoleImpl toRole(JCRNodeWrapper roleNode) throws RepositoryException {
        String site = roleNode.getPath().startsWith("/sites/") ? StringUtils.substringBetween(roleNode.getPath(),
                "/sites/", "/" + rolesNodeName + "/") : null;
        RoleImpl role = site != null ? new SiteRoleImpl(roleNode.getName(), site) : new RoleImpl(roleNode.getName());
        role.setPath(roleNode.getPath());
        if (roleNode.hasProperty("jcr:title")) {
            role.setTitle(roleNode.getProperty("jcr:title").getString());
        }
        if (roleNode.hasProperty("jcr:description")) {
            role.setDescription(roleNode.getProperty("jcr:description").getString());
        }
        if (roleNode.hasProperty("j:permissions")) {
            Value[] values = roleNode.getProperty("j:permissions").getValues();
            if (values != null) {
                for (Value value : values) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (permissionNode != null) {
                        role.getPermissions().add(toPermission(permissionNode));
                    }
                }
            }
        }
        return role;
    }

}