var MY_COOKIE = "pw"

function setCookie(value) {
    var d = new Date();
    d.setTime(d.getTime() + (365 * 24 * 60 * 60 * 1000));
    document.cookie = MY_COOKIE + "=" + CryptoJS.enc.Hex.stringify(CryptoJS.enc.Utf8.parse(value)) + ";" + "expires=" + d.toUTCString();
}

function getCookie() {
    var nameIs = MY_COOKIE + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(nameIs) === 0) {
            let hex = c.substring(nameIs.length, c.length);
            return CryptoJS.enc.Hex.parse(hex).toString(CryptoJS.enc.Utf8);
        }
    }
    return null;
}

function deleteCookie() {
    if (getCookie()) {
        document.cookie = MY_COOKIE + "=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    }
}

function decryptToText(key, data, iv) {
    return CryptoJS.enc.Utf8.stringify(CryptoJS.AES.decrypt(
        {
            ciphertext: CryptoJS.enc.Base64.parse(data)
        },
        CryptoJS.enc.Base64.parse(CryptoJS.MD5(key).toString(CryptoJS.enc.Base64)), {
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7,
            iv: CryptoJS.enc.Base64.parse(iv),
        }));
}

function decryptClicked() {
    let passwordText = document.getElementById("password");
    setCookie(passwordText.value)
    try {
        tryDecryptAndReplace(passwordText.value)
    } catch (e) {
    }
}

function tryDecryptAndReplace(pw) {
    let innerHTML = decryptToText(pw, MY_DATA, MY_IV);
    if (!innerHTML.includes("<html>")) {
        throw null;
    }
    //document.write(innerHTML);
    var doc = document.open("text/html");
    doc.write(innerHTML);
    doc.close();
    dispatchEvent(new Event('load'));
}

function tryCookie() {
    if (new URLSearchParams(window.location.search).get('r') != null) {
        setCookie("");
    } else {
        let cookie = getCookie()
        if (cookie != null && cookie !== "") {
            try {
                tryDecryptAndReplace(cookie)
            } catch (e) {
                setCookie("");
            }
        }
    }
}