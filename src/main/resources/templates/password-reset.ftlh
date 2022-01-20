<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Password Reset">
    <form id="form" action="/pw-reset" method="post">
        <p class="title">Password Reset</p>
        <#if msg??>
            <div class="msg">${msg}</div>
        </#if>
        <div class="instructions">
            Please enter your email address below.
            I will email you a link to generate a new password.
        </div>
        <input type="text" placeholder="email address" name="email" id="email" autofocus/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <button onclick="document.getElementById('loginForm').submit()">
            <span class="state">Reset Password</span>
        </button>
    </form>
</@d.unauthenticatedDialog>