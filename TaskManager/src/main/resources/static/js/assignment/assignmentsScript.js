//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Need to get rid of path names as ajax call would include them
const currentUrl = window.location.href;
const baseUrlWithoutPath = currentUrl.split(window.location.pathname)[0];

//Get the current user logged in
const user = $('#_principal').data("principal");

let files = [];
let filters = [];
let sortOption;

/**
 * When a file dropzone is needed they will all use the same config
 * @type {{init: dropzoneConfig.init, addRemoveLinks: boolean, autoProcessQueue: boolean, maxFilesize: number, paramName: string, url: string, dictDefaultMessage: string}}
 */
const dropzoneConfig = {
    paramName: "file",
    maxFilesize: 5,
    dictDefaultMessage: "Drop file attachments here!",
    addRemoveLinks: true,
    autoProcessQueue: false,
    url: "/dummy",
    init: function () {
        let totalUploadedSize = 0;

        this.on("addedfile", function (file) {
            if (totalUploadedSize + file.size > maxFileSize) {
                this.removeFile(file);
                showToastError("Total file size exceeds the limit!");
            } else {
                files.push(file);
                totalUploadedSize += file.size;
            }
        });

        this.on("removedfile", function (file) {
            files = files.filter((f) => {
                return f.name !== file.name;
            });
            totalUploadedSize -= file.size;
        });
    },
};

let createAssignmentDropzone = new Dropzone("#assignment-dropzone", dropzoneConfig);
let createTaskDropzone = new Dropzone("#task-dropzone", dropzoneConfig);

/**
 * Helper function to create the start button for assignments
 * @param id
 * @param inProgress
 * @param complete
 * @returns {string}
 */
function buttonStartAssignment(id,inProgress,complete){
    if(inProgress){
        return `<button type="button" class="btn btn-outline-danger" id="startAssignment" data-assignment="${id}" data-inprogress="${false}">Un-Start</button>`;
    }else if(!inProgress && !complete){
        return `<button type="button" class="btn btn-outline-success" id="startAssignment" data-assignment="${id}" data-inprogress="${true}">Start</button>`;
    }
    return `<button type="button" class="btn btn-outline-danger disabled" id="startAssignment" data-assignment="${id}" data-inprogress="${false}">Un-Start</button>`;
}

/**
 * Helper function to create the complete button for assignments
 * @param id
 * @param complete
 * @returns {string}
 */
function buttonCompleteAssignment(id,complete){
    if(!complete){
        return `<button type="button" class="btn btn-outline-success mx-2" id="completeAssignment" data-assignment="${id}" data-complete="${true}">Set as Complete</button>`;
    }
    return `<button type="button" class="btn btn-outline-danger mx-2" id="completeAssignment" data-assignment="${id}" data-complete="${false}">Not Complete</button>`;
}

/**
 * Helper function to create the start button for tasks
 * @param id
 * @param inProgress
 * @param complete
 * @returns {string}
 */
function buttonStartTask(id,inProgress,complete){
    if(inProgress){
        return `<button type="button" class="btn btn-outline-danger" id="startTask" data-task="${id}" data-inprogress="${false}">Un-Start</button>`;
    }else if(!inProgress && !complete){
        return `<button type="button" class="btn btn-outline-success" id="startTask" data-task="${id}" data-inprogress="${true}">Start</button>`;
    }
    return `<button type="button" class="btn btn-outline-danger disabled" id="startTask" data-task="${id}" data-inprogress="${false}">Un-Start</button>`;
}

/**
 * Helper function to create the complete button for tasks
 * @param id
 * @param complete
 * @returns {string}
 */
function buttonCompleteTask(id,complete){
    if(!complete){
        return `<button type="button" class="btn btn-outline-success mx-2" id="completeTask" data-task="${id}" data-complete="${true}">Set as Complete</button>`;
    }
    return `<button type="button" class="btn btn-outline-danger mx-2" id="completeTask" data-task="${id}" data-complete="${false}">Not Complete</button>`;
}

function assignmentDateDetails(assignment){
    return `Released: ${new Date(assignment.startDate).toDateString()} ${getFormattedTime(new Date(assignment.startDate))} | Due Date: ${new Date(assignment.endDate).toDateString()} ${getFormattedTime(new Date(assignment.endDate))}`;
}

/**
 * Helper function to get task date in a particular way
 * @param model
 * @returns {string}
 */
function taskDateDetails(model){
    let endDateTime = getFormattedTime(new Date(model.endDate));
    if(new Date(model.endDate).getDate() === new Date(model.startDate).getDate() &&
        new Date(model.endDate).getMonth() === new Date(model.startDate).getMonth() &&
        new Date(model.startDate).getFullYear() ===  new Date(model.endDate).getFullYear()){
        return `${new Date(model.startDate).toDateString()}, ${getFormattedTime(new Date(model.startDate))} to ${endDateTime}`;
    }else{
        endDateTime = new Date(model.endDate).toDateString() + " "+ getFormattedTime(new Date(model.endDate));
    }
    return `${new Date(model.startDate).toDateString()}, ${getFormattedTime(new Date(model.startDate))} to ${endDateTime}`;
}

/**
 * Helper function to create HTML for search bar
 * @returns {string}
 */
function getFriendSearchContent(){
    return `<div id="addUsers" class="border border-3 rounded overflow-y-auto p-0" style="height: 150px;">
              <div class="d-flex">
                <input id="searchFriend" class="form-control" type="search" placeholder="Search" aria-label="Search Friends">
                <a class="btn btn-success" id="searchFriends">
                  <i class="bi bi-search"></i>
                </a>
              </div>
              <div id="friendResults"></div>
           </div>`;
}

/**
 * Helper function for creating list of users added to an assignment in the creation form
 * @returns {string}
 */
function getUsersAddedContent(){
    return `<label for="usersAdded" class="form-label fw-semibold">Users added to assignment</label>
            <div id="usersAdded" class="border border-3 rounded overflow-y-auto p-0" style="height: 150px;"></div>`;
}

/**
 * Helper function to create HTML for list of users a user can add to task depending
 * on the people on the assignment
 * @returns {string}
 */
function getUserAddContentForTask(id,username){
    return `<div class="border-bottom d-flex align-items-center rounded-3" id="addUser${id}ToTask">
                <div class="d-flex align-items-center highlight-card w-100">
                        ${getUserPfpBody(getUserPfpString(id))}
                        <h4 class="m-0 fw-bold">@${username}</h4>
                </div>
                <a class="btn btn-primary ms-auto add-user-task" data-user="${id}" data-username="${username}">
                    <i class="bi bi-person-add"></i>
                </a>
            </div>`;
}

/**
 * Helper function to display users who can be added to a task
 * @param users
 * @returns {string}
 */
function getUsersToAddForTask(users){
    let addContent = "";
    users = users.filter((u) => {
       return u.id !== user;
    });
    if(users.length !== 0){
        users.forEach((user) => {
            addContent += getUserAddContentForTask(user.id,user.username);
        });
    }
    return `<div id="userToAdd" class="border border-3 rounded overflow-y-auto" style="height: 150px;">
                ${addContent}
            </div>`;
}

/**
 * Helps with adding current user to a task and for displaying whos on a task
 * @param currentUser
 * @returns {string}
 */
function getUsersAddedToTask(currentUser){
    return `<label id="usersOnTask"  class="form-label fw-semibold">Users added to task</label>
            <div id="usersAddedToTask" class="border border-3 rounded overflow-y-auto" style="height: 150px;">
                <div id="addedUser${currentUser.id}ToTask" data-user="${currentUser.id}" class="d-flex border-bottom  align-items-center rounded-3">
                    <div class="d-flex align-items-center highlight-card w-100">
                        ${getUserPfpBody(currentUser.profilePic)}
                        <h4 class="m-0 fw-bold">@${currentUser.username}</h4>
                    </div>
                </div>
            </div>`;
}

/**
 * Displays users on an assignment
 * @param model
 * @returns {string}
 */
