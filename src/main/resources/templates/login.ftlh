<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Login">
    <form id="form" action="/login" method="post">
        <p class="title">Mike's eBook Library</p>
        <#if msg??>
            <div class="msg">${msg}</div>
        </#if>
        <input type="text" placeholder="Username" name="username" id="username" autofocus/>
        <input type="password" placeholder="Password" name="password" id="password"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <a href="#">Forgot your password?</a>
        <button onclick="document.getElementById('loginForm').submit()">
            <span class="state">Log in</span>
        </button>
    </form>
</@d.unauthenticatedDialog>