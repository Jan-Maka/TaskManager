const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

const error = $("#error");
const success = $("#success");
const email = $("#email");
const submit = $("#submit");

/**
 * Handles event of checking if an email inputted exists
 */
email.change(function (){
   $.ajax({
        type:"GET",
        url:"api/auth/check-email/" + email.val(),
        dataType: 'json',
       success: function (){
            error.hide();
            submit.prop('disabled', false);
           },
       error: function () {
            success.hide();
            error.show();
            success.hide();
        }
   });
});

/**
 * Handles the event of sending a reset-password email
 */
submit.click(function (){
    $.ajax({
        type: "POST",
        url: "api/auth/reset/password/" + email.val(),
        success: function () {
            success.show();
            submit.prop('disabled',true);
            email.prop('disabled',true);
        },
        error:function (){
            console.log("ERROR!");
        }
    });
});




