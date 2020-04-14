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
package org.jahia.services.content.nodetypes;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.commons.nodetype.InvalidConstraintException;
import org.apache.jackrabbit.spi.commons.nodetype.constraint.ValueConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:02:49
 */
public class ExtendedPropertyDefinition extends ExtendedItemDefinition implements PropertyDefinition {
    private static Logger logger = LoggerFactory.getLogger(ExtendedPropertyDefinition.class);

    private NodeTypeRegistry registry;

    private int requiredType = 0;

    private boolean internationalized = false;

    private Value[] valueConstraints = new Value[0];
    private Value[] defaultValues = new Value[0];

    private boolean multiple;

    public static final int INDEXED_NO = IndexType.NO;
    public static final int INDEXED_TOKENIZED = IndexType.TOKENIZED;
    public static final int INDEXED_UNTOKENIZED = IndexType.UNTOKENIZED;
    public static final int STORE_YES = 0;
    public static final int STORE_NO = 1;
    public static final int STORE_COMPRESS = 2;

    private int index = IndexType.TOKENIZED;
    private double scoreboost = 1.;
    private String analyzer;
    
    private boolean queryOrderable = true;
    private boolean fulltextSearchable = true;
    private boolean facetable = false;
    private boolean hierarchical = false;
    private String[] availableQueryOperators = Lexer.ALL_OPERATORS;
    
    private Map<Locale, Map<String, String>> messageMaps = new ConcurrentHashMap<Locale, Map<String, String>>(1);

    public ExtendedPropertyDefinition(NodeTypeRegistry registry) {
        this.registry = registry;
    }

    public void setDeclaringNodeType(ExtendedNodeType declaringNodeType) {
        super.setDeclaringNodeType(declaringNodeType);
        declaringNodeType.setPropertyDefinition(getName(), this);
    }

    public int getRequiredType() {
        return requiredType;
    }

    public void setRequiredType(int requiredType) {
        this.requiredType = requiredType;
        if (selector == 0 && SelectorType.defaultSelectors.get(requiredType) != null) {
            setSelector(SelectorType.defaultSelectors.get(requiredType));
        }
    }

    public Value[] getValueConstraintsAsUnexpandedValue() {
        return valueConstraints;
    }

    public Value[] getValueConstraintsAsValue() {
        List<Value> res = new ArrayList<Value>();
        for (int i = 0; i < valueConstraints.length; i++) {
            if (valueConstraints[i] instanceof DynamicValueImpl) {
                Value[] v = ((DynamicValueImpl)valueConstraints[i]).expand();
                for (Value value : v) {
                    res.add(value);
                }
            } else {
                res.add(valueConstraints[i]);
            }
        }
        return res.toArray(new Value[res.size()]);
    }
    
    public ValueConstraint[] getValueConstraintObjects() {
        ValueConstraint[] constraintObjs = null;
        try {
            String[] constraints = getValueConstraints();
            if (requiredType == PropertyType.REFERENCE
                    || requiredType == PropertyType.WEAKREFERENCE) {
                String[] expandedConstraints = new String[constraints.length];
                int i = 0;
                for (String constraint : constraints) {
                    try {
                        ExtendedNodeType nodeType = registry.getNodeType(constraint);
                        Name name = nodeType.getNameObject();
                        expandedConstraints[i++] = "{" + name.getUri() + "}" + name.getLocalName();
                    } catch (RepositoryException ex) {
                    }
                }
                constraints = expandedConstraints;
            }
            constraintObjs = ValueConstraint.create(getRequiredType(), constraints);
        } catch (InvalidConstraintException e) {
            logger.warn("Internal error during creation of constraint.", e);
        }
        return constraintObjs;
    }

    public String[] getValueConstraints() {
        Value[] value = getValueConstraintsAsValue();
        String[] res = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            try {
                res[i] = value[i].getString();
            } catch (RepositoryException e) {
            }
        }
        return res;
    }

    public void setValueConstraints(Value[] valueConstraints) {
        if (requiredType != PropertyType.BOOLEAN) {
            this.valueConstraints = valueConstraints;
        } 
    }

    public Value[] getDefaultValues() {
        return getDefaultValues(null);
    }

    /**
     * Get default values, as value object
     *
     * @param locale locale used to expand I15dValueInitializer
     * @return the default values
     */
    public Value[] getDefaultValues(Locale locale) {
        List<Value> res = new ArrayList<Value>();
        for (int i = 0; i < defaultValues.length; i++) {
            if (defaultValues[i] instanceof DynamicValueImpl) {
                Value[] v = ((DynamicValueImpl)defaultValues[i]).expand(locale);
                for (Value value : v) {
                    res.add(value);
                }
            } else {
                res.add(defaultValues[i]);
            }
        }
        return res.toArray(new Value[res.size()]);
    }

    /**
     * Get default values, unexpanded
     * @return
     */
    public Value[] getDefaultValuesAsUnexpandedValue() {
       return defaultValues;
    }
    
    public boolean hasDynamicDefaultValues() {
        return defaultValues.length > 0 && (defaultValues[0] instanceof DynamicValueImpl);
    }

    public void setDefaultValues(Value[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isInternationalized() {
        return internationalized;
    }

    public void setInternationalized(boolean internationalized) {
        this.internationalized = internationalized;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getScoreboost() {
        return scoreboost;
    }

    public void setScoreboost(double scoreboost) {
        this.scoreboost = scoreboost;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public boolean isQueryOrderable() {
        return queryOrderable;
    }

    public void setQueryOrderable(boolean sortable) {
        this.queryOrderable = sortable;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public void setFacetable(boolean facetable) {
        this.facetable = facetable;
    }

    public boolean isHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

    public boolean isFullTextSearchable() {
        return fulltextSearchable;
    }

    public void setFullTextSearchable(boolean fulltextSearchable) {
        this.fulltextSearchable = fulltextSearchable;
    }

    public String[] getAvailableQueryOperators() {
        return availableQueryOperators;
    }

    public void setAvailableQueryOperators(String[] availableQueryOperators) {
        this.availableQueryOperators = availableQueryOperators;
    }
    
    public String getMessage(String msgKeySuffix, Locale locale) {
        Map<String, String> messageMap = messageMaps.get(locale);
        if (messageMap == null) {
            messageMap = new HashMap<String, String>();
            messageMaps.put(locale, messageMap);
        }
        String message = messageMap.get(msgKeySuffix);
        if (message == null) {
            message = getDeclaringNodeType().lookupLabel(
                    !StringUtils.isEmpty(msgKeySuffix) ? getResourceBundleKey() + "."
                            + msgKeySuffix : getResourceBundleKey(), locale, "");
            messageMap.put(msgKeySuffix, message);
        }
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExtendedPropertyDefinition that = (ExtendedPropertyDefinition) o;
        if (getName().equals("*")) {
            if (requiredType != that.requiredType) return false;
            if (multiple != that.multiple) return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Same as super.hashCode();
        return super.hashCode();
    }

    /**
     * Remove definition from declaring node type
     */
    public void remove() {
        getDeclaringNodeType().removePropertyDefinition(this);
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
