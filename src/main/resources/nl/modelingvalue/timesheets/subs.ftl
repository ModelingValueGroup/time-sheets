<#--noinspection CssUnusedSymbol-->
<#macro css>
    <style type="text/css">
		body {
			text-align       : left;
			font-family      : Verdana, serif;
			font-weight      : bold;
			font-size        : 12px;
			color            : #444444;
			background-color : lightyellow;
		}
		table {
			text-align       : center;
			font-family      : Verdana, serif;
			font-weight      : bold;
			font-size        : 12px;
			color            : #444444;
			background-color : #ffffff;
			border           : 1px solid #6699cc;
			border-collapse  : collapse;
			border-spacing   : 0;
		}
		td {
			padding-left     : 5px;
			padding-right    : 5px;
			white-space      : nowrap;
			text-align       : right;
			border           : 1px solid #6699cc;
			background-color : #bbccdd;
			width            : 5em;
		}
		.center {
			text-align : center;
		}
		.left {
			text-align : left;
		}
		.header {
			font-weight : bold;
			font-size   : 24px;
			padding     : 10px;
		}
		.white {
			background-color : #ffffff;
			border-top       : 1px solid #eeeeee;
			border-left      : 1px solid #eeeeee;
		}
		.light {
			background-color : #ddeeff;
		}
		.personName {
			width : 10em;
		}
		.negative {
			color : #ff4040;
		}
		.wide {
			width : 7em;
		}
		.notInTotals {
			background-color : #aaaaaa;
		}
		.tooltipped {
			position : relative;
			z-index  : 24;
		}
		.tooltipped:hover {
			z-index          : 25;
			background-color : #ccffcc;
		}
		.tooltipped span {
			display : none;
		}
		.tooltipped:hover span {
			display          : block;
			position         : absolute;
			top              : 1.2em;
			left             : 1.2em;
			border           : 1px solid #bbaa22;
			background-color : #ffff66;
			color            : #000000;
			text-align       : left;
			text-decoration  : none;
			padding          : 3px;
		}
		.terminal {
			background-color : black;
			background-image : radial-gradient(rgba(0, 150, 0, 0.75), black 120%);
			display          : block;
			overflow         : auto;
			color            : white;
			padding          : 20px;
			margin           : 0 0 20px 0;
			font             : 1.1rem Inconsolata, monospace;
		}
		.terminal-title {
			background-color : #4caf50;
			display          : block;
			overflow         : auto;
			color            : white;
			padding          : 14px 16px;
			margin           : 20px 0 0 0;
			font             : 1.1rem Verdana, serif;
			font-weight      : bold;
			width            : 6em;
			text-align       : center;
		}
		.error {
			color : orangered;
		}
		.info {
			color : greenyellow;
		}
		.trace {
			color : powderblue;
		}
		.debug {
			color : deepskyblue;
		}
		ul {
			font             : 1.1rem Inconsolata, monospace;
			list-style-type  : none;
			margin           : 0;
			padding          : 0;
			overflow         : hidden;
			background-color : #333333;
			margin-bottom    : 2px;
		}
		li {
			float : left;
		}
		li a {
			display         : block;
			color           : white;
			text-align      : center;
			padding         : 14px 16px;
			text-decoration : none;
		}
		li a:hover:not(.active) {
			background-color : darkgreen;
		}
		.active {
			background-color : #4caf50;
			width            : 6em;
			font-family      : Verdana, serif;
			font-weight      : bold;
		}
		.separator {
			height : 100px;
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
            <td colspan=18 class="center header">${model.name} - ${model.year}</td>
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
                <td></td>
                <td class="light personName">${u.name}</td>
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
