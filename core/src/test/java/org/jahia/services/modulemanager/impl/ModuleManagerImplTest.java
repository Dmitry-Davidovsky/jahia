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
package org.jahia.services.modulemanager.impl;

import java.util.Collection;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ModuleManagerImplTest {

    private static ModuleManagerImpl moduleManager;

    @BeforeClass
    public static void oneTimeSetup() {
        moduleManager = new ModuleManagerImpl();
    }

    @Test
    public void moduleInstallShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.install((Collection<Resource>) null, null, false));
    }

    @Test
    public void moduleUninstallShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.uninstall("bundleKey", null));
    }

    @Test
    public void moduleStartShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.start("bundleKey", null));
    }

    @Test
    public void moduleStopShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.stop("bundleKey", null));
    }

    @Test
    public void moduleUpdateShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.update("bundleKey", null));
    }

    @Test
    public void storeAllLocalPersistentStatesShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.storeAllLocalPersistentStates());
    }

    @Test
    public void applyBundlesPersistentStatesShouldFailInReadOnlyMode() {
        verifyFailureInReadOnlyMode(() -> moduleManager.applyBundlesPersistentStates(null));
    }

    private void verifyFailureInReadOnlyMode(Runnable action) {
        moduleManager.switchReadOnlyMode(true);
        try {
            action.run();
            Assert.fail("The action should have failed due to read only mode");
        } catch (ModuleManagementException e) {
            if (!(ExceptionUtils.getRootCause(e) instanceof ReadOnlyModeException)) {
                Assert.fail("The action should have failed due to read only mode");
            }
        } finally {
            moduleManager.switchReadOnlyMode(false);
        }
    }
}