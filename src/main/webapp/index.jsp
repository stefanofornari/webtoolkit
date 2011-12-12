<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>BeanShell Servlet test page</title>
    </head>
    <body>
        Hello <c:out value="${firstname}"/> <c:out value="${lastname}"/>!
        <hr/>
        <form action="index.bsh" method="post">
            Your first name: <input type="text" name="firstname" value="<c:out value="${firstname}"/>"/><br/>
            Your last name: <input type="text" name="lastname" value="<c:out value="${lastname}"/>"/><br/>
            <input type="submit" value="Say hello"/><br/>
        </form>
    </body>
</html>
