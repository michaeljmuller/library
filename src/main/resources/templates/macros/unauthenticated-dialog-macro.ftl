<#macro unauthenticatedDialog title>
<!DOCTYPE html>
<#setting url_escaping_charset="UTF-8">
<html lang="en">
    <head>
        <#-- This meta tag is hugely important for making 1px on mobile look like 1px on desktop -->
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <meta charset="UTF-8">
        <title>${title}</title>
        <link rel="stylesheet" href="/styles/style.css">
        <link rel="stylesheet" href="/styles/unauthenticated-dialog.css">
    </head>
    <body>
        <div id="dialog">
            <#nested/>
        </div>
    </body>
</html>
</#macro>