<#-- @ftlvariable name="" type="nl.modelingvalue.timesheets.model.PageModel" -->
<#import "subs.ftl" as subs>
<!DOCTYPE html>
<html>
<head>
    <title>${name} - ${year}</title>
    <link href="styles.css" rel="stylesheet">
    <script src="scripts.js"></script>
</head>
<body onload="setupBudgets()">

<@subs.table model=totalTable/>
<br>
<#list subTables as sub>
    <@subs.table model=sub/>
    <br>
</#list>


<label for="spend"><input type="radio" name="spend" id="spend"/>Hours Spend</label><br>
<label for="budget"><input type="radio" name="budget" id="budget"/>Budget Hours</label>
<br>
<br>
Changes in JIRA will not be reflected immediately, but this page is automatically recalculated regularly

</body>
</html>
