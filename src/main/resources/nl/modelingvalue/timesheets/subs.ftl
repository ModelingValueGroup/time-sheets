<#--noinspection CssUnusedSymbol-->

<#macro table model>
    <table cellspacing='0'>
        <tr>
            <td colspan=18 class="center header">${model.name} - ${model.year}</td>
        </tr>
        <tr>
            <td colspan=2 class=center>
                <a class='tooltipped' target='_blank' href='${model.writeTimeUrl}'>
                    write time
                    <span class="tooltiptext">${model.nbsp("Click here to register additional hours for ${model.name}.")}</span>
                </a>
            </td>
            <#list model.months as m>
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
        <#list model.users as u>
            <tr>
                <td></td>
                <td class="light personName">${u.name}</td>
                <#list u.months as m>
                    <td class=white>
                    <span class=spend>
                        <a class='tooltipped' target='_blank'
                           href='${m.url}'>
                            ${m.worked}
                            <#if m.hasBudget()>
                                <span class="tooltiptext">${model.nbsp("budget: ${m.budget}")}</span>
                            </#if>
                        </a>
                    </span>
                        <span class=budget>${m.budget}</span>
                    </td>
                </#list>
                <td>${u.budget}</td>
                <td>${u.worked}</td>
                <td class='${u.budgetLeftClass}'>${u.budgetLeft}</td>
                <td class='left light personName'>${u.name}</td>
            </tr>
        </#list>
        <tr>
            <td colspan=2>spend:</td>
            <#list model.months as m>
                <td class='monTotal'>${m.worked}</td>
            </#list>
            <td></td>
            <td>${model.worked}</td>
            <td colspan=2></td>
        </tr>
        <tr>
            <td colspan=2>budget</td>
            <#list model.months as m>
                <td>${m.budget}</td>
            </#list>
            <td>${model.budget}</td>
            <td colspan=3></td>
        </tr>
        <tr>
            <td colspan=2>left:</td>
            <#list model.months as m>
                <td class='${m.budgetLeftClass}'>${m.budgetLeft}</td>
            </#list>
            <td colspan=2></td>
            <td class='budgetLeft negative'>${model.budgetLeft}</td>
            <td></td>
        </tr>
        <tr>
            <td colspan=2>left NOW:</td>
            <#list model.months as m>
                <td class='${m.budgetLeftNowClass}'>${m.budgetLeftNow}</td>
            </#list>
            <td colspan=2></td>
            <td class='${model.budgetLeftClass}'>${model.budgetLeft}</td>
            <td></td>
        </tr>
    </table>
</#macro>
