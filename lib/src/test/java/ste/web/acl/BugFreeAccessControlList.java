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
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * TODO: regex special characters in permissions shall be escaped
 */
public class BugFreeAccessControlList {
    
    private final AccessControlList EMPTY = new AccessControlList();
    private final AccessControlList ACL = new AccessControlList();
    private final HashSet<String> PERMISSION_SET_1 = new HashSet<>();
    private final HashSet<String> PERMISSION_SET_2 = new HashSet<>();
    private final HashSet<String> PERMISSION_SET_3 = new HashSet<>();
    
    @Before
    public void before() {
        ACL.add("ste.web.acl.permissions.star");
        
        PERMISSION_SET_1.add("ste.web.acl.permissions.dummy");
        PERMISSION_SET_2.add("ste.web.acl.permissions.dummy");
        PERMISSION_SET_2.add("ste.web.acl.permissions.star");
        PERMISSION_SET_3.add("ste.web.acl.permissions.dummy");
        PERMISSION_SET_3.add("ste.web.acl.permissions.star");
        PERMISSION_SET_3.add("ste.web.acl.permissions.other");
    }
    
    @Test
    public void check_permissions_fails_if_no_given_permissions() {
        try {
            ACL.check(null);
        } catch (AccessControlException x) {
            then(x).hasMessageContaining("no permissions given");
        }
        
        try {
            ACL.check(new HashSet<String>());
        } catch (AccessControlException x) {
            then(x).hasMessage("given permissions [] miss some of [ste.web.acl.permissions.star]");
        }
    }
    
    @Test
    public void check_permissions_fail_if_no_permissions() {
        try {
            EMPTY.check(PERMISSION_SET_1);
        } catch (AccessControlException x) {
            then(x).hasMessageContaining("no allowed permissions");
        }
        
        try {
            EMPTY.check(PERMISSION_SET_2);
        } catch (AccessControlException x) {
            then(x).hasMessageContaining("no allowed permissions");
        }
    }
    
    @Test
    public void check_permissions_fail_if_not_all_permissions_given() {
        try {
            ACL.check(PERMISSION_SET_1);
        } catch (AccessControlException x) {
            then(x).hasMessage("given permissions [ste.web.acl.permissions.dummy] miss some of [ste.web.acl.permissions.star]");
        }
        
        try {
            ACL.check(PERMISSION_SET_3);
        } catch (AccessControlException x) {
            then(x).hasMessage("given permissions " + PERMISSION_SET_3 + " miss some of [ste.web.acl.permissions.star]");
        }
    }
    
    @Test
    public void check_permissions_ok_if_all_permissions_given() {
        ACL.check(PERMISSION_SET_2);
    }
    
    @Test
    public void check_permissions_with_regex() {
        AccessControlList acl = new AccessControlList();
        
        acl.add(".*");
        acl.check(PERMISSION_SET_1);
        acl.check(PERMISSION_SET_2);
        acl.check(PERMISSION_SET_3);
    }
    
    @Test
    public void get_permissions() {
        then(EMPTY.getPermissions()).isEmpty();
        then(ACL.getPermissions()).containsExactly("ste.web.acl.permissions.star");
        ACL.add("ste.web.acl.permissions.dummy");
        then(ACL.getPermissions())
            .contains("ste.web.acl.permissions.star")
            .contains("ste.web.acl.permissions.dummy");
    }
}
