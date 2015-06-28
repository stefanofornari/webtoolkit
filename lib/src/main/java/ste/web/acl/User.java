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

import java.security.Principal;
import java.util.Set;

/**
 *
 * @author ste
 */
public class User implements Principal {
    
    final private String name;
    
    private String secret;
    private Set<String> permissions;
    
    public User(String name) {
               this.name = name;
        this.permissions = null;
             this.secret = null;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
