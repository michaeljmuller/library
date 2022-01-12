<html>
<head>
</head>
<body>
    <#if msg??>
        <div>${msg}</div>
    </#if>

    <form action="/login" method="post">
        <input type="text" id="username" name="username" /><br />
        <input type="password" id="password" name="password" /><br />
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <button type="submit" class="btn">Log in</button>
    </form>
</body>
</html>
