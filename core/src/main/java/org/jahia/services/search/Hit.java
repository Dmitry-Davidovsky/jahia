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

import java.util.Date;
import java.util.List;

/**
 * Describes basic properties of a search hit item.
 * 
 * @author Sergiy Shyrkov
 */
public interface Hit<T> {

    /**
     * Returns the MIME type of the hit content, if applicable.
     * 
     * @return the MIME type of the hit content, if applicable
     */
    String getContentType();

    /**
     * Returns the content creation date.
     * 
     * @return the content creation date
     */
    Date getCreated();

    /**
     * Returns the resource author (creator).
     * 
     * @return the resource author (creator)
     */
    String getCreatedBy();

    /**
     * Returns the short description, abstract or excerpt of the hit's content.
     * 
     * @return the short description, abstract or excerpt of the hit's content
     */
    String getExcerpt();

    /**
     * Returns the last modification date.
     * 
     * @return the last modification date
     */
    Date getLastModified();

    /**
     * Returns the last contributor.
     * 
     * @return the last contributor
     */
    String getLastModifiedBy();

    /**
     * Returns the URL to the hit page.
     * 
     * @return the URL to the hit page
     */
    String getLink();

    /**
     * Returns the raw hit object.
     * 
     * @return the raw hit object
     */
    T getRawHit();

    /**
     * Returns the hit score.
     * 
     * @return the hit score
     */
    float getScore();

    /**
     * Returns the title text.
     * 
     * @return the title text
     */
    String getTitle();

    /**
     * Returns the hit type.
     * 
     * @return the hit type
     */
    String getType();

    /**
     * Returns the list of hits that use the current hit. If the underlying implementation doesn't support usages
     * computation, this method should return an empty list.
     *
     * @return the list of hits that use the current hit or an empty list if no hits use this hit (or the
     * implementation doesn't support usages computation)
     */
    List<Hit> getUsages();
}