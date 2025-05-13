//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

const rePassword = $("#re-password");
const newPassword = $("#new-password");
const password = $("#password");
const submit = $("#save");
const passError = $("#passError");
let isNewPassValid = true;
let passValid = false;

$("#re-password").on("change", function() {
    let match = false;
    if(rePassword.val()!= newPassword.val()){
        passError.text("Passwords don't match!");
        passError.show();
    }else{
        passError.hide();
        match = true;
    }
    if(match){
        $.ajax({
            type:"GET",
            url:"/api/auth/password-valid/"+rePassword.val(),
            contentType: "application/json; charset=utf-8",
            success:function (){
                isNewPassValid = true;
                passError.hide();
            },
            error:function (){
                passError.text("Password must contain one number, uppercase letter, special character and must be at least 8 characters long!");
                passError.show()
                submit.prop('disabled', true);
            }
        });
    }
});

function checkPassword(password){
    let result = false;
    $.ajax({
        type:"GET",
        url:"/api/auth/is-user-password/"+password,
        async: false,
        success:function (res){
            result = res;
        }
    });
    return result;
}

$("#password").on("change", function (){
    let res = checkPassword(password.val());
    passValid = res;

    if(passValid && isNewPassValid){
        submit.prop('disabled', false);
    }else{
        submit.prop('disabled', true);
    }
});

const privacyPassword = $("#passwordPrivacy");
const submitPrivacy = $("#savePrivacy");

$("#passwordPrivacy").on("change", function (){
    let res = checkPassword(privacyPassword.val());
    passValid = res;
    if(passValid){
        submitPrivacy.prop('disabled', false);
    }else{
        submitPrivacy.prop('disabled', true);
    }
});

const notificationsPassword = $("#passwordNotifications");
const submitNotification = $("#saveNotifications");

$("#passwordNotifications").on("change", function (){
   let res = checkPassword(notificationsPassword.val());
   passValid = res;
    if(passValid){
        submitNotification.prop('disabled', false);
    }else{
        submitNotification.prop('disabled', true);
    }
});

$(document).on("click",".emoticon", function (){
   $("#mood").val($(this).data("rating"));
});
