/*
 * Copyright (C) 2016 Stefano Fornari.
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
package ste.web.acl;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeAuthenticator {
    
    /**
     * The authenticator can provide a message to describe the authentication 
     * mechanism. This can be used to set the WWW-Authenticate header
     */
    @Test
    public void set_and_get_auth_message() {
        //
        // The default is "Basic login"
        //
        AbstractAuthenticator a = new AbstractAuthenticator() {

            @Override
            public void check(User user) throws MissingCredentialsException, InvalidCredentialsException {
            }
        };
        then(a.getMessage()).isEqualTo("Basic login");
        
        final String MSG = "Form authentication";
        a.setMessage(MSG);
        then(a.getMessage()).isEqualTo(MSG);
        
    }
            
}
