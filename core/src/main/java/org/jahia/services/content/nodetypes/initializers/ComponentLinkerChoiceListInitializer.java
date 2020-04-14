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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 11 mai 2010
 */
public class ComponentLinkerChoiceListInitializer implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ComponentLinkerChoiceListInitializer.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();
        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) context.get("contextNode");
        ExtendedPropertyDefinition[] propertyDefs;
        if (nodeWrapper == null) {
            return Collections.emptyList();
        }
        try {
            if (nodeWrapper.hasProperty(param)) {
                JCRNodeWrapper boundNode = (JCRNodeWrapper) nodeWrapper.getProperty(param).getNode();
                if (boundNode.isNodeType("jnt:contentList")  || boundNode.isNodeType("jnt:absoluteArea") || boundNode.isNodeType("jnt:levelAbsoluteArea") || boundNode.isNodeType("jnt:area")) {
                    if (boundNode.hasProperty("j:allowedTypes") && boundNode.isNodeType("jmix:listRestrictions")) {
                        final Value[] values1 = boundNode.getProperty("j:allowedTypes").getValues();
                        propertyDefs = getCommonChildNodeDefinitions(values1, true, true,
                                                                     new LinkedHashSet<String>());
                    } else if (boundNode.hasNodes()) {
                        propertyDefs = SortableFieldnamesChoiceListInitializerImpl.getCommonChildNodeDefinitions(
                                boundNode, true, true, new LinkedHashSet<String>()).getPropertyDefinitions();
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    propertyDefs = boundNode.getPrimaryNodeType().getPropertyDefinitions();
                }
            } else {
                return Collections.emptyList();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        for (ExtendedPropertyDefinition propertyDef : propertyDefs) {
            choiceListValues.add(new ChoiceListValue(propertyDef.getLabel(locale), null, new ValueImpl(
                    propertyDef.getName(), PropertyType.STRING, false)));
        }
        Collections.sort(choiceListValues);
        return choiceListValues;
    }

    @SuppressWarnings("unchecked")
    public static ExtendedPropertyDefinition[] getCommonChildNodeDefinitions(Value[] values, boolean showHidden,
                                                                             boolean showProtected,
                                                                             Set<String> excludedNodeTypes)
            throws RepositoryException {
        Map<String, Map<String, ExtendedPropertyDefinition>> defs = null;
        final NodeTypeRegistry typeRegistry = NodeTypeRegistry.getInstance();
        if (values.length > 0) {
            for (Value value : values) {
                final ExtendedNodeType type = typeRegistry.getNodeType(value.getString());
                if (defs == null) {
                    // first child
                    defs = LazyMap.decorate(new HashMap<String, Map<String, ExtendedPropertyDefinition>>(),
                                            new Factory() {
                                                public Object create() {
                                                    return new HashMap<String, ExtendedPropertyDefinition>();
                                                }
                                            });
                    for (ExtendedPropertyDefinition propertyDef : type.getPropertyDefinitions()) {
                        // filter out hidden and protected if needed
                        if ((showHidden || !propertyDef.isHidden()) && (showProtected || !propertyDef.isProtected())) {
                            ExtendedNodeType nodeType = propertyDef.getDeclaringNodeType();
                            if (excludedNodeTypes.isEmpty() || !excludedNodeTypes.contains(nodeType.getName())) {
                                defs.get(nodeType.getName()).put(propertyDef.getName(), propertyDef);
                            }
                        }
                    }
                } else {
                    // filter out node types
                    for (Iterator<String> iterator = defs.keySet().iterator(); iterator.hasNext();) {
                        String commonType = iterator.next();
                        if (!type.isNodeType(commonType)) {
                            // the node has no such type --> remove the type from common
                            iterator.remove();
                        }
                    }
                }

                if (defs.isEmpty()) {
                    // no common property definitions found -> stop
                    break;
                }
            }
            List<ExtendedPropertyDefinition> propertyDefinitions = new LinkedList<ExtendedPropertyDefinition>();
            if (defs != null) {
                for (Map<String, ExtendedPropertyDefinition> props : defs.values()) {
                    propertyDefinitions.addAll(props.values());
                }
            }
            return propertyDefinitions.toArray(new ExtendedPropertyDefinition[propertyDefinitions.size()]);
        } else {
            return new ExtendedPropertyDefinition[0];
        }
    }
}
