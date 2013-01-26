<!DOCTYPE html>
<html>
    <head>
        <title>BeanShell Web Console</title>
        <link href="favicon.png" rel="icon">
        <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <div class="well pagination-centered"><h1>BeanShell Web Console</h1></div>
        <form class="form" accept-charset="utf-8" action="exec.bsh" method="POST">
            <div class="row-fluid">
            <textarea class="span10 offset1" name="script" rows="10">$script</textarea>
            </div>
            <div class="row-fluid">
            <button class="btn span10 offset1"type="submit">Exec</button>
            </div>
        </form>
        <div id ="result" class="row-fluid">
        <pre class="span10 offset1">$result</pre>
        </div>
    </body>
</html>