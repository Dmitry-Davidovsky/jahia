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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.ModuleState;
import org.jahia.data.templates.ModuleState.State;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet is used to catch the end of the initialization of the web application, as the order of
 * initialization of a web application is :
 * - all listeners
 * - all filters
 * - all servlets
 * Using this servlet we can perform actions once the web application has completed, such as perform an
 * HTTP request to it to check if the web application has completed initialization or not.
 */
public class EndInit extends HttpServlet {

    private static final long OSGI_STARTUP_WAIT_TIMEOUT = Long.getLong("org.jahia.osgi.startupWaitTimeout", 10 * 60 * 1000L);

    private static Logger logger = LoggerFactory.getLogger(EndInit.class);

    private static final long serialVersionUID = -2221764992780224013L;

    private boolean initialized = false;

    private void appendModulesInfo(StringBuilder out) {
        JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        out.append("  Modules:");
        for (State state : ModuleState.State.values()) {
            List<Bundle> modules = templateService.getModulesByState(state);
            if (modules.isEmpty()) {
                continue;
            }
            out.append("\n      ").append(state).append(": ").append(modules.size());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("Digital Experience Manager is shutting down, please wait...");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!initialized) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private void finishInit() {
        try {
            JahiaContextLoaderListener.endContextInitialized();
            
            // start schedulers
            ServicesRegistry.getInstance().getSchedulerService().startSchedulers();
        } catch (JahiaInitializationException e) {
            logger.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        
        logger.info("Got into EndInit");
        
        if (OSGI_STARTUP_WAIT_TIMEOUT > 0) {
            waitForStartup();
        }
        
        finishInit();
        
        printEndMessage();
        
        initialized = true;
    }

    private void printEndMessage() {
        long initializationTime = System.currentTimeMillis() - JahiaContextLoaderListener.getStartupTime() ;
        StringBuilder out = new StringBuilder(256);
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  D E V E L O P M E N T   M O D E   A C T I V E" +
            "\n" +
            "\n  In development mode, Digital Experience Manager will allow JSPs to be modified, modules to be" +
            "\n  re-deployed and other modifications to happen immediately, but these DO have a performance impact." +
            "\n  It is strongly recommended to switch to production mode when running performance tests or going live." +
            "\n  The setting to change modes is called operatingMode in the jahia.properties configuration file.");
        } else if (SettingsBean.getInstance().isDistantPublicationServerMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  D I S T A N T  P U B L I C A T I O N  S E R V E R  M O D E   A C T I V E");
        } else {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  P R O D U C T I O N   M O D E   A C T I V E");
        }
        out.append("\n--------------------------------------------------------------------------------------------------\n");
        appendModulesInfo(out);
        out.append("\n--------------------------------------------------------------------------------------------------"+
        "\n  ").append(Jahia.getFullProductVersion()).append(" is now ready. Initialization completed in ").append((initializationTime/1000)).append(" seconds");
        out.append("\n--------------------------------------------------------------------------------------------------");
        logger.info(out.toString());
    }

    private void waitForStartup() {
        boolean stopWaiting = false;
        FrameworkService instance = FrameworkService.getInstance();
        synchronized (instance) {
            if (!instance.isStarted()) {
                logger.info("Start waiting for OSGi framework startup");
                while (!stopWaiting && !instance.isStarted()) {
                    try {
                        instance.wait(OSGI_STARTUP_WAIT_TIMEOUT);
                        stopWaiting = true;
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                logger.info("Stopped waiting for OSGi framework startup");
            }
        }
    }

}
