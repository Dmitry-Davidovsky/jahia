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
package org.jahia.services.search;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;

import javax.jcr.RepositoryException;

/**
 * File and folder search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public class FileHit extends JCRNodeHit {

    /**
     * Initializes an instance of this class.
     * 
     * @param node search result item to be wrapped
     * @param context
     */
    public FileHit(JCRNodeWrapper node, RenderContext context) {
        super(node, context);
    }

    /**
     * Returns the folder path for this hit.
     * 
     * @return the folder path for this hit
     */
    public String getFolderPath() {
        return isFolder() ? getPath() : FilenameUtils.getFullPathNoEndSeparator(getPath());
    }

    /**
     * Returns an icon name that corresponds to the current item. Mapping
     * between file extensions and icons is configured in the
     * <code>applicationcontext-basejahiaconfig.xml</code> file.
     * 
     * @return an icon name that corresponds to the current item
     */
    public String getIconType() {
        Map<String, String> types = getIconTypes();
        String extension = isFolder() ? "dir" : FilenameUtils.getExtension(getName());
        String icon = StringUtils.isNotEmpty(extension) ? types.get(extension.toLowerCase()) : null;

        return icon != null ? icon : types.get("unknown");
    }

    private Map<String, String> getIconTypes() {
        return JCRContentUtils.getInstance().getFileExtensionIcons();
    }

    public String getLink() {
        return resource.getUrl();
    }

    /**
     * Returns the resource content length in bytes if applicable.
     *
     * @return resource content length in bytes if applicable
     */
    public long getContentLength() {
        return resource.getFileContent().getContentLength();
    }

    /**
     * Returns the resource content length in kilobytes if applicable.
     *
     * @return resource content length in kilobytes if applicable
     */
    public long getContentLengthKb() {
        long length = getContentLength();
        return (long) (length > 0 ? length / 1000f : 0);
    }

    public String getContentType() {
        return resource.getFileContent().getContentType();
    }    

    /**
     * Returns <code>true</code> if this search hit represents a folder.
     * 
     * @return <code>true</code> if this search hit represents a folder
     */
    public boolean isFolder() {
        try {
            return resource.isNodeType("nt:folder");
        } catch (RepositoryException e) {
            return false;
        }
    }
}
