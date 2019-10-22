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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.BasicCredentialsProvider;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * User: toto
 * Date: 27 déc. 2007
 * Time: 19:30:31
 * 
 */
public class JahiaSessionCredentials extends BasicCredentialsProvider {

    /**
     * Constructs a new JahiaSessionCredentials with {@link BasicCredentialsProvider#EMPTY_DEFAULT_HEADER_VALUE}
     * as the default header value.
     */
    public JahiaSessionCredentials() {
        this(EMPTY_DEFAULT_HEADER_VALUE);
    }

    public JahiaSessionCredentials(String defaultHeaderValue) {
        super(defaultHeaderValue);
    }

    @Override
    public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
        JahiaUser jahiaUser = (JahiaUser) request.getSession(true).getAttribute("org.jahia.usermanager.jahiauser");
        if (jahiaUser != null) {
            request.setAttribute("isGuest", Boolean.FALSE);
            return JahiaLoginModule.getCredentials(jahiaUser.getName(), jahiaUser.getRealm());
        } else {
            SimpleCredentials c = (SimpleCredentials) super.getCredentials(request);
            if (c != null) {
                return c;
            }            
        }
        request.setAttribute("isGuest", Boolean.TRUE);
        return JahiaLoginModule.getGuestCredentials();
    }

}
