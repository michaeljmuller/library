<#import "macros/page-macro.ftl" as c/>

<#assign hdr>

    <link rel="stylesheet" href="https://unpkg.com/dropzone@5/dist/min/dropzone.min.css" type="text/css" />
    <script src="https://unpkg.com/dropzone@5/dist/min/dropzone.min.js"></script>
    <script>
        Dropzone.autoDiscover = false;
        window.onload = function() {
            let myDropzone = new Dropzone("div#dz", {
                url: '/book',
                method: 'post',
                chunking: true,
                <#if _csrf??>
                init: function() {
                    this.on("sending", function(file, xhr, formData) {
                        formData.append("${_csrf.parameterName}", "${_csrf.token}")
                    })
                }
                </#if>
            })
        }
    </script>
</#assign>

<@c.page title="Upload Asset" additionalHeadContent=hdr>
    <div id="dz" class="dropzone"></div>
</@c.page>