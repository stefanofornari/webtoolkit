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


import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

public class BugFreeUser {
    @Test
    public void constructors() {
        try {
            for (String BLANK: BLANKS) {
                new User(BLANK);
                fail("missing not empty check on name");
            }
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("name can not be empty");
        }
        
        for (String name: new String[] {"a user", "another user"}) {
            User u = new User(name);
            then(u.getName()).isEqualTo(name);
        }
        
        for (String secret: new String[] {null, "", "a password", "  "}) {
            User u = new User("a user", secret);
            then(u.getSecret()).isEqualTo(secret);
        }
    }
}