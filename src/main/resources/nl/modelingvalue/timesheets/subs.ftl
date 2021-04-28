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