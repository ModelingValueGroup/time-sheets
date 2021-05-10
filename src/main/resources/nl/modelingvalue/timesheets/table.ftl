<#--noinspection CssUnusedSymbol-->

<#macro table model>
    <table cellspacing="0">
        <tr>
            <td colspan=16 class="center header">${model.name} - ${model.year}</td>
            <#if model.hasBudget()>
                <td colspan=2></td>
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
                <td colspan=1 class="center">budget</td>
                <td colspan=1 class="center">left</td>
            </#if>
        </tr>
        <#list model.users as u>
            <tr>
                <td></td>
                <td class="light personName">${u.name}</td>
                <#list u.months as m>
                    <td class="white">
                    <span class="spend">
                        <a class="tooltipped" target="_blank"
                           href="${m.url}">
                            ${m.worked}
                            <#if m.hasBudget()>
                                <span class="tooltiptext">${model.nbsp("budget: ${m.budget}")}</span>
                            </#if>
                        </a>
                    </span>
                        <span class="budget">${m.budget}</span>
                    </td>
                </#list>
                <td>${u.worked}</td>
                <td class="left light personName">${u.name}</td>
                <#if model.hasBudget()>
                    <td>${u.budget}</td>
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
                <td colspan=2></td>
            </#if>
        </tr>
        <#if model.hasBudget()>
            <tr>
                <td colspan=2>budget</td>
                <#list model.months as m>
                    <td>${m.budget}</td>
                </#list>
                <td colspan=2></td>
                <td>${model.budget}</td>
                <td colspan=1></td>
            </tr>
            <tr>
                <td colspan=2>budget left</td>
                <#list model.months as m>
                    <td class="${m.budgetLeftClass}">${m.budgetLeft}</td>
                </#list>
                <td colspan=2></td>
                <td colspan=1></td>
                <td class="${model.budgetLeftClass}">${model.budgetLeft}</td>
            </tr>
            <tr>
                <td colspan=2>cummulative budget left</td>
                <#list model.months as m>
                    <td class="${m.budgetLeftCumulatedClass}">${m.budgetLeftCumulated}</td>
                </#list>
                <td colspan=2></td>
                <td colspan=1></td>
                <td class="${model.budgetLeftCumulatedClass}">${model.budgetLeftCumulated}</td>
            </tr>
        </#if>
    </table>
</#macro>
