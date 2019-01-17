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
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.*;
import org.jahia.services.render.*;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import java.io.IOException;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ListTag extends ModuleTag implements ParamParent {

    private static final long serialVersionUID = -3608856316200861402L;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ListTag.class);

    private String listType = "jnt:contentList";

    public void setListType(String listType) {
        this.listType = listType;
    }

    @Override
    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        return "area";
    }

    @Override
    protected void missingResource(RenderContext renderContext, Resource currentResource) throws RepositoryException, IOException {
        try {
            if (renderContext.isEditMode()) {
                JCRSessionWrapper session = currentResource.getNode().getSession();
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if(!nodeWrapper.isCheckedOut())
                        nodeWrapper.checkout();
                    node = nodeWrapper.addNode(path, listType);
                    session.save();
                } else {

                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    if(!parent.isCheckedOut())
                        parent.checkout();
                    node = parent.addNode(StringUtils.substringAfterLast(path, "/"), listType);
                    session.save();
                }
            }
        } catch (ConstraintViolationException e) {
            super.missingResource(renderContext, currentResource);
        } catch (RepositoryException e) {
            logger.error("Cannot create area",e);
        }
    }

}