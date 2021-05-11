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

var crc = "";

function refreshCheck() {
    var noCacheHdr = new Headers();
    noCacheHdr.append('pragma', 'no-cache');
    noCacheHdr.append('cache-control', 'no-cache');

    let crcFile = window.location.pathname.split("/").pop() + ".crc.json";
    fetch(new Request(crcFile), {method: 'GET', headers: noCacheHdr,})
        .then(response => response.json())
        .then(data => {
            var newCrc = data.crc
            if (crc === "") {
                crc = newCrc
                console.log("page crc = " + crc)
                setTimeout(refreshCheck, 30000);
            } else if (newCrc === crc) {
                setTimeout(refreshCheck, 30000);
            } else {
                window.location.reload(true);
            }
        });
}

setTimeout(refreshCheck, 1000);
