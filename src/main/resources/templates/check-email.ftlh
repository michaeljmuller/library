<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Password Reset">
    <form id="form" >
        <p class="title">Check Your Email</p>
        <div class="instructions">
            <p>Please check your email; you have been sent an email with a
            link that you can use to reset your password.</p>
            <p>If you have already reset your password, you can log in
            using the link below.</p>
        </div>
        <a href="/login">Return to Login</a>
    </form>
</@d.unauthenticatedDialog>