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

import org.apache.http.HeaderElement;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

/**
 * TODO: see cobertura report
 * 
 * @author ste
 */
public class BugFreeSessionHeader {
    
    @Test
    public void header_has_session_path_secure_httponly() {
        SessionHeader h = new SessionHeader("12345");
        then(h.getName()).isEqualTo("Set-Cookie");
        then(h.toString()).isEqualTo(h.DEFAULT_SESSION_HEADER + "=12345; Path=/; Secure; HttpOnly");
        
        h = new SessionHeader("67890");
        then(h.toString()).isEqualTo(h.DEFAULT_SESSION_HEADER + "=67890; Path=/; Secure; HttpOnly");
    }
    
    @Test
    public void get_elements() {
        SessionHeader h = new SessionHeader("12345");
        HeaderElement[] elements = h.getElements();
        
        then(elements).isNotNull().hasSize(1);
        then(elements[0].getName()).isEqualTo(h.getName());
        then(elements[0].getValue()).isEqualTo(h.getValue());
    }
    
    @Test
    public void provide_session_id_name() {
        SessionHeader h = new SessionHeader("testid1", "12345");
        then(h.toString()).isEqualTo("testid1=12345; Path=/; Secure; HttpOnly");
        
        h = new SessionHeader("testid2", "67890");
        then(h.toString()).isEqualTo("testid2=67890; Path=/; Secure; HttpOnly");
    }
    
    @Test
    public void invalid_session_id_names() {
        for (String BLANK: BLANKS) {
            try {
                 new SessionHeader(BLANK, null);
                 fail("missing argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("session id name can not be null");
            }
        }
    }
}
