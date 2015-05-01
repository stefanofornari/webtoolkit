/*
 * BeanShell Web
 * Copyright (C) 2012 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.web.beanshell;

import bsh.EvalError;
import bsh.TargetError;

/**
 * BeanShellError wraps the other BeanShell exceptions so to look like normal 
 * JDK 6+ exceptions (with proper use of the cause)
 * 
 */
public class BeanShellError extends Exception {

    /**
     * Creates a new BeanShellError
     * 
     * @param cause - NOT NULL
     */
    public BeanShellError(final EvalError cause) {
        super(cause);
        if (cause == null) {
            throw new IllegalArgumentException("cause can not be null");
        }
    }
    
    @Override
    public Throwable getCause() {
        Throwable t = super.getCause();
        if (!(t instanceof TargetError)) {
            return null;
        }
        
        TargetError cause = (TargetError)t;
        
        if (cause.getTarget() == null) {
            return cause;
        }
        
        return cause.getTarget();
    }
    
    @Override
    public String getMessage() {
        return super.getCause().getMessage();
    }
    
}
