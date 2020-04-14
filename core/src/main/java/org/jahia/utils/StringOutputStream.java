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
package org.jahia.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * Output stream implementation that writes to a string buffer.
 * 
 * @author Sergiy Shyrkov
 */
public class StringOutputStream extends OutputStream {

    private StringWriter stringWriter = new StringWriter(256);

    private OutputStream wrapped;

    /**
     * Initializes an instance of this class.
     */
    public StringOutputStream() {
        this(null);
    }

    /**
     * Initializes an instance of this class with the supplied wrapped {@link OutputStream}.
     * 
     * @param wrapped
     *            a stream to write the output into
     */
    public StringOutputStream(OutputStream wrapped) {
        super();
        this.wrapped = wrapped;
    }

    public int getLength() {
        return getStringBuffer().length();
    }

    public StringBuffer getStringBuffer() {
        return stringWriter.getBuffer();
    }

    @Override
    public String toString() {
        return stringWriter.toString();
    }

    @Override
    public void write(int b) throws IOException {
        this.stringWriter.write(b);
        if (wrapped != null) {
            wrapped.write(b);
        }
    }
}
