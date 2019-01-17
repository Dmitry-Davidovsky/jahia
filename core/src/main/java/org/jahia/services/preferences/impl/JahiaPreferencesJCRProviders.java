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
package org.jahia.services.preferences.impl;

import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreferencesQueryHelper;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedPropertyException;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JCR based preferences provider implementation.
 * User: jahia
 * Date: 11 mars 2009
 * Time: 12:18:34
 */
public class JahiaPreferencesJCRProviders<T extends JCRNodeWrapper> implements JahiaPreferencesProvider<T> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JahiaPreferencesJCRProviders.class);

    // node name
    private static final String PREFERENCE = "preference";

    // bean values
    private String type;
    private String nodeType;
    private JCRSessionFactory sessionFactory;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public JCRSessionFactory getJCRStoreService() {
        return sessionFactory;
    }

    public void setJCRSessionFactory(JCRSessionFactory jcrStoreService) {
        this.sessionFactory = jcrStoreService;
    }

    /**
     * Create a new JahiaPreference Node
     *
     * @param principal the user creating this preference
     * @return the newly created JahiaPreference
     */
    public JahiaPreference createJahiaPreferenceNode(Principal principal) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Create Jahia Preference Node [" + principal + "]");
            }

            Node preferencesNode = getProviderPreferencesNode(principal);
            if (preferencesNode != null) {
                Node newChildNode = preferencesNode.addNode(PREFERENCE, nodeType);
                return createJahiaPreference(principal, newChildNode);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Preferences node not found for user [" + principal + "]");
                }
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private JahiaPreference createJahiaPreference(Principal p, Node node) {
        try {
            JahiaPreference pref = new JahiaPreference((JCRNodeWrapper) node);
            pref.setPrincipal(p);
            return pref;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Validate the preference
     * @param jahiaPreference to validate
     * @return true if valid throw a JahiaPreferencesNotValidException otherwise
     * @throws JahiaPreferencesNotValidException
     *
     */
    public boolean validate(JahiaPreference jahiaPreference) throws JahiaPreferencesNotValidException {
        return true;
    }

    /**
     * Get the existing JahiaPreference associated to this path
     * @param principal the user for searching the preference
     * @param sqlConstraint the sql constraint of the preference
     * @return a JahiaPreference if found null otherwise 
     */
    public JahiaPreference getJahiaPreference(Principal principal, String sqlConstraint) {
        return getJahiaPreference(principal, sqlConstraint, false);
    }

    /**
     * Get or create the requested preference at the path
     * @param principal the user for searching the preference
     * @param sqlConstraint the sql constraint of the preference
     * @param notNull if true create the preference if not found
     * @return a JahiaPreference if prefrence found or notNull is true, null otherwise
     */
    public JahiaPreference getJahiaPreference(Principal principal, String sqlConstraint, boolean notNull) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get preference -" + getType() + " - " + principal.getName() + " - " + sqlConstraint + "- ");
        }
        List<JahiaPreference<T>> jahiaPreferences = findJahiaPreferences(principal, sqlConstraint);
        if (jahiaPreferences != null && !jahiaPreferences.isEmpty()) {
            return jahiaPreferences.get(0);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preference - " + sqlConstraint + "- not found.");
        }
        if (notNull) {
            return createJahiaPreferenceNode(principal);
        }
        return null;
    }

    /**
     * Get all preferences of a user.
     * WARNING: if there is lots of preferences, it can be time consuming.
     * @param principal the user for whom we want the preferences
     * @return a List of all JahiaPreference for this user
     */
    public List<JahiaPreference<T>> getJahiaAllPreferences(Principal principal) {
        return findJahiaPreferences(principal, null);
    }

    /**
     * Find all preferences for a user matching certain sqlConstraint
     * @param principal the user for whom we want the preferences
     * @param sqlConstraint the sql constraint of the preference
     * @return a List of all JahiaPreference for this user
     */
    public List<JahiaPreference<T>> findJahiaPreferences(Principal principal, String sqlConstraint) {
        NodeIterator ni = findPreferenceNodeByJahiaPreferenceSQL(principal, sqlConstraint);
        if (ni == null) {
            return new ArrayList<JahiaPreference<T>>();
        }
        List<JahiaPreference<T>> jahiaPreferenceList = new ArrayList<JahiaPreference<T>>();
        while (ni.hasNext()) {
            Node currentPrefNode = ni.nextNode();
            jahiaPreferenceList.add(createJahiaPreference(principal, currentPrefNode));
        }
        return jahiaPreferenceList;
    }

    /**
     * Delete Jahia Preference
     *
     * @param principal the user for whom we want the preferences
     * @param sqlConstraint the sql constraint of the preference
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void deleteJahiaPreference(Principal principal, String sqlConstraint) throws JahiaPreferenceNotDefinedAttributeException {
        NodeIterator ni = findPreferenceNodeByJahiaPreferenceSQL(principal, sqlConstraint);
        while (ni.hasNext()) {
            try {
                Node next = ni.nextNode();

                //  Node parentNode = next.getParent();
                next.remove();
                //  parentNode.save();
            } catch (RepositoryException e) {
                logger.error("Error while deleting preference " + sqlConstraint, e);
            }
        }
        try {
            getProviderPreferencesNode(principal).save();
        } catch (RepositoryException e) {
            logger.error("Error while deleting preference " + sqlConstraint, e);
        }
    }

    /**
     * Delete Jahia Prefernce
     *
     * @param jahiaPreference the preference to delete
     */
    public void deleteJahiaPreference(JahiaPreference jahiaPreference) {
        if (logger.isDebugEnabled()) {
            logger.debug("Delete Jahia Preference");
        }
        if (jahiaPreference == null) {
            return;
        }
        try {
            Node parentNode = jahiaPreference.getNode().getParent();
            jahiaPreference.getNode().remove();
            parentNode.save();
        } catch (RepositoryException re) {
            logger.error("Error while deleting preference " + jahiaPreference, re);
        }
    }

    /**
     * Set Jahia preference
     *
     * @param jahiaPreference the preference to set
     */
    public void setJahiaPreference(JahiaPreference jahiaPreference) {
        if (jahiaPreference == null) {
            logger.warn("Can't set a null jahia preference");
            return;
        }
        try {
            JCRNodeWrapper node = jahiaPreference.getNode();
            node.getParent().save();
            if (logger.isDebugEnabled()) {
                logger.debug("Jahia preference [" + jahiaPreference + "] saved.");
                logger.debug("[" + node.getPath() + JahiaPreferencesQueryHelper.convertToSQLPureStringProperties(node.getPropertiesAsString()) + "] saved.");
            }
        } catch (RepositoryException re) {
            logger.error("Error while setting preference " + jahiaPreference, re);
        }
    }

    /**
     * Set jahia preference by properties Map
     *
     * @param jahiaPreferenceAttributes
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreferenceByMaps(Map<String, String> jahiaPreferenceAttributes) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException {
        
    }

    /**
     * Delete all preferences by node
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        try {
            JCRNodeWrapper preferencesNode = getPreferencesNode(principal);
            if (preferencesNode != null) {
                Node ppNode = getPreferencesNode(principal).getNode(getType());
                if (ppNode != null) {
                    ppNode.remove();
                }
                preferencesNode.save();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private JCRNodeWrapper getPreferencesNode(Principal principal) {
        String nodePath = getPreferencesNodePath(principal);
        try {
            return sessionFactory.getCurrentUserSession().getNode(nodePath);
        } catch (Exception e) {
            logger.error("Node path = [" + nodePath + "]");
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private Node getProviderPreferencesNode(Principal principal) {
        try {
            JCRNodeWrapper preferences = getPreferencesNode(principal);
            if (!preferences.hasNode(getType())) {
                if(!preferences.isCheckedOut()) {
                    preferences.checkout();
                }
                Node p = preferences.addNode(getType(), "jnt:preferenceProvider");
                preferences.save();
                return p;
            }
            return preferences.getNode(getType());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private String getPreferenceProviderNodePath(Principal principal) {
        return getPreferencesNodePath(principal) + "/" + getType();
    }

    private String getPreferencesNodePath(Principal principal) {
        String principalName;
        if (principal instanceof JahiaUser) {
            principalName = ((JahiaUser) principal).getLocalPath();
            return principalName + "/preferences";
        }
        return "";
    }

    private NodeIterator findPreferenceNodeByJahiaPreferenceSQL(Principal p, String sqlConstraint) {
        String path = getPreferenceProviderNodePath(p);
        return findNodeIteratorBySQL(p, "select * from ["+getNodeType()+"] as p where ischildnode(p,["+ JCRContentUtils.sqlEncode(path) + "])" + (sqlConstraint != null ? " and " + sqlConstraint : ""));
    }

    private NodeIterator findNodeIteratorBySQL(Principal p, String sqlRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Find node by xpath[ " + sqlRequest + " ]");
        }
        if (p instanceof JahiaGroup) {
            logger.warn("Preference provider not implemented for Group");
            return null;
        }
        try {
            QueryManager queryManager = sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager();
            if (queryManager != null) {
                Query q = queryManager.createQuery(sqlRequest, Query.JCR_SQL2);
                // execute query
                QueryResult queryResult = q.execute();

                // get node iterator
                NodeIterator ni = queryResult.getNodes();
                if (ni.hasNext()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Path[" + sqlRequest + "] --> found [" + ni.getSize() + "] values.");
                    }
                    return ni;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Path[" + sqlRequest + "] --> empty result.");
                    }
                }
            }
        } catch (javax.jcr.PathNotFoundException e) {
            logger.debug("javax.jcr.PathNotFoundException: Path[" + sqlRequest + "]");
        } catch (javax.jcr.ItemNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (javax.jcr.query.InvalidQueryException e) {
            logger.error("InvalidQueryException ---> [" + sqlRequest + "] is not valid.", e);
        }
        catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
