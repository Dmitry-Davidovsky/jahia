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
package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.servlet.jsp.JspException;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import java.io.IOException;

/**
 * Generates a download URL for a given JCR node using the provided path.
 * This behaves like the standard a tag (surrounding tag).
 *
 * User: romain
 * Date: 27 mai 2009
 * Time: 16:14:28
 */
public class JCRNodeLinkTag extends AbstractJCRTag {

    private static final long serialVersionUID = -8322805743385450335L;

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTag.class);

    private String path =  null;
    private boolean absolute = false;
    private JCRNodeWrapper node = null;

    public void setPath(String path) {
        this.path = path;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public int doStartTag() throws JspException {
        try {
            node = getJCRSession().getNode(path);
            if (node.isFile()) {
                StringBuilder link = new StringBuilder("<a href=\"");
                if (absolute) {
                    link.append(node.getAbsoluteUrl(pageContext.getRequest()));
                } else {
                    link.append(node.getUrl());
                }
                link.append("\">");
                pageContext.getOut().print(link.toString());
            } else {
                logger.warn("The path '" + path + "' is not a file");
            }
        } catch (PathNotFoundException e) {
            logger.error("The path '" + path + "' does not exist");
        } catch (RepositoryException e) {
            logger.error("Could not retrieve JCR node using path '" + path + "'", e);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        if (node != null && node.isFile()) {
            try {
                pageContext.getOut().print("</a>");
            } catch (IOException e) {
                logger.error(e.toString(), e);
            }
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        absolute = false;
        node = null;
        path = null;
        super.resetState();
    }
}
