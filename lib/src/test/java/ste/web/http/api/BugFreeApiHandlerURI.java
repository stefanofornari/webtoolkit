/*
 * Copyright (C) 2015 Stefano Fornari.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * STEFANO FORNARI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. STEFANO FORNARI SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package ste.web.http.api;

import java.io.File;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHttpRequest;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;
import static ste.web.http.api.BugFreeRRequest.*;

/**
 * @TODO: limit to POST only?
 * @TODO: source the script and then invoke a method with the same name of the action
 * 
 * @author ste
 */
public class BugFreeApiHandlerURI extends BugFreeApiHandlerBase {
    
    private static final String ROOT = "src/test/apiroot";
        
    public BugFreeApiHandlerURI() {
        super(ROOT);
    }
    
    @Before
    public void setup() throws Exception {
        super.setup();
    }
    
    /**
     * URI syntax:
     * <code>
     *   /{webcontext}/{application}/{action}/[/{resource}]
     * 
     *   examples:
     *   /siteroot/api/shop/purchase/items/123
     *   /content/api/publisher/get/ids
     * </code>
     */
    @Test
    public void identify_the_action() throws Throwable {
        handler.handle(request("/api" + TEST_API_URI01), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("save");
        handler.handle(request("/api" + TEST_API_URI02), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("get");
    }
    
    @Test
    public void script_not_found() throws Exception {
        handler.handle(request("/api" + TEST_API_URI03), response, context);
        then(HttpStatus.SC_NOT_FOUND).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase()).contains(new File(ROOT, "store/none/get.bsh").getAbsolutePath());
    }
    
    @Test
    public void get_handler_only_request() throws Exception {
        handler.handle(request("/api" + TEST_API_URI04), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("get");
    }
    
    @Test
    public void get_restful_resource() throws Exception {
        handler.handle(request("/api" + TEST_API_URI01), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        
        then(resource()).containsExactly("items", "10");
        
        handler.handle(request("/api" + TEST_API_URI05), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        
        then(resource()).containsExactly("items", "10", "sets", "5", "subsets", "2");
    }
    
    @Test
    public void invalid_restful_request() throws Exception {
        handler.handle(request("/api" + TEST_API_URI07), response, context);
        then(HttpStatus.SC_BAD_REQUEST).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase())
            .contains("invalid rest request")
            .contains(TEST_API_URI07)
            .contains(StringEscapeUtils.escapeHtml4("/<apicontext>/<application>/<action>/<resource>"));
        
    }
    
    @Test
    /**
     * The result of the script shall be stored in a variable with the same name 
     * of the script. 
     */
    public void result_in_script_name_var() throws Exception {
        handler.handle(request("/api" + TEST_API_URI01), response, context);
        
        then(context.getAttribute("items")).isEqualTo("{one: '111', two: '222'}");
    }
    
    @Test
    public void provide_webcontext() throws Exception {
        handler = new ApiHandler(new File(ROOT).getAbsolutePath(), "/mysite/sapi");
        
        handler.handle(request("/mysite/sapi" + TEST_API_URI01), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("save");
    }
    
    // ------------------------------------------------------- protected methods
    
    @Override
    protected BasicHttpRequest request(final String uri) {
        return new BasicHttpRequest("POST", uri(uri));
    }
    
    // --------------------------------------------------------- private methods
    
    private String[] resource() {
        return (String[])context.getAttribute("resource");
    }
}
