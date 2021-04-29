<#import "subs.ftl" as subs>
<html>
<head>
    <title>${name} timesheets ${year}</title>
    <@subs.css/>
</head>
<body>
<table cellspacing='0'>
    <tr>
        <td colspan=18 class=center>${name} timesheets ${year} (generated@${now}).</td>
    </tr>
    <tr>
        <td colspan=2 class=center>
            <a class='tooltipped' target='_blank' href='${writeTimeUrl}'>
                write time
                <span>${nbsp("Click here to register additional hours for DCL.")}</span>
            </a>
        </td>
        <#list months as m>
            <td class='center wide'>${m.name}</td>
        </#list>
        <td class=center colspan=4></td>
    </tr>
    <tr>
        <td colspan=2 class=center>totals/reports</td>
        <td colspan=12 class=center></td>
        <td colspan=1 class=center>budget</td>
        <td colspan=1 class=center>spend</td>
        <td colspan=1 class=center>left</td>
        <td colspan=1 class=center>name</td>
    </tr>
    <#list users as u>
        <tr>
            <td class=light></td>
            <td class=light>${u.name}</td>
            <#list u.months as m>
                <td class=white>
                    <span class=spend>
                        <a class='tooltipped' target='_blank'
                           href='${m.url}'>
                            ${m.worked}
                            <span>${nbsp("budget: ${m.budget}")}</span>
                        </a>
                    </span>
                    <span class=budget>${m.budget}</span>
                </td>
            </#list>
            <td>${u.budget}</td>
            <td>${u.worked}</td>
            <td class='${u.budgetLeftClass}'>${u.budgetLeft}</td>
            <td class='center light'>${u.name}</td>
        </tr>
    </#list>
    <tr>
        <td colspan=2>budget</td>
        <#list months as m>
            <td>${m.budget}</td>
        </#list>
        <td>${budget}</td>
        <td colspan=3></td>
    </tr>
    <tr>
        <td colspan=2>spend:</td>
        <#list months as m>
            <td class='monTotal'>${m.total}</td>
        </#list>
        <td></td>
        <td>${total}</td>
        <td colspan=2></td>
    </tr>
    <tr>
        <td colspan=2>left:</td>
        <#list months as m>
            <td class='${m.budgetLeftClass}'>${m.budgetLeft}</td>
        </#list>
        <td colspan=2></td>
        <td class='budgetLeft negative'>${budgetLeft}</td>
        <td></td>
    </tr>
    <tr>
        <td colspan=2>left NOW:</td>
        <#list months as m>
            <td class='${m.budgetLeftNowClass}'>${m.budgetLeftNow}</td>
        </#list>
        <td colspan=2></td>
        <td class='${budgetLeftClass}'>${budgetLeft}</td>
        <td></td>
    </tr>
</table>
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
