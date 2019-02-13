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
package org.jahia.bundles.blueprint.extender.config;

import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseCheckException;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Listener for OSGi bundle application context life cycle events. Performs logging of events. Stops the corresponding bundle if the context
 * initialization fails. Performs cross-module import/exports of Spring beans.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBundleApplicationContextListener implements
        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleApplicationContextListener.class);

    private static Boolean getBooleanValue(SettingsBean settings, String key, boolean defaultValue) {
        String value = settings.getPropertiesFile().getProperty(key);
        return value != null ? Boolean.valueOf(value.trim()) : defaultValue;
    }

    private boolean skipStopOnInvalidBundleContext = true;

    private boolean stopBundleIfContextFails = true;

    private boolean isBundleContextValid(OsgiBundleContextFailedEvent event) {
        return event.getBundle() != null
                ? JahiaOsgiBundleXmlApplicationContext.isBundleContextValid(event.getBundle().getBundleContext())
                : false;
    }

    protected void logEvent(OsgiBundleApplicationContextEvent event, String bundleDisplayName) {
        if (event instanceof OsgiBundleContextRefreshedEvent) {
            logger.info("Application context successfully refreshed for bundle {}", bundleDisplayName);
        }

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
            if (shouldStopBundle(failureEvent)) {
                logger.error("Application context refresh failed for bundle " + bundleDisplayName,
                        failureEvent.getFailureCause());
            } else {
                logger.warn("Application context refresh failed for bundle " + bundleDisplayName
                        + " because the bundle context is no longer valid", failureEvent.getFailureCause());
            }
        }

        if (event instanceof OsgiBundleContextClosedEvent) {
            OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
            Throwable error = closedEvent.getFailureCause();

            if (error == null) {
                logger.info("Application context succesfully closed for bundle {}", bundleDisplayName);
            } else {
                logger.error("Application context close failed for bundle " + bundleDisplayName, error);
            }
        }
    }

    public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        Bundle bundle = event.getBundle();
        String bundleDisplayName = OsgiStringUtils.nullSafeNameAndSymName(event.getBundle());

        logEvent(event, bundleDisplayName);

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent contextFailedEvent = (OsgiBundleContextFailedEvent) event;
            Throwable cause = getRootCause(contextFailedEvent.getFailureCause());
            BundleUtils.setContextStartException(bundle.getSymbolicName(),cause);
            JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getRegisteredModules().get(bundle.getSymbolicName());
            if (module != null) {
                module.getState().setState(ModuleState.State.SPRING_NOT_STARTED);
            }

            if (cause instanceof LicenseCheckException) {
                logger.info("Stopping module, no license");
                try {
                    bundle.stop();
                    logger.info("...bundle {} stopped", bundleDisplayName);
                } catch (BundleException e) {
                    logger.error("Unable to stop bundle " + bundleDisplayName + " due to: " + e.getMessage(), e);
                }
            } else {
                // do we need to stop the bundle on context failure?
                if (stopBundleIfContextFails) {
                    if (shouldStopBundle(contextFailedEvent)) {
                        logger.info("Stopping bundle {}", bundleDisplayName);
                        try {
                            bundle.stop();
                            logger.info("...bundle {} stopped", bundleDisplayName);
                        } catch (BundleException e) {
                            logger.error("Unable to stop bundle " + bundleDisplayName + " due to: " + e.getMessage(), e);
                        }
                    }
                } else {
                    logger.error("Cannot start spring context for bundle {}", bundleDisplayName);
                }
                return;
            }
        }

        if (event instanceof OsgiBundleContextRefreshedEvent && BundleUtils.isJahiaModuleBundle(bundle)) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            // set the context
            module.setContext((AbstractApplicationContext) event.getApplicationContext());
            BundleUtils.setContextStartException(bundle.getSymbolicName(), null);

            TemplatePackageRegistry moduleRegistry = null;
            // if module's Jahia late-initialization services were not initialized yet and the global initialization was already done
            // (isAfterInitializeDone() == true) -> initialize services
            if (module != null
                    && !module.isServiceInitialized()
                    && (moduleRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .getTemplatePackageRegistry()).isAfterInitializeDone()) {
                // initializing services for module
                moduleRegistry.afterInitializationForModule(module);
            }
        }
    }

    public Throwable getRootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    public void setStopBundleIfContextFails(boolean stopBundleIfContextFails) {
        this.stopBundleIfContextFails = stopBundleIfContextFails;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SettingsBean settings = SettingsBean.getInstance();

        // if not explicitly set, we stop the bundle by default unless we are in development mode
        stopBundleIfContextFails = getBooleanValue(settings, "jahia.modules.stopBundleIfContextFails",
                !settings.isDevelopmentMode());

        // if not explicitly set, we skip stopping bundle with invalid bundle context by default
        skipStopOnInvalidBundleContext = getBooleanValue(settings, "jahia.modules.skipStopOnInvalidBundleContext",
                true);

        logger.info(
                "Initialized listener for OSGi bundle application context life cycle events with settings: "
                        + "stopBundleIfContextFails={}, skipStopOnInvalidBundleContext={}",
                stopBundleIfContextFails, skipStopOnInvalidBundleContext);
    }

    private boolean shouldStopBundle(OsgiBundleContextFailedEvent contextFailedEvent) {
        // should we skip stop if the bundle context is already invalid?
        return !skipStopOnInvalidBundleContext || isBundleContextValid(contextFailedEvent);
    }
}
