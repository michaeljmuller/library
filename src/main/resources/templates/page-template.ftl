<#macro page title>
<!DOCTYPE html>
<#setting url_escaping_charset="UTF-8">
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>${title?html}</title>
        <link rel="stylesheet" href="/styles/style.css">

        <!-- these are for the top nav, which was lifted from https://codepen.io/andornagy/pen/xhiJH -->
        <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/styles/topnav.css">
    </head>
    <body>

        <form id="logoutform" action="/logout" method="post"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></form>


        <div id="container">
            <nav>
                <ul>
                    <li><a href="/">Home</a></li>
                    <li><a href="/authors">Authors</a></li>
                    <li><a href="/tags">Tags</a></li>
                    <li><a href="#">Administration</a>
                        <ul>
                            <li><a href="/metadata">Metadata</a></li>
                            <li><a href="#">Users</a></li>
                        </ul>
                    </li>
                    <li class="right"><a href="#">${user.firstName?default("user")}</a>
                        <ul>
                            <li><a href="#">Profile</a></li>
                            <li><a href="javascript:;" onclick="document.getElementById('logoutform').submit()">Logout</a></li>
                        </ul>
                    </li>
                </ul>
            </nav>
            <div class="title">${title?html}</div>
            <div>
                <#nested/>
            </div>
        </div>
    </body>
</html>
</#macro>