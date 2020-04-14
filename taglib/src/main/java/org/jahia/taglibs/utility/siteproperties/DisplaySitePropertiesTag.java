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
package org.jahia.taglibs.utility.siteproperties;

import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspWriter;
import java.util.ResourceBundle;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplaySitePropertiesTag extends AbstractJahiaTag {

    private static final transient Logger logger = LoggerFactory.getLogger(DisplaySitePropertiesTag.class);

    public String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int doStartTag() {
        try {
            final JahiaSite theSite = getRenderContext().getSite();
            final StringBuilder buff = new StringBuilder();
            ResourceBundle i18n = retrieveResourceBundle();

            buff.append("<ul>\n");

            buff.append("<li class=\"siteKey\">");
            buff.append(i18n.getString("siteproperty.siteKey"));
            buff.append(": ");
            buff.append(theSite.getSiteKey());
            buff.append("</li>\n");

            buff.append("<li class=\"sitename\">");
            buff.append(i18n.getString("siteproperty.sitename"));
            buff.append(": ");
            buff.append(theSite.getTitle());
            buff.append("</li>\n");

            buff.append("<li class=\"servername\">");
            buff.append(i18n.getString("siteproperty.servername"));
            buff.append(": ");
            buff.append(theSite.getServerName());
            buff.append("</li>\n");

            buff.append("<li class=\"templates\">");
            buff.append(i18n.getString("siteproperty.templates"));
            buff.append(": ");
            buff.append(theSite.getTemplatePackageName());
            buff.append("</li>\n");

            buff.append("</ul>\n");

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }
}
