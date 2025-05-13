//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Get the current user logged in
const user = $('#_principal').data("principal");
const personalType = $("#personalType").data("personal");
const catId = $("#catId").data("catid");
const group = $("#group").data("group");
let files = [];

/**
 * Handles the configuration of dropzone
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
let createTaskDropZone;
if($("#my-dropzone").length !== 0){
    createTaskDropZone = new Dropzone("#my-dropzone", dropzoneConfig);
}

/**
 * Makes ajax call for getting a logged in users personal tasks
 * @returns {*}
 */
function getAllPersonalTasks(){
    let tasks;
    $.ajax({
        type: "GET",
        url: "/api/task/user/tasks",
        dataType:"json",
        async: false,
        success: function (res){
            tasks = res;
        },
        error: function (){
            tasks = [];
        }
    });
    return tasks;
}

/**
 * Gets all categories a user has
 * @returns {*[]}
 */
function getAllCategories(){
    let categories = [];
    $.ajax({
        type: "GET",
        url: "/api/task/categories",
        dataType:"json",
        async: false,
        success: function (res){
            categories = res;
            $("#addTaskBtn").prop("disabled", false);
        },
        error: function (){
            $("#addTaskBtn").prop("disabled", true);
        }
    });
    return categories;
}

/**
 * Gets a category via its id
 * @param id
 * @returns {*}
 */
function getCategoryById(id){
    let cat;
    $.ajax({
        type:"GET",
        url:"/api/task/categories/"+id,
        dataType:"json",
        async:false,
        success: function (res){
            cat = res;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (xhr.status == 404) {
                task = null;
            } else {
                console.log(thrownError);
            }
        }
    });
    return cat;
}

/**
 * Makes ajax call for getting a task via its id
 * @param id
 * @returns {*}
 */
function getTaskById(id){
    let task;
    $.ajax({
        type: "GET",
        url: "/api/task/" + id,
        dataType: 'json',
        async: false,
        success: function (res) {
            task = res;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (xhr.status == 404) {
                task = null;
            } else {
                console.log(thrownError);
            }
        }
    });
    return task;
}

/**
 * Mkaes ajax call to get all users group tasks
 * @returns {*[]}
 */
function getAllGroupTasks(){
    let groupTasks = [];
    $.ajax({
        type:"GET",
        url:"/api/task/group",
        dataType:"json",
        async: false,
        success: function (res){
            groupTasks = res;
        },
        error: function (){
            groupTasks = [];
        }
    });
    return groupTasks;
}

/**
 * Makes ajax call to get a group task via id
 * @param id
 * @returns {*}
 */
function getGroupTaskById(id){
    let task;
    $.ajax({
        type:"GET",
        url:"/api/task/group/"+id,
        dataType:"json",
        async:false,
        success: function(res){
            task = res;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (xhr.status == 404) {
                task = null;
            } else {
                console.log(thrownError);
            }
        }
    });
    return task;
}

/**
 * Makes ajax call to get all of the archived tasks a user has
 * @returns {*[]}
 */
function getAllArchivedTasks(){
    let tasks = [];
    $.ajax({
        type:"GET",
        url:"/api/task/archived",
        dataType:"json",
        async: false,
        success: function (res){
            tasks = res;
        },
        error: function (){
            tasks = [];
        }
    });
    return tasks;
}

/**
 * Handles ajax call for archiving a task
 * @param id
 * @param archive
 */
function archiveTask(id, archive){
    $.ajax({
        type:"PATCH",
        url:"/api/task/"+id+"/archive",
        data:{
            "archive":archive
        },
        success:function (){
            $('#taskDetails').modal('toggle');
            $('[data-task="'+id+'"]').remove();
            if(archive){
                showToastSuccess("Task has been archived and moved!");
            }else{
                showToastSuccess("Task has been un-archived and moved!");
            }
        }
    });
}

/**
 * Makes ajax call to get search results for a user
 * @param search
 * @returns {*[]}
 */
