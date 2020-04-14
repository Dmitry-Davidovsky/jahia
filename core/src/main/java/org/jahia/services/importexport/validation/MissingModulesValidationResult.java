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
package org.jahia.services.importexport.validation;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a validation result, containing missing modules in the content to be imported.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingModulesValidationResult implements ValidationResult, Serializable {

    private static final long serialVersionUID = 5602829144652574806L;

    private Set<String> missing = new TreeSet<String>();

    private String targetTemplateSet;

    private boolean targetTemplateSetPresent;

    /**
     * Initializes an instance of this class, merging the two validation results into one.
     * 
     * @param result1
     *            the first validation result instance to be merged
     * @param result2
     *            the second validation result instance to be merged
     */
    protected MissingModulesValidationResult(MissingModulesValidationResult result1,
            MissingModulesValidationResult result2) {
        super();
        targetTemplateSet = result1.getTargetTemplateSet();
        targetTemplateSetPresent = result1.isTargetTemplateSetPresent();
        if (targetTemplateSetPresent && !result2.isTargetTemplateSetPresent()) {
            targetTemplateSet = result2.getTargetTemplateSet();
            targetTemplateSetPresent = false;
        }
        missing.addAll(result1.getMissingModules());
        missing.addAll(result2.getMissingModules());
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param missingModules
     *            missing modules information
     * @param targetTemplateSet
     *            the template set from the import file
     * @param targetTemplateSetPresent
     *            is template set from import file present on the system?
     */
    public MissingModulesValidationResult(Set<String> missingModules, String targetTemplateSet,
            boolean targetTemplateSetPresent) {
        super();
        if (missing != null) {
            this.missing.addAll(missingModules);
        }
        this.targetTemplateSet = targetTemplateSet;
        this.targetTemplateSetPresent = targetTemplateSetPresent;
    }

    /**
     * Returns a set with missing modules.
     * 
     * @return a set with missing modules
     */
    public Set<String> getMissingModules() {
        return missing;
    }

    /**
     * Returns the name of the template set, specified in the imprt file.
     * 
     * @return the name of the template set, specified in the imprt file
     */
    public String getTargetTemplateSet() {
        return targetTemplateSet;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful, meaning no missing templates were detected.
     * 
     * @return <code>true</code> if the current validation result is successful, meaning no missing templates were detected
     */
    public boolean isSuccessful() {
        return isTargetTemplateSetPresent() && missing.isEmpty();
    }

    public boolean isTargetTemplateSetPresent() {
        return targetTemplateSet == null || targetTemplateSetPresent;
    }

    /**
     * Performs a merge of current validation results with provided one.
     * 
     * @return returns a merged view of both results
     */
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingModulesValidationResult) ? this
                : new MissingModulesValidationResult(this,
                        (MissingModulesValidationResult) toBeMergedWith);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", targetTemplateSet=").append(targetTemplateSet);
            out.append(", targetTemplateSetPresent=").append(targetTemplateSetPresent);
            out.append(", missingModules=").append(missing);
        }
        out.append("]");

        return out.toString();
    }
}