function usersOnAssignment(assignment){
    let usersBody = "";
    assignment.users.forEach((u) => {
        usersBody += `<div id="userAdded${u.id}" data-user="${u.id}" class="border-bottom d-flex align-items-center rounded-3">
                        <a class="d-flex align-items-center highlight-card w-100" href="/social/profile/${u.username}" style="text-decoration: none;">
                            ${getUserPfpBody(u.profilePic)}
                            <h4 class="m-0 fw-bold">@${u.username}</h4>
                        </a>
                        ${(u.id !== user && u.id !== assignment.user) ? `<a type="button" class="btn btn-danger ms-auto remove-user" aria-label="Remove" data-user="${u.id}" style="display: none"><i class="bi bi-person-dash"></i></a>`: ""}
                     </div>`;
    });

    return `<div class="col" id="edit-assignment-users">
                <h5>Add users to assignment</h5>
            </div>
            <div class="col">
               <h5>Users on assignment 🕴️:</h5>
               <div id="usersAdded" class="col border border-3 rounded p-0 overflow-y-auto" style="height: 150px;">
                  ${usersBody}
               </div>
            </div>`;
}

/**
 * Displays users on a task
 * @param model
 * @returns {string}
 */
function usersOnTask(task){
    let usersToAdd = getAssignmentUsers(task.assignment);

    usersToAdd = usersToAdd.filter((user) =>{
        return !task.users.some((u) => {
            return user.id === u.id;
        });
    });
    let usersBody = "";
    task.users.forEach((u) => {
        usersBody += `<div id="addedUser${u.id}ToTask" data-user="${u.id}" class="d-flex border-bottom  align-items-center rounded-3">
                        <a class="d-flex align-items-center highlight-card w-100" href="/social/profile/${u.username}" style="text-decoration: none;">
                            ${getUserPfpBody(u.profilePic)}
                            <h4 class="m-0 fw-bold">@${u.username}</h4>
                        </a>
                        ${u.id !== user  ? `<a type="button" class="btn btn-danger ms-auto remove-user-task" aria-label="Remove" data-user="${u.id}" data-username="${u.username}" style="display: none"><i class="bi bi-person-dash"></i></a>`: ""}
                     </div>`;
    });

    return `<div class="col" id="edit-task-users" style="display: none">
                <h5>Add Users to Task</h5>
                ${getUsersToAddForTask(usersToAdd)}
            </div>
            <div class="col">
                <h5>Users on task 🕴️:</h5>
                <div id="usersAddedToTask" class="border border-3 rounded overflow-y-auto" style="height: 150px;">
                    ${usersBody}
                </div>
            </div>`;
}

/**
 * Helper function for displaying a task on an assignment
 * @param tasks
 * @returns {string}
 */
function getAssignmentTaskBody(tasks){
    let body = "";
    if(tasks.length !== 0){
        tasks.forEach((task) =>{
            const t = `<div class="card highlight-card assignment-task" data-task="${task.id}">
                        <div class="m-2">
                            <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                            ${isOverdue(task) ? `<span class="badge text-bg-danger mb-1">Overdue</span>`:""}
                            <p class="card-text">📆: ${dateDetails(task)}</p>  
                        </div>
                     </div>`;
            body += t;
        });
        return body;
    }
    return `<h5 class="m-3">There are no tasks for this assignment!</h5>`;
}

/**
 * Gets all study sessions that are part of the assignment
 * @param sessions
 * @returns {string}
 */
function getAssignmentStudySessions(sessions){
    let body = "";
    if(sessions.length !== 0){
        sessions.forEach((session) => {
            const s = `<a class="card highlight-card study-session" href="/academic/study-sessions/${session.id}" style="text-decoration: none;">
                        <div class="m-2">
                            <h5 class="card-title">${session.title} 🎓</h5>
                            <p class="card-text">📆: ${dateDetails(session)}</p>  
                        </div>
                     </a>`;
            body += s;
        });
        return body;
    }
    return `<h5 class="m-3">There are no study sessions for this assignment!</h5>`;
}

/**
 * Displays a list of file attachments for a task or assignment
 * @param model
 * @returns {string}
 */
function getFileAttachmentBody(model){
    let body = "";
    const isOwner = model.user === user || model.owner === user;
    if(model.fileAttachments.length !== 0){
        model.fileAttachments.forEach((file) => {
            const attachment = `<div class="card text-dark bg-light d-flex flex-row">
                                     <a href="/api/files/${file.id}/download" class="card-body highlight-card">
                                         <div class="d-flex align-items-center">
                                            <i class="fs-3 bi bi-file-earmark-break-fill me-3"></i>
                                            <span class="fs-5 text-primary">${file.title.slice(0,10)}..</span>
                                         </div>
                                     </a>
                                    ${isOwner ? `<button type="button" class="btn btn-danger me-auto del-file" aria-label="Delete" data-file="${file.id}"><i class="bi bi-file-earmark-x"></i></button>` : ""}
                                </div>`;
            body += attachment;
        });
        return body;
    }
    return `<h5 class="m-3">There are no files attached!</h5>`;
}

/**
 * Needed to help render when a user is filtering assignments
 * @param assignment
 * @returns {string}
 */
function assignmentCardBody(assignment){
    const titleElString = `<h4 class="card-title"></h4>`;
    const titleEl = $(titleElString);
    titleEl.text(`${assignment.title}:${getStatus(assignment.inProgress,assignment.complete)}`);

    const courseElString =  `<h5></h5>`;
    const courseEl = $(courseElString);
    courseEl.text(`For Course 📚: ${assignment.course}`);


    return `<div class="card highlight-card m-3 fade-left assignment" data-assignment="${assignment.id}">
                        <div class="card-body">
                            ${titleEl.prop("outerHTML")}
                            ${isOverdue(assignment) ? `<span class="badge text-bg-danger mb-1" >Overdue</span>`:""}
                            ${courseEl.prop("outerHTML")}
                            <h6 >Type 🚨: ${getType(assignment)}</h6>          
                            <hr>                  
                            <h6 class="card-text">Due Date 📅: ${new Date(assignment.endDate).toDateString()}</h6>
                        </div>
                   </div>`;
}

/**
 * Helper function used to render assignments and also used for check cases as well;
 * It checks for whether there are any filters active
 * @param assignments
 * @returns {*[]}
 */
function getAndRenderAssignmentsList(assignments){
    if(!assignments){
        assignments = getAllUserAssignment();
    }

    let filteredAssignments = [];
    if(filters.length !== 0){
        filters.forEach((filter) => {
            switch (filter){
                case "Not Started":
                    assignments.forEach((assignment) => {
                        if(!assignment.complete && !assignment.inProgress)filteredAssignments.push(assignment);
                    });
                    break;
                case "In Progress":
                    assignments.forEach((assignment) => {
                        if(!assignment.complete && assignment.inProgress)filteredAssignments.push(assignment);
                    });
                    break;
                case "Completed":
                    assignments.forEach((assignment) => {
                        if(assignment.complete)filteredAssignments.push(assignment);
                    });
                    break;
            }
        });
    }else{
        filteredAssignments = assignments;
    }

    //If sorting option chosen then it will
    if(sortOption != "None"){
        switch (sortOption){
            case "Importance":
                filteredAssignments.sort((a,b) => {
                    const importanceA = getAssignmentImportance(a);
                    const importanceB = getAssignmentImportance(b);
                    return importanceB - importanceA;
                });
                break;
            case "Alphabetically":
                filteredAssignments.sort((a,b) => {
                    const titleA = a.title.toLowerCase();
                    const titleB = b.title.toLowerCase();
                    return titleA.localeCompare(titleB);
                });
                break;
            case "Due Date":
                filteredAssignments.sort((a,b) => {
                    const dateA = new Date(a.endDate);
                    const dateB = new Date(b.endDate);
                    return dateA - dateB;
                });
                break;
        }
    }
    $("#assignmentTab").html("");
    filteredAssignments.forEach((assignment) => {
        $("#assignmentTab").append(assignmentCardBody(assignment));
    });
    return filteredAssignments;
}