function getSearchFriendsResults(search){
    let results = [];
    $.ajax({
        type:"GET",
        url:"/api/user/search/friends",
        async:false,
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
 * Gets the task card body
 * @param t
 * @returns {string}
 */
function taskBodyInTab(t){

    //Since its text injecting HTML tags so we do this to ensure it doesn't happen.
    const titleElString = `<h3 class="card-title"></h3>`;
    const titleEl = $(titleElString);
    titleEl.text(t.title + getStatus(t.inProgress,t.complete));

    const descriptionElString =  `<p class="card-text"></p>`;
    const descEl = $(descriptionElString);
    descEl.text("Task Details 📝: "+t.description.slice(0,150)+"...");

    return          `<div class="task card m-4 highlight-card fade-from-bottom" data-task="${t.id}" data-type="${t.taskType}">
                        <div class="card-body">
                            ${titleEl.prop("outerHTML")}
                            ${isOverdue(t) ? `<span class="badge text-bg-danger mb-1" >Overdue</span>`:""}
                            ${t.taskType === "task" ? `<h4 class="card-text text-muted">Category 📚: ${t.categoryName}</h4>`:
                                `<h4 class="card-text text-muted">Users 🕴️:${t.users.length}</h4>`}
                            <h5>Task Type 🚨: ${getType(t)}</h5>
                            <h6>Progress 📊:</h6>
                            <div class="pb-2">
                                ${getProgressBar(t.progress)}
                            </div>
                            <h6>Task Info 📓:</h6>
                            <p class="card-text">Date 📅: ${dateDetails(t)}</p>
                            ${descEl.prop("outerHTML")}
                        </div>
                      </div>`;
}

/**
 * Renders all tasks onto the page
 * @param tasks
 */
function renderAllTasks(tasks){
    $("#tasks").html("");
    if(tasks.length === 0){
        $("#tasks").html(`<h1 class="text-muted m-3">Start Creating Tasks!</h1>`);
    }
    tasks.forEach((task) => {
        $("#tasks").append(taskBodyInTab(task));
    });
}

/**
 * Renders tasks for a category except those that are archived
 * @param tasks
 */
function renderTasksForCat(tasks){
    $("#tasks").html("");
    if(tasks.length === 0){
        $("#tasks").html(`<h1 class="text-muted m-3">Start Creating Tasks!</h1>`);
    }
    tasks.forEach((task) => {
        if(!task.archive){
            $("#tasks").append(taskBodyInTab(task));
        }
    });
}

/**
 * Renders all group tasks that user is in
 * @param groupTasks
 */
function renderGroupTasks(groupTasks){
    $("#tasks").html("");
    if(tasks.length === 0){
        $("#tasks").html(`<h1 class="text-muted m-3">Start Creating Tasks!</h1>`);
    }
    groupTasks.forEach((task) => {
        $("#tasks").append(taskBodyInTab(task));
    })
}

/**
 * If a category is added then it will be rendered onto the page
 * @param category
 */
function renderAddedCategoryToList(category){
    const body  = `<li class="w-100">
                    <a href="/tasks/category?cat=${category.name}&id=${category.id}" class="nav-link px-2"> <span>${category.name}</span></a>
                   </li>`;
    $("#categories > li:last-child").before(body);
    $("#category").append(`<option value="${category.id}">${category.name}</option>`)
}

/**
 * Updates task card on the page
 * @param task
 */
function updateTaskCard(task){
    $('.task[data-task="'+task.id+'"]').replaceWith(taskBodyInTab(task));
    if(isOverdue(task) && $("#taskOverdue").length === 0){
        $("#taskDetailBody").prepend(`<span class="badge text-bg-danger mb-1" id="taskOverdue">Overdue</span>`);
    }else if($("#taskOverdue").length !== 0 && !isOverdue(task)){
        $("#taskOverdue").remove();
    }
}

/**
 * Handels event of creating a new category
 */
$("#createCategory").on("submit",function (event){
    event.preventDefault();
    let cat = $("#newCategory").val();
    let catreplace = cat.replace(/[^\w\s]/gi, '').replace(/\d+/g, '');
    if(catreplace.length === 0){
        showToastError("Cannot be just number and punctuation!");
    }else{
        $.ajax({
            type:"POST",
            url:"/api/task/add/category/"+cat,
            success: function (res) {
                showToastSuccess("Added: "+ cat + " category!");
                if(!userRoles.includes("MEMBER") && getAllCategories().length >= 4){
                    $("#addCat").prop("disabled",true);
                }
                renderAddedCategoryToList(res);
                $("#addTaskBtn").prop("disabled",false);
                $("#addingCategory").modal('toggle');
            },
            error: function (){
                showToastError("Category already exists!");
            }
        });
    }
});

/**
 * Handles the event of opening modal if a user want to delete a category
 */
$('#delCat').on('click', function(){
    let catId = $(this).data('cat');
    let cat = $(this).data('name');
    const header = `<h1 class="modal-title fs-5 text-white" >Delete Category ${cat}</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>`;
    const body = `<h5>Are you sure you want to delete the ${cat} category?</h5>
                  <p>Any task that is within the ${cat} category will be deleted!</p>
                  <button type="button" class="btn btn-danger" id="delCategory" data-cat="${catId}" data-name="${cat}">Delete Category</button>
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>`;
    $("#delCatHeader").html(header);
    $("#delCatBody").html(body);
});

/**
 * Handels and makes ajax call for deleting a category
 */
$(document).on('click', "#delCategory",function(){
    let id = $(this).data('cat');
    $.ajax({
        type: "DELETE",
        url:"/api/task/delete/category/"+id,
        success: function (){
            location.href="/tasks/personal";
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
});

/**
 * Creates task model template based on form and other fields
 * @param data
 * @returns {{}}
 */
function getTaskModelFromForm(data){
    let taskModel = {};
    $(data).each(function (i,field){
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

    taskModel["user"] = user;
    taskModel["progress"] = 0;
    taskModel["created"] = new Date();
    taskModel["finished"] = null;
    taskModel["modified"] = new Date();
    taskModel["complete"] = false;
    taskModel["inProgress"] = false;

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

    return taskModel;
}

/**
 * Handles event of creating a task and making ajax calls
 */
$("#createTask").submit(function(event){
    event.preventDefault();
    let data = $("#createTask").serializeArray();
    let taskModel = getTaskModelFromForm(data);

    if(hasDateErrors(taskModel)){
        showToastError("Start date as to be before the end date!");
        return;
    }else if(taskModel.description.trim() == "" || taskModel.title.trim() == ""){
        showToastError("Fields cannot be empty!");
        return;
    }

    let formData = new FormData();
    formData.append("taskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    $.ajax({
        type: "POST",
        url: "/api/task",
        contentType: false,
        processData: false,
        data: formData,
        success:function(){
            files = [];
            $("#createTask")[0].reset();
            createTaskDropZone.removeAllFiles(true);
            showToastSuccess("Task successfully created!")
            if(personalType === "all"){
                renderAllTasks(getAllPersonalTasks());
            }else{
                renderTasksForCat(getCategoryById(catId).tasks);
            }
        }
    });
    $("#addingTask").modal('toggle');
});

/**
 * Creates HTML elements for displaying fields of users a user can add to group task form
 * @param users
 */
function populateUserField(users){
    users = users.filter((user) => {
        const element = $("#added-user-"+user.id);
        return element.length === 0;
    });

    if(users.length !== 0){
        let options = "";
        users.forEach((u) => {
            options += `<div class="border-bottom d-flex align-items-center rounded-3" id="user-${u.id}">
                                <div class="d-flex align-items-center highlight-card w-100">
                                    ${getUserPfpBody(u.profilePic)}
                                    <h4 class="m-0 fw-bold">@${u.username}</h4>
                                </div>
                                <a type="button" class="btn btn-success ms-auto add-user" aria-label="Add" data-user="${u.id}" data-username="${u.username}"><i class="bi bi-person-add"></i></a>
                              </div>`
        });
        $("#user-friends").html(options);
    }else{
        $("#user-friends").html("No Results");
    }
}

/**
 * Handles event when user makes a search for friends available to be added to task
 */
$(document).on("click", "#search-friends", function (){
    $("#user-friends").html("");
    const search = $("#search-friend-input").val();
    let results = getSearchFriendsResults(search);
    if(results.length === 0){
        $("#user-friends").html("No Results");
        showToastError("No Friends with that username found!");
    }else{
        populateUserField(results);
    }
});

$(document).on("input", "#search-friend-input", function(){
    let inputVal = $(this).val();
    if(inputVal.trim() == ""){
        $("#user-friends").html("");
    }
});

/**
 * Adds user to a task from
 * @param userId
 * @param username
 */
function addUserToForm(userId,username){
    const userToAdd =`<div class="user d-flex border-bottom align-items-center rounded-3" id="added-user-${userId}"  data-user="${userId}">
                        <div class="d-flex align-items-center highlight-card w-100">
                            ${getUserPfpBody(getUserPfpString(userId))}
                            <h4 class="m-0 fw-bold">@${username}</h4>
                        </div>
                        <a type="button" class="btn btn-danger ms-auto remove-user" aria-label="Close" data-user="${userId}"><i class="bi bi-person-dash"></i></a>
            </div>`;

    $("#users-added").append(userToAdd);
}

$(document).on("click", ".add-user", function(){
    const id = $(this).data("user");
    const username = $(this).data("username");
    $("#user-"+id).remove();
    addUserToForm(id,username);
});

$(document).on("click", ".remove-user", function (){
    const id = $(this).data("user");
    $("#added-user-"+id).remove();
});

/**
 * Handles event for creating a group task with ajax call
 */
$("#createGroupTask").on("submit", function (event){
    event.preventDefault();
    let data = $("#createGroupTask").serializeArray();
    let taskModel = getTaskModelFromForm(data);

    if(hasDateErrors(taskModel)){
        showToastError("Start date has to be before the end date!");
        return;
    }else if(taskModel.description.trim() === "" && taskModel.title.trim() == ""){
        showToastError("Fields cannot be empty!");
        return;
    }else if($("#users-added div.d-flex").length === 0){
        showToastError("For a group task there must be at least one user added!");
        return;
    }

    let users = [];
    $("#users-added div.user").each(function() {
        let dataUserValue = $(this).attr("data-user");
        let userModel = {};
        userModel["id"] = dataUserValue;
        users.push(userModel);
    });
    taskModel["users"] = users;

    let formData = new FormData();
    formData.append("groupTaskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    $.ajax({
        type: "POST",
        url: "/api/task/group",
        contentType: false,
        processData: false,
        data: formData,
        success:function(){
            files = [];
            $("#createGroupTask")[0].reset();
            createTaskDropZone.removeAllFiles(true);
            renderGroupTasks(getAllGroupTasks());
            $("#users-added").html("");
            showToastSuccess("Group-Task successfully created!")
        }
    });
    $("#addingGroupTask").modal('toggle');
});

/**
 * Used to render category that can be selected for a task to be a part of when editing a task
 * @param task
 * @returns {string}
 */
function categoryDetail(task){
    let body = "";
    if(task.taskType === "task"){
        body = `<h5>Category 📚:</h5> 
                  <p id="task-category" contenteditable="false">${task.categoryName}</p>
                  <div id="edit-category" class="mb-3">
                  <select id="edit-task-category" class="form-select">
                    ${editCategoryOptions(task)}
                  </select>
                </div>`;
    }
    return body;
}

/**
 * Used to render category options for when a user is in edit mode
 * @param task
 * @returns {*}
 */
function editCategoryOptions(task){
    let options;
    const categories = getAllCategories();
    options += `<option value="${task.category}">${task.categoryName}</option>`
    categories.forEach((cat) => {
        if(task.category !== cat.id){
            options += `<option value="${cat.id}">${cat.name}</option>`;
        }
    });
    return options;
}

/**
 * Checks is a user is the owner of a task
 * @param task
 * @returns {string}
 */
function ownerOfTask(task){
    const body = "";
    if(task.taskType === "group"){
        return `<h5 contenteditable="false" class="mb-3" id="taskLeader">Task Leader: ${task.leader}</h5>`;
    }
    return body;
}

/**
 * Renders start button
 * @param id
 * @param inProgress
 * @param complete
 * @param type
 * @returns {string}
 */
function buttonStart(id,inProgress, complete,type){
    if(inProgress){
        return `<button type="button" class="btn btn-outline-danger" id="startTask" data-task="${id}" data-inprogress="${false}" data-type="${type}">Un-Start</button>`;
    }else if(!inProgress && !complete){
        return `<button type="button" class="btn btn-outline-success" id="startTask" data-task="${id}" data-inprogress="${true}" data-type="${type}">Start</button>`;
    }else{
        return `<button type="button" class="btn btn-outline-danger disabled" id="startTask" data-task="${id}" data-inprogress="${false}" data-type="${type}">Un-Start</button>`;
    }
    return "";
}

/**
 * Renders complete button
 * @param id
 * @param complete
 * @param type
 * @returns {string}
 */
function buttonComplete(id,complete,type) {
    if(!complete){
        return `<button type="button" class="btn btn-outline-success" id="completeTask" data-task="${id}" data-complete="${true}" data-type="${type}">Set as Complete</button>`;
    }else{
        return `<button type="button" class="btn btn-outline-danger" id="completeTask" data-task="${id}" data-complete="${false}" data-type="${type}">Not Complete</button>`;
    }
    return "";
}

/**
 * Provides html body for a user
 * @param id
 * @param username
 * @returns {string}
 */
function userBodyDetail(id,username){
        return  `<div id="user-added-${id}" data-user="${id}" class="border-bottom d-flex align-items-center rounded-3">
                    <a class="d-flex align-items-center highlight-card w-100" href="/social/profile/${username}" style="text-decoration: none;">
                        ${getUserPfpBody(getUserPfpString(id))}
                        <h4 class="m-0 fw-bold">@${username}</h4>
                    </a>
                    ${id !== user ? `<a type="button" class="btn btn-danger ms-auto remove-user-task" aria-label="Close" data-user="${id}" style="display: none"><i class="bi bi-person-dash"></i></a>` : ''}
                </div>`;
}

/**
 * Gets HTML body for users on a group task
 * @param task
 * @returns {string}
 */
function usersOnTaskDetail(task){
    let body = "";
    if(task.taskType === "group"){
        let userList = "";
        task.users.forEach((u => userList+=userBodyDetail(u.id,u.username)));

        body = `<div class="mb-3" id="group-users" contenteditable="false">
                    <div id="edit-users">
                        <h5>Add to Task</h5>
                        <div id="users-to-add" class="col overflow-auto mb-3" style="height: 150px;">
                            <form id="add-user-task" class="d-flex" role="search">
                                <input id="add-user" class="form-control" type="search" placeholder="Search" aria-label="Search">
                                <button class="btn btn-success" type="submit">
                                    <i class="bi bi-search"></i>
                                </button>
                            </form>
                            <div id="search-user-results"></div>
                        </div>
                    </div>
                    <div>
                        <h5>Users on Task:</h5>
                        <div id="users-on-task" class="col border border-3 rounded overflow-auto" style="max-height: 150px;">
                            ${userList}
                        </div>
                    </div>
                </div>`;
    }
    return body;
}

$(document).on("submit", "#add-user-task", function (event){
    event.preventDefault();
    const search = $("#add-user").val();
    let results = getSearchFriendsResults(search);
    if(results.length === 0){
        $("#search-user-results").html("No results");
    }else{
        results = results.filter((user) => {
            const element = $("#user-added-"+user.id);
            return element.length === 0;
        });

        if(results.length !== 0){
            results.forEach((u) => {
                const body =  `<div class="border-bottom d-flex align-items-center rounded-3" id="user-friend-${u.id}">
                                <div class="d-flex align-items-center highlight-card w-100">
                                    ${getUserPfpBody(u.profilePic)}
                                    <h4 class="m-0 fw-bold">@${u.username}</h4>
                                </div>
                                <a type="button" class="btn btn-success ms-auto add-user-task" aria-label="Add" data-user="${u.id}" data-username="${u.username}"><i class="bi bi-person-add"></i></a>
                              </div>`;
                $("#search-user-results").append(body);
            });
        }else{
            showToastError("No results found!")
            $("#search-user-results").html("No Results");
        }
    }
});

$(document).on("input", "#add-user", function(){
    let inputVal = $(this).val();
    if(inputVal.trim() == ""){
        $("#search-user-results").html("");
    }
});

$(document).on("click", ".remove-user-task", function(){
    const userId = $(this).data("user");
    $("#user-added-"+userId).remove();
});

$(document).on("click", ".add-user-task", function(){
    const userId = $(this).data("user");
    const username = $(this).data("username");
    $("#users-on-task").append(userBodyDetail(userId,username));
    $("#user-friend-"+userId).remove();
});

/**
 * Configure the edit drop-zone
 */
let editDropzone;
function initEditDropzone() {
    // Destroy existing dropzone
    if (editDropzone) {
        editDropzone.destroy();
    }
    editDropzone = new Dropzone("#edit-dropzone", dropzoneConfig);
}

/**
 * Gets file attachment body for task detail body
 * @param attachments
 * @param task
 * @returns {string}
 */
function getTaskFileAttachments(attachments, task){
    let content = "";
    const isTaskOwner = task.user === user;
    if(attachments.length > 0){
        attachments.forEach((file) => {
            const attachment = `<div class="card text-dark bg-light d-flex flex-row">
                                    <a href="/api/files/${file.id}/download" class="card-body highlight-card">
                                        <div class="d-flex align-items-center">
                                            <i class="fs-3 bi bi-file-earmark-break-fill me-3"></i>
                                            <span class="fs-5 text-primary">${file.title.slice(0,10)}..</span>
                                       </div>
                                    </a>
                                    ${isTaskOwner && !task.archive ? 
                                    `<button type="button" id="delFile" class="btn btn-danger me-auto" aria-label="Delete" data-type="${task.taskType}" data-file="${file.id}">
                                        <i class="bi bi-file-earmark-x"></i>
                                    </button>` : ""}
                                </div>`;
            content += attachment;
        });
        return content;
    }
    return `<h6 class="m-2">There are no files attached to this task!</h6>`;
}

/**
 * Provides HTML header for task detail header
 * @param task
 * @returns {string}
 */
function getTaskTitle(task){
    const title = `<div class="d-flex flex-row">
                    <h1 class="modal-title fs-5 text-white" id="taskTitle" contenteditable="false"></h1>
                    <h1 class="modal-title fs-5 text-white" id="taskStatus">${getStatus(task.inProgress,task.complete)}</h1>
                   </div>
                   <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>`;
    return title;
}

/**
 * Provides the HTML detail body of task for task detail modal
 * @param task
 * @returns {string}
 */
function getTaskDetailBody(task){
    const body = `<div id="taskDetailBody">
                    ${isOverdue(task) ? `<span class="badge text-bg-danger mb-1" id="taskOverdue">Overdue</span>`:""}
                    ${ownerOfTask(task)}
                    <h5>Progress 📊:</h5>
                    <div id="progress" contenteditable="false">
                        ${getProgressBar(task.progress)}
                    </div>   
                    ${!task.archive ? 
                    `<div id="edit-progress">
                        <input id="bar" type="range" class="form-range" min="0" max="100" value="${task.progress}"/>
                    </div>` : ""}
                    ${!task.archive ? 
                    `<div id="buttons" class="mb-3">
                        ${buttonStart(task.id,task.inProgress,task.complete,task.taskType)}
                        ${buttonComplete(task.id,task.complete,task.taskType)}
                    </div>` : ""}  
                    ${categoryDetail(task)}
                    <h5>Type 🚨:</h5>
                    <p id="task-type" contenteditable="false">${getType(task)}</p>
                    ${!task.archive ? 
                    `<div id="edit-type" class="mb-3">
                        <select id="edit-task-type" class="form-select">
                            <option>Urgent/Important</option>
                            <option>Urgent</option>
                            <option>Important</option>
                            <option>Not important or Urgent</option>
                        </select>
                    </div>`: ""}
                    <h5>Task Details 📝:</h5>
                    <p id="taskDescription" class="card-text" contenteditable="false"></p> 
                    <h5>Dates For Task 📅:</h5>
                    <p class="card-text" id="task-detail-date" contenteditable="false"> ${dateDetails(task)}</p>
                    ${!task.archive ? 
                    `<div id="edit-task-date" class="mb-3">
                        <h5>Start</h5>
                        <input id="task-edit-startdate" class="form-select" name="startDate" type="datetime-local" placeholder="start date" required>
                        <h5>Date For Completion</h5>
                        <input id="task-edit-enddate" class="form-select" name="endDate" type="datetime-local" placeholder="end date date" required> 
                    </div>`:""}
                    <h5 id="workloadTitle">Workload ⚖:</h5>
                    <div id="task-workload" class="mb-3" contenteditable="false">
                        ${getWorkloadRating(task.workload)}
                    </div>
                    ${!task.archive ? 
                    `<div class="mb-3" id="edit-workload">
                        <div id="task-edit-workload" class="d-flex flex-wrap justify-content-center">
                            ${getWorkloadEmoticons(task.workload)}
                        </div>
                    </div>`: ""}
                    <h5 id="moodTitle">Affect on Mood 🎭:</h5>     
                    <div id="task-mood" class="mb-3" contenteditable="false">
                        ${getMoodRating(task.mood)}
                    </div>
                    ${!task.archive ? 
                    `<div class="mb-3" id="edit-mood">
                        <div id="task-edit-mood" class="d-flex flex-wrap justify-content-center">
                            ${getEmoticons(task.mood)}
                        </div>
                    </div>`: ""}
                    ${usersOnTaskDetail(task)}
                    <h5>File Attachments 🗃️:</h5>
                    <div id="task-files" class="border  border-3 rounded" class="mb-3" contenteditable="false">
                        ${getTaskFileAttachments(task.fileAttachments, task)}
                    </div>
                    ${!task.archive ?
                    `<div id="edit-file-attachments">
                        <div id="edit-dropzone" class="dropzone"></div>
                    </div>` : ""}
                  </div>`;
    return body;
}

/**
 * Provides footer for task detail modal
 * @param task
 * @returns {string}
 */
function getTaskFooter(task){
    if(task.archive){
        return `<button class="me-auto btn btn-warning un-archive-task" data-task="${task.id}">
                    <i class="bi bi-archive"></i>
                </button>`;
    }
    return      `${userRoles.includes("MEMBER") && task.complete ? 
                    `<button class="me-auto btn btn-info archive-task" data-task="${task.id}">
                        <i class="bi bi-archive-fill"></i>
                    </button>`: ""}
                    <div id="footer" data-tasks="${task.id}" data-type="${task.taskType}">
                        <span class="fade-left edit-task-btn" data-task="${task.id}">
                            <i class="bi bi-pen-fill"></i>
                        </span>
                        ${task.user === user ?
                        `<button class="btn btn-danger fade-left delete-task-btn mx-1" style="display: none;" data-task="${task.id}">
                            <i class="bi bi-trash"></i>
                         </button>`:
                        `<button class="btn btn-danger fade-left leave-task-btn mx-1" style="display: none;" data-task="${task.id}">
                            <i class="bi bi-arrow-left-circle"></i>
                         </button>`}
                        <button class="btn btn-success fade-left save-task-btn mx-1" style="display: none;" data-task="${task.id}">
                            <i class="bi bi-save"></i>
                        </button>
                        <button class="btn btn-secondary fade-left cancel-edit-mode mx-1" style="display: none;" data-task="${task.id}">
                            <i class="bi bi-x-square"></i>
                        </button>
                    </div>`;
}

$(document).on("click",".task", function(){
    const id = $(this).data("task");
    const taskType = $(this).data("type");
    let task;
    if(taskType === "task"){
        task = getTaskById(id);
    }else{
        task = getGroupTaskById(id);
    }
    const title = getTaskTitle(task);
    const body = getTaskDetailBody(task);
    const footer = getTaskFooter(task);

    $("#taskHeader").html(title);
    $("#taskBody").html(body);
    $("#taskTitle").text(task.title);
    $("#taskDescription").text(task.description);
    $("#taskFooter").html(footer);
    $('#taskDetails').modal('toggle');

    initEditDropzone();
});

/**
 * Handles event of deleting a file attachment by making a user onfirm
 */
$(document).on("click", "#delFile", function(){
    let confirmed = confirm("Are you sure you want to delete this file attachment?");
    if(confirmed){
        let file = $(this).data("file");
        let taskType = $(this).data("type");
        $.ajax({
            type:"DELETE",
            url:"/api/files/"+file,
            success: function(){
                let task;
                if(taskType === "task"){
                    task = getTaskById($("#footer").data("tasks"));
                }else{
                    task = getGroupTaskById($("#footer").data("tasks"));
                }
                $("#task-files").html(getTaskFileAttachments(task.fileAttachments, task));
                showToastSuccess("Deleted File Attachment!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});


$(document).on("click", ".archive-task",function (){
    const confirmed = confirm("Do you want to archive this task?");
    if(confirmed){
        const id = $(this).data("task");
        archiveTask(id, true);
    }
});

$(document).on("click",".un-archive-task", function (){
    const confirmed = confirm("Are you sure you want to un-archive this task?");
    if(confirmed){
        const id = $(this).data("task");
        archiveTask(id, false);
    }
});

$(document).on("click", ".save-task-btn", function(){
    const taskType = $("#footer").data("type");
    if(taskType === "task"){
        saveTaskChanges();
    }else{
        saveGroupTaskChanges();
    }
});


$(document).on("click", ".edit-task-btn", function(){
    toggleEditTask();
});

function toggleEditButtons(){
    $(".edit-task-btn").toggle();
    $(".delete-task-btn").toggle();
    $(".save-task-btn").toggle();
    $(".cancel-edit-mode").toggle();
    $(".leave-task-btn").toggle();
}

/**
 * Method used to toggle elements for edging mode
 */
function toggleEditTask(){
    let val = $('#taskTitle').attr('contenteditable');
    if (val === 'false' || val === null) {
        val = 'true';
    } else {
        val = 'false';
    }

    const taskType = $("#footer").data("type");
    let task;
    switch (taskType) {
        case "task":
            task = getTaskById($("#footer").data("tasks"));
            break;
        case "group":
            task = getGroupTaskById($("#footer").data("tasks"));
            break;
    }

    if(task != null){
        const dateTimeString = $("#task-detail-date").text().split(",");
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
        if(task.user !== user){
            $("#group-users").toggle();
            $("#workloadTitle").toggle();
            $("#moodTitle").toggle();
        }
    }
    if(task.user === user){
        $("#taskTitle").attr('contenteditable', val);
    }
    $("#taskLeader").toggle();
    $("#taskStatus").toggle();
    $("#progress").toggle();
    $("#edit-progress").toggle();
    $("#buttons").toggle();
    $("#task-category").toggle();
    $("#edit-category").toggle();
    $("#task-type").toggle();
    $("#edit-type").toggle();
    $("#taskDescription").attr('contenteditable', val);
    $("#task-detail-date").toggle();
    $("#edit-task-date").toggle();
    $("#task-workload").toggle();
    $("#edit-workload").toggle();
    $("#task-mood").toggle();
    $("#edit-mood").toggle();
    $(".remove-user-task").toggle();
    $("#edit-users").toggle();
    $("#users-on-task").removeClass("border border-3 rounded");
    $("#task-files").toggle();
    $("#edit-file-attachments").toggle();
    toggleEditButtons();
}

$(document).on("click", ".cancel-edit-mode", function(){
    $('[contenteditable="true"]').prop('contenteditable', false);
    const type = $("#footer").data("type");
    let task;
    if(type === "task"){
        task = getTaskById($("#footer").data("tasks"));
    }else{
        task = getGroupTaskById($("#footer").data("tasks"));
    }
    $("#taskHeader").html(getTaskTitle(task));
    $("#taskTitle").text(task.title);
    $("#taskBody").html(getTaskDetailBody(task));
    $("#taskDescription").text(task.description);
    toggleEditButtons();
    initEditDropzone();
    showToastInfo("No changes have been made!");
});

/**
 * Handles the deletion of a task
 */
$(document).on("click", ".delete-task-btn", function(){
    const task = $("#footer").data("tasks");
    const taskType  = $("#footer").data("type");
    let confirmed = confirm("Are you sure you want to delete the task?");
    if(confirmed){
        if(taskType === "task"){
            $.ajax({
                type: "DELETE",
                url:"/api/task/"+task,
                success: function (){
                    $("#taskDetails").modal('toggle');
                    $('[data-task="'+task+'"]').remove();
                    showToastSuccess("Task successfully deleted!");
                },
                error: function (){
                    showToastError("Server Error Occurred");
                }
            });
        }else{
            $.ajax({
                type:"DELETE",
                url:"/api/task/group/"+task,
                success: function (){
                    $("#taskDetails").modal('toggle');
                    $('[data-task="'+task+'"]').remove();
                    showToastSuccess("Task successfully deleted!");
                },
                error: function (){
                    showToastError("Server Error Occurred");
                }
            });
        }
    }
});

/**
 * If a user leaves a group task then this will handle the event
 */
$(document).on("click", ".leave-task-btn", function(){
    const task = $("#footer").data("tasks");
    let confirmed = confirm("Are you sure you want to leave this task?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:"/api/task/group/leave/"+task,
            success: function(){
                $("#taskDetails").modal('toggle');
                $('[data-task="'+task+'"]').remove();
                showToastSuccess("Left Task successfully")
            },
            error: function(){
                showToastError("Server Error Occurred");
            }
        });
    }
});

/**
 * Helper method for creating task model template for the update task
 * @returns {{}}
 */
function createUpdatedTaskModel(){
    let taskModel = {};
    const taskId = $("#footer").data("tasks");
    const taskType = $("#footer").data("type");
    let task;
    if(taskType === "task"){
        task = getTaskById(taskId);
    }else{
        task = getGroupTaskById(taskId);
    }
    taskModel["id"] = taskId;
    taskModel["title"] = $("#taskTitle").text();
    taskModel["important"] = $("#edit-task-type").val() === "Important" || $("#edit-task-type").val() === "Urgent/Important" ? true : false;
    taskModel["urgent"] = $("#edit-task-type").val() === "Urgent" || $("#edit-task-type").val() === "Urgent/Important" ? true : false;
    if(task.user === user){
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
    }else{
        taskModel["workload"] = task.workload;
        taskModel["mood"] = task.mood;
    }
    taskModel["description"] = $("#taskDescription").text();
    if(taskType === "task"){
        taskModel["category"] = $("#edit-task-category").val();
    }else{
        let users = [];
        $("#users-on-task div.d-flex").each(function() {
            let dataUserValue = $(this).attr("data-user");
            let userModel = {};
            userModel["id"] = dataUserValue;
            users.push(userModel);
        });
        taskModel["users"] = users;
    }
    taskModel["startDate"] = new Date($("#task-edit-startdate").val());
    taskModel["endDate"] = new Date($("#task-edit-enddate").val());
    taskModel["progress"] = $("#bar").val();
    taskModel["modified"] = new Date();

    return taskModel;
}

/**
 * Saves the changes of an updated task via ajax call
 */
function saveTaskChanges(){
    let taskModel = createUpdatedTaskModel();
    const taskId = $("#footer").data("tasks");
    const task = getTaskById(taskId);
    let errors = false;
    if(hasDateErrors(taskModel)){
        showToastError("Start date has to be before the end date!");
        errors = true;
    }

    if(!isFileAttachmentSizeValid(task.fileAttachments,files)){
        errors = true;
        showToastError("Total file attachment size exceeded!");
    }

    for (const [k,v] of Object.entries(taskModel)){
        if(k === "title" && v.trim() === ""){
            showToastError("Title cannot be empty field!");
            errors = true;
        }
        if(k === "description" && v.trim() === ""){
            showToastError("Description field cannot be empty!");
            errors = true;
        }
    }
    let formData = new FormData();
    formData.append("taskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    if(!errors){
        $.ajax({
            type:"PUT",
            url:"/api/task/"+taskId,
            contentType: false,
            processData: false,
            data:formData,
            success: function(){
                console.log("Updated!");
                const task = getTaskById(taskId);
                $("#taskTitle").attr("contenteditable",false);
                $("#taskTitle").text(taskModel["title"]);
                $("#taskBody").html(getTaskDetailBody(task));
                $("#taskDescription").text(taskModel["description"]);
                updateTaskCard(task);
                $("#taskCardDetails").text("Details 📝: " +taskModel["description"].slice(0,150)+"...");
                initEditDropzone();
                files = [];
                showToastSuccess("Changes have been saved!");
                toggleEditButtons();
            },
            error: function (){
                showToastError("Server Error Occurred!");
            }
        });
    }
}

/**
 * Saves a group tasks updated details
 */
function saveGroupTaskChanges(){
    let taskModel = createUpdatedTaskModel();
    const taskId = $("#footer").data("tasks");
    const task = getGroupTaskById(taskId);

    let errors = false;
    if(hasDateErrors(taskModel)){
        showToastError("Start date as to be before the end date!");
        errors = true;
    }

    if(!isFileAttachmentSizeValid(task.fileAttachments,files)){
        errors = true;
        showToastError("Total file attachment size exceeded!");
    }


    for (const [k,v] of Object.entries(taskModel)){
        if(k === "title" && v.trim() === ""){
            showToastError("Title cannot be empty field!");
            errors = true;
        }
        if(k === "description" && v.trim() === ""){
            showToastError("Description field cannot be empty!");
            errors = true;
        }
    }
    let formData = new FormData();
    formData.append("groupTaskDTO", new Blob([JSON.stringify(taskModel)], { type: "application/json" }));
    if (files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append("fileAttachments", files[i], files[i].name);
        }
    }

    if(!errors){
        $.ajax({
            type:"PUT",
            url:"/api/task/group/"+taskId,
            contentType: false,
            processData: false,
            data:formData,
            success: function(){
                console.log("Updated!");
                const task = getGroupTaskById(taskId);
                $("#taskTitle").attr("contenteditable",false);
                $("#taskTitle").text(taskModel["title"]);
                $("#taskBody").html(getTaskDetailBody(task));
                $("#taskDescription").text(taskModel["description"]);
                updateTaskCard(task);
                initEditDropzone();
                files = [];
                showToastSuccess("Changes have been saved!");
                toggleEditButtons();
            },
            error: function (){
                showToastError("Server Error Occurred!");
            }
        });
    }

}

/**
 *Makes ajax call to update the status of a task
 * @param type
 * @param id
 * @param inProgress
 * @param complete
 * @param progress
 */
function changeTaskStatus(type,id,inProgress,complete,progress){
    $.ajax({
        type:"PATCH",
        url:"/api/task/"+id+"/set-status",
        data:{
            "inProgress": inProgress,
            "complete":complete,
            "type":type
        },
        success: function (){
            showToastSuccess("Task status changed!");
            $("#buttons").html("");
            $("#buttons").append(buttonStart(id,inProgress,complete,type));
            $("#buttons").append(buttonComplete(id,complete,type));
            $("#taskStatus").text(getStatus(inProgress,complete));
            if(progress != null){
                $("#progress").html(getProgressBar(progress));
            }
            const task = type === "task" ? getTaskById(id) : getGroupTaskById(id);
            updateTaskCard(task);
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}


/**
 * Handles logic for starting tasks
 */
$(document).on("click", "#startTask",function(){
    const taskId = $(this).data("task");
    const isInProgress = $(this).data("inprogress");
    const type = $(this).data("type");
    const complete = false;
    let progress = null;
    if(!isInProgress && !complete){
        progress = 0;
    }
    changeTaskStatus(type,taskId,isInProgress,complete,progress);
});

/**
 * Handles logic for completing tasks
 */
$(document).on("click","#completeTask", function (){
    const taskId = $(this).data("task");
    const complete = $(this).data("complete");
    const type =  $(this).data("type");
    const isInProgress = (complete ? false: true);
    let progress;
    if(!isInProgress && complete){
        progress = 100;
    }
    changeTaskStatus(type,taskId,isInProgress,complete,progress);
});

/**
 * Handles whenever a user makes search requests and displays results
 */
$("#searchTasks").on("submit", function(event){
    event.preventDefault();
    if(personalType === "all"){
        $.ajax({
            type:"GET",
            url:"/api/task/search",
            data:{
                "search": $("#searchInput").val(),
            },
            success: function(res){
                renderAllTasks(res);
                showToastSuccess("Tasks matching search input loaded!");
            },
            error: function(){
                $("#tasks").html(`<h1 class="text-muted m-3">No results!</h1>`)
                showToastInfo("No tasks matching search found!");
            }
        });
    }else if(personalType === "cat"){
        $.ajax({
            type:"GET",
            url:"/api/task/search/category",
            data:{
                "search": $("#searchInput").val(),
                "catId": catId,
            },
            success:function (res){
                renderTasksForCat(res);
                showToastSuccess("Tasks matching search input loaded!");
            },
            error: function (){
                $("#tasks").html(`<h1 class="text-muted m-3">No results!</h1>`)
                showToastInfo("No tasks matching search found!");
            }
        });
    }else if(group === "group"){
        $.ajax({
            type: "GET",
            url: "/api/task/group/search",
            data: {
                "search": $("#searchInput").val(),
            },
            success: function (res) {
                renderGroupTasks(res);
                showToastSuccess("Group Tasks matching search input loaded!");
            },
            error: function () {
                $("#tasks").html(`<h1 class="text-muted m-3">No results!</h1>`)
                showToastInfo("No Group Tasks matching search found!");
            }
        });
    }else{
        $.ajax({
            type:"GET",
            url:"/api/task/archived/search",
            data: {
                "query": $("#searchInput").val(),
            },
            success: function (res){
                renderAllTasks(res);
                showToastSuccess("Archived Tasks matching search input loaded!");
            },
            error: function () {
                $("#tasks").html(`<h1 class="text-muted m-3">No results!</h1>`)
                showToastInfo("No Archived Tasks matching search found!");
            }
        });
    }
});

/**
 * If input field for search is reset then it will render all tasks again
 */
$("#searchInput").on("input", function(){
    const inputVal = $(this).val();
    if(inputVal.trim() == ""){
        if(personalType === "all"){
            renderAllTasks(getAllPersonalTasks());
        }else if(personalType === "cat"){
            renderTasksForCat(getCategoryById(catId).tasks);
        }else if(group === "group"){
            renderGroupTasks(getAllGroupTasks());
        }else{
            renderAllTasks(getAllArchivedTasks());
        }
    }
});