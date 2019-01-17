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
package org.jahia.ajax.gwt.client.widget.calendar;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.google.gwt.user.client.Event;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * 
 * User: toto
 * Date: Dec 31, 2008
 * Time: 3:24:55 PM
 * 
 */
public class CalendarPanel extends LayoutContainer {

    // TODO GXT 2

    public CalendarPanel(final String callback, String activeDate) {
        setLayout(new FlowLayout());
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
        final DateMenu menu = new DateMenu();
        if (activeDate != null && !"".equals(activeDate.trim())) {
            menu.getDatePicker().setValue(dateTimeFormat.parse(activeDate), true);
        }
        menu.addListener(new EventType(Event.ONKEYDOWN+Event.ONKEYUP), new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce){
                nativeCallback(callback, dateTimeFormat.format(menu.getDate()));
            }
        });

        add(menu.getDatePicker());

    }

    public static native void nativeCallback(String callback, String date) /*-{
        try {
            eval('$wnd.'+callback)(date);
        } catch (e) {};
    }-*/;

}
