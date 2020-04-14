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
package org.jahia.services.content;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;

/**
 * Jahia wrappers around <code>javax.jcr.Item</code> to be able to inject
 * Jahia specific actions. 
 *
 * @author toto
 */
public class JCRItemWrapperImpl implements JCRItemWrapper {
    protected JCRStoreProvider provider;
    protected Item item;
    protected String localPath;
    protected String localPathInProvider;
    protected JCRSessionWrapper session;

    protected JCRItemWrapperImpl(JCRSessionWrapper session, JCRStoreProvider provider) {
        this.session = session;
        this.provider = provider;
    }

    protected void setItem(Item item) {
        this.item = item;
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        if ("/".equals(provider.getMountPoint())) {
            return localPath;
        } else if ("/".equals(localPath)) {
            return provider.getMountPoint();
        }
        if (localPath.contains("@/")) {
            return localPath;
        }
        return provider.getMountPoint() + localPath.substring(provider.getRelativeRoot().length());
    }

    /**
     * {@inheritDoc}
     */
    public String getCanonicalPath() {
        if ("/".equals(provider.getMountPoint())) {
            return localPathInProvider;
        } else if ("/".equals(localPathInProvider)) {
            return provider.getMountPoint();
        }

        return provider.getMountPoint() + localPathInProvider.substring(provider.getRelativeRoot().length());
    }

    /**
     * {@inheritDoc}
     */
    public String getName() throws RepositoryException {
        return item.getName();
    }

    /**
     * {@inheritDoc}
     * <code>Item</code> at the specified <code>depth</code>. 
     */
    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (i >= provider.getDepth()) {
            return provider.getNodeWrapper((Node) item.getAncestor(i-provider.getDepth()), getSession());
        } else if (i < 0) {
            throw new ItemNotFoundException();            
        }
        return session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(),"/")).getAncestor(i);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it  
     */
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();

//        JCRNodeWrapper parent = provider.getFileNodeWrapper(provider.decodeInternalName(getParent().getPath()), user, session);
//        return parent;
    }

    /**
     * {@inheritDoc}
     */
    public int getDepth() throws RepositoryException {
        return provider.getDepth() + item.getDepth();
    }

    /**
     * {@inheritDoc}
     */
    public JCRSessionWrapper getSession() {
        return session;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNode() {
        return item.isNode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNew() {
        return item.isNew();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModified() {
        return item.isModified();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSame(Item otherItem) throws RepositoryException {
        return otherItem.isSame(item);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        item.accept(itemVisitor);
    }

    /**
     * {@inheritDoc}
     * @deprecated As of JCR 2.0, {@link javax.jcr.Session#save()} should
     *             be used instead. 
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getSession().save();
    }

    /**
     * {@inheritDoc}
     */
    public void saveSession()  throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getSession().save();
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        item.refresh(b);
    }

    /**
     * {@inheritDoc}
     */
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        getSession().removeFromCache(this);
        item.remove();
    }
    
    /**
     * Returns the path of this item for use in diagnostic output.
     *
     * @return "/path/to/item"
     */
    public String toString() {
        return item.toString();
    }
}
