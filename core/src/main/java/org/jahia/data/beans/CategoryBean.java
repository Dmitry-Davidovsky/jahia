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
package org.jahia.data.beans;

import org.jahia.content.JahiaObject;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRSessionFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Title: A wrapper JavaBean compliant class that uses the current
 * request context to display values back to the output. </p>
 * <p>Description: This class encapsulates a ProcessingContext and a Category class
 * to provide helper methods for template developers to access using Struts
 * or JSTL accessors to beans.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class CategoryBean extends AbstractJahiaObjectBean {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CategoryBean.class);

    private Category category;

    static {
        registerType(Category.class.getName(), CategoryBean.class.getName());
    }

    /**
     * Empty constructor to follow JavaBean compliance rules
     */
    public CategoryBean() {
    }

    /**
     * Constructor for wrapper
     *
     * @param category the category to wrap
     */
    public CategoryBean(Category category) {
        this.category = category;
    }

    /**
     * Static instantiator called from the getInstance() method of the
     * AbstractJahiaObjectBean class.
     *
     * @param jahiaObject       the JahiaObject instance to build this wrapper for
     * @return an instance of an AbstractJahiaObjectBean descendant corresponding
     *         to the JahiaObject type and request context
     */
    public static AbstractJahiaObjectBean getChildInstance(JahiaObject jahiaObject) {
        return new CategoryBean((Category) jahiaObject);
    }

    /**
     * @return the enclosed Category instance
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @return the title of the category for the current locale accessed through
     *         the Constants.getLocale() method.
     */
    public String getTitle() {
        final String title = category.getTitle(JCRSessionFactory.getInstance().getCurrentLocale());
        if (title != null) {
            return title;
        } else {
            return category.getKey();
        }
    }

    /**
     * @return the full set of properties
     */
    public Properties getProperties() {
        return category.getProperties();
    }

    public static Set<CategoryBean> getCategoryBeans(final Set<Category> categories) {
        final Set<CategoryBean> result = new HashSet<CategoryBean>();
        for (Category cat : categories) {
            result.add(new CategoryBean(cat));
        }
        return result;
    }

    /**
     * Returns the category key.
     *
     * @return the category key
     */
    public String getKey() {
        return category.getKey();
    }

}