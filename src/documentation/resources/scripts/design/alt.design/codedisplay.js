// $Id$
var showing = new Array();
var x = -1; // scrollX
var y = -1; // scrollY

function toggleCode(id, src, height, width) {
    //alert('In toggleCode');
    if (showing[id]) {
        hideCode(id);
    } else {
        showCode(id, src, height, width);
    }
}

function showCode(id, src, height, width) {
    //alert('In showCode');
    if (showing[id]) { return; }
    if (document.getElementById && document.createElement) {
        if (window.scrollX) {
            x = window.scrollX;
            y = window.scrollY;
        }
        var parent = document.getElementById(id);
        var span = document.createElement('SPAN');
        parent.appendChild(span);
        var iframe = document.createElement('IFRAME');
        iframe.setAttribute('src', src);
        iframe.setAttribute('height', height);
        iframe.setAttribute('width', width);
        parent.replaceChild(iframe, parent.lastChild);
        // window.scrollTo(x,y);
        showing[id] = true;
    } else {
        alert(
                "Requires Navigator >= 7, Mozilla >= 1.2.1 or IE >= 6");
        return;
    }
}

function hideCode(id) {
    //alert('In hideCode');
    if ( ! showing[id]) { return; }
    if (document.getElementById && document.createElement) {
        var parent = document.getElementById(id);
        parent.removeChild(parent.lastChild);
        if (x >= 0) {
            window.scrollTo(x,y);
        }
        showing[id] = false;
    } else {
        alert(
                "Requires Navigator >= 7, Mozilla >= 1.2.1 or IE >= 6");
        return;
    }
}

