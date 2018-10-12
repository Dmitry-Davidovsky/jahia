/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.mutable.MutableInt;
import org.jahia.services.content.*;
import org.jahia.services.content.JCRNodeWrapper.NodeNamingConflictResolutionStrategy;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * User: toto
 * Date: Dec 4, 2008
 * Time: 10:21:11 AM
 */
public class JCRNodeDecorator implements JCRNodeWrapper {

    private static final Logger logger = LoggerFactory.getLogger(JCRNodeDecorator.class);

    protected JCRNodeWrapper node;

    public JCRNodeDecorator(JCRNodeWrapper node) {
        if (node instanceof JCRNodeDecorator) {
            logger.warn("An already decorated node shouldn't be decorated");
            Thread.dumpStack();
        }
        this.node = node;
    }

    public Node getRealNode() {
        return node.getRealNode();
    }

    public JCRUserNode getUser() {
        return node.getUser();
    }

    public boolean hasTranslations() throws RepositoryException {
        return node.hasTranslations();
    }

    public boolean checkI18nAndMandatoryPropertiesForLocale(Locale locale)
            throws RepositoryException {
        return node.checkI18nAndMandatoryPropertiesForLocale(locale);
    }

    public Map<String, List<String[]>> getAclEntries() {
        return node.getAclEntries();
    }

    public Map<String, Map<String, String>> getActualAclEntries() {
        return node.getActualAclEntries();
    }

    public Map<String, List<JCRNodeWrapper>> getAvailableRoles() throws RepositoryException {
        return node.getAvailableRoles();
    }

    public boolean hasPermission(String perm) {
        return node.hasPermission(perm);
    }

    public Set<String> getPermissions() {
        return node.getPermissions();
    }

    public BitSet getPermissionsAsBitSet() {
        return node.getPermissionsAsBitSet();
    }

    public boolean grantRoles(String principalKey, Set<String> roles) throws RepositoryException {
        return node.grantRoles(principalKey, roles);
    }

    public boolean denyRoles(String principalKey, Set<String> roles) throws RepositoryException {
        return node.denyRoles(principalKey, roles);
    }

    public boolean changeRoles(String principalKey, Map<String, String> perm) throws RepositoryException {
        return node.changeRoles(principalKey, perm);
    }

    public boolean revokeRolesForPrincipal(String principalKey) throws RepositoryException {
        return node.revokeRolesForPrincipal(principalKey);
    }

    public boolean revokeAllRoles()  throws RepositoryException{
        return node.revokeAllRoles();
    }

    public boolean getAclInheritanceBreak() throws RepositoryException {
        return node.getAclInheritanceBreak();
    }

    public boolean setAclInheritanceBreak(boolean breakAclInheritance) throws RepositoryException {
        return node.setAclInheritanceBreak(breakAclInheritance);
    }

