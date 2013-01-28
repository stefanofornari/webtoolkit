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

/**
 *
 * @author ste
 */
public interface Constants {
    
    public static final String DEFAULT_CONTROLLERS_PREFIX = "/";
    public static final String DEFAULT_VIEWS_PREFIX = "/";    
    public static final String LOG_NAME = "ste.web";
    public static final String PARAM_CONTROLLERS = "controllers-prefix";
    public static final String PARAM_VIEWS = "views-prefix";
    public static final String ATTR_APP_ROOT = "WEBROOT";
    public static final String ATTR_VIEW = "view";
    
    public static final String VAR_SOURCE   = "source"  ;
    public static final String VAR_REQUEST  = "request" ;
    public static final String VAR_RESPONSE = "response";
    public static final String VAR_SESSION  = "session" ;
    public static final String VAR_OUT      = "out"     ;
    public static final String VAR_LOG      = "log"     ;
    
}
