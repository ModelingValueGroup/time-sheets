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

<#if err?size != 0>
    <H2>Errors:</H2>
    <div class="terminal err">
        <#list err as s>
            ${nbsp(s)}<br>
        </#list>
    </div>
</#if>

<#if info?size != 0>
    <H2>Infos:</H2>
    <div class="terminal info">
        <#list info as s>
            ${nbsp(s)}<br>
        </#list>
    </div>
</#if>

<#if log?size != 0>
    <H2>Logs:</H2>
    <div class="terminal log">
        <#list log as s>
            ${nbsp(s)}<br>
        </#list>
    </div>
</#if>

</body>
</html>
