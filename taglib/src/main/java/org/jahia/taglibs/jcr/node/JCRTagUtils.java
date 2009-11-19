/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.node;

import org.apache.jackrabbit.rmi.iterator.ArrayIterator;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.NodeIteratorImpl;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.Locale;

/**
 * JCR content related utilities.
 * User: jahia
 * Date: 28 mai 2009
 * Time: 15:46:07
 */
public class JCRTagUtils {
    private static final transient Logger logger = Logger.getLogger(JCRTagUtils.class);

    /**
     * Get the label value depending on the current local
     *
     * @param nodeObject
     * @return
     */
    public static String label(Object nodeObject) {
        return label(nodeObject, Jahia.getThreadParamBean().getLocale());
    }

    /**
     * Get the node or property display name depending on the locale
     *
     * @param item the item to get the label for
     * @param locale current locale
     * @return the node or property display name depending on the locale
     */
    public static String label(Object nodeObject, Locale local) {
        return JCRContentUtils.getDisplayLabel(nodeObject, local);
    }

    /**
     * Get the label value dependind on the local
     *
     * @param nodeObject
     * @param locale as a string
     * @return
     */
    public static String label(Object nodeObject, String locale) {
        return label(nodeObject, LanguageCodeConverters.languageCodeToLocale(locale));
    }

    public static boolean isNodeType(JCRNodeWrapper node, String type) {        
        try {
            return node.isNodeType(type);
        } catch (RepositoryException e) {
            logger.error(e, e);
            return false;
        }
    }

    public static NodeIterator getNodes(JCRNodeWrapper node, String type) {
        try {
            return node.getSession().getWorkspace().getQueryManager().createQuery("select * from ["+type+"] as sel where ischildnode(sel,['"+node.getPath()+"'])",
                                                                                  Query.JCR_SQL2).execute().getNodes();
        } catch (InvalidQueryException e) {
            logger.error("Error while retrieving nodes", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes", e);
        }
        return new NodeIteratorImpl(new ArrayIterator(new Object[0]), 0);
    }

	/**
	 * Returns the first parent of the current node that has the specified node
	 * type. If no matching node is found, <code>null</code> is returned.
	 * 
	 * @param node
	 *            the current node to start the lookup from
	 * @param type
	 *            the required type of the parent node
	 * @return the first parent of the current node that has the specified node
	 *         type. If no matching node is found, <code>null</code> is returned
	 */
	public static JCRNodeWrapper getParentOfType(JCRNodeWrapper node, String type) {
		JCRNodeWrapper matchingParent = null;
		try {
			JCRNodeWrapper parent = node.getParent();
			while (parent != null) {
				if (parent.isNodeType(type)) {
					matchingParent = parent;
					break;
				}
				parent = parent.getParent();
			}
		} catch (ItemNotFoundException e) {
			// we reached the hierarchy top
		} catch (RepositoryException e) {
			logger.error("Error while retrieving nodes parent node. Cause: " + e.getMessage(), e);
		}
		return matchingParent;
	}
}
