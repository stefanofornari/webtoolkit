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

/**
 *
 * @author ste
 */
public abstract class AbstractAuthenticator implements Authenticator {
    
    private String message = "Basic login";
    
    /**
     * The authenticator can provide a message to describe the authentication 
     * mechanism. This can be used to set the WWW-Authenticate header
     *
     * @return the authentication message
     */
    @Override
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String msg) {
        message = msg;
    }
}
