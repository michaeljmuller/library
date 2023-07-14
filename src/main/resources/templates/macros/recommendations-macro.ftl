<#macro recommendations recoList>
    <#list recoList as rec>
        <a href="/book/${rec.book().id?c}" style="display: block; text-decoration: none;">
            <div class="rec-container rec-clearfix">
                <div class="rec-cover-div">
                    <img src="/book/cover/${rec.book().id?c}" />
                </div>
                <div class="rec-text">
                    <div><span class="rec-title">${rec.book().title}</span> by ${rec.book().author}</div>
                    <div>
                        <span class="rec-user">Recommended by ${rec.review().user.firstName}: </span>
                        <span class="rating">
                            <#switch rec.review().rating>
                                <#case 0>Did not finish<#break>
                                <#case 1>Zero Stars<#break>
                                <#case 2>⭐<#break>
                                <#case 3>⭐½<#break>
                                <#case 4>⭐⭐<#break>
                                <#case 5>⭐⭐½<#break>
                                <#case 6>⭐⭐⭐<#break>
                                <#case 7>⭐⭐⭐½<#break>
                                <#case 8>⭐⭐⭐⭐<#break>
                                <#case 9>⭐⭐⭐⭐½<#break>
                                <#case 10>⭐⭐⭐⭐⭐<#break>
                            </#switch>
                        </span>
                    </div>
                    <div>${rec.review().reviewHtml ! }</div>
                </div>
            </div>
        </a>
    </#list>
</#macro>