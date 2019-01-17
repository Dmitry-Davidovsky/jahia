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
package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;

import java.util.List;

/**
 * User: rfelden
 * Date: 16 janv. 2009 - 15:54:26
 */
public class FilterStatusBar extends MyStatusBar {

    public FilterStatusBar(List<String> filters, List<String> mimeTypes, List<String> nodeTypes) {
        // display filters to inform user
        StringBuilder disp = new StringBuilder() ;
        if (filters != null && filters.size()>0) {
            disp.append(Messages.get("filters.label")).append(" : ").append(filters) ;
        }
        if (mimeTypes != null && mimeTypes.size()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.get("mimes.label")).append(" : ").append(mimeTypes) ;
        }
        if (nodeTypes != null && nodeTypes.size()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.get("nodes.label")).append(" : ").append(nodeTypes) ;
        }
        setMessage(disp.toString());
    }

}
