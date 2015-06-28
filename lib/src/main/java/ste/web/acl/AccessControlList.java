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

import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Not thread safe
 * 
 */
public class AccessControlList {
    
    Set<String> permissions = new HashSet<>();
    
    public AccessControlList add(final String permission) {
        permissions.add(permission);
        
        return this;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void checkPermissions(final Set<String> permissions)
    throws AccessControlException {
        if (permissions == null) {
            throw new AccessControlException("no permissions given");
        }
        
        if (this.permissions.isEmpty()) {
            throw new AccessControlException("no allowed permissions");
        }
        
        if (permissions.isEmpty() || !containsAll(permissions)) {
            throw new AccessControlException("given permissions " + permissions + " miss some of " + this.permissions);
        }
    }
    
    // --------------------------------------------------------- private methods
    
    private boolean containsAll(final Set<String> permissions) {
        for(String p: this.permissions) {
            if (!contains(permissions, p)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean contains(final Set<String> permissions, final String toCheck) {
        for(String p: permissions) {
            if (p.matches(toCheck)) {
                return true;
            }
        }
        
        return false;
    }
}
