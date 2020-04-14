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
package org.jahia.services.pwdpolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * Java-based condition evaluator that expects the fully qualified class name to
 * be called. The class must implement the {@link PasswordPolicyRuleCondition}
 * interface.
 * 
 * @author Sergiy Shyrkov
 */
class JavaConditionEvaluator implements ConditionEvaluator {

	private static Map<String, PasswordPolicyRuleCondition> evaluatorsCache = new HashMap<String, PasswordPolicyRuleCondition>();

	public boolean evaluate(JahiaPasswordPolicyRule rule, EvaluationContext ctx) {
		return getConditionClazz(rule.getCondition()).evaluate(
		        rule.getConditionParameters(), ctx);
	}

	private PasswordPolicyRuleCondition getConditionClazz(String condition) {

		PasswordPolicyRuleCondition condClazz = null;
		if (evaluatorsCache.containsKey(condition)) {
			condClazz = evaluatorsCache.get(condition);
		} else {
			synchronized (JavaConditionEvaluator.class) {
				if (!evaluatorsCache.containsKey(condition)) {
					try {
						condClazz = (PasswordPolicyRuleCondition) Thread
						        .currentThread().getContextClassLoader()
						        .loadClass(condition).newInstance();
						evaluatorsCache.put(condition, condClazz);
					} catch (Exception ex) {
						throw new RuntimeException(
						        "Unable to instantiate condition class "
						                + condition, ex);
					}
				} else {
					condClazz = evaluatorsCache.get(condition);
				}
			}
		}

		return condClazz;
	}
}