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
package org.jahia.bin;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.ServletRequestUtils;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;

/**
 * Base handler for content actions.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class Action {

    /**
     * Returns a single value for the specified parameter. If the parameter is not present throws the {@link JahiaBadRequestException}.
     * 
     * @param parameters
     *            the map of action parameters
     * @param paramName
     *            the name of the parameter in question
     * @return a single value for the specified parameter. If the parameter is not present throws the {@link JahiaBadRequestException}
     * @throws JahiaBadRequestException
     *             if the specified parameter is not present in the request
     * 
     */
    protected static String getRequiredParameter(Map<String, List<String>> parameters,
            String paramName) throws JahiaBadRequestException {
        if (parameters.get(paramName) == null) {
            throw new JahiaBadRequestException("Missing required '" + paramName
                    + "' parameter in request.");
        }

        return getParameter(parameters, paramName, null);
    }

    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns <code>null</code>.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns <code>null</code>
     */
    protected static String getParameter(Map<String, List<String>> parameters, String paramName) {
        return getParameter(parameters, paramName, null);
    }
    
    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns the provided default value.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @param defaultValue the default value to be used if the parameter is not
     *            present or its value is empty
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns the provided default
     *         value
     */
    protected static String getParameter(Map<String, List<String>> parameters, String paramName, String defaultValue) {
        List<String> vals = parameters.get(paramName);
        return CollectionUtils.isNotEmpty(vals) && StringUtils.isNotEmpty(vals.get(0)) ? vals.get(0) : defaultValue;
    }

    private String name;

    private boolean requireAuthenticatedUser = true;

    private String requiredPermission;

    private String requiredWorkspace;

    protected JCRNodeWrapper createNode(HttpServletRequest req, Map<String, List<String>> parameters,
                                        JCRNodeWrapper node, String nodeType, String nodeName, boolean forceCreation)
            throws RepositoryException {
        JCRNodeWrapper newNode;
        boolean isNodeNameToBeNormalized = ServletRequestUtils.getBooleanParameter(req, Render.NORMALIZE_NODE_NAME, false);
        if (StringUtils.isBlank(nodeName)) {
            String nodeNameProperty = "jcr:title";
            if (parameters.get(Render.NODE_NAME_PROPERTY) != null) {
                nodeNameProperty = parameters.get(Render.NODE_NAME_PROPERTY).get(0);
            }
            if (parameters.get(nodeNameProperty) != null) {
                nodeName = JCRContentUtils.generateNodeName(parameters.get(nodeNameProperty).get(0));
            } else {
                nodeName = nodeType.substring(nodeType.lastIndexOf(":") + 1);
            }
            if (isNodeNameToBeNormalized) {
                nodeName = JCRContentUtils.generateNodeName(nodeName);
            }
            nodeName = JCRContentUtils.findAvailableNodeName(node, nodeName);
        } else if (isNodeNameToBeNormalized) {
            nodeName = JCRContentUtils.generateNodeName(nodeName);
        } else {
            nodeName = JCRContentUtils.escapeLocalNodeName(nodeName);
        }
        if(forceCreation) {
            nodeName = JCRContentUtils.findAvailableNodeName(node, nodeName);
        }
        try {
            newNode = node.getNode(nodeName);
            if (!newNode.isCheckedOut()) {
                newNode.checkout();
            }
        } catch (PathNotFoundException e) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            newNode = node.addNode(nodeName, nodeType);
        }

        if (parameters.containsKey(Constants.JCR_MIXINTYPES)) {
            for (Object o : ((ArrayList) parameters.get(Constants.JCR_MIXINTYPES))) {
                String mixin = (String) o;
                newNode.addMixin(mixin);
            }
        }
        setProperties(newNode, parameters);

        return newNode;
    }

    protected void setProperties(JCRNodeWrapper newNode, Map<String, List<String>> parameters) throws RepositoryException {
        Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
        for (Map.Entry<String, List<String>> entry : set) {
            String key = entry.getKey();
            if (!Render.RESERVED_PARAMETERS.contains(key)) {
                List<String> values = entry.getValue();
                ExtendedPropertyDefinition propertyDefinition = null;
                propertyDefinition = newNode.getApplicablePropertyDefinition(key);
                if (propertyDefinition == null) {
                    continue;
                }
                if (propertyDefinition.isMultiple()) {
                    newNode.setProperty(key, values.toArray(new String[values.size()]));
                } else if (values.get(0).length() > 0) {
                    if (propertyDefinition.getRequiredType() == ExtendedPropertyType.DATE) {
                        DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values.get(0));
                        newNode.setProperty(key, dateTime.toCalendar(Locale.ENGLISH));
                    } else {
                        newNode.setProperty(key, values.get(0));
                    }
                }
            }
        }
    }

    protected JSONObject getJSONConstraintError(ConstraintViolationException e) throws RepositoryException {
        Map<String,String> m = new HashMap<String, String>();
        m.put("message",e.getMessage());
        if (e instanceof NodeConstraintViolationException) {
            m.put("constraintMessage",((NodeConstraintViolationException)e).getConstraintMessage());
            Locale locale = ((NodeConstraintViolationException) e).getLocale();
            if (locale != null) {
                m.put("locale", locale.toString());
            }
            m.put("path",((NodeConstraintViolationException)e).getPath());
        }
        if (e instanceof PropertyConstraintViolationException) {
            ExtendedPropertyDefinition definition = ((PropertyConstraintViolationException) e).getDefinition();
            m.put("propertyName", definition.getName());
            m.put("propertyLabel", definition.getLabel(LocaleContextHolder.getLocale(), definition.getDeclaringNodeType()));
        }
        return new JSONObject(m);
    }


    public abstract ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                           JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;

    /**
     * Returns the current user.
     * 
     * @return current user
     */
    protected JahiaUser getCurrentUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.bin.Action#getName()
     */
    public String getName() {
        return name != null ? name : StringUtils.uncapitalize(StringUtils.substringBeforeLast(getClass().getSimpleName(), "Action"));
    }

    /**
     * Returns a permission, required to execute this action or <code>null</code> if no particular permission is required.
     * 
     * @return a permission, required to execute this action or <code>null</code> if no particular permission is required
     */
    public String getRequiredPermission() {
        return requiredPermission;
    }


    /**
     * Returns JCR workspace name this action should be executed for; <code>null</code> if there is no required workspace.
     * 
     * @return JCR workspace name this action should be executed for; <code>null</code> if there is no required workspace.
     */
    public String getRequiredWorkspace() {
        return requiredWorkspace;
    }
    
    public boolean isPermitted(JCRNodeWrapper node) throws RepositoryException {
        if (StringUtils.isEmpty(getRequiredPermission())) {
            return true;
        }

        return JahiaControllerUtils.hasRequiredPermission(node, getCurrentUser(),
                getRequiredPermission());
    }

    /**
     * Returns <code>true</code> if the action can be executed only by an authenticated user.
     * 
     * @return <code>true</code> if the action can be executed only by an authenticated user
     */
    public boolean isRequireAuthenticatedUser() {
        return requireAuthenticatedUser;
    }
    
    /**
     * Sets the action name.
     * 
     * @param name the action name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Defines if the action can be executed only by an authenticated user.
     * 
     * @param requireAuthenticatedUser
     *            <code>true</code> if the action can be executed only by an authenticated user
     */
    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }
    
    /**
     * Defines a permission, required to execute this action or <code>null</code> if no particular permission is required.
     * 
     * @param requiredPermission
     *            a permission, required to execute this action or <code>null</code> if no particular permission is required
     */
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    /**
     * Sets the JCR workspace name this action should be executed for; <code>null</code> if there is no required workspace.
     * 
     * @param requiredWorkspace
     *            the JCR workspace name this action should be executed for; <code>null</code> if there is no required workspace
     */
    public void setRequiredWorkspace(String requiredWorkspace) {
        this.requiredWorkspace = requiredWorkspace;
    }

}
