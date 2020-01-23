
function isAndroidBrowser() {
    return /Android/i.test(navigator.userAgent);
}

function postButtonHandler(loginType) {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let companyname = document.getElementById("companyname").value;

    console.log(loginType, username, password, companyname);

    var data = { userName: username, password: password };
    if(loginType === "login")
        data.companyName = companyname;
    else if(loginType === "enterpriseAdminLogin") {
        data.companyName = companyname;
        data.admin = true;
    }
    else if(loginType === "globalAdminLogin") 
        data.admin = true;

    var dataStr = JSON.stringify(data);
    invokePostAPI("/api/v1/login/", dataStr, true, function() {
        if(this.readyState == 4 && this.status == 200) {
            document.getElementById("updateStatus").innerHTML = this.responseText;
        }
        else if(this.readyState == 4){
            document.getElementById("updateStatus").innerHTML = this.responseText;
            //alert("Error in invoking API. please retry.");
        }
    });
    return false;
}

function login() {
    return postButtonHandler("login");
}

function enterpriseAdminLogin() {
    return postButtonHandler("enterpriseAdminLogin");
}

function globalAdminLogin() {
    return postButtonHandler("globalAdminLogin");
}
