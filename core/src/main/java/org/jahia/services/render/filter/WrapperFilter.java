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
package org.jahia.services.render.filter;

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 * WrapperFilter
 *
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 *
 */
public class WrapperFilter extends AbstractFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(WrapperFilter.class);

    private String wrapper;

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        JCRNodeWrapper node = resource.getNode();
        if (wrapper == null) {
            while (resource.hasWrapper()) {
                String wrapper = resource.popWrapper();
                previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
            }
        } else {
            previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
        }
        return previousOut;
    }

    private String wrap(RenderContext renderContext, Resource resource, String output, JCRNodeWrapper node,
                        String wrapper) throws RepositoryException {
        try {
//                renderContext.getRequest().setAttribute("wrappedResource", resource);
            Resource wrapperResource = new Resource(node, resource.getTemplateType(),
                    wrapper,
                    Resource.CONFIGURATION_WRAPPER);
            if (service.hasView(node, wrapper, resource.getTemplateType(), renderContext)) {
                Object wrappedContent = renderContext.getRequest().getAttribute("wrappedContent");
                try {
                    renderContext.getRequest().setAttribute("wrappedContent", output);
                    output = RenderService.getInstance().render(wrapperResource, renderContext);
                } finally {
                    renderContext.getRequest().setAttribute("wrappedContent", wrappedContent);
                }
            } else {
                logger.warn("Cannot get wrapper "+wrapper);
            }
        } catch (TemplateNotFoundException e) {
            logger.debug("Cannot find wrapper "+wrapper,e);
        } catch (RenderException e) {
            logger.error("Cannot execute wrapper "+wrapper,e);
        }
        return output;
    }
}
