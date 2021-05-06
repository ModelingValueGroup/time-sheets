<#macro css>
    <style type="text/css">
		body {
			text-align       : left;
			font-family      : Verdana, serif;
			font-weight      : bold;
			font-size        : 12px;
			color            : #444;
			background-color : #fff;
		}
		table {
			text-align       : center;
			font-family      : Verdana, serif;
			font-weight      : bold;
			font-size        : 12px;
			color            : #444;
			background-color : #fff;
			border           : 1px solid #69C;
			border-collapse  : collapse;
			border-spacing   : 0;
		}
		td {
			padding-left     : 5px;
			padding-right    : 5px;
			white-space      : nowrap;
			text-align       : right;
			border           : 1px solid #69C;
			background-color : #BCD;
			width            : 5em;
		}
		.center {
			text-align : center;
		}
		.white {
			background-color : #fff;
			border-top       : 1px solid #eee;
			border-left      : 1px solid #eee;
		}
		.light {
			background-color : #def;
		}
		.negative {
			color : #ff4040;
		}
		.wide {
			width : 7em;
		}
		.notInTotals {
			background-color : #aaa;
		}
		.tooltipped {
			position : relative;
			z-index  : 24;
		}
		.tooltipped:hover {
			z-index          : 25;
			background-color : #cfc;
		}
		.tooltipped span {
			display : none;
		}
		.tooltipped:hover span {
			display          : block;
			position         : absolute;
			top              : 1.2em;
			left             : 1.2em;
			border           : 1px solid #ba2;
			background-color : #ff6;
			color            : #000;
			text-align       : left;
			text-decoration  : none;
			padding          : 3px;
		}
    </style>
</#macro>

<#macro js>
    <script type="text/javascript" charset="utf-8">
        (function () {
                var radios = document.getElementsByTagName('input');
                for (var r = 0; r < radios.length; r++) {
                    radios[r].onclick = function () {
                        var spans = document.getElementsByTagName('span');
                        for (var s = 0; s <= spans.length; s++) {
                            if (spans[s] && (spans[s].classList.contains('spend') || spans[s].classList.contains('budget'))) {
                                spans[s].style.display = spans[s].classList.contains(this.id) ? 'block' : 'none';
                            }
                        }
                        for (var i = 0; i < radios.length; i++) {
                            radios[i].checked = radios[i] === this;
                        }
                    }
                    if (radios[r].id === 'spend') {
                        radios[r].onclick();
                    }
                }
            }
        )();
    </script>
</#macro>

<#macro table model>
    <table cellspacing='0'>
        <tr>
            <td colspan=18 class=center>${model.name} - ${model.year}</td>
        </tr>
        <tr>
            <td colspan=2 class=center>
                <a class='tooltipped' target='_blank' href='${model.writeTimeUrl}'>
                    write time
                    <span>${model.nbsp("Click here to register additional hours for ${model.name}.")}</span>
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
                <td class=light></td>
                <td class=light>${u.name}</td>
                <#list u.months as m>
                    <td class=white>
                    <span class=spend>
                        <a class='tooltipped' target='_blank'
                           href='${m.url}'>
                            ${m.worked}
                            <span>${model.nbsp("budget: ${m.budget}")}</span>
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
            <#list model.months as m>
                <td>${m.budget}</td>
            </#list>
            <td>${model.budget}</td>
            <td colspan=3></td>
        </tr>
        <tr>
            <td colspan=2>spend:</td>
            <#list model.months as m>
                <td class='monTotal'>${m.total}</td>
            </#list>
            <td></td>
            <td>${model.total}</td>
            <td colspan=2></td>
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
