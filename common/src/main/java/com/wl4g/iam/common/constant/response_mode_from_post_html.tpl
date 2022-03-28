<html>
    <head>
        <title>Submit This Form</title>
    </head>
    <body onload="javascript:document.forms[0].submit()">
        <form method="post" action="%s">
            <input type="hidden" name="state" value="%s" />
            <input type="hidden" name="code" alue="%s" />
            <input type="hidden" name="id_token" alue="%s" />
            <input type="hidden" name="token" alue="%s" />
        </form>
    </body>
</html>