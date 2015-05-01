/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.web.beanshell;

import bsh.EvalError;
import bsh.TargetError;
import java.io.FileNotFoundException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeBeanShellError {
    @Test
    public void constructors() {
        try {
            new BeanShellError(null);
            fail("missing invalid arguments check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("cause can not be null");
        }
        
        Throwable c = new FileNotFoundException();
        TargetError e = new TargetError(c, null, null);
        then(new BeanShellError(e).getCause()).isSameAs(c);
    }
    
    @Test
    public void get_cause_with_TargetError_with_target() {
        Throwable c = new FileNotFoundException();
        TargetError e = new TargetError(c, null, null);
        then(new BeanShellError(e).getCause()).isSameAs(c);
        
        c = new IllegalArgumentException();
        e = new TargetError(c, null, null);
        then(new BeanShellError(e).getCause()).isSameAs(c);
    }
    
    @Test
    public void get_cause_with_EvalError_returns_null() {
        EvalError e = new EvalError("a message", null, null);
        then(new BeanShellError(e).getCause()).isNull();
    }
    
    @Test
    public void get_message_returns_the_EvalError_message() {
        EvalError e = new EvalError("a message", null, null);
        then(new BeanShellError(e).getMessage()).isEqualTo("a message");
        
        e = new TargetError("another message", new Exception(), null, null, true);
        then(new BeanShellError(e).getMessage()).isEqualTo("another message");
    }
    
    
}
