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
package org.jahia.services.pwdpolicy;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Rule parameter service object.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyRuleParam implements Serializable, Cloneable {

    private static final long serialVersionUID = -7015454747220376620L;

	private String id;

    private String name;

    private String value;

    /**
     * Initializes an instance of this class.
     */
    public JahiaPasswordPolicyRuleParam() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     * @param name
     * @param value
     */
    public JahiaPasswordPolicyRuleParam(String id, String name, String value) {

        this();
        this.id = id;
        this.name = name;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        JahiaPasswordPolicyRuleParam param = null;
        try {
            param = (JahiaPasswordPolicyRuleParam) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        return param;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            JahiaPasswordPolicyRuleParam castOther = (JahiaPasswordPolicyRuleParam) obj;
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                    .isEquals();
        }
        return false;
    }

    /**
     * Returns the parameter id.
     * 
     * @return the rule id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the parameter name.
     * 
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    /**
     * Sets the value of id.
     * 
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the value of parameter name.
     * 
     * @param name
     *            the parameter name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value for this parameter.
     * 
     * @param value
     *            the value for this parameter
     */
    public void setValue(String value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name)
                .append("value", value).toString();
    }

}
