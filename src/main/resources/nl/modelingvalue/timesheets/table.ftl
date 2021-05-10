<#--noinspection CssUnusedSymbol-->

<#macro table model>
    <table cellspacing="0">
        <tr>
            <td colspan=16 class="center header">${model.name} - ${model.year}</td>
            <#if model.hasBudget()>
                <td class="budget" colspan=2></td>
            </#if>
        </tr>
        <tr>
            <td colspan=2 class="center">
                <a class="tooltipped" target="_blank" href="${model.writeTimeUrl}">
                    write time
                    <span class="tooltiptext">${model.nbsp("Click here to register additional hours for ${model.name}.")}</span>
                </a>
            </td>
            <#list model.months as m>
                <td class="center wide">${m.name}</td>
            </#list>
            <td colspan=1 class="center">worked</td>
            <td colspan=1 class="center">name</td>
            <#if model.hasBudget()>
                <td colspan=1 class="budget center">budget</td>
                <td colspan=1 class="budget center">left</td>
            </#if>
        </tr>
        <#list model.users as u>
            <tr>
                <td></td>
                <td class="light personName">${u.name}</td>
                <#list u.months as m>
                    <td class="white">
                        <span class="spend">
                        <#if m.url!="">
                            <a class="tooltipped" target="_blank" href="${m.url}">
                        </#if>
                                ${m.worked}
                                <#if m.hasBudget()>
                                    <span class="tooltiptext">${model.nbsp("budget: ${m.budget}")}</span>
                                </#if>
                                <#if m.url!="">
                            </a>
                        </#if>
                    </span>
                        <span class="budget">${m.budget}</span>
                    </td>
                </#list>
                <td>${u.worked}</td>
                <td class="left light personName">${u.name}</td>
                <#if model.hasBudget()>
                    <td class="budget">${u.budget}</td>
                    <td class="${u.budgetLeftClass}">${u.budgetLeft}</td>
                </#if>
            </tr>
        </#list>
        <tr>
            <td colspan=2>worked</td>
            <#list model.months as m>
                <td class="monTotal">${m.worked}</td>
            </#list>
            <td>${model.worked}</td>
            <td colspan=1></td>
            <#if model.hasBudget()>
                <td class="budget" colspan=2></td>
            </#if>
        </tr>
        <#if model.hasBudget()>
            <tr>
                <td class="budget" colspan=2>budget</td>
                <#list model.months as m>
                    <td class="budget">${m.budget}</td>
                </#list>
                <td class="budget" colspan=2></td>
                <td class="budget">${model.budget}</td>
                <td class="budget" colspan=1></td>
            </tr>
            <tr>
                <td class="budget" colspan=2>budget left</td>
                <#list model.months as m>
                    <td class="budget" class="${m.budgetLeftClass}">${m.budgetLeft}</td>
                </#list>
                <td class="budget" colspan=3></td>
                <td class="budget" class="${model.budgetLeftClass}">${model.budgetLeft}</td>
            </tr>
            <tr>
                <td class="budget" colspan=2>cummulative budget left</td>
                <#list model.months as m>
                    <td class="${m.budgetLeftCumulatedClass}">${m.budgetLeftCumulated}</td>
                </#list>
                <td class="budget" colspan=4></td>
            </tr>
        </#if>
    </table>
</#macro>
