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
package org.jahia.bin;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Jahia specific action controllers.
 * 
 * @author Sergiy Shyrkov
 */
public final class JahiaControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(JahiaControllerUtils.class);
    
    public static void checkUserAuthorized(JahiaUser user, String permissions)
            throws JahiaForbiddenAccessException {
        checkUserAuthorized(null, user, permissions);
    }

    public static void checkUserAuthorized(JCRNodeWrapper node, JahiaUser user, String permissions)
            throws JahiaForbiddenAccessException {
        try {
            if (JahiaUserManagerService.isGuest(user)) {
                throw new JahiaUnauthorizedException(
                        "You need to authenticate yourself to use this service");
            } else if (node != null ? !hasRequiredPermission(node, user, permissions)
                    : !hasRequiredPermission(user, permissions)) {
                throw new JahiaForbiddenAccessException(
                        "You have not enough permissions to use this service");
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get permission", e);
            throw new JahiaForbiddenAccessException(
                    "You have not enough permissions to use this service");
        }
    }

    public static void checkUserLoggedIn(JahiaUser user) throws JahiaForbiddenAccessException {
        if (JahiaUserManagerService.isGuest(user)) {
            throw new JahiaUnauthorizedException(
                    "You need to authenticate yourself to use this service");
        }
    }

    /**
     * Simple utility method to retrieve an integer parameter from a request and throws an {@link JahiaBadRequestException} (results in a 400 error)
     * in case the parameter is not found.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return an integer value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request or a parsing exception occurs
     */
    public final static int getIntParameter(final HttpServletRequest request, final String name)
            throws JahiaBadRequestException {
        final String value = request.getParameter(name);
        if (value == null) {
            throw new JahiaBadRequestException("Missing required '" + name
                    + "' parameter in request.");
        }
        int param = 0;
        if (value != null) {
            try {
                param = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new JahiaBadRequestException(e);
            }
        }
        return param;
    }

    /**
     * Retrieves the integer value of the specified request parameter. If it does not exist, returns the provided default value.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @param defaultValue
     *            the default parameter value
     * @return the value of the specified request parameter. If it does not exist, returns the provided default value
     */
    public final static int getIntParameter(final HttpServletRequest request, final String name,
            int defaultValue) {
        final String value = request.getParameter(name);
        int param = defaultValue;
        if (value != null) {
            try {
                param = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return param;
    }

    /**
     * Simple utility method to retrieve a parameter from a request and throws an {@link JahiaBadRequestException} (results in a 400 error)
     * in case the parameter is not found.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return A String containing the value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request
     */
    public final static String getParameter(final HttpServletRequest request, final String name)
            throws JahiaBadRequestException {
        final String value = request.getParameter(name);
        if (value == null) {
            throw new JahiaBadRequestException("Missing required '" + name
                    + "' parameter in request.");
        }
        return value;
    }

    /**
     * Retrieves the value of the specified request parameter. If it does not exist, returns the provided default value.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @param defaultValue
     *            the default parameter value
     * @return the value of the specified request parameter. If it does not exist, returns the provided default value
     */
    public final static String getParameter(final HttpServletRequest request, final String name,
            String defaultValue) {
        final String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    public static boolean hasRequiredPermission(JahiaUser user, String permissions)
            throws RepositoryException {
        return StringUtils.isNotEmpty(permissions) ? hasRequiredPermission(JCRSessionFactory
                .getInstance().getCurrentUserSession().getRootNode(), user, permissions) : true;
    }

    public static boolean hasRequiredPermission(JCRNodeWrapper node, JahiaUser user,
            String permissions) throws RepositoryException {
        if (StringUtils.isEmpty(permissions)) {
            return true;
        }

        boolean hasPermission = false;

        if (permissions.contains("|") || permissions.contains("+")) {
            // we have multiple permissions
            if (permissions.contains("|") && permissions.contains("+")) {
                throw new IllegalArgumentException("Unsupported permission format '" + permissions
                        + "'");
            }
            boolean andOperator = permissions.contains("+");
            String[] parsedPermissions = andOperator ? Patterns.PLUS.split(permissions) : Patterns.PIPE.split(permissions);
            hasPermission = andOperator;
            for (String perm : parsedPermissions) {
                hasPermission = node.hasPermission(perm.trim());
                if (andOperator) {
                    if (!hasPermission) {
                        break;
                    }
                } else if (hasPermission) {
                    break;
                }
            }
        } else {
            hasPermission = node.hasPermission(permissions);
        }
        return hasPermission;
    }

    /**
     * Initializes an instance of this class.
     */
    private JahiaControllerUtils() {
        super();
    }
}
