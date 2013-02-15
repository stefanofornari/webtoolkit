HOW TO INSTALL
--------------

1. Copy bsh-2.0b4.jar, jstl-1.2.jar under your webapp's WEB-INF/lib directory
2. Copy BeanShellServlet.class under your webapp's WEB-INF/classes/ste/web/beanshell
3. Add the XML fragment below in your webapp's WEB-INF/web.xml
   <servlet>
       <servlet-name>BeanShellServlet</servlet-name>
       <servlet-class>ste.web.beanshell.BeanShellServlet</servlet-class>
   </servlet>
   <servlet-mapping>
       <servlet-name>BeanShellServlet</servlet-name>
       <url-pattern>*.bsh</url-pattern>
   </servlet-mapping>
4. Copy index.bsh and index.jsp under your webapp's <ROOT>/bsh
5. Restart the web application and hit bsh/index.jsp to test the installation