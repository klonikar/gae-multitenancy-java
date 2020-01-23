// This file is to be hosted on the API server. This has a function to invoke POST APIs which can not invoked using jsonp pattern
function createCORSRequest(method, url, isAsync) {
    var xmlhttp = null;
    if(/MSIE/i.test(navigator.userAgent)) {
        xmlhttp = new XDomainRequest();
        xmlhttp.open(method, url, isAsync);
        return xmlhttp;
    }
    if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
        try {
        xmlhttp = new XMLHttpRequest();
        }
        catch(ex) { xmlhttp = new ActiveXObject("Microsoft.XMLHTTP"); }
    }
    else {// code for IE6, IE5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if("withCredentials" in xmlhttp) {
        console.log("withCredentials: " + xmlhttp.withCredentials);
        //xmlhttp.withCredentials = true;
        xmlhttp.open(method, url, isAsync);
    }
    else if(typeof XDomainRequest != "undefined") {
        xmlhttp = new XDomainRequest();
        xmlhttp.open(method, url);
    }
    else {
        xmlhttp = null;
    }
    //xmlhttp.setRequestHeader("Access-Control-Request-Method", method);
    //xmlhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    if(window.document.location.hostname === "localhost")
        xmlhttp.setRequestHeader("ServerName", "payroll1.appspot.com");
    //xmlhttp.setRequestHeader("X-Custom-Header", "value");

    return xmlhttp;
}

function invokePostAPI(url, postData, isAsync, asyncHandler) {
    var xmlhttp = createCORSRequest("POST", url, isAsync);
    if(isAsync) {
        xmlhttp.onreadystatechange = asyncHandler;
    }
    xmlhttp.send(postData);
    return xmlhttp;
}

function invokeGetAPI(url, isAsync, asyncHandler, noWaitMessage) {
    var xmlhttp = createCORSRequest("GET", url, isAsync);
    if(isAsync && /MSIE/i.test(navigator.userAgent)) {
        xmlhttp.onload = asyncHandler;
    }
    else if(isAsync) {
        xmlhttp.onreadystatechange = asyncHandler;
    }
    xmlhttp.send();
    return xmlhttp;
}

function invokeDeleteAPI(url, isAsync, asyncHandler) {
    var xmlhttp = createCORSRequest("DELETE", url, isAsync);
    if(isAsync) {
        xmlhttp.onreadystatechange = asyncHandler;
    }
    xmlhttp.send();
    return xmlhttp;
}

function invokePutAPI(url, putData, isAsync, asyncHandler) {
    var xmlhttp = createCORSRequest("PUT", url, isAsync);
    if(isAsync) {
        xmlhttp.onreadystatechange = asyncHandler;
    }
    xmlhttp.send(postData);
    return xmlhttp;
}

