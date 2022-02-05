<#macro page title additionalHeadContent="">
<!DOCTYPE html>
<#setting url_escaping_charset="UTF-8">
<html lang="en">
    <head>
        <#-- This meta tag is hugely important for making 1px on mobile look like 1px on desktop -->
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <meta charset="UTF-8">
        <title>${title?html}</title>
        <link rel="stylesheet" href="/styles/style.css">

        <#-- these are for the top nav, which was lifted from https://codepen.io/andornagy/pen/xhiJH -->
        <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/styles/topnav.css">

        ${additionalHeadContent!}
    </head>
    <body>

        <form id="logoutform" action="/logout" method="post"><#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"></#if></form>

        <div id="container">
            <nav>
                <ul>
                    <li><a href="/">Home</a></li>
                    <li><a href="/authors">Authors</a></li>
                    <li><a href="/tags">Tags</a></li>
                    <li><a href="#">Admin</a>
                        <ul>
                            <li><a href="/metadata">Metadata</a></li>
                            <li><a href="#">Users</a></li>
                            <li><a href="https://cloud.linode.com/object-storage/buckets/us-east-1/michaeljmuller-media" target="_blank">Assets</a></li>
                            <li><a href="/addBooks">Add Books</a></li>
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
            <div id="title">${title?html}</div>
        </div>
        <div class="page-content">
            <#nested/>
        </div>
    </body>
</html>
</#macro>