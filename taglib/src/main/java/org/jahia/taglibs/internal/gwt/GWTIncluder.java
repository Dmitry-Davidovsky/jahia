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
package org.jahia.taglibs.internal.gwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.util.Map;

/**
 * Helper class for generating script element with the GWT module.
 *
 * @author Romain Felden
 */
public class GWTIncluder {

    public static final String GWT_MODULE_PATH = "/gwt";

    /**
     * Generate the import string for a given module.
     *
     * @param pageContext the page context to format the path
     * @param module      the fully qualified module name
     * @return the string to write to html
     */
    public static String generateGWTImport(PageContext pageContext, String module) {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        return generateGWTImport(request, response, module);
    }

    public static String generateGWTImport(HttpServletRequest request, HttpServletResponse response, String module ) {
        StringBuilder ret = new StringBuilder();
        final String gwtModulePath = response.encodeURL(new StringBuilder(64).append(request.getContextPath()).append(GWT_MODULE_PATH + "/")
                .append(module).append("/").append(module)
                .append(".nocache.js").toString());
        return ret.append("<script id='jahia-gwt' type='text/javascript' src='").append(gwtModulePath).append("'></script>\n").toString();
    }

    /**
     * Get place holder for a jahiaModule  .
     * Example: <div jahiaType="categoriesPiker" start="/root" id="cat_2"/>
     * @param templateUsage true means that the module is used in a template
     * @param cssClassName  the css class name
     * @param jahiaType   the jahiaType
     * @param id  the id
     * @param extraParams map of extra parameter. Example {("start","/root"}
     * @return place holder for a jahiaModule
     */
    public static String generateJahiaModulePlaceHolder(boolean templateUsage, String cssClassName, String jahiaType, String id, Map<String, Object> extraParams) {
        // css depending on type of module
        StringBuilder css = new StringBuilder();
        if (templateUsage) {
            css.append("jahia-template-gxt");
        } else {
            css.append("jahia-admin-gxt");
        }
        if (jahiaType != null) {
            css.append(" ").append(jahiaType).append("-gxt");
        }
        if (cssClassName != null) {
            css.append(" ").append(cssClassName);
        }

        final StringBuilder outBuf = new StringBuilder("<div class=\"").append(css).append("\" id=\"").append(id).append("\" ");
        if (jahiaType != null) {
            outBuf.append("jahiatype=\"");
            outBuf.append(jahiaType);
            outBuf.append("\"");
            outBuf.append(" ");
            outBuf.append(getParam(extraParams));
            outBuf.append("></div>\n");
        } else {
            outBuf.append(extraParams);
            outBuf.append("></div>\n");
        }
        return outBuf.toString();
    }

    protected static String getParam(Map<String, Object> extraParams) {
        final StringBuilder outBuf = new StringBuilder();
        for (String name : extraParams.keySet()) {
            Object value = extraParams.get(name);
            if (value == null) {
                value = "";
            }
            outBuf.append(name).append("=\"").append(value).append("\" ");
        }
        return outBuf.toString();
    }


}
