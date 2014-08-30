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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 * TODO: see cobertura report
 * 
 * @author ste
 */
public class BugFreeSessionHeader {
    
    @Test
    public void headerMustHaveSessionAndPath() {
        SessionHeader h = new SessionHeader("12345");
        then(h.getName()).isEqualTo("Set-Cookie");
        then(h.toString()).isEqualTo("JSESSIONID=\"12345\";$Path=\"/\"");
        
        h = new SessionHeader("67890");
        then(h.toString()).isEqualTo("JSESSIONID=\"67890\";$Path=\"/\"");
    }
}