    public JCRNodeWrapper uploadFile(String name, InputStream is, String contentType) throws RepositoryException {
        return node.uploadFile(name, is, contentType);
    }

    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return node.createCollection(name);
    }

    public String getAbsoluteUrl(ServletRequest request) {
        return node.getAbsoluteUrl(request);
    }

    public String getUrl() {
        return node.getUrl();
    }

    public String getAbsoluteWebdavUrl(final HttpServletRequest request) {
        return node.getAbsoluteWebdavUrl(request);
    }

    public String getWebdavUrl() {
        return node.getWebdavUrl();
    }

    public List<String> getThumbnails() {
        return node.getThumbnails();
    }

    public String getThumbnailUrl(String name) {
        return node.getThumbnailUrl(name);
    }

    public Map<String, String> getThumbnailUrls() {
        return node.getThumbnailUrls();
    }

    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        return node.getPropertiesAsString();
    }

    public String getPrimaryNodeTypeName() throws RepositoryException {
        return node.getPrimaryNodeTypeName();
    }

    public List<String> getNodeTypes() throws RepositoryException {
        return node.getNodeTypes();
    }

    public boolean isCollection() {
        return node.isCollection();
    }

    public boolean isFile() {
        return node.isFile();
    }

    public boolean isPortlet() {
        return node.isPortlet();
    }

    public Date getLastModifiedAsDate() {
        return node.getLastModifiedAsDate();
    }

    public Date getLastPublishedAsDate() {
        return node.getLastPublishedAsDate();
    }

    public Date getContentLastModifiedAsDate() {
        return node.getContentLastModifiedAsDate();
    }

    public Date getContentLastPublishedAsDate() {
        return node.getContentLastPublishedAsDate();
    }

    public Date getCreationDateAsDate() {
        return node.getCreationDateAsDate();
    }

    public String getCreationUser() {
        return node.getCreationUser();
    }

    public String getModificationUser() {
        return node.getModificationUser();
    }

    public String getPublicationUser() {
        return node.getPublicationUser();
    }

    public String getLanguage() {
        return node.getLanguage();
    }

    public String getPropertyAsString(String name) {
        return node.getPropertyAsString(name);
    }

    public JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException {
        return decorateProperty(node.setProperty(namespace, name, value));
    }

    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        return node.getAncestors();
    }

    public boolean rename(String newName) throws RepositoryException {
        return node.rename(newName);
    }

    public boolean copy(String dest) throws RepositoryException {
        return node.copy(dest);
    }

    public boolean copy(String dest, String name) throws RepositoryException {
        return node.copy(dest, name);
    }

    public boolean copy(String dest, String name, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException {
        return node.copy(dest, name, namingConflictResolutionStrategy);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes) throws RepositoryException {
        return node.copy(dest, name, allowsExternalSharedNodes);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException {
        return node.copy(dest, name, allowsExternalSharedNodes, namingConflictResolutionStrategy);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references) throws RepositoryException {
        return node.copy(dest, name, allowsExternalSharedNodes, references);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, List<String> ignoreNodeTypes, int maxBatch) throws RepositoryException {
        return node.copy(dest, name, allowsExternalSharedNodes, ignoreNodeTypes, maxBatch);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batch) throws RepositoryException {
        return internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batch, true);
    }

    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batch, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException {
        return internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batch, true, namingConflictResolutionStrategy);
    }

    public boolean internalCopy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batch, boolean isTopObject) throws RepositoryException {
        return internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batch, isTopObject, NodeNamingConflictResolutionStrategy.MERGE);
    }

    private boolean internalCopy(
        JCRNodeWrapper dest,
        String name,
        boolean allowsExternalSharedNodes,
        Map<String, List<String>> references,
        List<String> ignoreNodeTypes,
        int maxBatch,
        MutableInt batch,
        boolean isTopObject,
        NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy
    ) throws RepositoryException {
        if (!isTopObject && node instanceof JCRNodeWrapperImpl) {
            return ((JCRNodeWrapperImpl) node).internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batch, isTopObject, namingConflictResolutionStrategy);
        } else {
            return node.copy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batch, namingConflictResolutionStrategy);
        }
    }

    public void copyProperties(JCRNodeWrapper destinationNode, Map<String, List<String>> references) throws RepositoryException {
        node.copyProperties(destinationNode, references);
    }

    public boolean lockAndStoreToken(String type, String userID) throws RepositoryException {
        return node.lockAndStoreToken(type, userID);
    }

    public boolean lockAndStoreToken(String type) throws RepositoryException {
        return node.lockAndStoreToken(type);
    }

    public String getLockOwner() throws RepositoryException {
        return node.getLockOwner();
    }

    public Map<String, List<String>> getLockInfos()  throws RepositoryException {
        return node.getLockInfos();
    }

    public void versionFile() {
        node.versionFile();
    }

    public boolean isVersioned() {
        return node.isVersioned();
    }

    public void checkpoint() {
        node.checkpoint();
    }

    public List<String> getVersions() {
        return node.getVersions();
    }

    //    public JCRNodeWrapper getFrozenVersionAsRegular(Date versionDate)  throws RepositoryException {
