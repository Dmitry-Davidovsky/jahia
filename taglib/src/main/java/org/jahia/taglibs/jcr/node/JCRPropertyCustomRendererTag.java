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
package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.*;

/**
 * This Tag allows access to specific property of a node.
 * <p/>
 *
 * @author cmailleux
 */
public class JCRPropertyCustomRendererTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 1707457930932639809L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRPropertyCustomRendererTag.class);
    private JCRNodeWrapper node;
    private String name;
    private String var;
    private String renderer;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    /**
     * Default processing of the start tag, returning SKIP_BODY.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        int returnValue = SKIP_BODY;
        if (var != null) {
            pageContext.removeAttribute(var);
        }
        try {
            final Property property = node.getProperty(name);
            if (property != null) {
                boolean isMultiple = property.getDefinition().isMultiple();
                if (!"".equals(renderer)) {
                    final ChoiceListRenderer renderer1 = ChoiceListRendererService.getInstance().getRenderers().get(
                            renderer);
                    final RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext",
                                                                                           PageContext.REQUEST_SCOPE);
                    List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
                    Map<String,Object> m = new HashMap<String,Object>();
                    String s = "";
                    if (isMultiple) {
                        for (Value v : property.getValues()) {
                            if (var != null) {
                                l.add(renderer1.getObjectRendering(renderContext, (ExtendedPropertyDefinition) property.getDefinition(), v.getString()));
                            } else {
                                s = (!"".equals(s)?s + ", ":"")  + renderer1.getStringRendering(renderContext, (ExtendedPropertyDefinition) property.getDefinition(), v.getString());
                            }
                        }
                    } else {
                        if (var !=null) {
                            m = renderer1.getObjectRendering(renderContext,(JCRPropertyWrapper) property);
                        } else {
                            s = renderer1.getStringRendering(renderContext,(JCRPropertyWrapper) property);
                        }
                    }
                    if (var != null) {
                        pageContext.setAttribute(var, isMultiple ?l:m);
                        returnValue = EVAL_BODY_INCLUDE;
                    } else {
                        pageContext.getOut().print(s);

                    }
                } else if (var != null) {
                    returnValue = EVAL_BODY_INCLUDE;
                    if (isMultiple) {
                        pageContext.setAttribute(var, property.getValues());
                    } else {
                        pageContext.setAttribute(var, property.getValue());
                    }
                } else if (!isMultiple) {
                    pageContext.getOut().print(property.getValue().getString());
                } else {
                    Value[] values1 = property.getValues();
                    for (Value value : values1) {
                        pageContext.getOut().print(value.getString() + "<br/>");
                    }
                }
            }
        } catch (PathNotFoundException e) {
            logger.debug("Property : " + name + " not found in node " + node.getPath());
        } catch (ConstraintViolationException e) {
            logger.warn("Property : " + name + " not defined in node " + node.getPath());
        } catch (RepositoryException e) {
            throw new JspException(e);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return returnValue;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }

    /**
     * Specify the name of the property you want to get value of.
     *
     * @param name the name of the property you want to get value of
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * If you do not want to output directly the value of the property (call javax.jcr.Value.getString())
     * The define a value for this.
     *
     * @param var The name in the pageContext in which you will find the javax.jcr.Value or javax.jcr.Value[] object associated with this property
     */
    public void setVar(String var) {
        this.var = var;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void resetState() {
        name = null;
        node = null;
        renderer = null;
        var = null;
        super.resetState();
    }
}
