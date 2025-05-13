//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Get the current user logged in
const user = $('#_principal').data("principal");

//Get the user id for the profile viewed
const userProfile = $("#userProfile").data("principal");

//Get charts
const taskChartEl = $("#activityOnTasks");
const assignmentTaskChartEl = $("#assignmentTasksActivity");

/**
 * Gets task activity over 7 days
 * @returns {*[]}
 */
function getTaskChartData(){
    let data = [];
    $.ajax({
        type:"GET",
        url:"/api/task/user/"+userProfile+"/num-completed",
        async: false,
        success: function(res){
            data = res;
        },
        error: function(){
            console.log("Error!");
        }
    });
    return data;
}

let taskChart = new Chart(taskChartEl, {
    type: 'line',
    data: {
        labels: getPreviousDaysFromNow(),
        datasets: [{
            label: '# Tasks Completed',
            data: getTaskChartData(),
            borderWidth: 1,
            backgroundColor: ['rgba(0, 123, 255, 1)'],
            borderColor: 'rgba(75, 192, 192, 1)',
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true,
            }
        },
        maintainAspectRatio: false,
    }
});

/**
 * Gets assignment task activity over 7 days
 * @returns {*[]}
 */
function getAssignmentTaskChartData(){
    let data = [];
    $.ajax({
        type:"GET",
        url:"/api/assignments/tasks/user/"+userProfile+"/num-completed",
        async: false,
        success: function(res){
            data = res;
        },
        error: function(){
            console.log("Error!");
        }
    });
    return data;
}

let assignmentTaskChart = new Chart(assignmentTaskChartEl, {
    type: 'line',
    data: {
        labels: getPreviousDaysFromNow(),
        datasets: [{
            label: '# Assignment Tasks Completed',
            data: getAssignmentTaskChartData(),
            borderWidth: 1,
            backgroundColor: ['rgba(0, 123, 255, 1)'],
            borderColor: 'rgba(75, 192, 192, 1)',
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true,
            }
        },
        maintainAspectRatio: false,
    }
});

/**
 * If a user denys/cancels a request then this will make necessary api call
 * @param id
 */
function deleteFriendRequestById(id){
    $.ajax({
        type:"DELETE",
        url:"/api/user/friend-request/"+id,
        success: function() {
            showToastSuccess("Friend Request Cancelled!");
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}

/**
 * If a user is viewing anothers page they can send a friend request if they are not friends
 */
$(document).on("click","#friendRequest",function (){
    $.ajax({
        type:"POST",
        url:"/api/user/send/friend-request/"+userProfile,
        success: function (res){
            showToastSuccess("Friend Request Sent!");
            $("#friendRequest").attr("id","cancelRequest");
            $("#cancelRequest").attr("data-request",res);
            $("#cancelRequest").removeClass().addClass("btn btn-danger mx-2");
            $("#cancelRequest").text("Cancel Friend Request ");
            $("#cancelRequest").append(`<i class="fa-solid bi-person-fill-dash"></i>`);
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
});

/**
 * If a user is viewing another users profile whilst having sent a friend request this will handle logic for cancelling
 */
$(document).on("click","#cancelRequest",function (){
    const id = $(this).attr("data-request");
    deleteFriendRequestById(id);
    $(this).attr("id", "friendRequest")
        .removeAttr("data-request")
        .removeClass().addClass("btn btn-success mx-2")
        .text("Send Friend Request ")
        .append(`<i class="fa-solid bi-person-fill-add"></i>`);
});

function acceptRequest(id){
    $.ajax({
        type:"DELETE",
        url:"/api/user/friend-request/accept/"+id,
        success: function(){
            showToastSuccess("Added User to friend list!");
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}

$(document).on("click","#addUser" ,function (){
    const id = $(this).data("request");
    acceptRequest(id);
    $("#addUser").attr("id","removeFriend");
    $("#removeFriend").attr("data-user",userProfile);
    $("#removeFriend").removeClass().addClass("btn btn-danger mx-2");
    $("#removeFriend").text("Remove Friend ");
    $("#removeFriend").append(`<i class="fa-solid bi-person-fill-x"></i>`);
    $("#denyRequest").remove();
});

$(document).on("click", "#denyRequest", function (){
    const id = $(this).attr("data-request");
    deleteFriendRequestById(id);
    $("#addUser").remove();
    $(this).attr("id", "friendRequest")
        .removeAttr("data-request")
        .removeClass().addClass("btn btn-success mx-2")
        .text("Send Friend Request ")
        .append(`<i class="fa-solid bi-person-fill-add"></i>`);
});

$(document).on("click","#removeFriend", function (){
    const friend = $(this).attr("data-user");
    $.ajax({
        type:"DELETE",
        url:"/api/user/friends/remove/"+friend,
        success: function (){
            showToastSuccess("Removed user as friend!");
            $("#removeFriend").attr("id","friendRequest");
            $("#friendRequest").removeAttr("data-user");
            $("#friendRequest").removeClass().addClass("btn btn-success mx-2");
            $("#friendRequest").text("Send Friend Request ");
            $("#friendRequest").append(`<i class="fa-solid bi-person-fill-add"></i>`);
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
});

$(document).on("click", ".accept-request" ,function (){
    const id = $(this).data("request");
    acceptRequest(id);
    $('[data-request="'+id+'"]').remove();
});

$(document).on("click", ".deny-request", function (){
    const id = $(this).data("request");
    deleteFriendRequestById(id);
    $('[data-request="'+id+'"]').remove();
});

$(document).on("click", ".cancel-request", function (){
    const id = $(this).data("request");
    deleteFriendRequestById(id);
    $('[data-request="'+id+'"]').remove();
});

$("#pfpFileInput").on("change", function (){
   const file = this.files[0];
   if(file){
       let fileReader = new FileReader();
       fileReader.onload = function (event){
           $("#previewPfp").attr("src", event.target.result);
       };
       fileReader.readAsDataURL(file);
   }
});

$("#cancelPfpBtn").on("click", function (){
    $('#pfpFileInput').val(null);
    $('#previewPfp').attr("src", "/images/defaultPfp.jpg");
});

$("#saveProfileChanges").on("submit", function (event){
    event.preventDefault();
    const userModel = {};
    userModel["id"] = user;
    const data = $("#saveProfileChanges").serializeArray();
    $(data).each(function(i,field){
        const key = field.name;
        const val = field.value;
        userModel[key] = val;
    });
    let formData = new FormData();
    formData.append("userDTO", new Blob([JSON.stringify(userModel)], { type: "application/json" }));
    const file = $("#pfpFileInput").prop("files")[0];
    if(file){
        formData.append("pfp", file);
    }
    $.ajax({
        type:"PATCH",
        url:"/api/user/edit/save-changes",
        contentType: false,
        processData: false,
        data: formData,
        success:function (){
            showToastSuccess("Profile successfully updated!");
            $("#profileUsername").text("@"+userModel["username"]);
            $("#profileBio").text(userModel["bio"]);
            $("#profileLocation").text(userModel["location"]);
            if(file){
                let fileReader = new FileReader();
                fileReader.onload = function (event){
                    $("#profilePic").attr("src", event.target.result);
                };
                fileReader.readAsDataURL(file);
            }
            $("#editProfile").modal('toggle');
        },
        error: function (){
            showToastError("Username already in use!");
        }
    });
});