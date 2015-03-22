/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.web.http.api;

import java.io.File;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHttpRequest;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;
import static ste.web.http.api.BugFreeRRequest.*;
import static ste.web.http.api.Constants.VAR_RREQUEST;

/**
 * @TODO: limit to POST only?
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
     *   /api/{action}/{handler}[/{resource}]
     * </code>
     */
    @Test
    public void identify_the_action() throws Throwable {
        handler.handle(request(TEST_API_URI01), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("save");
        handler.handle(request(TEST_API_URI02), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }
    
    @Test
    public void script_not_found() throws Exception {
        handler.handle(request(TEST_API_URI03), response, context);
        then(HttpStatus.SC_NOT_FOUND).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase()).contains(new File(ROOT, "none/none.bsh").getAbsolutePath());
    }
    
    @Test
    public void get_handler_only_request() throws Exception {
        handler.handle(request(TEST_API_URI04), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("action")).isEqualTo("get");
    }
    
    @Test
    public void get_restful_resource() throws Exception {
        handler.handle(request(TEST_API_URI01), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        
        then(resource()).containsExactly("items", "10");
        
        handler.handle(request(TEST_API_URI05), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        
        then(resource()).containsExactly("items", "10", "sets", "5", "subsets", "2");
    }
    
    @Test
    public void invalid_restful_request() throws Exception {
        handler.handle(request(TEST_API_URI06), response, context);
        then(HttpStatus.SC_BAD_REQUEST).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase())
            .contains("invalid rest request")
            .contains(TEST_API_URI06)
            .contains(StringEscapeUtils.escapeHtml4("/api/<action>/<resource>"));
        
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
