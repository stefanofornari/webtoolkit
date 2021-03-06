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
package ste.web.acl;

/**
 *
 * @author ste
 */
public interface Authenticator {
    /**
     * Authenticate the given user
     * 
     * @param user the user to authenticate - MAY BE NULL
     * 
     * @throws InvalidCredentialsException if the authentication fails
     * @throws MissingCredentialsException if user does not contain credentials or is null
     */
    public void check(User user) throws MissingCredentialsException, InvalidCredentialsException;
    
    /**
     * The authenticator can provide a message to describe the authentication 
     * mechanism. This can be used to set the WWW-Authenticate header
     *
     * @return the authentication message
     */
    public String getMessage();
}