/**
 * Helper function to update the card that is clickable
 * @param assignment
 */
function updateAssignmentCard(id){
    const assignment = getAssignmentById(id);
    $(".assignment[data-assignment="+id+"]").replaceWith(assignmentCardBody(assignment));
    if(isOverdue(assignment) && $("#assignmentOverdue").length === 0){
        $("#assignBody").prepend(`<span class="badge text-bg-danger mb-1" id="assignmentOverdue">Overdue</span>`);
    }else if($("#assignmentOverdue").length !== 0 && !isOverdue(assignment)){
        $("#assignmentOverdue").remove();
    }
}

/**
 * Whenever a user wants to add a user to a task or assignment
 * they will search for them via username which makes an api call.
 * Async false due to needing the content to display HTML
 * @param search
 * @returns {*[]} Which is a list of userDTOs that match with the search input.
 */
function getSearchFriendsResults(search){
    let results = [];
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/user/search/friends",
        async: false,
        data:{
            "search":search,
        },
        success: function (res){
            results = res;
        },
        error: function(){
            console.log("Error");
        }
    });
    return results;
}

/**
 * Finds assignment via ID
 * Async false due to needing the content to display HTML
 * @param id
 * @returns {{}}
 */
function getAssignmentById(id){
    let assignment = {};
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/assignments/"+id,
        async: false,
        dataType: 'json',
        success: function (res){
            assignment = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return assignment;
}

function getAllUserAssignment(){
    let assignments = [];
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/assignments",
        async: false,
        dataType: "json",
        success: function (res){
            assignments = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return assignments;
}

/**
 * Gets a list of users on the assignment
 * Async false due to needing the content to display HTML
 * @param id
 * @returns {{}}
 */
function getAssignmentUsers(id){
    let users = {};
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/assignments/"+id+"/users",
        async: false,
        dataType: "json",
        success: function (res){
            users = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return users;
}

/**
 * Gets the task by id
 * Async false due to needing the content to display HTML
 * @param id
 * @returns {{}}
 */
function getAssignmentTaskById(id){
    let task = {};
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/assignments/tasks/"+id,
        async:false,
        dataType:"json",
        success: function (res){
            task = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return task;
}

/**
 * Whenever a user search's for friends they want to add
 * this event will generate the results of their search.
 */
$(document).on("click","#searchFriends", function(){
    $("#friendResults").html("");
    const search = $("#searchFriend").val();
    let results = getSearchFriendsResults(search);
    results = results.filter((user) => {
        const element = $("#userAdded"+user.id);
        return element.length === 0;
    });
    if(results.length === 0){
        $("#friendResults").html("No Results");
        showToastError("No Friends with that username found!");
        return;
    }
    let options = "";
    results.forEach((u) => {
        options += `<div id="addUser${u.id}" class="user d-flex border-bottom align-items-center rounded-3">
                        <div class="d-flex align-items-center highlight-card w-100">
                            ${getUserPfpBody(u.profilePic)}
                            <h4 class="m-0 fw-bold">@${u.username}</h4>
                        </div>
                        <a type="button" class="btn btn-primary ms-auto add-user" aria-label="Close" data-user="${u.id}" data-username="${u.username}"><i class="bi bi-person-add"></i></a>
                   </div>`;
    });
    $("#friendResults").html(options);
});

/**
 * If the search bar is cleared then also clear results box
 */
$(document).on("input","#searchFriend" ,function (){
    let inputVal = $(this).val();
    if(inputVal.trim() === ""){
        $("#friendResults").html("");
    }
});

/**
 * If user decides to add user to task or assignment then it will
 * add to the users added div
 */
$(document).on("click",".add-user", function (){
    const id = $(this).data("user");
    const username = $(this).data("username");
    $("#addUser"+id).remove();
    const addUser = `<div id="userAdded${id}" data-user="${id}" class="user d-flex border-bottom align-items-center rounded-3">
                        <a class="d-flex align-items-center highlight-card w-100" style="text-decoration: none">
                            ${getUserPfpBody(getUserPfpString(id))}
                            <h4 class="m-0 fw-bold">@${username}</h4>
                        </a>
                        <a type="button" class="btn btn-danger ms-auto remove-user" aria-label="Remove" data-user="${id}"><i class="bi bi-person-dash"></i></a>'
                     </div>`;
    $("#usersAdded").append(addUser);
});

/**
 *If user has added a user to a task then they can click to remove the user
 */
$(document).on("click", ".remove-user", function (){
    const id = $(this).data("user");
    $("#userAdded"+id).remove();
});

/**
 * If user decides to add a user to a task then it will be added to the list and displayed
 */
$(document).on("click", ".add-user-task", function (){
    const id = $(this).data("user");
    const username = $(this).data("username");
    $("#addUser"+id+"ToTask").remove();
    const addUser = `<div id="addedUser${id}ToTask" data-user="${id}" class="user d-flex border-bottom  align-items-center rounded-3">
                        <a class="d-flex align-items-center highlight-card w-100" style="text-decoration: none">
                            ${getUserPfpBody(getUserPfpString(id))}
                            <h4 class="m-0 fw-bold">@${username}</h4>
                        </a>
                        <a type="button" class="btn btn-danger ms-auto remove-user-task" aria-label="Remove" data-user="${id}" data-username="${username}"><i class="bi bi-person-dash"></i></a>'
                     </div>`;
    $("#usersAddedToTask").append(addUser);
});

/**
 * Removes the users from task list
 */
$(document).on("click", ".remove-user-task", function (){
    const id = $(this).data("user");
    const username = $(this).data("username");
    $("#addedUser"+id+"ToTask").remove();
    $("#userToAdd").append(getUserAddContentForTask(id,username));
});

//If an assignment is view then this will make sure to make the search bar be on the add assignment modal
let usersAdded = [];
$("#addingAssignment").on("show.bs.modal", function (){
    //Check if a users is in edit mode so that we can cancel it
    if($("#addUsers").length !== 0){
        toggleEditAssignment();
    }
    //Check if an assignment is viewed and remove elements
    if($("#usersAdded").length !== 0){
        $("#usersAdded").find("div").each(function(){
            usersAdded.push($(this).attr("id"));
            $(this).removeAttr("id");
        });
        $("#usersAdded").addClass("usersAdded");
        $("#usersAdded").removeAttr("id");
    }
    $("#addingAssignmentSearch").append(getFriendSearchContent());
    $("#addedUsersToAssignment").html(getUsersAddedContent());
});

//Used to clear the search bar and if an assignment is viewed give back necessary id
$("#addingAssignment").on("hidden.bs.modal",function(){
    $("#addingAssignmentSearch #addUsers").remove();
    $("#addedUsersToAssignment").html("");
    //Check if assignment had view
    if($(".usersAdded").length !== 0){
        $(".usersAdded").attr("id","usersAdded");
        $("#usersAdded").removeClass("usersAdded");
        let i = 0;
        $("#usersAdded").find("div").each(function(){
           $(this).attr("id",usersAdded[i]);
           i++;
        });
        usersAdded = [];
    }
});

/**
 * Creates an assignment by getting all field inputs and then making an object model
 * that's similar to the assignmentDTO and then the api call makes the assignment on the
 * database.
 */
$("#createAssignment").on("submit", function (event){
    event.preventDefault();
    const data = $("#createAssignment").serializeArray();
    let assignmentModel = {};
    $(data).each(function(i,field){
        const key = field.name;
        const val = field.value;
        if(key === "type"){
            if(val === "Urgent/Important")assignmentModel["important"] = true, assignmentModel["urgent"] = true;
            else if(val === "Urgent") assignmentModel["important"] = false, assignmentModel["urgent"] = true;
            else if(val === "Important") assignmentModel["important"] = true, assignmentModel["urgent"] = false;
            else assignmentModel["important"] = false, assignmentModel["urgent"] = false;
        }else {
            assignmentModel[key] = val;
        }
    });
    assignmentModel["user"] = user;
    assignmentModel["progress"] = 0;
    assignmentModel["created"] = new Date();
    assignmentModel["finished"] = null;
    assignmentModel["modified"] = new Date();
    assignmentModel["complete"] = false;
    assignmentModel["inProgress"] = false;

    if(hasDateErrors(assignmentModel)){
        showToastError("Start date has to be before the end date!");
        return;
    }

    if($("#usersAdded div.user").length !== 0){
        let users = [];
        $("#usersAdded div.user").each(function(){
            let dataUserValue = $(this).data("user");
            let userModel = {};
            userModel["id"] = dataUserValue;
            users.push(userModel);
        });
        assignmentModel["users"] = users;
        console.log(users);
    }

    let formData = new FormData();
    formData.append("assignmentDTO",new Blob([JSON.stringify(assignmentModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    $.ajax({
        type:"POST",
        url:baseUrlWithoutPath+"/api/assignments",
        contentType: false,
        processData: false,
        data: formData,
        success:function(){
            getAndRenderAssignmentsList();
            if(!userRoles.includes("MEMBER") && getAllUserAssignment().length >= 4){
                $("#addAssignmentBtn").prop("disabled", true);
            }
            files = [];
            $("#createAssignment")[0].reset();
            createAssignmentDropzone.removeAllFiles(true);
            $("#usersAdded").html("");
            showToastSuccess("Assignment successfully added!");
        }
    });
    $("#addingAssignmentSearch").html("");
    $("#addedUsersToAssignment").html("");
    $("#addingAssignment").modal("toggle");
});

/**
 * Creates assignment title (it's a function for the animation)
 * @returns {string}
 */
function getAssignmentTitle(assignment){
    return `<div class="d-flex flex-row fade-left">
                <h1 id="titleAssignment" class="navbar-brand mb-0 text-white" contenteditable="false"></h1>
                <h1 class="navbar-brand mb-0 text-white">${getStatus(assignment.inProgress,assignment.complete)}</h1>
            </div>`;
}

/**
 * Gets all study sessions linked to assignment but first checks if the user has
 * premium MEMBER role as only they can have study sessions
 * @param assignment
 * @returns {string}
 */
function getAssignmentStudySessionsBody(assignment){
    if(userRoles.includes("MEMBER")){
        return `<div class="col">
                <div class="d-flex align-items-center">
                    <h5>Study Sessions 🏫</h5>
                </div>
                <div class="border border-3 rounded overflow-y-auto" style="height: 150px;">
                    ${getAssignmentStudySessions(assignment.sessions)}
                </div>
            </div>`
    }
    return "";
}

/**
 * Gets users on assignment body only if the user is a premium user with role MEMBER
 * @param assignment
 * @returns {string}
 */
function getUsersOnAssignmentBody(assignment){
    if(userRoles.includes("MEMBER")){
        return `<div class="row mb-3" id="assignmentUsers">
                    ${usersOnAssignment(assignment)}
                </div>`;
    }
    return"";
}


/**
 * Generates the assignment details body
 * @param assignment
 * @returns {string}
 */
function getAssignmentBody(assignment){
    return      `<div id="assignBody" class="fade-left m-3">
                    ${isOverdue(assignment) ? `<span class="badge text-bg-danger mb-1" id="assignmentOverdue" >Overdue</span>`:""}
                    <div id="courseDetails" class="mb-2">
                        <h5>Course 📚</h5>
                        <p class="card-text" id="assignmentCourse" contenteditable="false"></p>
                    </div>
                    <div id="dateDetails" class="mb-2">
                        <h5>Dates 📅</h5>
                        <p class="card-text" id="assignmentDates">${assignmentDateDetails(assignment)}</p>
                    </div>
                    <div id="edit-assignment-date" class="mb-3">
                        <h5>Start</h5>
                        <input id="assignment-edit-startdate" class="form-select" name="startDate" type="datetime-local" placeholder="start date" required>
                        <h5>Date Due</h5>
                        <input id="assignment-edit-duedate" class="form-select" name="dueDate" type="datetime-local" placeholder="due date" required> 
                    </div>
                    <div id="typeDetails">
                        <h5>Type 🚨:</h5>
                        <p id="assignmentType">${getType(assignment)}</p>
                    </div>
                    <div id="edit-assignment-type" class="mb-3">
                        <select id="edit-assign-type" class="form-select">
                            <option>Urgent/Important</option>
                            <option>Urgent</option>
                            <option>Important</option>
                            <option>Not important or Urgent</option>
                        </select>
                    </div>  
                    <h5>Assignment Details 📝</h5>
                    <p id="assignmentDescription" class="card-text" contenteditable="false"></p>
                    <h5 id="assingmentProgressHeader">Progress:</h5>
                    <div id="assignmentProgress" contenteditable="false">
                        ${getProgressBar(assignment.progress)}
                    </div>
                    <div id="edit-assignment-progress" class="mb-3">
                       <input id="bar" type="range" class="form-range" min="0" max="100" value="${assignment.progress}"/>
                    </div>
                    <div id="assignmentBtns" class="mb-3">
                        ${buttonStartAssignment(assignment.id,assignment.inProgress,assignment.complete)}
                        ${buttonCompleteAssignment(assignment.id,assignment.complete)}
                    </div>    
                    <div id="assignmentTasks" class="mb-3 row">
                        <div class="col">
                            <div class="d-flex align-items-center">
                                <h5>Assignment Tasks 📃</h5>
                                <a id="addTaskBtn" class="px-3 p-0 btn btn-primary" data-bs-toggle="modal" data-bs-target="#addingTask" aria-label="Add Assignment" data-assignment="${assignment.id}">
                                    <i class="bi bi-clipboard-plus"></i>
                                </a>
                            </div>
                            <div id="assignmentTaskDetail" class="border border-3 rounded overflow-y-auto" style="height: 150px;">
                                ${getAssignmentTaskBody(assignment.tasks)}
                            </div>
                        </div>
                        ${getAssignmentStudySessionsBody(assignment)}
                    </div>
                    ${getUsersOnAssignmentBody(assignment)}
                    <h5>File Attachments 🗃️</h5>
                    <div id="assignmentAttachments" class="border border-3 rounded overflow-y-auto" class="mb-3" style="max-height: 150px;">
                        ${getFileAttachmentBody(assignment)}
                    </div>
                    <div id="edit-assignment-attachments">
                        <div id="edit-assignment-dropzone" class="dropzone"></div>
                    </div>
                  </div>`;
}

/**
 * Used to make the edit dropzone go away
 */
let editAssignmentDropzone;
function initAssignmentEditDropzone(){
    // Destroy existing dropzone
    if (editAssignmentDropzone) {
        editAssignmentDropzone.destroy();
    }
    editAssignmentDropzone = new Dropzone("#edit-assignment-dropzone", dropzoneConfig);
}

/**
 * Displays content of an assignment
 */
$(document).on("click", ".assignment", function () {
    $(".assignment").removeClass("bg-lightgray");
    $(this).addClass("bg-lightgray");
    const id = $(this).data("assignment");
    const assignment = getAssignmentById(id);
    const title = getAssignmentTitle(assignment);
    const editBtn = `<span class="fade-left edit-assignment-btn" data-assignment="${assignment.id}">
                        <i class="bi bi-pen-fill"></i>
                     </span>
                     ${assignment.user === user ?  
                     `<button class="btn btn-danger fade-left del-assignment-btn mx-1" style="display: none;" data-assignment="${assignment.id}">
                        <i class="bi bi-trash"></i>
                     </button>`:
                     `<button class="btn btn-danger fade-left leave-assignment-btn mx-1" style="display: none;" data-assignment="${assignment.id}">
                        <i class="bi bi-arrow-left-circle"></i>
                     </button>`}
                     <button class="btn btn-success fade-left save-assignment-btn mx-1" style="display: none;" data-assignment="${assignment.id}">
                        <i class="bi bi-save"></i>
                     </button>
                     <button class="btn btn-secondary fade-left cancel-assignment-edit mx-1" style="display: none;" data-assignment="${assignment.id}">
                        <i class="bi bi-x-square"></i>
                     </button>`;
    const body = getAssignmentBody(assignment);
    $("#assignmentTitle").html(title);
    $("#assignmentDetail").html(body);
    //Have to do this since a user can input HTML tags meaning they can inject javaScript this is done to prevent it.
    $("#titleAssignment").text(assignment.title);
    $("#assignmentDescription").text(assignment.description);
    $("#assignmentCourse").text(assignment.course);
    $("#editAssignmentBtn").html(editBtn);
});

/**
 * Used to change the task progress
 * @param id
 * @param inProgress
 * @param complete
 * @param progress
 */
function changeAssignmentStatus(id,inProgress,complete,progress){
    $.ajax({
        type:"PATCH",
        url:baseUrlWithoutPath+"/api/assignments/set-status/"+id,
        data:{
            "inProgress": inProgress,
            "complete":complete
        },
        success: function (){
            $("#assignmentBtns").html("");
            $("#assignmentBtns").append(buttonStartAssignment(id,inProgress,complete));
            $("#assignmentBtns").append(buttonCompleteAssignment(id,complete));
            $("#assingmentProgressHeader").text(`Progress | Status: ${getStatus(inProgress,complete)}`);
            if(progress != null){
                $("#assignmentProgress").html(getProgressBar(progress));
            }
            updateAssignmentCard(id);
            showToastSuccess("Assignment status changed!");
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}

/**
 * Handles logic for starting assignment
 */
$(document).on("click", "#startAssignment",function(){
    const assignmentId = $(this).data("assignment");
    const isInProgress = $(this).data("inprogress");
    const complete = false;
    let progress = null;
    if(!isInProgress && !complete){
        progress = 0;
    }
    changeAssignmentStatus(assignmentId,isInProgress,complete,progress);
});

/**
 * Handles logic for completing assignment
 */
$(document).on("click","#completeAssignment", function (){
    const assignmentId = $(this).data("assignment");
    const complete = $(this).data("complete");
    const isInProgress = (complete ? false: true);
    let progress;
    if(!isInProgress && complete){
        progress = 100;
    }
    changeAssignmentStatus(assignmentId,isInProgress,complete,progress);
});

/**
 * Universal for deleting a file attachment
 */
$(document).on("click", ".del-file", function(){
    let confirmed = confirm("Are you sure you want to delete this file attachment!");
    if (confirmed){
        let file = $(this).data("file");
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/files/"+file,
            success: function(){
                const assignmentId = $(".edit-assignment-btn").data("assignment");
                const assignment = getAssignmentById(assignmentId);
                $("#assignmentAttachments").html(getFileAttachmentBody(assignment));
                showToastSuccess("Deleted File Attachment!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * Used to generate the add users when the modal is shown
 */
$("#addingTask").on("show.bs.modal", function(){
    const assignment = $("#addTaskBtn").data("assignment");
    $(this).data("assignment",assignment);
    const users = getAssignmentUsers(assignment);
    const current = users.find((u) =>{return u.id === user});
    $("#addingTaskSearch").append(getUsersToAddForTask(users));
    $("#addedUsersToTask").html(getUsersAddedToTask(current));
});

/**
 * Get rid of the search elements as it might be used in the
 * editing of a task
 */
$("#addingTask").on("hidden.bs.modal", function(){
   $("#addingTaskSearch #userToAdd").remove();
    $("#addedUsersToTask").html("");
});

/**
 * Creates a task inside an assignment
 */
$("#createTask").on("submit", function(event){
   event.preventDefault();
   const assignmentId = $("#addingTask").data("assignment");
   const assignment = getAssignmentById(assignmentId);
   const data = $("#createTask").serializeArray();
   let taskModel = {};
    $(data).each(function(i,field){
        const key = field.name;
        const val = field.value;
        if(key === "type"){
            if(val === "Urgent/Important")taskModel["important"] = true, taskModel["urgent"] = true;
            else if(val === "Urgent") taskModel["important"] = false, taskModel["urgent"] = true;
            else if(val === "Important") taskModel["important"] = true, taskModel["urgent"] = false;
            else taskModel["important"] = false, taskModel["urgent"] = false;
        }else {
            taskModel[key] = val;
        }
    });

    if(hasDateErrors(taskModel)){
        showToastError("Start date has to be before the end date!");
        return;
    }

    if(taskDateHasErrorsWithAssignmentDates(taskModel,assignment)){
        showToastError("Task start and end date must be between assignment start and due dates!");
        return;
    }

    let users = [];
    $("#usersAddedToTask div.user").each(function() {
        const userId = $(this).data("user");
        let userModel = {};
        userModel["id"] = userId;
        users.push(userModel);
    });

    $("#workload span.workload-emoticon").each(function () {
        if ($(this).hasClass("enlarged")) {
            taskModel["workload"] = $(this).data("rating");
        }
    });

    $("#mood span.emoticon").each(function () {
        if ($(this).hasClass("enlarged")) {
            taskModel["mood"] = $(this).data("rating");
        }
    });
    taskModel["owner"] = user;
    taskModel["users"] = users;
    taskModel["assignment"] = assignmentId;

    let formData = new FormData();
    formData.append("assignmentTaskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if(files.length > 0){
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    $.ajax({
        type: "POST",
        url: "/api/assignments/"+assignmentId+"/create-task",
        contentType: false,
        processData: false,
        data: formData,
        success:function(res){
            files = [];
            $("#createTask")[0].reset();
            createTaskDropzone.removeAllFiles(true);
            showToastSuccess("Task for assignment successfully created!");
            $("#assignmentTaskDetail").html(getAssignmentTaskBody(res));
        }
    });
    $("#addingTask").modal("toggle");
});

let assignmentOwner;

/**
 * Toggles everything that's needs to be in edit mode or cancel out edit mode things
 * @param event
 */
function toggleEditAssignment(event){
    let value = $("#assignmentDescription").attr("contenteditable");
    if (value === "false" || value === null){
        value = "true";
    }else{
        value = "false";
    }

    if(event != null){
        const dateTimeString = $("#assignmentDates").text().replace("Released:", "").replace("Due Date:", "").split("|");
        const releaseTime = dateTimeString[0].trim().split(" ")[4];
        const dueTime = dateTimeString[1].trim().split(" ")[4];

        const assignmentId = event.data("assignment");
        const assignment = getAssignmentById(assignmentId);
        assignmentOwner = assignment.user;

        const startYear = new Date(assignment.startDate).getFullYear();
        let startMonth =  new Date(assignment.startDate).getMonth() +1;
        let startDate = new Date(assignment.startDate).getDate();

        const endYear = new Date(assignment.endDate).getFullYear();
        let endMonth = new Date(assignment.endDate).getMonth() +1;
        let endDate = new Date(assignment.endDate).getDate();

        if (startMonth < 10) {
            startMonth = "0" + startMonth;
        }
        if (startDate < 10) {
            startDate = "0" + startDate;
        }
        if (endMonth < 10) {
            endMonth = "0" + endMonth;
        }
        if (endDate < 10) {
            endDate = "0" + endDate;
        }

        const startDateVal = startYear + "-" + startMonth + "-" + startDate + `T${releaseTime}`;
        const endDateVal = endYear + "-" + endMonth + "-" + endDate + `T${dueTime}`;
        $("#assignment-edit-startdate").val(startDateVal);
        $("#assignment-edit-duedate").val(endDateVal);
    }

    $("#assignmentDates").toggle();
    $("#assignmentType").toggle();
    $("#assignmentProgress").toggle();
    $("#assignmentBtns").toggle();
    $("#assignmentTasks").toggle();
    $("#assignmentAttachments").toggle();

    if(assignmentOwner === user){
        $("#titleAssignment").attr("contenteditable",value);
        $("#assignmentCourse").attr("contenteditable", value);
        $("#edit-assignment-date").toggle();
        $("#edit-assignment-type").toggle();
    }else{
        $("#courseDetails").toggle();
        $("#dateDetails").toggle();
        $("#typeDetails").toggle();
    }
    $("#assignmentDescription").attr("contenteditable",value);
    $("#edit-assignment-progress").toggle();
    $("#edit-assignment-users").toggle();
    $(".remove-user").toggle();
    if(value === "true"){
        $("#edit-assignment-users").append(getFriendSearchContent());
        $("#addUsers").removeClass("border border-3 rounded");
        $("#usersAdded").removeClass("border border-3 rounded");
    }else{
        $("#edit-assignment-users #addUsers").remove();
        $("#usersAdded").addClass("border border-3 rounded");
    }
    $("#edit-assignment-attachments").toggle();
    initAssignmentEditDropzone();

    $(".edit-assignment-btn").toggle();
    $(".del-assignment-btn").toggle();
    $(".leave-assignment-btn").toggle();
    $(".save-assignment-btn").toggle();
    $(".cancel-assignment-edit").toggle();
}

/**
 * Turns edit mode on
 */
$(document).on("click", ".edit-assignment-btn" , function(){
    toggleEditAssignment($(this));
});

/**
 * Cancels edit mode
 */
$(document).on("click", ".cancel-assignment-edit", function(){
    const assignmentId = $(this).data("assignment");
    const assignment = getAssignmentById(assignmentId);
    toggleEditAssignment();
    $("#assignmentTitle").html(getAssignmentTitle(assignment));
    $("#assignmentDetail").html(getAssignmentBody(assignment));
    //Have to do this since a user can input HTML tags meaning they can inject javaScript, this is done to prevent it.
    $("#titleAssignment").text(assignment.title);
    $("#assignmentDescription").text(assignment.description);
    $("#assignmentCourse").text(assignment.course);
    showToastInfo("No changes have been made!");
});

/**
 * Deletes the assignment and all DOM elements associated with it
 */
$(document).on("click", ".del-assignment-btn", function(){
    const assignmentId = $(this).data("assignment");
    const confirmed = confirm("Are you sure you want to delete this assignment (It will delete all task's associated to it)?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/assignments/"+assignmentId,
            success: function (){
                $("#assignmentTitle").html(`<h1 class="navbar-brand mb-0 text-white" contenteditable="false">Assignment</h1>`);
                $("#assignmentDetail").html("");
                $('[data-assignment="'+assignmentId+'"]').remove();
                if(!userRoles.includes("MEMBER") && getAllUserAssignment().length < 4){
                    $("#addAssignmentBtn").prop("disabled", false);
                }
                $("#editAssignmentBtn").html("");
                showToastSuccess("Successfully deleted assignment!");
            },
            error: function (){
                showToastError("Server Error Occurred");
            }
        });
    }
});

/**
 * Removes a user from a task and deletees all DOM associated with it
 */
$(document).on("click", ".leave-assignment-btn", function(){
    const assignmentId = $(this).data("assignment");
    const confirmed = confirm("Are you sure you want to leave this assingment?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/assignments/"+assignmentId+"/remove-user",
            success: function(){
                $("#assignmentTitle").html(`<h1 class="navbar-brand mb-0 text-white" contenteditable="false">Assignment</h1>`);
                $("#assignmentDetail").html("");
                $('[data-assignment="'+assignmentId+'"]').remove();
                $("#editAssignmentBtn").html("");
                showToastSuccess("Successfully left assignment!");
            }
        });
    }
})

/**
 * If a user clicks save the edits they have made will be saved or checked for errors
 */
$(document).on("click", ".save-assignment-btn", function (){
    let assignmentModel = {};
    const assignmentId = $(this).data("assignment");
    let assignment = getAssignmentById(assignmentId);
    assignmentModel["id"] = assignmentId;
    assignmentModel["title"] = $("#titleAssignment").text();
    if(assignment.user === user){
        console.log($("#assignment-edit-startdate").val());
        assignmentModel["startDate"] = new Date($("#assignment-edit-startdate").val());
        assignmentModel["endDate"] = new Date($("#assignment-edit-duedate").val());
        assignmentModel["important"] = $("#edit-assign-type").val() === "Important" || $("#edit-assign-type").val() === "Urgent/Important" ? true : false;
        assignmentModel["urgent"] = $("#edit-assign-type").val() === "Urgent" || $("#edit-assign-type").val() === "Urgent/Important" ? true : false;
    }else{
        assignmentModel["startDate"] = new Date(assignment.startDate);
        assignmentModel["endDate"] = new Date(assignment.endDate);
        assignmentModel["important"] = assignment.important;
        assignmentModel["urgent"] = assignment.urgent;
    }
    assignmentModel["course"] = $("#assignmentCourse").text();
    assignmentModel["description"] = $("#assignmentDescription").text();
    assignmentModel["progress"] = $("#bar").val();

    let users = [];
    $("#usersAdded div.d-flex").each(function(){
        let dataUserValue = $(this).data("user");
        let userModel = {};
        userModel["id"] = dataUserValue;
        users.push(userModel);
    });
    assignmentModel["users"] = users;

    let errors = false;
    if(hasDateErrors(assignmentModel)){
        showToastError("Start date as to be before the end date!");
        errors = true;
    }

    if(!isFileAttachmentSizeValid(assignment.fileAttachments,files)){
        errors = true;
        showToastError("Total file attachment size exceeded!");
    }

    for (const [k,v] of Object.entries(assignmentModel)){
        if(k === "title" && v.trim() === ""){
            showToastError("Title cannot be empty !");
            errors = true;
        }
        if(k === "course" && v.trim() === ""){
            showToastError("Course cannot be empty!");
            errors = true;
        }
        if(k === "description" && v.trim() === ""){
            showToastError("Description field cannot be empty!");
            errors = true;
        }
    }

    let formData = new FormData();
    formData.append("assignmentDTO", new Blob([JSON.stringify(assignmentModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    if(!errors){
        $.ajax({
            type:"PUT",
            url:baseUrlWithoutPath+"/api/assignments/"+assignmentId,
            contentType: false,
            processData: false,
            data:formData,
            success: function (){
                toggleEditAssignment();
                assignment = getAssignmentById(assignmentId);
                updateAssignmentCard(assignmentId);
                $("#assignmentTitle").html(getAssignmentTitle(assignment));
                $("#assignmentDetail").html(getAssignmentBody(assignment));
                $("#titleAssignment").text(assignmentModel.title);
                $("#assignmentDescription").text(assignmentModel.description);
                $("#assignmentCourse").text(assignmentModel.course);
                files = [];
                showToastSuccess("Assignment has successfully updated!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * DOM element to display the details of a task in a modal
 * @param task
 * @returns {string}
 */
function getTaskDetailBody(task){
    return `<div id="taskDetailBody">
                ${isOverdue(task) ? `<span class="badge text-bg-danger mb-1 task-overdue">Overdue</span>`:""}
                <h5 id="taskProgressHeader">Progress 📊| Status: ${getStatus(task.inProgress,task.complete)}</h5>
                <div id="taskProgress">
                    ${getProgressBar(task.progress)}
                </div>
                <div class="mb-3" id="edit-task-progress">
                    <input id="taskBar" type="range" class="form-range" min="0" max="100" value="${task.progress}"/>
                </div>
                <div id="taskBtns" class="mb-3">
                    ${buttonStartTask(task.id,task.inProgress,task.complete)}
                    ${buttonCompleteTask(task.id,task.complete)}
                </div>
                <h5 id="taskTypeHeader">Type 🚨: </h5>
                <p id="taskType">${getType(task)}</p>
                <div id="edit-task-type" class="mb-3">
                    <select id="edit-type-task" class="form-select">
                        <option>Urgent/Important</option>
                        <option>Urgent</option>
                        <option>Important</option>
                        <option>Not important or Urgent</option>
                    </select>
                </div>
                <h5>Task Details 📝</h5>
                <p id="taskDescription" class="card-text" contenteditable="false"></p> 
                <div id="taskDates" class="mb-3">
                    <h5>Dates For Task 📅</h5>
                    <p id="taskDetailDate" class="card-text"> ${taskDateDetails(task)}</p>
                </div>    
                <div id="edit-task-date" class="mb-3">
                    <h5>Start</h5>
                    <input id="task-edit-startdate" class="form-select" name="startDate" type="datetime-local" placeholder="start date" required>
                    <h5>Date For Completion</h5>
                    <input id="task-edit-enddate" class="form-select" name="endDate" type="datetime-local" placeholder="end date date" required> 
                </div>
                <div id="taskWorkload" class="mb-3" contenteditable="false">
                    <h5>Workload ⚖:</h5>
                    <div id="workloadRating">
                        ${getWorkloadRating(task.workload)}
                    </div>
                </div>
                <div class="mb-3" id="edit-workload">
                    <div id="task-edit-workload" class="d-flex flex-wrap justify-content-center">
                        ${getWorkloadEmoticons(task.workload)}
                    </div>
                </div>
                <div id="taskMood" class="mb-3">
                    <h5>Affect on Mood 🎭:</h5>
                    <div id="moodRating">
                        ${getMoodRating(task.mood)}
                    </div>    
                </div>
                <div class="mb-3" id="edit-mood">
                    <div id="task-edit-mood" class="d-flex flex-wrap justify-content-center">
                        ${getEmoticons(task.mood)}
                    </div>
                </div>
                <div class="row mb-3" id="taskUsers">
                    ${usersOnTask(task)}
                </div>
                <h5>File Attachments 🗃️:</h5>
                <div id="taskFiles" class="border  border-3 rounded" class="mb-3" style="height: 150px;">
                    ${getFileAttachmentBody(task)}
                </div>
                <div id="edit-task-attachments">
                    <div id="edit-task-dropzone" class="dropzone"></div>
                </div>
            </div>`;
}

/**
 * Helps with toggling when needed
 */
let editTaskDropzone;
function initTaskEditDropzone(){
    // Destroy existing dropzone
    if (editTaskDropzone) {
        editTaskDropzone.destroy();
    }
    editTaskDropzone = new Dropzone("#edit-task-dropzone", dropzoneConfig);
}

/**
 * Opens modal which shows task details
 */
$(document).on("click",".assignment-task", function(){
    const taskId = $(this).data("task");
    $("#taskDetails").modal("toggle");
    const task = getAssignmentTaskById(taskId);
    const title = `<h1 class="modal-title fs-5 text-white" id="taskTitle" contenteditable="false"></h1>
                   <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>`;
    const footer = `<span class="fade-left edit-task-btn" data-task="${task.id}">
                        <i class="bi bi-pen-fill"></i>
                     </span>
                     ${task.owner === user ?
                     `<button class="btn btn-danger fade-left del-task-btn mx-1" style="display: none;" data-task="${task.id}">
                        <i class="bi bi-trash"></i>
                     </button>`:
                     `<button class="btn btn-danger fade-left leave-task-btn mx-1" style="display: none;" data-task="${task.id}">
                        <i class="bi bi-arrow-left-circle"></i>
                     </button>`}
                     <button class="btn btn-success fade-left save-task-btn mx-1" style="display: none;" data-task="${task.id}">
                        <i class="bi bi-save"></i>
                     </button>
                     <button class="btn btn-secondary fade-left cancel-task-edit mx-1" style="display: none;" data-task="${task.id}">
                        <i class="bi bi-x-square"></i>
                     </button>`;
    $("#taskHeader").html(title);
    $("#taskTitle").text(task.title);
    $("#taskBody").html(getTaskDetailBody(task));
    $("#taskDescription").text(task.description);
    const isUserOnTask = task.users.some((u) => u.id === user);
    if(isUserOnTask){
        $("#taskFooter").html(footer);
    }else{
        $("#taskFooter").remove();
    }
});

/**
 * Reset the task detail modal when its hidden
 */
$("#taskDetails").on("hidden.bs.modal", function(){
    $("#taskHeader").html("");
    $("#taskBody").html("");
    $("#taskFooter").html("");
});


/**
 * Updates task card in assignment detail
 * @param id
 */
function updateTaskCard(id){
    const task = getAssignmentTaskById(id);
    const cardBody = `<div class="card highlight-card assignment-task" data-task="${task.id}">
                        <div class="m-2">
                            <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                            ${isOverdue(task) ? `<span class="badge text-bg-danger mb-1" data-task="${task.id}">Overdue</span>`:""}
                            <p class="card-text">📆: ${dateDetails(task)}</p>  
                        </div>
                     </div>`;
    $("#assignmentTaskDetail .card.highlight-card.assignment-task[data-task='" + id + "']").replaceWith(cardBody);
    if(isOverdue(task) && $(".task-overdue").length === 0){
        $("#taskDetailBody").prepend(`<span class="badge text-bg-danger mb-1 task-overdue">Overdue</span>`);
    }else if($(".task-overdue").length !== 0 && !isOverdue(task)){
        $(".task-overdue").remove();
    }
}


/**
 * Helps with marking a task as complete or in progress
 * @param id
 * @param inProgress
 * @param complete
 * @param progress
 */
function changeTaskStatus(id,inProgress,complete,progress){
    $.ajax({
        type:"PATCH",
        url:baseUrlWithoutPath+"/api/assignments/tasks/set-status/"+id,
        data:{
            "inProgress": inProgress,
            "complete":complete
        },
        success: function (){
            $("#taskBtns").html("");
            $("#taskBtns").append(buttonStartTask(id,inProgress,complete));
            $("#taskBtns").append(buttonCompleteTask(id,complete));
            $("#taskProgressHeader").text(`Progress | Status: ${getStatus(inProgress,complete)}`);
            if(progress != null){
                $("#taskProgress").html(getProgressBar(progress));
            }
            updateTaskCard(id);
            showToastSuccess("Task status changed!");
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}

/**
 * Starts a task
 */
$(document).on("click", "#startTask",function(){
    const assignmentId = $(this).data("task");
    const isInProgress = $(this).data("inprogress");
    const complete = false;
    let progress = null;
    if(!isInProgress && !complete){
        progress = 0;
    }
    changeTaskStatus(assignmentId,isInProgress,complete,progress);
});

/**
 * Completes a task
 */
$(document).on("click","#completeTask", function (){
    const assignmentId = $(this).data("task");
    const complete = $(this).data("complete");
    const isInProgress = (complete ? false: true);
    let progress;
    if(!isInProgress && complete){
        progress = 100;
    }
    changeTaskStatus(assignmentId,isInProgress,complete,progress);
});

/**
 * Used to toggle DOM elements to be in or out of edit mode
 */
let taskOwner;
function toggleEditTask(event){
    let value = $("#taskDescription").attr("contenteditable");
    if (value === "false" || value === null){
        value = "true";
    }else{
        value = "false";
    }

    if(event != null){
        const taskId = event.data("task");
        const task = getAssignmentTaskById(taskId);
        taskOwner = task.owner;

        const dateTimeString = $("#taskDetailDate").text().split(",");
        const startTime = dateTimeString[1].trim().split(" ")[0];
        let endTime;

        if(new Date(task.endDate).getDate() === new Date(task.startDate).getDate() &&
            new Date(task.endDate).getMonth() === new Date(task.startDate).getMonth() &&
            new Date(task.startDate).getFullYear() === new Date(task.endDate).getFullYear()){
            endTime = dateTimeString[1].trim().split(" ")[2];
        } else{
            endTime = dateTimeString[1].trim().split(" ")[6];
        }

        let startYear = new Date(task.startDate).getFullYear();
        let startMonth =  new Date(task.startDate).getMonth() +1;
        let startDate = new Date(task.startDate).getDate();

        let endYear = new Date(task.endDate).getFullYear();
        let endMonth = new Date(task.endDate).getMonth() +1;
        let endDate = new Date(task.endDate).getDate();

        if (startMonth < 10) {
            startMonth = "0" + startMonth;
        }
        if (startDate < 10) {
            startDate = "0" + startDate;
        }
        if (endMonth < 10) {
            endMonth = "0" + endMonth;
        }
        if (endDate < 10) {
            endDate = "0" + endDate;
        }

        const startDateVal = startYear + "-" + startMonth + "-" + startDate + `T${startTime}`;
        const endDateVal = endYear + "-" + endMonth + "-" + endDate + `T${endTime}`;
        $("#task-edit-startdate").val(startDateVal);
        $("#task-edit-enddate").val(endDateVal);
    }

    $("#taskProgress").toggle();
    $("#taskBtns").toggle();
    $("#taskType").toggle();
    $("#taskDates").toggle();
    $("#workloadRating").toggle();
    $("#moodRating").toggle();
    $("#taskFiles").toggle();

    $("#taskTitle").attr("contenteditable",value);
    $("#edit-task-progress").toggle();
    $("#edit-task-type").toggle();
    $("#taskDescription").attr("contenteditable",value);
    $("#edit-task-date").toggle();
    $("#edit-workload").toggle();
    $("#edit-mood").toggle();
    $("#edit-task-users").toggle();
    if(taskOwner === user){
        $(".remove-user-task").toggle();
    }
    if(value === "true"){
        $("#userToAdd").removeClass("border border-3 rounded");
        $("#usersAddedToTask").removeClass("border border-3 rounded");
    }else{
        $("#usersAddedToTask").addClass("border border-3 rounded");
    }
    $("#edit-task-attachments").toggle();
    initTaskEditDropzone();

    $(".edit-task-btn").toggle();
    $(".del-task-btn").toggle();
    $(".leave-task-btn").toggle();
    $(".save-task-btn").toggle();
    $(".cancel-task-edit").toggle();
}

/**
 * Go's into edit task mode
 */
$(document).on("click", ".edit-task-btn", function(){
    toggleEditTask($(this));
});

/**
 * Cancels task edit mode
 */
$(document).on("click", ".cancel-task-edit",function (){
    const taskId = $(this).data("task");
    const task = getAssignmentTaskById(taskId);
    toggleEditTask();
    $("#taskBody").html(getTaskDetailBody(task));
    //Have to do this since a user can input HTML tags meaning they can inject javaScript, this is done to prevent it.
    $("#taskTitle").text(task.title);
    $("#taskDescription").text(task.description);
    showToastInfo("No changes have been made!");
});

/**
 * Deletes a task and all DOM elements associated with it
 */
$(document).on("click", ".del-task-btn", function(){
    const taskId = $(this).data("task");
    const confirmed = confirm("Are you sure you want to delete this task?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/assignments/tasks/"+taskId,
            success: function(){
                $("#taskDetails").modal('toggle');
                $('[data-task="'+taskId+'"]').remove();
                showToastSuccess("Task successfully deleted!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * Removes a user from a task
 */
$(document).on("click",".leave-task-btn",function(){
    const taskId = $(this).data("task");
    const confirmed = confirm("Are you sure you want to leave this task?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/assignments/tasks/"+taskId+"/remove-user",
            success: function(){
                $("#taskDetails").modal('toggle');
                showToastSuccess("Successfully left task!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * Saves the changes a user has made to a task or checks for any possible errors
 */
$(document).on("click", ".save-task-btn", function(){
    let taskModel = {};
    const taskId = $(this).data("task");
    const task = getAssignmentTaskById(taskId);
    const assignmentId = $(".edit-assignment-btn").data("assignment");
    const assignment = getAssignmentById(assignmentId);
    taskModel["id"] = taskId;
    taskModel["title"] = $("#taskTitle").text();
    taskModel["progress"] = $("#taskBar").val();
    taskModel["important"] = $("#edit-type-task").val() === "Important" || $("#edit-type-task").val() === "Urgent/Important" ? true : false;
    taskModel["urgent"] = $("#edit-type-task").val() === "Urgent" || $("#edit-type-task").val() === "Urgent/Important" ? true : false;
    taskModel["startDate"] = new Date($("#task-edit-startdate").val());
    taskModel["endDate"] = new Date($("#task-edit-enddate").val());
    taskModel["description"] = $("#taskDescription").text();
    $("#task-edit-workload span.workload-emoticon").each(function (){
        if($(this).hasClass("enlarged")){
            taskModel["workload"] = $(this).data("rating");
        }
    });
    $("#task-edit-mood span.emoticon").each(function (){
        if($(this).hasClass("enlarged")){
            taskModel["mood"] = $(this).data("rating");
        }
    });
    let users = [];
    $("#usersAddedToTask div.d-flex").each(function() {
        let user = {};
        let userId = $(this).data("user");
        user["id"] = userId;
        users.push(user);
    });
    taskModel["users"] = users;
    taskModel["modified"] = new Date();

    let errors = false;
    if(hasDateErrors(taskModel)){
        showToastError("Start date has to be before the end date!");
        errors = true;
    }

    if(taskDateHasErrorsWithAssignmentDates(taskModel,assignment)){
        showToastError("Task start and end date must be between assignment start and due dates!");
        errors = true;
    }

    if(!isFileAttachmentSizeValid(task.fileAttachments,files)){
        errors = true;
        showToastError("Total file attachment size exceeded!");
    }

    for (const [k,v] of Object.entries(taskModel)){
        if(k === "title" && v.trim() === ""){
            showToastError("Title field cannot be empty!");
            errors = true;
        }
        if(k === "description" && v.trim() === ""){
            showToastError("Description field cannot be empty!");
            errors = true;
        }
    }

    let formData = new FormData();
    formData.append("assignmentTaskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    if(!errors){
        $.ajax({
            type:"PUT",
            url:"/api/assignments/tasks/"+taskId,
            contentType: false,
            processData: false,
            data:formData,
            success: function(){
                toggleEditTask();
                $("#taskTitle").text(taskModel.title);
                $("#taskBody").html(getTaskDetailBody(getAssignmentTaskById(taskId)));
                $("#taskDescription").text(taskModel.description);
                updateTaskCard(taskId);
                files = [];
                showToastSuccess("Successfully updated Task!");
            },
            error: function (){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * Filters assignments from search and displays results
 */
$("#assignmentSearch").on("submit",function(event){
   event.preventDefault();
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/assignments/search",
        data:{
            "query": $("#search-input").val(),
        },
        success: function(res){
            if(getAndRenderAssignmentsList(res).length === 0){
                $("#assignmentTab").html(`<h5 class="m-2">No results found!</h5>`);
                showToastInfo("No Assignments matching search found!");
            }else{
                showToastSuccess("Assignments matching search found!");
            }
        },
        error: function(){
            $("#assignmentTab").html(`<h5 class="m-2">No results found!</h5>`);
            showToastInfo("No Assignments matching search found!");
        }
    });
});

/**
 * Reset the search query and show all assignments
 */
$("#search-input").on("input", function (){
    let inputVal = $("#search-input").val();
    if(inputVal.trim() == ""){
        getAndRenderAssignmentsList();
    }
});

/**
 * Helper function to help with sorting via importance
 * Assignments that are important and urgent have
 * bigger weight value and important task have more
 * than sole urgent tasks.
 * @param assignment
 * @returns {number}
 */
function getAssignmentImportance(assignment){
    if(assignment.important && assignment.urgent){
        return 3;
    }else if(assignment.important){
        return 2;
    }else if(assignment.urgent){
        return 1;
    }else{
        return 0;
    }
}

/**
 * Handles the event when a user is using sorting or filtering options
 */
$("#filterBtn").on("click", function (){
    filters = [];
    sortOption = $("input[name='sortOption']:checked").val();
    $("#search-input").val("");
    $("#filters li input[type='checkbox']").each(function(){
        if($(this).prop("checked")){
            filters.push($(this).val());
        }
    });

    const filteredAssignments = getAndRenderAssignmentsList();
    if(filteredAssignments.length === 0){
        $("#assignmentTab").html(`<h5 class="m-2">No results found!</h5>`);
        showToastInfo("No assignments found matching filters!");
    }else{
        showToastSuccess("Filters and sort method applied");
    }
});