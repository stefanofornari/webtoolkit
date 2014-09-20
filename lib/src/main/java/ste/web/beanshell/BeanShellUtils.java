/*
 * BeanShell Web
 * Copyright (C) 2012 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.web.beanshell;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 *
 * @author ste
 */
public class BeanShellUtils {

    // --------------------------------------------------------------- Constants

    public static final String DEFAULT_CONTROLLERS_PREFIX = "/";
    public static final String DEFAULT_VIEWS_PREFIX = "/";
    public static final String LOG_NAME = "ste.web";
    public static final String PARAM_CONTROLLERS = "controllers-prefix";
    public static final String PARAM_VIEWS = "views-prefix";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final Logger log = Logger.getLogger(LOG_NAME);

    // ---------------------------------------------------------- Public methods

    /**
     * Reads the given script enclosing it into a try-catch block.
     *
     * @param script the file to read - NOT NULL
     *
     * @return the script content
     *
     * @throws IOException in case of IO errors
     */
    public static String getScript(File script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream is = null;
        try {
            is = new FileInputStream(script);

            byte[] buf = new byte[1024];
            int n = 0;
            while ((n = is.read(buf)) >= 0) {
                baos.write(buf, 0, n);
            }
            return "try { " + baos.toString() + "} catch (Throwable t) { t.printStackTrace(); throw t; }";
        } finally {
            if (is != null) {
                is.close();
            }
            baos.close();
        }
    }

    /**
     * Replaces '.' with "_".
     * 
     * @param name the name to normalize - NOT NULL
     * 
     * @return the normalized version of the name
     */
    public static String normalizeVariableName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        return name.replaceAll("\\.", "_");
    }
    
    public static Object getJSONBody(final InputStream in) throws IOException {
        Object o = null;
        try {
            BufferedInputStream is = new BufferedInputStream(in);
            is.mark(0);
            if (is.read() == '{') {
                is.reset();
                o = new JSONObject(
                    new JSONTokener(new InputStreamReader(is))
                );
            } else {
                is.reset();
                o = new JSONArray(
                    new JSONTokener(new InputStreamReader(is))
                );
            }
        } catch (JSONException x) {
            throw new IOException("error parsing the body as a JSON object", x);
        }
        
        return o;
    }
}
