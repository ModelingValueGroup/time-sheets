function setupBudgets() {
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

