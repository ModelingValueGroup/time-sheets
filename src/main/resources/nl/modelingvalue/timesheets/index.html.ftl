<#-- @ftlvariable name="" type="nl.modelingvalue.timesheets.model.IndexModel" -->
<#import "subs.ftl" as subs>
<!DOCTYPE html>
<html>
<head>
    <title>MVG timesheets</title>
    <@subs.css/>
</head>
<body>

<#list pages as page>
    <ul>
        <li><a class="active">${page.name}</a></li>
        <#list page.years as y>
            <li><a href="${href(page.name,y)}">${y}</a></li>
        </#list>
    </ul>
</#list>

<div class="separator"></div>

<#if err?size != 0>
    <div class="terminal-title">error</div>
    <div class="terminal error">
        <#list err as s>
            ${nbsp(s)}<br>
        </#list>
    </div>
</#if>

<#if info?size != 0>
    <div class="terminal-title">info</div>
    <div class="terminal info">
        <#list info as s>
            ${nbsp(s)}<br>
        </#list>
    </div>
</#if>

<#if traceToHtml()>
    <#if trace?size != 0>
        <div class="terminal-title">trace</div>
        <div class="terminal trace">
            <#list trace as s>
                ${nbsp(s)}<br>
            </#list>
        </div>
    </#if>

    <#if debug?size != 0>
        <div class="terminal-title">debug</div>
        <div class="terminal debug">
            <#list debug as s>
                ${nbsp(s)}<br>
            </#list>
        </div>
    </#if>
</#if>

</body>
</html>
