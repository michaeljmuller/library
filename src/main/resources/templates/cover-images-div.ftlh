<#setting url_escaping_charset="UTF-8">
<div class="imagePicker">
    <#list images as image>
        <img src="/epubImage?objId=${epubObjId?url}&file=${image?url}" />
    </#list>
</div>