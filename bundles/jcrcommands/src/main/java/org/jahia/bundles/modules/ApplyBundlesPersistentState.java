/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.bundles.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;

/**
 * Short description of the class
 *
 * @author yousria
 */
@Command(
        scope = "bundle",
        name = "apply-bundle-states",
        description = "Apply persistent bundle states from JCR"
)
@Service
public class ApplyBundlesPersistentState implements Action {

    @Reference
    private ModuleManager moduleManager;

    @Argument(description = "target")
    private String target;

    @Override
    public Object execute() throws Exception {
        if (target == null) {
            target = "default";
        }

        OperationResult result = moduleManager.applyBundlesPersistentStates(target);
        if (result.getBundleInfos().isEmpty()) {
            System.out.println("All bundles status were up-to-date");
        } else {
            // Fill the table to output result.
            ShellTable table = new ShellTable();
            table.column(new Col("Symbolic-Name"));
            table.column(new Col("Version"));
            table.column(new Col("Location"));
            for (BundleInfo bundleInfo : result.getBundleInfos()) {
                table.addRow().addContent(bundleInfo.getSymbolicName(), bundleInfo.getVersion(), bundleInfo.getKey());
            }
            table.print(System.out, true);
        }

        return null;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}
