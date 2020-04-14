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
package org.jahia.services.render;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jahia.utils.Patterns;

/**
 * Template information including the name, the node path, the view name, an optional priority and a reference to the next template.
 * 
 * @author Thomas Draier
 */
public class Template implements Serializable {

    private static final long serialVersionUID = -1700784569502723022L;

    public String name;
    public Template next;
    public String node;
    public int priority;
    public String view;

    /**
     * Initializes an instance of this class by deserializing the provided string.
     * 
     * @param serialized
     *            the serialized form of the template
     */
    public Template(String serialized) {
        String[] s = Patterns.SLASH.split(StringUtils.substringBefore(serialized, "|"));
        this.view = s[0].equals("null") ? null : s[0];
        this.node = s[1];
        this.name = s[2].equals("null") ? null : s[2];
        String n = StringUtils.substringAfter(serialized, "|");
        if (!StringUtils.isEmpty(n)) {
            this.next = new Template(n);
        }
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param view
     *            the view name
     * @param node
     *            the node path
     * @param next
     *            the next {@link Template} in the resolution chain
     * @param name
     *            the name
     */
    public Template(String view, String node, Template next, String name) {
        this(view, node, next, name, 0);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param view
     *            the view name
     * @param node
     *            the node path
     * @param next
     *            the next {@link Template} in the resolution chain
     * @param name
     *            the name
     * @param priority
     *            the resolution priority
     */
    public Template(String view, String node, Template next, String name, int priority) {
        super();
        this.view = view;
        this.node = node;
        this.name = name;
        this.next = next;
        this.priority = priority;
    }

    /**
     * Returns the name of the template.
     * 
     * @return the name of the template
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the next {@link Template} in the resolution chain.
     * 
     * @return the next {@link Template} in the resolution chain
     */
    public Template getNext() {
        return next;
    }

    public List<Template> getNextTemplates() {
        List<Template> t;
        if (next == null) {
            t = new ArrayList<Template>();
        } else {
            t = next.getNextTemplates();
        }
        t.add(this);
        return t;
    }

    /**
     * Returns the node path.
     * 
     * @return the node path
     */
    public String getNode() {
        return node;
    }

    /**
     * Returns the template ordering priority.
     * 
     * @return the template ordering priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the name of the view.
     * 
     * @return the name of the view
     */
    public String getView() {
        return view != null ? view : "default";
    }

    /**
     * Returns the serialized form of this template.
     * 
     * @return the serialized form of this template
     */
    public String serialize() {
        return serialize(new StringBuilder(64)).toString();
    }

    /**
     * Appends the serialized form of this template to the specified buffer.
     * 
     * @param buffer
     *            the buffer to append a serialized form of this template to
     * @return the buffer with the serialized form of this template appended
     */
    protected StringBuilder serialize(StringBuilder buffer) {
        buffer.append(view).append("/").append(node).append("/").append(name);
        if (next != null) {
            buffer.append("|");
            next.serialize(buffer);
        }
        return buffer;
    }

    /**
     * Sets the next {@link Template} in the resolution chain.
     * 
     * @param next
     *            the next {@link Template} in the resolution chain
     */
    void setNext(Template next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "template " + name + " with view " + view + " for node " + node;
    }
}