//        return node.getFrozenVersionAsRegular(versionDate);
//    }
//
//    public JCRNodeWrapper getFrozenVersionAsRegular(String versionLabel) throws RepositoryException {
//        return node.getFrozenVersionAsRegular(versionLabel);
//    }
//

    public JCRStoreProvider getJCRProvider() {
        return node.getJCRProvider();
    }

    public JCRStoreProvider getProvider() {
        return node.getProvider();
    }

    public JCRFileContent getFileContent() {
        return node.getFileContent();
    }

    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName) throws RepositoryException {
        return node.getApplicablePropertyDefinition(propertyName);
    }

    @Override
    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName, int requiredPropertyType, boolean isMultiple) throws RepositoryException {
        return node.getApplicablePropertyDefinition(propertyName, requiredPropertyType, isMultiple);
    }

    public List<ExtendedPropertyDefinition> getReferenceProperties() throws RepositoryException {
        return node.getReferenceProperties();
    }

    public ExtendedNodeDefinition getApplicableChildNodeDefinition(String childName, String nodeType) throws ConstraintViolationException, RepositoryException {
        return node.getApplicableChildNodeDefinition(childName, nodeType);
    }

    public JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return node.addNode(s);
    }

    public JCRNodeWrapper addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return node.addNode(s, s1);
    }

    public JCRNodeWrapper addNode(String relPath, String primaryNodeTypeName, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return node.addNode(relPath, primaryNodeTypeName, identifier, created, createdBy, lastModified, lastModifiedBy);
    }

    public JCRPlaceholderNode getPlaceholder() throws RepositoryException {
        return new JCRPlaceholderNode(node);
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        node.orderBefore(s, s1);
    }

    public JCRPropertyWrapper setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, value));
    }

    public JCRPropertyWrapper setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, value, i));
    }

    public JCRPropertyWrapper setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, values));
    }

    public JCRPropertyWrapper setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, values, i));
    }

    public JCRPropertyWrapper setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, strings));
    }

    public JCRPropertyWrapper setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, strings, i));
    }

    public JCRPropertyWrapper setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, s1));
    }

    public JCRPropertyWrapper setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, s1, i));
    }

    public JCRPropertyWrapper setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, inputStream));
    }

    public JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(name, value));
    }

    public JCRPropertyWrapper setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, b));
    }

    public JCRPropertyWrapper setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, v));
    }

    public JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(name, value));
    }

    public JCRPropertyWrapper setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, l));
    }

    public JCRPropertyWrapper setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(node.setProperty(s, calendar));
    }

    public JCRPropertyWrapper setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return decorateProperty(this.node.setProperty(s, node));
    }

    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        return node.getNode(s);
    }

    public JCRNodeIteratorWrapper getNodes() throws RepositoryException {
        return node.getNodes();
    }

    public JCRNodeIteratorWrapper getNodes(String s) throws RepositoryException {
        return node.getNodes(s);
    }

    public JCRNodeIteratorWrapper getNodes(String[] nameGlobs) throws RepositoryException {
        return node.getNodes(nameGlobs);
    }

    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        return decorateProperty(node.getProperty(s));
    }

    public PropertyIterator getProperties() throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale);
        }
        return new LazyPropertyIterator(this, null);
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, s);
        }
        return new LazyPropertyIterator(this, null, s);
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, strings);
        }
        return new LazyPropertyIterator(this, null, strings);
    }

    public JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return node.getPrimaryItem();
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getUUID();
    }

    public String getIdentifier() throws RepositoryException {
        return node.getIdentifier();
    }

    public int getIndex() throws RepositoryException {
        return node.getIndex();
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return node.getReferences();
    }

    public PropertyIterator getReferences(String name) throws RepositoryException {
        return node.getReferences(name);
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return node.getWeakReferences();
    }

    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return node.getWeakReferences(name);
    }

    public boolean hasNode(String s) throws RepositoryException {
        return node.hasNode(s);
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return node.hasProperty(s);
    }

    public boolean hasNodes() throws RepositoryException {
        return node.hasNodes();
    }

    public boolean hasProperties() throws RepositoryException {
        return node.hasProperties();
    }

    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        return node.getPrimaryNodeType();
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        return node.getMixinNodeTypes();
    }

    public boolean isNodeType(String s) throws RepositoryException {
        return node.isNodeType(s);
    }

    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.setPrimaryType(nodeTypeName);
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.addMixin(s);
    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.removeMixin(s);
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return node.canAddMixin(s);
    }

    public ExtendedNodeDefinition getDefinition() throws RepositoryException {
        return node.getDefinition();
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return node.checkin();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        node.checkout();
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        node.doneMerge(version);
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        node.cancelMerge(version);
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        node.update(s);
    }

    public JCRNodeIteratorWrapper merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return node.merge(s, b);
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return node.getCorrespondingNodePath(s);
    }

    public JCRNodeIteratorWrapper getSharedSet() throws RepositoryException {
        return node.getSharedSet();
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.removeSharedSet();
    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.removeShare();
    }

    public boolean isCheckedOut() throws RepositoryException {
        return node.isCheckedOut();
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restore(s, b);
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        node.restore(version, b);
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restore(version, s, b);
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restoreByLabel(s, b);
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getVersionHistory();
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getBaseVersion();
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return node.lock(b, b1);
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return node.getLock();
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        node.unlock();
    }

    public void unlock(String type) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        node.unlock(type);
    }

    public void unlock(String type, String userID) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        node.unlock(type, userID);
    }

    public void clearAllLocks() throws RepositoryException  {
        node.clearAllLocks();
    }

    @Override
    public void checkLock() throws RepositoryException {
        node.checkLock();
    }

    public boolean holdsLock() throws RepositoryException {
        return node.holdsLock();
    }

    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        node.followLifecycleTransition(transition);
    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getAllowedLifecycleTransistions();
    }

    public boolean isLocked() {
        return node.isLocked();
    }

    public boolean isLockable() {
        return node.isLockable();
    }

    public List<Locale> getLockedLocales() throws RepositoryException {
        return node.getLockedLocales();
    }

    public String getPath() {
        return node.getPath();
    }

    public String getName() {
        return node.getName();
    }

    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node.getAncestor(i);
    }

    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node.getParent();
    }

    public int getDepth() throws RepositoryException {
        return node.getDepth();
    }

    public JCRSessionWrapper getSession() throws RepositoryException {
        return node.getSession();
    }

    public boolean isNode() {
        return node.isNode();
    }

    public boolean isNew() {
        return node.isNew();
    }

    public boolean isModified() {
        return node.isModified();
    }

    public boolean isSame(Item item) throws RepositoryException {
        return node.isSame(item);
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        node.accept(itemVisitor);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        node.save();
    }

    public void saveSession() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        node.saveSession();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        node.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.remove();
    }

    public List<Locale> getExistingLocales() throws RepositoryException {
        return node.getExistingLocales();
    }

    public NodeIterator getI18Ns() throws RepositoryException {
        return node.getI18Ns();
    }

    public Node getI18N(Locale locale) throws RepositoryException {
        return node.getI18N(locale);
    }

    @Override
    public Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
        return node.getI18N(locale, fallback);
    }

    public boolean hasI18N(Locale locale) throws RepositoryException {
        return node.hasI18N(locale);
    }

    @Override
    public boolean hasI18N(Locale locale, boolean fallback) throws RepositoryException {
        return node.hasI18N(locale, fallback);
    }

    public Node getOrCreateI18N(Locale locale) throws RepositoryException {
        return node.getOrCreateI18N(locale);
    }

    public Node getOrCreateI18N(Locale locale, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws RepositoryException {
        return node.getOrCreateI18N(locale, created,  createdBy, lastModified, lastModifiedBy);
    }

    public JCRNodeWrapper clone(JCRNodeWrapper sharedNode, String name) throws ItemExistsException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return node.clone(sharedNode, name);
    }

    public boolean checkValidity() {
        return node.checkValidity();
    }

    public boolean checkLanguageValidity(Set<String> languages) {
        return node.checkLanguageValidity(languages);
    }


    public JCRSiteNode getResolveSite() throws RepositoryException {
        return node.getResolveSite();
    }

    public List<VersionInfo> getVersionInfos() throws RepositoryException {
        return node.getVersionInfos();
    }

    public List<VersionInfo> getLinearVersionInfos() throws RepositoryException {
        return node.getLinearVersionInfos();
    }

    /**
     * Gets a list of all versions for this node as Version object
     *
     * @return a list of Version object
     */
    public List<Version> getVersionsAsVersion() {
        return node.getVersionsAsVersion();
    }

    public String getDisplayableName() {
        return node.getDisplayableName();
    }

    public String getUnescapedName() {
        return node.getUnescapedName();
    }

    public AccessControlManager getAccessControlManager() throws RepositoryException {
        return node.getAccessControlManager();
    }

    public boolean canMarkForDeletion() throws RepositoryException {
        return node.canMarkForDeletion();
    }

    public boolean isMarkedForDeletion() throws RepositoryException {
        return node.isMarkedForDeletion();
    }

    public void markForDeletion(String comment) throws RepositoryException {
        node.markForDeletion(comment);
    }

    public void unmarkForDeletion() throws RepositoryException {
        node.unmarkForDeletion();
    }

    public String toString() {
        return node.toString();
    }

    public String getCanonicalPath() {
        return node.getCanonicalPath();
    }

    public boolean equals(final Object o) {
        if (o != null && this.getClass() == o.getClass()) {
            return node.equals(((JCRNodeDecorator) o).node);
        }
        return false;
    }

    public int hashCode() {
        return node.hashCode();
    }

    public JCRNodeWrapper getDecoratedNode() {
        return node;
    }

    public JCRPropertyWrapper decorateProperty(final Property property) throws RepositoryException {

        if (property == null) {
            return null;
        }

        Property decoratedProperty = property;
        ExtendedPropertyDefinition extendedPropertyDefinition = null;
        while (decoratedProperty instanceof JCRPropertyWrapper) {
            JCRPropertyWrapper jcrPropertyWrapper = (JCRPropertyWrapper) decoratedProperty;
            decoratedProperty = jcrPropertyWrapper.getRealProperty();
            if (decoratedProperty == null) {
                return null;
            }
            extendedPropertyDefinition = (ExtendedPropertyDefinition) jcrPropertyWrapper.getDefinition();
        }

        if (extendedPropertyDefinition != null) {
            return new JCRPropertyWrapperImpl(this, decoratedProperty, getSession(), getProvider(), extendedPropertyDefinition);
        } else {
            throw new ConstraintViolationException("Couldn't find definition for property " + property.getPath());
        }
    }
}
