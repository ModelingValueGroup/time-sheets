function setupBudgets() {
    console.log("setupBudgets...")
    var radios = document.getElementsByTagName('input');
    console.log("radios", radios)
    for (var r = 0; r < radios.length; r++) {
        radios[r].onclick = function () {
            console.log("onclick radio")
            var spans = document.getElementsByTagName('span');
            console.log("onclick radio - spans",spans)
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
            console.log("spend CLICK ", radios[r])
            radios[r].onclick();
        }
    }
}

