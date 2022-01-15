<html>
<!-- adapted from https://codepen.io/boudra/pen/YXzLBN found here https://freshdesignweb.com/css-login-form-templates/ -->
<head>
    <link href='https://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/styles/login.css" />
    <style>
    </style>
</head>
<body>
    <div class="wrapper">
        <form id="loginForm" class="login" action="/login" method="post">
            <p class="title">Mike's eBook Library</p>
            <#if msg??>
                <div class="msg">${msg}</div>
            </#if>
            <input type="text" placeholder="Username" name="username" id="username" autofocus/>
            <i class="fa fa-user"></i>
            <input type="password" placeholder="Password" name="password" id="password"/>
            <i class="fa fa-key"></i>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <a href="#">Forgot your password?</a>
            <button onclick="document.getElementById('loginForm').submit()">
                <i class="spinner"></i>
                <span class="state">Log in</span>
            </button>
        </form>
        </p>
    </div>
</body>
</html>

