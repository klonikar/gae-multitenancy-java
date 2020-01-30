
function isAndroidBrowser() {
    return /Android/i.test(navigator.userAgent);
}

function postLoginButtonHandler(loginType) {
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
    return postLoginButtonHandler("login");
}

function enterpriseAdminLogin() {
    return postLoginButtonHandler("enterpriseAdminLogin");
}

function globalAdminLogin() {
    return postLoginButtonHandler("globalAdminLogin");
}

function logout() {
    invokeGetAPI("/api/v1/logout/", true, function() {
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

function createEnterprise() { 
    var dataStr = document.getElementById("enterpriseSpec").value;
    invokePostAPI("/api/v1/enterprise/", dataStr, true, function() {
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

function getEnterprise() {
    invokeGetAPI("/api/v1/enterprise/" + document.getElementById("enterpriseId").value, true, function() {
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

function createEmployee() { 
    var dataStr = document.getElementById("employeeSpec").value;
    invokePostAPI("/api/v1/employee/", dataStr, true, function() {
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

function getEmployee() {
    invokeGetAPI("/api/v1/employee/" + document.getElementById("employeeId").value, true, function() {
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