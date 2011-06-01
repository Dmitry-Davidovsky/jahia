/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

 package org.jahia.tools.files;

/*
 * FileUpload
 * Copyright (c) 2000 Jahia Ltd  All rights reserved.
 *
 */

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class FileUpload.
 * Class to handle fileupload
 * It's essentially a wrapper for the Multipart Request class
 * of the com.oreilly.servlet.Multipart class
 *
 * @author Khue ng
 * @version 1.0
 */
public class FileUpload {
    public static final String FILEUPLOAD_ATTRIBUTE = "fileUpload";

    private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger (FileUpload.class);
    private static final String UTF_8 = "UTF-8";

    private Map<String, List<String>> params;
    private Map<String, DiskFileItem> files;
    private Map<String, DiskFileItem> filesByFieldName;

    private HttpServletRequest req;

    private String savePath = "";
    private String encoding;

    /**
     * Constructor
     *
     * @param req
     * @param savePath the path where files should be saved
     * @param fileMaxSize the max size of file to upload
     */
    public FileUpload(final HttpServletRequest req,
                      final String savePath,
                      final int fileMaxSize)
        throws IOException {

        this.req = req;
        this.savePath = savePath;
        this.encoding = UTF_8;
        init();
    }

    /**
     * Constructor
     *
     * @param req
     * @param savePath    the path where files should be saved
     * @param fileMaxSize the max size of file to upload
     * @param charset The charset to use to decode the values (default = UTF-8)
     */
    public FileUpload(final HttpServletRequest req,
                      final String savePath,
                      final int fileMaxSize,
                      final String charset)
            throws IOException {

        this.req = req;
        this.savePath = savePath;
        if (charset == null) {
            this.encoding = UTF_8;
        } else {
            this.encoding = charset;
        }
        init();
    }

    /**
     * Init the MultiPartReq object if it's actually null
     *
     * @exception IOException
     */
    protected void init ()
        throws IOException {

        params = new HashMap<String, List<String>>();
        files = new HashMap<String, DiskFileItem>();
        filesByFieldName = new HashMap<String, DiskFileItem>();

        parseQueryString();

        if (checkSavePath(savePath)) {
            try {
                final DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1);
                factory.setRepository(new File(savePath));

                final ServletFileUpload upload = new ServletFileUpload(factory);

                final List<?> items = upload.parseRequest(req);

                final Iterator<?> iter = items.iterator();
                while (iter.hasNext()) {
                    final DiskFileItem item = (DiskFileItem) iter.next();

                    if (item.isFormField()) {
                        final String name = item.getFieldName();
                        final List<String> v ;
                        if (params.containsKey(name)) {
                            v = params.get(name);
                        } else {
                            v = new ArrayList<String>();
                            params.put(name,v);
                        }
                        v.add(item.getString(encoding));
                    } else {
                        if (item.getSize() > 0) {
                            files.put(item.getStoreLocation().getName(), item);
                            filesByFieldName.put(item.getFieldName(), item);
                        }
                    }

                }
            } catch (FileUploadException ioe) {
                logger.error("Error while initializing FileUpload class:", ioe);
                throw new IOException(ioe.getMessage());
            }
        } else {
            logger.error(
                    "FileUpload::init storage path does not exists or can write");
            throw new IOException(
                    "FileUpload::init storage path does not exists or cannot write");
        }
    }

    /**
     * Return an Iterator of parameters name
     *
     * @return an Iterator of parameters name
     */
    public Set<String> getParameterNames () {
        return params.keySet();
    }

    public Map<String, List<String>> getParameterMap () {
        return params;
    }

    public void setParameterMap(Map<String, List<String>> params) {
        this.params = params;
    }

    /**
     * Return the values of a parameter
     *
     * @param paramName the name of the parameter
     * @return the values of a parameter as a String Array
     */
    public String[] getParameterValues (final String paramName) {
        final List<String> list = ((List<String>) params.get(paramName));
        if (list == null) return null;
        String[] res = new String[list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Return an Iterator of parameters name
     *
     * @return an Iterator of parameters name
     */
    public Set<String> getFileNames () {
        return files.keySet();
    }

    public File getFile(String n) {
        return files.get(n).getStoreLocation();
    }

    /**
	 * Returns the map of all {@link FileUpload} objects keyed by the form field
	 * name.
	 * 
	 * @return the map of all {@link FileUpload} objects keyed by the form field
	 *         name
	 */
	public Map<String, DiskFileItem> getFileItems() {
		return filesByFieldName;
	}

    public String getFileSystemName(String n) {
        return files.get(n).getName();
    }

    public String getFormFieldName(String n) {
        return files.get(n).getFieldName();
    }

    public String getFileContentType(String n) {
        return files.get(n).getContentType();
    }

    public void markFilesAsConsumed() {
        files = null;
        filesByFieldName = null;
    }

    /**
     * Method checkSavePath
     * Check if the path where to save files is valid
     *
     * @param path the relative path where to save files to
     * @return true if this directory exists, and can be write to
     */
    protected boolean checkSavePath (String path) {

        if (path != null && (path.length() > 0)) {

            logger.debug("path is " + path);
            File tmpFile = new File(path);
            if (tmpFile.isDirectory() && tmpFile.canWrite()) {
                savePath = path;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Parse a query String and create an Map of parameter/value
     *
     */
    protected void parseQueryString () {

        logger.debug(req.getQueryString());

        if (req.getQueryString() == null) {
            return;
        }

        final StringTokenizer tokenizer = new StringTokenizer(req.getQueryString(),
                "&");
        while (tokenizer.hasMoreTokens()) {
            final String param = tokenizer.nextToken();
            int pos = param.indexOf("=");
            if (pos > 0) {
                final String name = param.substring(0, pos);
                final String value = param.substring(pos + 1, param.length());
                final List<String> v ;
                if (params.containsKey(name)) {
                    v = (List<String>) params.get(name);
                } else {
                    v = new ArrayList<String>();
                    params.put(name,v);
                }
                v.add(value);
            }
        }
    }

}