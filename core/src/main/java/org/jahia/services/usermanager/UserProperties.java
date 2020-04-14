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
package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class stores user properties, which are different from regular
 * java.util.Properties because they can also store information about read-only
 * state as well as the original provider of the properties.
 *
 * @author Serge Huber
 * @deprecated
 */
@Deprecated
public class UserProperties implements Serializable {

    private static final long serialVersionUID = -5885566091509965795L;

    public static final Set<String> DEFAULT_PROPERTIES_NAME = new HashSet<>();

    static {
        DEFAULT_PROPERTIES_NAME.add("email");
        DEFAULT_PROPERTIES_NAME.add("lastname");
        DEFAULT_PROPERTIES_NAME.add("firstname");
        DEFAULT_PROPERTIES_NAME.add("organization");
        DEFAULT_PROPERTIES_NAME.add("emailNotificationsDisabled");
        DEFAULT_PROPERTIES_NAME.add("preferredLanguage");
    }

    private Map<String, UserProperty> properties = new HashMap<>();

    public UserProperties() {
        super();
    }

    /**
     * Copy constructor. All properties copied from will be marked as read-write
     * Be very careful when using this method or you will loose readOnly tagging
     *
     * @param properties Properties
     * @param readOnly   specifies whether the copied properties should be marked
     *                   as read-only or not.
     */
    public UserProperties(Properties properties, boolean readOnly) {
        Enumeration<?> sourceNameEnum = properties.propertyNames();
        while (sourceNameEnum.hasMoreElements()) {
            String curSourceName = (String)sourceNameEnum.nextElement();
            UserProperty curUserProperty = new UserProperty(curSourceName,
                    properties.getProperty(curSourceName), readOnly);
            this.properties.put(curSourceName, curUserProperty);
        }
    }

    /**
     * Copy constructor
     *
     * @param copy the properties to create the copy from
     */
    public UserProperties(UserProperties copy) {
        this.properties = copy.properties.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new UserProperty(e.getValue())));
    }

    public void putAll(UserProperties ups) {
        ups.properties.keySet().removeAll(properties.keySet());
        properties.putAll(ups.properties);
    }

    public void putAll(Properties ups) {
        properties.keySet().removeAll(properties.keySet());
        for (Object o : ups.keySet()) {
            final String propName = o.toString();
            properties.put(propName, new UserProperty(propName, "", false));
        }
    }

    public UserProperty getUserProperty(String name) {
        return properties.get(name);
    }

    public void setUserProperty(String name, UserProperty value) {
        properties.put(name, value);
    }

    public UserProperty removeUserProperty(String name) {
        return properties.remove(name);
    }

    /**
     * Tests if a property is read-only.
     * Warning : this method also returns false if the property doesn't exist !
     *
     * @param name String
     * @return boolean
     */
    public boolean isReadOnly(String name) {
        UserProperty userProperty = properties.get(name);
        return userProperty != null && userProperty.isReadOnly();
    }

    public Iterator<String> propertyNameIterator() {
        return properties.keySet().iterator();
    }

    public Properties getProperties() {
        Properties propertiesCopy = new Properties();
        for (Map.Entry<String,UserProperty> o : properties.entrySet()) {
            Map.Entry<String,UserProperty> curEntry = o;
            UserProperty curUserProperty = curEntry.getValue();
            propertiesCopy.put(curUserProperty.getName(), curUserProperty.getValue());
        }
        return propertiesCopy;
    }

    public String getProperty(String name) {
        UserProperty userProperty = properties.get(name);
        return userProperty != null ? userProperty.getValue() : null;
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public void setProperty(String name, String value)
            throws UserPropertyReadOnlyException {
        UserProperty userProperty = properties.get(name);
        if (userProperty != null) {
            if (userProperty.isReadOnly()) {
                throw new UserPropertyReadOnlyException(userProperty,
                        "Property " + name + " is readonly");
            }
            userProperty.setValue(value);
        } else {
            userProperty = new UserProperty(name, value, false);
        }
        properties.put(name, userProperty);
    }

    public String removeProperty(String name)
            throws UserPropertyReadOnlyException {
        UserProperty userProperty = properties.get(name);
        if (userProperty != null) {
            if (userProperty.isReadOnly()) {
                throw new UserPropertyReadOnlyException(userProperty,
                        "Property " + name + " is readonly");
            } else {
                return userProperty.getValue();
            }
        }
        return null;
    }

    public int size() {
        return properties.keySet().size();
    }

    @Override
    public String toString() {
        return getProperties().toString();
    }

}
