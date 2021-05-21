<#-- @ftlvariable name="" type="nl.modelingvalue.timesheets.model.PageModel" -->
<#import "table.ftl" as imported>
<!DOCTYPE html>
<html>
<head>
    <title>${name} - ${year}</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <link href="${stylesCss}" rel="stylesheet">
    <script src="${scriptsJs}"></script>
</head>
<body onload="setupBudgets()">

<#if subTables?size!=1>
    <@imported.table model=totalTable main=1/>
    <br>
    <hr>
    <br>
</#if>
<#list subTables as sub>
    <@imported.table model=sub main=0/>
    <br>
</#list>


<label for="spend"><input type="radio" name="spend" id="spend"/>Hours Spend</label><br>
<label for="budget"><input type="radio" name="budget" id="budget"/>Budget Hours</label>
<br>
<br>
Changes in JIRA will not be reflected immediately, this page will refresh regularly.

</body>
</html>
