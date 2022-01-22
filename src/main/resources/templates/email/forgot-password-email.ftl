<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:o="urn:schemas-microsoft-com:office:office">
<!-- taken from https://webdesign.tutsplus.com/articles/build-an-html-email-template-from-scratch--webdesign-12770 -->
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <meta name="x-apple-disable-message-reformatting">
    <title></title>
    <!--[if mso]>
    <noscript>
        <xml>
            <o:OfficeDocumentSettings>
                <o:PixelsPerInch>96</o:PixelsPerInch>
            </o:OfficeDocumentSettings>
        </xml>
    </noscript>
    <![endif]-->
    <style>
        table, td, div, h1, p {font-family: Arial, sans-serif;}
    </style>
</head>
<body style="margin:0;padding:0;">
<table role="presentation" style="width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;">
    <tr>
        <td align="center" style="padding:0;">
            <table role="presentation" style="width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;">
                <tr>
                    <td style="padding:40px;background:#70bbd9;">
                        Mike's eBook Library
                    </td>
                </tr>
                <tr>
                    <td style="padding:0;">
                        <p>You have recieved this message because you request a password reset for Mike's eBook and Audiobook Library.</p>
                        <p>Please click the following link to reset your password:</p>
                        <p><a href="${applicationBaseURL}/pw-reset-process-token?userid=${user.id}&token=${token}">Reset Password</a></p>
                        <p>This token is only valid for ${tokenLifespan} minutes; it will expire at ${tokenExpirationDateTime?time}.</p>
                    </td>
                </tr>
                <#--
                <tr>
                    <td style="padding:20px;background:#ee4c50;">
                        <a href="https://library.themullers.org">Mike's eBook and Audiobook Library</a>
                    </td>
                </tr>
                -->
            </table>
        </td>
    </tr>
</table>
</body></html>


