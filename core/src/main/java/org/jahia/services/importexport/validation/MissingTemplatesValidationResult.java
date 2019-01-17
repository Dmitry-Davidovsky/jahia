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
package org.jahia.services.importexport.validation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a validation result, containing missing templates in the content to be imported.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingTemplatesValidationResult implements ValidationResult, Serializable {

    private static final long serialVersionUID = -6930213514434249772L;

    private Map<String, Set<String>> missing = new TreeMap<String, Set<String>>();

    private String targetTemplateSet;

    private boolean targetTemplateSetPresent;

    private Map<String, Integer> modulesMissingCounts = Collections.emptyMap();

    /**
     * Initializes an instance of this class.
     * 
     * @param missing
     *            missing templates information
     * @param targetTemplateSet
     *            the template set from the import file
     * @param targetTemplateSetPresent
     *            is template set from import file present on the system?
     * @param modulesMissingCounts
     *            if the target template set is not present on the system we verify templates against all available template sets and check
     *            how many are missing in each of them
     */
    public MissingTemplatesValidationResult(Map<String, Set<String>> missing,
            String targetTemplateSet, boolean targetTemplateSetPresent,
            Map<String, Integer> modulesMissingCounts) {
        super();
        this.missing = missing;
        this.targetTemplateSet = targetTemplateSet;
        this.targetTemplateSetPresent = targetTemplateSetPresent;
        if (modulesMissingCounts != null) {
            this.modulesMissingCounts = modulesMissingCounts;
        }
    }

    /**
     * Initializes an instance of this class, merging the two validation results into one.
     * 
     * @param result1
     *            the first validation result instance to be merged
     * @param result2
     *            the second validation result instance to be merged
     */
    protected MissingTemplatesValidationResult(MissingTemplatesValidationResult result1,
            MissingTemplatesValidationResult result2) {
        super();
        missing.putAll(result1.getMissingTemplates());
        targetTemplateSet = result1.getTargetTemplateSet();
        targetTemplateSetPresent = result1.isTargetTemplateSetPresent();
        if (targetTemplateSetPresent && !result2.isTargetTemplateSetPresent()) {
            targetTemplateSet = result2.getTargetTemplateSet();
            targetTemplateSetPresent = false;
        }
        for (Map.Entry<String, Set<String>> item : result2.getMissingTemplates().entrySet()) {
            if (missing.containsKey(item.getKey())) {
                missing.get(item.getKey()).addAll(item.getValue());
            } else {
                missing.put(item.getKey(), item.getValue());
            }
        }
    }

    /**
     * Returns a Map with missing templates as keys and as value a list of element paths having that template in the import.
     * 
     * @return a Map with missing templates as keys and as value a list of element paths having that template in the import
     */
    public Map<String, Set<String>> getMissingTemplates() {
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
     * If the target template set is not present on the system we verify templates against all available template sets and check how many
     * are missing in each of them. This method returns in this case a map, with template set names as keys and missing count as values. If
     * the target template set if found, the check against all other template sets is not done and this method returns an empty map.
     * 
     * @return a map, with template set names as keys and missing count as values
     */
    public Map<String, Integer> getTemplateSetsMissingCounts() {
        return modulesMissingCounts;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful, meaning no missing templates were detected.
     * 
     * @return <code>true</code> if the current validation result is successful, meaning no missing templates were detected
     */
    public boolean isSuccessful() {
        return missing.isEmpty();
    }

    public boolean isTargetTemplateSetPresent() {
        return targetTemplateSetPresent;
    }

    /**
     * Performs a merge of current validation results with provided one.
     * 
     * @return returns a merged view of both results
     */
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingTemplatesValidationResult) ? this
                : new MissingTemplatesValidationResult(this,
                        (MissingTemplatesValidationResult) toBeMergedWith);
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
        out.append(", targetTemplateSet=").append(targetTemplateSet);
        out.append(", targetTemplateSetPresent=").append(targetTemplateSetPresent);
        out.append(", modulesMissingCounts=").append(modulesMissingCounts);
        out.append(", missingTemplates=").append(missing);
        out.append("]");

        return out.toString();
    }
}
