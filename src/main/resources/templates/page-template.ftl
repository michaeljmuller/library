<#macro page title>
 <!DOCTYPE html>
 <#setting url_escaping_charset="UTF-8">
 <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>${title?html}</title>
        <link rel="stylesheet" href="style.css">
    </head>
    <body>

    <form id="logoutform" action="/logout" method="post"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></form>

    <div class="menu">
        <a href="/">home</a>
        <a href="/authors">authors</a>
        <a href="/tags">tags</a>
        <a href="/metadata">metadata</a>
        <a href="javascript:;" onclick="document.getElementById('logoutform').submit()">logout</a>
        <a class="profile" href="/profile">profile</a>
    </div>

    <div class="title">${title?html}</div>

    <div class="page-content">
        <#nested/>
    </div>

    <!--
    <div>footer</div>
    -->

    </body>
</html>
</#macro>