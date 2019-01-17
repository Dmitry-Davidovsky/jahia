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
package org.jahia.params.valves;

import org.jahia.bin.listeners.JahiaContextLoaderListener.RootContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Login configuration settings.
 *
 * @author Sergiy Shyrkov
 */
public class LoginConfig implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoginConfig.class);

    private LoginConfig() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final LoginConfig INSTANCE = new LoginConfig();
    }

    public static LoginConfig getInstance() {
        return Holder.INSTANCE;
    }

    private LoginUrlProvider loginUrlProvider;

    /**
     * Returns custom login URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     *
     * @param request current servlet request
     * @return custom login URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     */
    public String getCustomLoginUrl(HttpServletRequest request) {
        return loginUrlProvider != null ? loginUrlProvider.getLoginUrl(request) : null;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        Map<String, LoginUrlProvider> beansOfType = null;
        if (event instanceof RootContextInitializedEvent) {
            RootContextInitializedEvent rootContextInitializedEvent = (RootContextInitializedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    rootContextInitializedEvent.getContext(),
                    LoginUrlProvider.class);
        } else if (event instanceof ContextRefreshedEvent) {
            ContextRefreshedEvent contextRefreshedEvent = (ContextRefreshedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    contextRefreshedEvent.getApplicationContext(),
                    LoginUrlProvider.class);
        }
        if (beansOfType != null && !beansOfType.isEmpty()) {
            for (LoginUrlProvider provider : beansOfType.values()) {
                if (provider.hasCustomLoginUrl()) {
                    logger.info("Using login URL provider {}", provider);
                    loginUrlProvider = provider;
                    return;
                }
            }
        }
    }

}
