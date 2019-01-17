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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.Node;

/**
 * 
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:39:18
 * 
 */
public class JCRLayoutItemNode extends JCRNodeDecorator {

    public JCRLayoutItemNode(JCRNodeWrapper node) {
        super(node);
    }

    public Node getPortlet() throws RepositoryException {
        return getProperty("j:portlet").getNode();
    }

    public void setPortlet(JCRNodeWrapper portletNode) throws RepositoryException {
        setProperty("j:portlet", portletNode);
    }

    public int getColumnIndex() throws RepositoryException {
        return (int) getProperty("j:columnIndex").getLong();
    }

    public void setColumnIndex(int columnIndex) throws RepositoryException {
        setProperty("j:columnIndex", columnIndex);
    }

    public int getRowIndex() throws RepositoryException {
        return (int) getProperty("j:rowIndex").getLong();
    }

    public void setRowIndex(int rowIndex) throws RepositoryException {
        setProperty("j:rowIndex", rowIndex);
    }

    public String getStatus() throws RepositoryException {
        return getProperty("j:status").getString();
    }

    public void setStatus(String status) throws RepositoryException {
        setProperty("j:status", status);
    }
}
