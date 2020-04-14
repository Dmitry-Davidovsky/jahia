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
package org.jahia.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JahiaException extends Exception
{
    private static Logger logger = LoggerFactory.getLogger(JahiaException.class);

    private static final long   serialVersionUID = -1837958722110615030L;

    public static final int     WARNING_SEVERITY        =    1;
    public static final int     ERROR_SEVERITY          =    2;
    public static final int     CRITICAL_SEVERITY       =    3;
    public static final int     FATAL_SEVERITY   =    4;

    public static final int     INITIALIZATION_ERROR    =    0;
    public static final int     DATABASE_ERROR          =    1;
    public static final int     FILE_ERROR              =    2;
    public static final int     DATA_ERROR              =    3;
    public static final int     TEMPLATE_ERROR          =    4;
    public static final int     PERSISTENCE_ERROR       =    5;


    /** User error code */
    public static final int     USER_ERROR              =    5;

    /** Page error code */
    public static final int     PAGE_ERROR              =    6;

    public static final int     WINDOW_ERROR            =    7;
    public static final int     PARAMETER_ERROR         =    8;
    public static final int     APPLICATION_ERROR       =    9;
    public static final int     NEWSFEED_ERROR          =   10;
    public static final int     CONFIG_ERROR            =   11;
    public static final int     SERVICE_ERROR           =   12;
    public static final int     REGISTRY_ERROR          =   13;
    public static final int     SERVLET_ERROR           =   14;
    public static final int     LISTENER_ERROR          =   15;

    /** Security error code (Access denied) */
    public static final int     SECURITY_ERROR          =   16;

    /** Access Control List error code */
    public static final int     ACL_ERROR               =   17;

    public static final int     CACHE_ERROR             =   18;
    public static final int     OBJECT_ERROR            =   19;
    public static final int     ENGINE_ERROR            =   20;

    /** Lock error code */
    public static final int     LOCK_ERROR              =   21;

    /** Session error code */
    public static final int     SESSION_ERROR           =   22;

    /** Accounts error code */
    protected static final int  ACCOUNTS_ERROR          =   23;

    /** License error code */
    protected static final int  LICENSE_ERROR           =   24;

    /** Jef file error code */
    public static final int  JEF_ERROR           		=   25;

    /** Java Security ERROR */
    public static final int  JAVASECURITY_ERROR     	=   26;

    /** Engine Validation Errors */
    public static final int ENGINE_VALIDATION_ERROR     =   27;

    /** Service unavailable ERROR */
    public static final int UNAVAILABLE_ERROR     	    =   28;

    /** Archive File error code **/
    public static final int	ENTRY_NOT_FOUND				= 	30;

    /** Site not found **/
    public static final int	SITE_NOT_FOUND				= 	40;

    /** Template deployment and management error **/
    public static final int TEMPLATE_SERVICE_ERROR      =   50;

    protected String  mUserErrorMsg;
    protected String  mJahiaErrorMsg;
    protected int     mErrorCode;
    protected int     mErrorSeverity;

    private int responseErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    //-------------------------------------------------------------------------
    /**
     * constructor
     * EV    31.10.2000
     *
     */
    public JahiaException  (String  userErrorMsg,
                            String  jahiaErrorMsg,
                            int     errorCode,
                            int     errorSeverity)
    {
        super ("User message=" + userErrorMsg +
                ", System message=" + jahiaErrorMsg);

        mUserErrorMsg   = userErrorMsg;
        mJahiaErrorMsg  = jahiaErrorMsg;
        mErrorCode      = errorCode;
        mErrorSeverity  = errorSeverity;

    } // end constructor

    /**
     * This exception constructor allows us to keep the original exception information
     * so that we may display it and access more details about the real cause
     * of the exception. Hopefully the need for this will disappear with the
     * introduction of exception chaining in JDK 1.4
     */
    public JahiaException  (String  userErrorMsg,
                            String  jahiaErrorMsg,
                            int     errorCode,
                            int     errorSeverity,
                            Throwable t)
    {
        super ("User message=" + userErrorMsg +
                ", System message=" + jahiaErrorMsg +
                ", root cause:" + ((t == null) ? "null" : t.getMessage()), t);

        mUserErrorMsg   = userErrorMsg;
        mJahiaErrorMsg  = jahiaErrorMsg;
        mErrorCode      = errorCode;
        mErrorSeverity  = errorSeverity;

    }


    //-------------------------------------------------------------------------
    /**
     * getSeverity
     * EV    31.10.2000
     *
     */
    public int getSeverity()
    {
        return mErrorSeverity;
    } // end getSeverity


    //-------------------------------------------------------------------------
    // FH       8 Jan. 20001
    /**
     * Return a string holding the user readeable error message.
     *
     * @return Return a string of the user error message.
     */
    public final String getUserErrorMsg ()
    {
        return mUserErrorMsg;
    }


    //-------------------------------------------------------------------------
    // FH       8 Jan. 20001
    /**
     * Return a string holding the internal (jahia) error message.
     *
     * @return Return a string of the internal error message.
     */
    public final String getJahiaErrorMsg ()
    {
        return mJahiaErrorMsg;
    }


    //-------------------------------------------------------------------------
    // FH       8 Jan. 20001
    /**
     * Return the error code.
     *
     * @return Return the error code.
     */
    public final int getErrorCode ()
    {
        return mErrorCode;
    }

    /**
     * Accessor method for the root cause exception.
     * @return Throwable the return value is an exception OR can be null in
     * which case this signifies we didn't specify at creation the original source
     * of the exception (or that this was an original exception we generated and
     * didn't need to catch).
     */
    public final Throwable getRootCause() {
        return getCause();
    }

    // the following methods exists so that we always print out details
    // about the root cause if there is one.

    public String toString() {
        logger.debug("called.");
        StringBuilder result = new StringBuilder();
        result.append(super.toString());
        if (getRootCause() != null) {
            result.append(" root cause=");
            result.append(getRootCause().toString());
        }
        return result.toString();
    }

    public void printStackTrace(PrintWriter s) {
        logger.debug("(PrintWriter s) called.");
        super.printStackTrace(s);
        if (getRootCause() != null) {
            getRootCause().printStackTrace(s);
        }
    }

    public void printStackTrace(PrintStream s) {
        logger.debug("(PrintStream s) called.");
        super.printStackTrace(s);
        if (getRootCause() != null) {
            getRootCause().printStackTrace(s);
        }
    }

    public void printStackTrace() {
        logger.debug("() called.");
        super.printStackTrace();
        if (getRootCause() != null) {
            getRootCause().printStackTrace();
        }
    }

    public int getResponseErrorCode() {
        return responseErrorCode;
    }

} // end JahiaException


