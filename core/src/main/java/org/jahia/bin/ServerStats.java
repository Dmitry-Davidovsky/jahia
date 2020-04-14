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

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.RequestLoadAverage;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

/**
 * Simple servlet to expose server statistics in JSON.
 */
public class ServerStats implements Controller {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ServerStats.class);

    private RequestLoadAverage requestLoadAverage;

    public void setRequestLoadAverage(RequestLoadAverage requestLoadAverage) {
        this.requestLoadAverage = requestLoadAverage;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        String sessionId = null;
        try {
            if (logger.isInfoEnabled()) {
                sessionId = request.getSession().getId();
            }
            if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
                handle(request, response);
            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS, POST");
            } else {
                response.sendError(SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(request.getRequestURI());
                JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
                if (user != null) {
                    sb.append("] user=[").append(user.getUsername());
                }
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[").append(
                        sessionId).append("] in [").append(
                        System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException, JSONException {

        response.setContentType("application/json; charset=UTF-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currentRequestCount", JahiaContextLoaderListener.getRequestCount());
        jsonObject.put("currentSessionCount", JahiaContextLoaderListener.getSessionCount());
        jsonObject.put("oneMinuteRequestLoadAverage", requestLoadAverage.getOneMinuteLoad());
        jsonObject.put("fiveMinuteRequestLoadAverage", requestLoadAverage.getFiveMinuteLoad());
        jsonObject.put("fifteenMinuteRequestLoadAverage", requestLoadAverage.getFifteenMinuteLoad());
        jsonObject.write(response.getWriter());
    }
}
