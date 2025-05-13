//Get token so post request will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

const submit = $("#submit");
const password = $("#password");
const repassword = $("#re-password");
const error = $("#error");
const success = $("#success");

//Need to get rid of path names as ajax call would include them
const currentUrl = window.location.href;
const baseUrlWithoutPath = currentUrl.split(window.location.pathname)[0];

/**
 * Handles event of checking if passwords inputted match
 */
repassword.change(function(){
    let match = false;
    if(repassword.val()!= password.val()){
        error.text("Passwords do not match!");
        error.show();
    }else{
        match = true;
        error.hide();
    }
    if(match){
        $.ajax({
            type:"GET",
            url:baseUrlWithoutPath+"/api/auth/password-valid/"+repassword.val(),
            contentType: "application/json; charset=utf-8",
            success:function (){
                error.hide();
                submit.prop('disabled', false);
            },
            error:function (res){
                error.text("Password must contain one number, uppercase letter, special character and must be at least 8 characters long!");
                error.show()
                submit.prop('disabled', true);
            }
        });
    }
});

/**
 * Handles the changing of a users password
 */
submit.click(function (){
    $.ajax({
        type:"PATCH",
        url:baseUrlWithoutPath+"/api/auth/reset/password",
        data:{
            "token": $("#token").val(),
            "password":repassword.val()
        },
        success:function (){
            success.show();
            submit.prop('disabled',true);
            password.prop('disabled',true);
            repassword.prop('disabled',true);
        },
        error:function (){
            console.log("ERROR!");
        }
    });
});

