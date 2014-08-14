/*
 * Copyright (C) 2014 Stefano Fornari.
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

package ste.web.http;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeBasicHttpRequest {
    
    public static final String TEST_METHOD = "http";
    public static final String TEST_URI = "index.html";
    
    @Test
    public void constructors() throws Exception {
        final String[] BLANKS = new String[] { null, "", " ", "  \t\n" };
        
        for (String blank: BLANKS) {
            try {
                new BasicHttpRequest(blank);
                fail("missing check for not blankable parameters");
            } catch (IllegalArgumentException x) {
                then(x.getMessage()).containsIgnoringCase("uri")
                                    .contains("not be " + ((blank == null) ? "null" : "blank"));
            }
        }
    }
    
    //
    // NOTE: we give a few basic specs here; for more details see BugFreeQueryString
    //
    
    @Test
    public void getNoParameters() throws Exception {
        BasicHttpRequest r = new BasicHttpRequest(TEST_URI);
        then(r.getQueryString().getMap()).isEmpty();
        
        r = new BasicHttpRequest(TEST_URI + "?");
        then(r.getQueryString().getMap()).isEmpty();
    }
    
    @Test
    public void getSingleParameter() throws Exception {
        BasicHttpRequest r = new BasicHttpRequest(TEST_URI + "?param1=value1");
        
        Map<String, List<String>> parameters = r.getQueryString().getMap();
        then(parameters).hasSize(1);
        then(parameters).containsEntry("param1", Arrays.asList("value1"));
    }
    
    @Test
    public void getMultipleParameters() throws Exception {
        BasicHttpRequest r = new BasicHttpRequest(TEST_URI + "?param1=value11&param2=value21&param1=value12");
        
        Map<String, List<String>> parameters = r.getQueryString().getMap();
        then(parameters).hasSize(2);
        then(parameters).containsEntry("param1", Arrays.asList("value11", "value12"))
                        .containsEntry("param2", Arrays.asList("value21"));
        
    }
}
