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
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.pipelines;

import org.jahia.pipelines.valves.Valve;

import java.util.Map;

/**
 * @author <a href="mailto:david@bluesunrise.com">David Sean Taylor</a>
 * @version $Id$
 */
public interface Pipeline {

    void initialize() throws PipelineException;

    /**
     * <p>Add a new Valve to the end of the pipeline.</p>
     *
     * @param valve Valve to be added.
     * @throws IllegalStateException If the pipeline has not been
     *                               initialized.
     */
    void addValve(Valve valve);

    /**
     * <p>Add a new Valve at the specified position of the pipeline.</p>
     *
     * @param valve Valve to be added.
     * @throws IllegalStateException If the pipeline has not been
     *                               initialized.
     */
    void addValve(int position, Valve valve);

    /**
     * <p>Return the set of all Valves in the pipeline.  If there are no
     * such Valves, a zero-length array is returned.</p>
     *
     * @return An array of valves.
     */
    Valve[] getValves();

    /**
     * <p>Cause the specified request and response to be processed by
     * the sequence of Valves associated with this pipeline, until one
     * of these Valves decides to end the processing.</p>
     * <p/>
     * <p>The implementation must ensure that multiple simultaneous
     * requests (on different threads) can be processed through the
     * same Pipeline without interfering with each other's control
     * flow.</p>
     *
     * @param context The run-time information, including the servlet
     *                request and response we are processing.
     * @throws PipelineException an input/output error occurred.
     */
    void invoke(Object context) throws PipelineException;

    /**
     * <p>Remove the specified Valve from the pipeline, if it is found;
     * otherwise, do nothing.</p>
     *
     * @param valve Valve to be removed.
     */
    void removeValve(Valve valve);

    /**
     * <p>Tell if (at least) one of the valves of the pipeline is an instance
     * of a given class or interface.</p>
     *
     * @param c the class or interface
     * @return
     */
    public boolean hasValveOfClass(Class<Valve> c);

    /**
     * <p>Return the first valve of the pipeline that is an instance
     * of a given class or interface.</p>
     *
     * @param c the class or interface
     * @return a Valve instance, or null if no valve matches.
     */
    public Valve getFirstValveOfClass(Class<Valve> c);

    /**
     * Used by users of the pipeline to provide an environment in which the valves
     * will execute. This can be useful for example to provide service references, or
     * other shared data among all the valves.
     *
     * @param environment
     */
    public void setEnvironment(Map<String, Object> environment);

}
