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

import org.apache.http.config.ConnectionConfig;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.reflect.PrivateAccess.getInstanceValue;

/**
 * Note that the code of this class is taken from the apache code, not all specs
 * have been created. For now there are only the specs I need so far.
 * 
 * @author ste
 */
public class BugFreeBasicHttpConnectionFactory {
    
    @Test
    public void singleton() {
        then(
            BasicHttpConnectionFactory.INSTANCE
        ).isNotNull();
    }
    
    @Test
    public void defaultInstanceShallHaveDefaultConfiguration() throws Exception {
        //
        // the default instance shall have default configuration parameters
        //
        then(
            getInstanceValue(new BasicHttpConnectionFactory(), "config")
        ).isEqualTo(ConnectionConfig.DEFAULT);
    }
}
