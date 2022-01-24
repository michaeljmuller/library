<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Login">
    <form id="form" action="/login" method="post">
        <p class="title">Mike's eBook Library</p>
        <#if msg??>
            <div class="msg">${msg}</div>
        </#if>
        <input type="text" placeholder="Email Address" name="username" id="username" autofocus/>
        <input type="password" placeholder="Password" name="password" id="password"/>
        <#if _csrf??>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </#if>
        <a href="/pw-reset">Forgot your password?</a>
        <button onclick="document.getElementById('loginForm').submit()">
            <span class="state">Log in</span>
        </button>
    </form>
</@d.unauthenticatedDialog>