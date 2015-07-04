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

import java.util.HashMap;

/**
 *
 */
public class HashMapAuthenticator 
extends HashMap<String, String>
implements Authenticator {
    public HashMapAuthenticator(User[] users) {
        for (User u: users) {
            put(u.getName(), u.getSecret());
        }
    }
    
    public HashMapAuthenticator() {
    }

    public void check(User user) throws MissingCredentialsException, InvalidCredentialsException {
        if (user == null){
            throw new MissingCredentialsException();
        }
        
        if (!this.containsKey(user.getName())) {
            throw new InvalidCredentialsException();
        }
        
        String password = get(user.getName());
        
        if (password == null) {
            if (user.getSecret() != null) {
                throw new InvalidCredentialsException();
            }
        } else {
            if (!password.equals(user.getSecret())) {
                throw new InvalidCredentialsException();
            }
        }
    }
}
