<#import "subs.ftl" as subs>
<!DOCTYPE html>
<html>
<head>
    <title>${name} - ${year}</title>
    <@subs.css/>
</head>
<body>

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
Changes in JIRA will not be reflected immediately.
<br>
This page is automatically recalculated every hour (<a class='tooltipped'
                                                       href='${recalcUrl}'>recalc
    now<span>${nbsp("force a recalculation now")}</span></a>)
<#if otherProjects?size != 0>
    <br>
    <br>
    Other pages:
    <#list otherProjects as p>
        <a class='tooltipped' href='${p.url}'>
            ${p.name} (${p.year})
            <span>${nbsp("Go to the ${p.name} page.")}</span>
        </a>
    </#list>
</#if>


<@subs.js/>

</body>
</html>
