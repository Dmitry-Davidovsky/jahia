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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

/**
 * Content export handler.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 2 avr. 2010
 */
public class Export extends JahiaController implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Export.class);
    public static final String CLEANUP = "cleanup";
    private static final String CONTROLLER_MAPPING = "/export";
    private static final String EXPORT_SITES_REQUIRED_PERMISSION = "adminVirtualSites";
    private static final Pattern URI_PATTERN = Pattern.compile(CONTROLLER_MAPPING + "/("
            + Constants.LIVE_WORKSPACE + "|" + Constants.EDIT_WORKSPACE + ")/(.*)\\.(xml|zip)");

    public static String getExportServletPath() {
        // TODO move this into configuration
        return "/cms" + CONTROLLER_MAPPING;
    }

    private String cleanupXsl;
    
    private boolean downloadExportedXmlAsFile;

    private ImportExportService importExportService;
    private String templatesCleanupXsl;

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {

            checkUserLoggedIn();

            Matcher m = StringUtils.isNotEmpty(request.getPathInfo()) ? URI_PATTERN.matcher(request.getPathInfo()) : null;
            if (m == null || !m.matches()) {
                throw new JahiaBadRequestException("Requested URI '" + request.getRequestURI()
                        + "' is malformed");
            }
            String workspace = m.group(1);
            String nodePath = "/" + m.group(2);
            String exportFormat = m.group(3);
            String serverDirectory = null;
            if (StringUtils.isNotEmpty(request.getParameter("exportformat"))) {
                exportFormat = request.getParameter("exportformat");
            }

            Map<String, Object> params = getParams(request);

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            JCRNodeWrapper exportRoot = null;

            if (request.getParameter("root") != null) {
                exportRoot = session.getNode(request.getParameter("root"));
            }

            if (StringUtils.isNotBlank(request.getParameter("exportPath"))) {
                serverDirectory = request.getParameter("exportPath");
                params.put(ImportExportService.SERVER_DIRECTORY, serverDirectory);
            }

            if ("all".equals(exportFormat) || "site".equals(exportFormat)) {
                if (JahiaUserManagerService.isGuest(session.getUser())) {
                    throw new JahiaUnauthorizedException("User guest is not allowed to export site content");
                } else if (!session.getRootNode().hasPermission(EXPORT_SITES_REQUIRED_PERMISSION)) {
                    throw new JahiaForbiddenAccessException(
                            "User has no sufficient permissions to perform export of site content");
                }
                JahiaUser userToReset = null;
                if (!session.getUser().isRoot()) {
                    // if an authorized user is not root, we explicitly use root user for the export
                    userToReset = JCRSessionFactory.getInstance().getCurrentUser();
                    JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
                }
                try {
                    if ("all".equals(exportFormat)) {
        
                        response.setContentType("application/zip");
                        //make sure this file is not cached by the client (or a proxy middleman)
                        WebUtils.setNoCacheHeaders(response);
        
                        params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                        params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                        params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                        params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                        params.put(ImportExportService.XSL_PATH, cleanupXsl);
        
                        OutputStream outputStream = response.getOutputStream();
                        importExportService.exportAll(outputStream, params);
                        outputStream.close();
        
                    } else if ("site".equals(exportFormat)) {
        
                        List<JCRSiteNode> sites = new ArrayList<JCRSiteNode>();
                        String[] sitekeys = request.getParameterValues("sitebox");
                        if (sitekeys != null) {
                            for (String sitekey : sitekeys) {
                                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                                sites.add((JCRSiteNode) site);
                            }
                        }
        
                        if (sites.isEmpty()) {
                            // Todo redirect to new administration
                        } else {
                            response.setContentType("application/zip");
                            //make sure this file is not cached by the client (or a proxy middleman)
                            WebUtils.setNoCacheHeaders(response);
        
                            params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                            params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                            params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                            params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                            if (request.getParameter("live") == null || Boolean.valueOf(request.getParameter("live"))) {
                                params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
                            }
                            if (request.getParameter("users") == null && SettingsBean.getInstance().getPropertiesFile().getProperty("siteExportUsersDefaultValue") != null) {
                                Boolean siteExportUsersDefaultValue = Boolean.valueOf(SettingsBean.getInstance().getPropertiesFile().getProperty("siteExportUsersDefaultValue"));
                                if (siteExportUsersDefaultValue.booleanValue()) {
                                    params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
                                } else {
                                    params.remove(ImportExportService.INCLUDE_USERS);
                                }
                            } else if (request.getParameter("users") != null) {
                                if (Boolean.valueOf(request.getParameter("users"))) {
                                    params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
                                } else {
                                    params.remove(ImportExportService.INCLUDE_USERS);
                                }
                            } else {
                                params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
                            }
                            params.put(ImportExportService.INCLUDE_ROLES, Boolean.TRUE);
                            params.put(ImportExportService.INCLUDE_MOUNTS, Boolean.TRUE);
                            params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                            params.put(ImportExportService.XSL_PATH, cleanupXsl);
        
                            OutputStream outputStream = response.getOutputStream();
                            importExportService.exportSites(outputStream, params, sites);
                            outputStream.close();
                        }
                    }
                } finally {
                    if (userToReset != null)  {
                        JCRSessionFactory.getInstance().setCurrentUser(userToReset);
                    }
                }
            } else if ("xml".equals(exportFormat)) {

                JCRNodeWrapper node = session.getNode(nodePath);
                response.setContentType("text/xml");
                //make sure this file is not cached by the client (or a proxy middleman)
                WebUtils.setNoCacheHeaders(response);
                if (downloadExportedXmlAsFile) {
                    WebUtils.setFileDownloadHeaders(response, StringUtils.substringBeforeLast(node.getName(), ".") + ".xml");
                }

                if ("template".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, templatesCleanupXsl);
                } else if ("simple".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, cleanupXsl);
                }
                OutputStream outputStream = response.getOutputStream();
                Cookie exportedNode = new Cookie("exportedNode", node.getIdentifier());
                exportedNode.setMaxAge(60);
                exportedNode.setPath("/");
                response.addCookie(exportedNode);
                //No export log for the node export
                importExportService.exportNode(node, exportRoot, outputStream, params);

            } else if ("zip".equals(exportFormat)) {

                JCRNodeWrapper node = session.getNode(nodePath);
                response.setContentType("application/zip");
                //make sure this file is not cached by the client (or a proxy middleman)
                WebUtils.setNoCacheHeaders(response);

                if ("template".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, templatesCleanupXsl);
                } else if ("simple".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, cleanupXsl);
                }
                if (request.getParameter("live") == null || Boolean.valueOf(request.getParameter("live"))) {
                    params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
                }
                OutputStream outputStream = response.getOutputStream();
                Cookie exportedNode = new Cookie("exportedNode", node.getIdentifier());
                exportedNode.setMaxAge(60);
                exportedNode.setPath("/");
                response.addCookie(exportedNode);
                importExportService.exportZip(node, exportRoot, outputStream, params);
                outputStream.close();
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            if (logger.isDebugEnabled())
                logger.debug("Cannot export due to some IO exception", e);
            else logger.warn("Cannot export due to some IO exception :" + e.getMessage());
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } catch (Exception e) {
            logger.error("Cannot export", e);
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }

        return null;
    }

    private Map<String, Object> getParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>(6);
        params.put(ImportExportService.VIEW_CONTENT, !"false".equals(request.getParameter("viewContent")));
        params.put(ImportExportService.VIEW_VERSION, "true".equals(request.getParameter("viewVersion")));
        params.put(ImportExportService.VIEW_ACL, !"false".equals(request.getParameter("viewAcl")));
        params.put(ImportExportService.VIEW_METADATA, !"false".equals(request.getParameter("viewMetadata")));
        params.put(ImportExportService.VIEW_JAHIALINKS, !"false".equals(request.getParameter("viewLinks")));
        params.put(ImportExportService.VIEW_WORKFLOW, "true".equals(request.getParameter("viewWorkflow")));
        return params;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        cleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/" + "cleanup.xsl");
        templatesCleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/"
                + "templatesCleanup.xsl");
    }

    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    public void setDownloadExportedXmlAsFile(boolean downloadExportedXmlAsFile) {
        this.downloadExportedXmlAsFile = downloadExportedXmlAsFile;
    }
}
