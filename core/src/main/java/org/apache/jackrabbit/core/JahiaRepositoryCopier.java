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
package org.apache.jackrabbit.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.lock.LockManagerImpl;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.persistence.JahiaPersistenceCopier;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.QNodeTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for migrating or backing up the Jackrabbit content repository. Optimized version of the
 * {@link org.apache.jackrabbit.core.RepositoryCopier}.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaRepositoryCopier {

    private static final Logger logger = LoggerFactory.getLogger(JahiaRepositoryCopier.class);

    private int batchSize;

    private RepositoryContext source;

    private RepositoryContext target;
    
    public JahiaRepositoryCopier(RepositoryImpl source, RepositoryImpl target, int batchSize) {
        this.source = source.getRepositoryContext();
        this.target = target.getRepositoryContext();
        this.batchSize = batchSize;
    }

    /**
     * Copies the full content from the source to the target repository.
     * <p>
     * The source repository <strong>must not be modified</strong> while the copy operation is running to avoid an inconsistent copy.
     * <p>
     * This method leaves the search indexes of the target repository in an Note that both the source and the target repository must be
     * closed during the copy operation as this method requires exclusive access to the repositories.
     * 
     * @throws RepositoryException
     *             if the copy operation fails
     */
    public void copy() throws RepositoryException {
        try {
            copyNamespaces();
            copyNodeTypes();
            copyVersionStore();
            copyWorkspaces();
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }

    private void copyNamespaces() throws RepositoryException {
        NamespaceRegistry sourceRegistry = source.getNamespaceRegistry();
        NamespaceRegistry targetRegistry = target.getNamespaceRegistry();

        logger.info("Copying registered namespaces");

        Collection<String> existing = Arrays.asList(targetRegistry.getURIs());
        for (String uri : sourceRegistry.getURIs()) {
            if (!existing.contains(uri)) {
                // TODO: what if the prefix is already taken?
                targetRegistry.registerNamespace(sourceRegistry.getPrefix(uri), uri);
            }
        }
    }

    private void copyNodeTypes() throws RepositoryException {
        NodeTypeRegistry sourceRegistry = source.getNodeTypeRegistry();
        NodeTypeRegistry targetRegistry = target.getNodeTypeRegistry();

        logger.info("Copying registered node types");

        Collection<Name> existing = Arrays.asList(targetRegistry.getRegisteredNodeTypes());
        Collection<QNodeTypeDefinition> register = new ArrayList<QNodeTypeDefinition>();
        for (Name name : sourceRegistry.getRegisteredNodeTypes()) {
            // TODO: what about modified node types?
            if (!existing.contains(name)) {
                register.add(sourceRegistry.getNodeTypeDef(name));
            }
        }
        try {
            targetRegistry.registerNodeTypes(register);
        } catch (InvalidNodeTypeDefException e) {
            throw new RepositoryException("Unable to copy node types", e);
        }
    }

    private void copyVersionStore() throws RepositoryException {
        logger.info("Copying version histories");

        JahiaPersistenceCopier copier = new JahiaPersistenceCopier(source
                .getInternalVersionManager().getPersistenceManager(), target
                .getInternalVersionManager().getPersistenceManager(), target.getDataStore(),
                batchSize);
        copier.copy(RepositoryImpl.VERSION_STORAGE_NODE_ID);
        copier.copy(RepositoryImpl.ACTIVITIES_NODE_ID);
        copier.flush();
    }

    private void copyWorkspaces() throws RepositoryException {
        Collection<String> existing = Arrays.asList(target.getRepository().getWorkspaceNames());
        for (String name : source.getRepository().getWorkspaceNames()) {
            logger.info("Copying workspace {}", name);

            if (!existing.contains(name)) {
                logger.info("Creating workspace {}", name);
                target.getRepository().createWorkspace(name);
            }

            logger.info("Copy all the workspace content for workspace {}", name);
            JahiaPersistenceCopier copier = new JahiaPersistenceCopier(source.getRepository()
                    .getWorkspaceInfo(name).getPersistenceManager(), target.getRepository()
                    .getWorkspaceInfo(name).getPersistenceManager(), target.getDataStore(),
                    batchSize);
            copier.excludeNode(RepositoryImpl.SYSTEM_ROOT_NODE_ID);
            copier.copy(RepositoryImpl.ROOT_NODE_ID);
            copier.flush();

            logger.info("Copy all the active open-scoped locks");
            LockManagerImpl sourceLockManager = source.getRepository().getLockManager(name);
            LockManagerImpl targetLockManager = target.getRepository().getLockManager(name);
            targetLockManager.copyOpenScopedLocksFrom(sourceLockManager);
        }
    }

}
