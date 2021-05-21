<#-- @ftlvariable name="" type="nl.modelingvalue.timesheets.model.IndexModel" -->
<!DOCTYPE html>
<html>
<head>
    <title>MVG timesheets</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <link href="${stylesCss}" rel="stylesheet">
    <script src="${scriptsJs}"></script>
</head>
<body>

<#list groups as g>
    <ul>
        <li><a class="active">${g.name}</a></li>
        <#list g.years as y>
            <#if y?has_content>
                <li><a href="${href(g.name,y)}">${y}</a></li>
            <#else >
                <li>&nbsp;</li>
            </#if>
        </#list>
    </ul>
</#list>
<button class="iconButton tooltipped"
        onclick='document.cookie = "pw=;expires=Thu, 01 Jan 1970 00:00:01 GMT";location.reload();'>
    <i class="fa fa-lock"></i>
    <span class="tooltiptext">remove password</span>
</button>

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
