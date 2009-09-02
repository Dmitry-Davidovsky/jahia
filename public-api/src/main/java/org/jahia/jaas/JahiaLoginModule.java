/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.jaas;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.jahia.api.user.JahiaUserService;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 27 févr. 2006
 * Time: 12:00:21
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLoginModule implements LoginModule {
    public static final String SYSTEM = " system ";
    public static final String GUEST = " guest ";

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();
    private static Map<String, Token> systemPass = new HashMap<String, Token>();

    private static JahiaUserService userService;

    private Subject subject;
    private Principal user = null;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback[] { new NameCallback("name?"), new PasswordCallback("pass?",false) } ;
            callbackHandler.handle(callbacks);

            String name = ((NameCallback)callbacks[0]).getName();
            char[] pass = ((PasswordCallback)callbacks[1]).getPassword();
            if (name != null) {
                if (SYSTEM.equals(name)) {
                    String key = new String(pass);
                    Token token = removeToken(name, key);
                    if (token != null) {
                        user = new JahiaPrincipal(SYSTEM, true, false, token.deniedPath);
                    }
                } else if (name.startsWith(SYSTEM)) {
                    String key = new String(pass);
                    Token token = removeToken(name, key);
                    if (token != null) {
                        user = new JahiaPrincipal(name.substring(SYSTEM.length()), true, false, token.deniedPath);
                    }
                } else if (GUEST.equals(name)) {
                    user = new JahiaPrincipal(GUEST, false, true, null);
                } else {
                    String key = new String(pass);
                    Token token = removeToken(name, key);
                    if ((token != null) || getUserService().checkPassword(name,key)) {
                        user = new JahiaPrincipal(name, token != null ? token.deniedPath : null);
                    }
                }
                if (user == null) {
                    throw new FailedLoginException();
                }
            }
        } catch (UnsupportedCallbackException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user != null;
    }

    public static Token removeToken(String name, String key) {
        if (systemPass.get(key) != null && systemPass.get(key).username.equals(name)) {
            return systemPass.remove(key);
        }
        return null;
    }

    public static Token getToken(String name, String key) {
        if (systemPass.get(key) != null && systemPass.get(key).username.equals(name)) {
            return systemPass.get(key);
        }
        return null;
    }

    public boolean commit() throws LoginException {
        if (user == null) {
            return false;
        } else {
            // add a principals (authenticated identities) to the Subject
            subject.getPrincipals().add(user);
            return true;
        }
    }

    public boolean abort() throws LoginException {
        if (user == null) {
            return false;
        } else {
            logout();
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(user);
        user = null;
        return true;
    }

    private static synchronized String getSystemPass(String user, List<String> deniedPathes) {
        String p = idGen.nextIdentifier().toString();
        systemPass.put(p, new Token(user, deniedPathes));
        return p;
    }

    public static Credentials getSystemCredentials() {
        return new SimpleCredentials(JahiaLoginModule.SYSTEM, getSystemPass(JahiaLoginModule.SYSTEM, null).toCharArray());
    }

    public static Credentials getSystemCredentials(String username) {
        if (username == null) {
            return getSystemCredentials();
        }
        return new SimpleCredentials(JahiaLoginModule.SYSTEM + username, getSystemPass(JahiaLoginModule.SYSTEM + username, null).toCharArray());
    }

    public static Credentials getSystemCredentials(String username, List<String> deniedPathes) {
        if (username == null) {
            return getSystemCredentials();
        }
        return new SimpleCredentials(JahiaLoginModule.SYSTEM + username, getSystemPass(JahiaLoginModule.SYSTEM + username, deniedPathes).toCharArray());
    }

    public static Credentials getGuestCredentials() {
        return new SimpleCredentials(JahiaLoginModule.GUEST, new char[0]);
    }

    public static Credentials getCredentials(String username) {
        return new SimpleCredentials(username, getSystemPass(username, null).toCharArray());
    }

    public static Credentials getCredentials(String username, List<String> deniedPathes) {
        return new SimpleCredentials(username, getSystemPass(username, deniedPathes).toCharArray());
    }

//    public JahiaUserService getJahiaUserService() throws NamingException {
//        String serviceName = "jahia/users";
//        Hashtable env = new Hashtable();
//        InitialContext initctx = new InitialContext(env);
//        try {
//            Context ctx = (Context) initctx.lookup("java:comp/env");
//            return (JahiaUserService) ctx.lookup(serviceName);
//        } catch (NamingException e) {
//            return (JahiaUserService) initctx.lookup("java:jahia/users");
//        }
//    }

    public static JahiaUserService getUserService() {
        return userService;
    }

    public static void setUserService(JahiaUserService userService) {
        JahiaLoginModule.userService = userService;
    }

    public static class Token {
        public String username;
        public List<String> deniedPath;

        Token(String username, List<String> deniedPath) {
            this.username = username;
            this.deniedPath = deniedPath;
        }
    }
}
