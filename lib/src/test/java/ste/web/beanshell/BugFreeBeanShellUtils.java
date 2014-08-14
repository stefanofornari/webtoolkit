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

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static ste.web.beanshell.Constants.*;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests. Note that specific behaviour for different types of containers
 * are specified in dedicated bug free classes.
 *
 * @author ste
 */
public class BugFreeBeanShellUtils {
    
    protected static final String[] TEST_JSON_ERRORS = {
            "", "   ", "\t ",
            "a string", "{ 'label': value", "'label': 'value'", "[{} {}]",
            "[{} {}]", "['uno', 'due'", "'tre', 'quattro']"
        };

    @Test
    public void getScriptNull() throws Exception {
        try {
            BeanShellUtils.getScript(null);
            fail("getScript cannot be invoched with null");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }
    }

    @Test
    public void getScript() throws Exception {
        then(
            BeanShellUtils.getScript(
                new File("src/test/resources/firstlevelscript.bsh")
            )
        ).contains("first = true;");
    }

    @Test
    public void getNotExistingScript() throws Exception {
        try {
            BeanShellUtils.getScript(new File("src/test/resources/notexistingscript.bsh"));
            fail("file not found exception expected");
        } catch (FileNotFoundException e) {
            //
            // OK
            //
        }
    }
    
    // ------------------------------------------------------- protected methods

    /**
     * 
     * @param i
     * @param parameters
     * @param attributes
     * 
     * @throws java.lang.Exception
     */
    protected void checkSetup(
        Interpreter i, 
        Map<String, List<String>> parameters,
        Map<String, List<String>> attributes
    ) throws Exception {
        then(i.get(VAR_REQUEST)).isNotNull();
        then(i.get(VAR_RESPONSE)).isNotNull();
        then(i.get(VAR_SESSION)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(i.get(VAR_LOG)).isNotNull();

        for (String name: parameters.keySet()) {
            then(i.get(name)).isEqualTo(parameters.get(name).get(0));
        }
        
        for (String name: attributes.keySet()) {
            then(i.get(name)).isEqualTo(attributes.get(name).get(0));
        }
    }

    protected void checkCleanup(
        Interpreter i, 
        Set<String> parameters
    ) throws Exception {
        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        for (String name: parameters) {
            then(i.get(name)).isNull();
        }

        //
        // Make sure we do not unset too much :)
        //
        then(i.get(VAR_REQUEST)).isNotNull();
        then(i.get(VAR_RESPONSE)).isNotNull();
        then(i.get(VAR_SESSION)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(i.get(VAR_LOG)).isNotNull();
    }
    
    protected void checkBodyAsNotSpecifiedType(final Interpreter i) throws Exception {
        then(i.get(VAR_BODY)).isNull();
    }
    
    protected void checkJSONObject(Interpreter i, final String label, final String value) 
    throws JSONException, EvalError {
        Object o = i.get(VAR_BODY);
        then(o).isNotNull().isInstanceOf(JSONObject.class);
        then(((JSONObject)o).getString(label)).isEqualTo(value);
    }
    
    protected void checkJSONArray(Interpreter i, final String[] labels, final String[] values) 
    throws JSONException, EvalError {
        Object o = i.get(VAR_BODY);
        then(o).isNotNull().isInstanceOf(JSONArray.class);
        JSONArray a = (JSONArray)o;
        then(a.length()).isEqualTo(labels.length); 
        for (int j=0; j<labels.length; ++j) {
            o = a.getJSONObject(j);
            then(((JSONObject)o).getString(labels[j])).isEqualTo(values[j]);
        }
    }
}
