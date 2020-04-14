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
package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.services.content.JCRNodeWrapper;

public class PublishedNodeFact extends AbstractNodeFact {
    boolean unpublished;
    String language;
    
    public PublishedNodeFact(JCRNodeWrapper node, String language, boolean unpublished) throws RepositoryException {
        super(node);
        this.language = language;
        this.unpublished = unpublished;
    }

    @Override
    public String getLanguage() {
        return language;
    }
    
    public String toString() {
        return (isUnpublished() ? "un" : "") + "published " + node.getPath() + (isTranslation() ? " in `" + getLanguage() + "`" : "");
    }

    public boolean isTranslation() {
        return language != null;
    }

    public boolean isUnpublished() {
        return unpublished;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        PublishedNodeFact that = (PublishedNodeFact)o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(this.getLanguage(), that.getLanguage())
                .append(this.isUnpublished(), that.isUnpublished()).isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(node).append(workspace).append(
                operationType).append(language).append(unpublished).toHashCode();
    }
}
