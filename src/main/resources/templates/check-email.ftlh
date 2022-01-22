<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Password Reset">
    <form id="form" >
        <p class="title">Check Your Email</p>
        <div class="instructions">
            <p>If you have an account on this system, then I will send you an
                email with a link that you can use to reset your password.
                Your email provider may think I'm an evil hacker phishing robot,
                in which case <b>my email may be in your spam folder</b>.</p>
            <p>If you do NOT have an account on this system, then I will
                quietly do nothing.</p>
            <p>If you have already reset your password, you can log in
                using the link below.</p>
        </div>
        <a href="/login">Return to Login</a>
    </form>
</@d.unauthenticatedDialog>