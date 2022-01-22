<#import "macros/unauthenticated-dialog-macro.ftl" as d />

<@d.unauthenticatedDialog title="Password Reset">
    <form id="form">
        <p class="title">Password Reset Complete</p>
        <div class="instructions">
            <p>I generated the following password for you:</p>
            <p style="font-weight: bold; font-size: 150%;">${password}</p>
            <p>Once you've saved this in your password manager, you can log in to my library using the link below.</p>
            <p><a href="/login">Log in to Mike's Library</a></p>
            <p style="font-style: italic; font-size: 75%">You do use a password manager, right?</p>
        </div>
    </form>
</@d.unauthenticatedDialog>