//  Copyright 1999-2004 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
// $Id$
var showing = new Array();
var x = new Array(); // scrollX
var y = new Array(); // scrollY

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
        if (window.scrollX != undefined) {
            x[id] = window.scrollX;
            y[id] = window.scrollY;
        }
        var parent = document.getElementById(id);
        var span = document.createElement('SPAN');
        parent.appendChild(span);
        var br = document.createElement('BR');
        var iframe = document.createElement('IFRAME');
        iframe.setAttribute('src', src);
        iframe.setAttribute('height', height);
        iframe.setAttribute('width', width);
        span.appendChild(br);
        span.appendChild(iframe);
        //parent.replaceChild(iframe, parent.lastChild);
        // window.scrollTo(x[id],y[id]);
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
        if (x[id] != undefined) {
            window.scrollTo(x[id],y[id]);
        }
        showing[id] = false;
    } else {
        alert(
                "Requires Navigator >= 7, Mozilla >= 1.2.1 or IE >= 6");
        return;
    }
}

