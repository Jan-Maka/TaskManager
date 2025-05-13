//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Get the current user logged in
const user = $('#_principal').data("principal");

let drawItems = [];
const tasks = getUserTasks();
const groupTasks = getUserGroupTasks();
const assignments = getUserAssignments();
const studySessions = getUserStudySessions();

/**
 * For calendar view tasks or assignments that are red:"not started", light-blue:"in progress" and green: "completed"
 * @param model
 * @returns {string}
 */
function getColour(model){
    if(model.inProgress){
        return "light-blue";
    }else if(model.complete){
        return "green";
    }
    return "red";
}

/**
 * Gets the colour of an event is its in between the current data then it will be blue
 * if current date is after the end date then will return green else red
 * @param model
 * @returns {string}
 */
function getColourForEvents(model){
    if(new Date(model.startDate).getDate() <= new Date().getDate() <= new Date(model.endDate).getDate()){
        return "light-blue";
    }else if(new Date(model.endDate).getDate() < new Date().getDate()){
        return "green";
    }
    return "red";
}

/**
 * Populate the calendar with tasks/group-tasks/assignments/study-sessions
 */
tasks.forEach((task) => {
    const data = task;
    data.title = "Task: "+data.title;
    data.start = new Date(data.startDate);
    data.end = new Date(data.endDate);
    data.color = getColour(task);
    data.type = "task";
    if(!task.complete){
        data.editable = true;
    }
    drawItems.push(data);
});

groupTasks.forEach((task) => {
    const data = task;
    data.title = "Group Task: "+data.title;
    data.start = new Date(data.startDate);
    data.end = new Date(data.endDate);
    data.color = getColour(task);
    data.type = "groupTask";
    if(userRoles.includes("MEMBER") || !task.complete) data.editable = true;
    drawItems.push(data);
});

assignments.forEach((ass) => {
    const data = ass;
    data.title = "Assignment: "+data.title;
    data.start = new Date(data.startDate);
    data.end = new Date(data.endDate);
    data.color = getColour(ass);
    data.type = "assignment";
    if(!ass.complete){
        data.editable = true;
    }
    ass.tasks.forEach((task) => {
        const data = task;
        data.title = "Assignment Task: "+data.title;
        data.start = new Date(data.startDate);
        data.end = new Date(data.endDate);
        data.color = getColour(task);
        data.type = "assignmentTask";
        if(!task.complete){
            data.editable = true;
        }
        drawItems.push(data);
    });
    drawItems.push(data);
});

studySessions.forEach((session) => {
    const data = session;
    data.title = "Study Session: "+data.title;
    data.start = new Date(data.startDate);
    data.end = new Date(data.endDate);
    data.color = getColourForEvents(session);
    data.type = "session";
    if(data.organiser === user && userRoles.includes("MEMBER")) data.editable = true;
    drawItems.push(data);
});

/**
 * Gets all the users tasks
 * @returns {*[]}
 */
function getUserTasks(){
    let tasks = [];
    $.ajax({
        type:"GET",
        url:"/api/task/user/tasks",
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
 * Gets all the users group tasks
 */
function getUserGroupTasks(){
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
 * Gets all of the users assignments
 * @returns {*[]}
 */
function getUserAssignments(){
    let assignments = [];
    $.ajax({
        type:"GET",
        url:"/api/assignments",
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
 * Gets all of the users study sesions
 */
function getUserStudySessions(){
    let studySessions = [];
    $.ajax({
        type:"GET",
        url:"/api/events/study-sessions",
        async: false,
        dataType: "json",
        success: function (res){
            studySessions = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return studySessions;
}

/**
 * Helper function get user body elements for a task or assignment
 * @param users
 * @returns {string}
 */
function getUsersBody(users){
    let userList = "";
    users.forEach((u) => {
        userList += `<a id="user-added-${u.id}" data-user="${u.id}" class="border-bottom d-flex align-items-center highlight-card" href="/social/profile/${u.username}" style="text-decoration: none;">
                           ${getUserPfpBody(u.profilePic)}
                           <h4 class="m-0 fw-bold">@${u.username}</h4>
                        </a>`;
    });
    return `<div class="border border-3 rounded overflow-y-auto mb-2" id="users-to-add" style="max-height: 150px;">
                ${userList}
            </div>`;
}

/**
 * Helper function to get body that contains tasks an assignment has
 * @param tasks
 */
function assignmentTaskBody(tasks){
    let taskList = "";
    if(tasks.length === 0){
        taskList = `<h5 class="m-2">No Tasks for Assignment!</h5>`;
    }
    tasks.forEach((task) => {
        taskList += `<a class="card highlight-card" href="/academic/assignments/${task.assignment}/tasks/${task.id}" style="text-decoration: none;">
                        <div class="m-2">
                            <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                            <p class="card-text">📆: ${dateDetails(task)}</p>  
                        </div>
                     </a>`;
    });
    return `<div class="border border-3 rounded overflow-auto mb-2" style="max-height: 150px;">
                ${taskList}
            </div>`;
}

/**
 * Gets google maps iframe of in person location inputted
 * @param location
 * @returns {string}
 */
function mapLocation(location){
    const locationQuery = location.replace(/\s/g, '%20');
    return `<iframe width="100%" height="350px" id="gmap_canvas" src="https://maps.google.com/maps?q=${locationQuery}&t=&z=13&ie=UTF8&iwloc=&output=embed"></iframe>`;
}

/**
 * If a user clicks on task on the calendar this will generate the details body for the modal
 * @param model
 * @returns {string}
 */
function getTaskDetailBody(model,id){
    let catBody = "";
    let userBody = "";
    let assignmentBody = "";
    if(model.category){
        catBody = `<h3 class="text-muted">Category 📚: ${model.categoryName}</h3>`;
        $("#link").attr("href", `/tasks/personal/${id}`);
    }
    if(model.users){
        userBody = `<h5>Users on Task 🕴️</h5>
                    ${getUsersBody(model.users)}`;
        if(!model.assignment)$("#link").attr("href", `/tasks/group/${id}`);
    }
    if(model.assignment){
        assignmentBody = `<h3 class="text-muted">For 📒: ${model.assignmentName}</h3>`;
        $("#link").attr("href", `/academic/assignments/${model.assignment}/tasks/${id}`);
    }
    return `<div>
                ${catBody}
                ${assignmentBody}
                <h4>Progress 📊 | Status: ${getStatus(model.inProgress,model.complete)}</h4>
                ${getProgressBar(model.progress)}
                <h5 class="mb-3">Type 🚨: ${getType(model)}</h5>
                <div class="d-flex align-items-center mb-2">
                    <h5>Date Details 📅: </h5>
                    <p class="mb-2 ms-1">${dateDetails(model)}</p>
                </div>
                <div class="d-flex align-items-center">
                    <h5>Workload Rating ⚖️:</h5>
                    ${getWorkloadRating(model.workload)}
                </div>
                <div class="d-flex align-items-center">
                    <h5>Affect on Mood 🎭:</h5>
                    ${getMoodRating(model.mood)}
                </div>
                <h5>Details 📝</h5>
                <div class="border border-3 rounded mb-2">
                    <p class="m-2" id="description"></p>
                </div>
                ${userBody}
                <h5>File Attachments 🗃️</h5>
                ${getFileAttachmentBody(model.fileAttachments)}
            </div>`;
}

/**
 * Used to show assignment details
 * @param model
 * @returns {string}
 */
function getAssignmentBody(model,id){
    $("#link").attr("href", `/academic/assignments/${id}`);
    return `<div>
                <h3 class="text-muted">Course: ${model.course}</h3>
                <h4>Progress 📊| Status: ${getStatus(model.inProgress,model.complete)}</h4>
                ${getProgressBar(model.progress)}
                 <h5 class="mb-3">Type 🚨: ${getType(model)}</h5>
                <div class="d-flex align-items-center mb-2">
                    <h5>Date Details 📅: </h5>
                    <p class="mb-2 ms-1">${dateDetails(model)}</p>
                </div>
                 <h5>Details 📝</h5>
                <div class="border border-3 rounded mb-2">
                    <p class="m-2" id="assDescription"></p>
                </div>
                <h5>Users on Assignment 🕴️</h5>
                ${getUsersBody(model.users)}
                <h5>Tasks 📃</h5>
                ${assignmentTaskBody(model.tasks)}
                <h5>File Attachments 🗃️</h5>
                ${getFileAttachmentBody(model.fileAttachments)}
            </div>`;
}

function getStudySessionBody(session,id){
    let location = `<svg width="100%" height="350px" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg" stroke-width="3" stroke="#000000" fill="none"><path d="M50.28,23.29V43.94a1.74,1.74,0,0,1-1.74,1.74H11.3a1.74,1.74,0,0,1-1.74-1.74V17.85a1.74,1.74,0,0,1,1.74-1.74H37.07"/><line x1="9.56" y1="39.19" x2="50.28" y2="39.19"/><path d="M22.23,52.54a5.72,5.72,0,0,0,3-6.86"/><path d="M38.38,52.54a5.73,5.73,0,0,1-3.05-6.86"/><line x1="17.45" y1="52.54" x2="42.39" y2="52.54" stroke-linecap="round"/><circle cx="22.13" cy="25.21" r="3.53"/><path d="M29.28,39.19a7.15,7.15,0,0,0-7.15-7.14h0A7.14,7.14,0,0,0,15,39.19Z"/><path d="M53.58,23.29h-8.4L40.1,26.88a.09.09,0,0,1-.14-.07l0-3.52H37.93a.87.87,0,0,1-.86-.86V12.32a.86.86,0,0,1,.86-.86H53.58a.86.86,0,0,1,.86.86V22.43A.87.87,0,0,1,53.58,23.29Z"/></svg>`;
    let locationStr = `<a href="${session.location}">${session.location}</a>`;
    $("#link").attr("href", `/academic/study-sessions/${id}`);
    if(!session.online){
        locationStr = session.location;
        location = `<div class="border border-3 border-info rounded">
                        ${mapLocation(locationStr)}
                    </div>`;
    }
    let assignmentName = "";
    let owner = "";
    if(session.assignmentName != null){
        assignmentName = `<div class="d-flex">
                            <p class="fw-bold mx-2">For Assignment 📒:</p>
                            <p id="forAssignment"></p>
                          </div>`;
        owner = `<hr>
                 <div id="sessionOrganiser">
                    <p class="my-2 fw-bold">Organiser 👤: </p>
                    <p>${session.user}</p>
                 </div> `;
    }
    return `<div>
                ${assignmentName}
                ${location}
                <hr>
                <p id="sessionDate" class="fw-bold my-2">
                    ${dateDetails(session)} 📅
                </p>
                <hr>
                <p class="my-2 fw-bold">Location 📍: </p>
                <p class="my-2">${locationStr}</p>
                <hr>
                <p class="my-2 fw-bold">Details 📝: </p>
                <p id="session-details"></p>
                ${owner}  
            </div>`;
}

/**
 * Works out the day difference between two dates as dragging an item on teh calendar that doesn't last multiple days doesn't set an end date.
 * @param a
 * @param b
 * @returns {number}
 */
function dateDiffInDays(a, b) {
    const MS_PER_DAY = 1000 * 60 * 60 * 24;
    const utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
    const utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());
    return Math.floor((utc2 - utc1) / MS_PER_DAY);
}

/**
 * Whenever use drags or resizes anything this will call api to update the dates
 * @param data
 * @param type
 */
function updateDates(data,type){
    //For whenever a single day event is dropped on month view
    let end = new Date(data.end);
    if(data.end === null){
        let endDate = new Date(data.extendedProps.endDate);
        let dateDiff = dateDiffInDays(endDate,new Date(data.start));
        endDate.setDate(endDate.getDate() + dateDiff);
        end = endDate;
    }
    $.ajax({
        url:"/api/events/calendar",
        type:"PATCH",
        data:{
            "start":new Date(data.start),
            "end":end,
            "type":type,
            "id":data.id
        },
        success:function (){
            showToastSuccess("Dates changed for "+type+"!");
        },
        error: function (){
            showToastError("Server Error Occurred!");
        }
    });
}

/**
 * Rendering of calendar and also has handling of calendar events
 * @type {HTMLElement}
 */
const calendarEl = document.getElementById('calendar');
const calendar = new FullCalendar.Calendar(calendarEl,{
    themeSystem: 'bootstrap5',
    height: '100%',
    expandRows: true,
    dayMaxEvents:true,
    selectable:true,
    nowIndicator: true,
    headerToolbar: {
        start: 'dayGridMonth,timeGridWeek,timeGridDay',
        center: 'title',
        end: 'prevYear,prev,next,nextYear'},
    events:drawItems,
    eventClick: (info) => {
        const eventClicked = info.event;
        let data = {...eventClicked.extendedProps};
        const header = `<h1 class="modal-title fs-5 text-white" id="title" contenteditable="false"></h1>
                        <a class="mx-2" id="link"><i class="bi bi-arrow-up-right-square text-white"></i></a>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>`;
        $("#detailsHeader").html(header);
        $("#title").text(eventClicked.title);
        data["startDate"] = new Date(eventClicked.start);
        if(eventClicked.end !== null){
            data["endDate"] = new Date(eventClicked.end);
        }else{
            let endDate = new Date(data.endDate);
            const dateDiff = dateDiffInDays(endDate,new Date(eventClicked.start));
            endDate.setDate(endDate.getDate() + dateDiff);
            data["endDate"] = endDate;
        }
        switch (data.type){
            case "task":
            case "groupTask":
            case "assignmentTask":
                $("#detailsBody").html(getTaskDetailBody(data,eventClicked.id));
                $("#description").text(data.description);
                break;
            case "assignment":
                $("#detailsBody").html(getAssignmentBody(data,eventClicked.id));
                $("#assDescription").text(data.description);
                break;
            case "session":
                $("#detailsBody").html(getStudySessionBody(data,eventClicked.id));
                $("#forAssignment").text(data.assignmentName);
                $("#session-details").text(data.info);
                break;
        }
        $("#detailsModal").modal("toggle");
    },
    eventDrop: function(info){
        const data = info.event;
        const type = data.extendedProps.type;
        let errors = false;
        if(type === "assignmentTask"){
            errors = checkTaskDateErrorsAgainstAssignmentForCalendar(data,{...data.extendedProps});
        }
        if(!errors){
            updateDates(data,type);
        }else{
            setInterval(refreshPage,5000);
        }
    },
    eventResize: function (info){
        const data = info.event;
        const type = data.extendedProps.type;
        let errors = false;
        if(type === "assignmentTask"){
            errors = checkTaskDateErrorsAgainstAssignmentForCalendar(data,{...data.extendedProps});
        }
        if(!errors){
            updateDates(data,type);
        }else{
            setInterval(refreshPage,5000);
        }
    }
});

/**
 * Renders page if a date error occurred for assignment Tasks
 */
function refreshPage(){
    window.location.reload();
}

/**
 * Checks assignment task date event resize is valid against assignment it's a part of
 * @param data
 * @param model
 * @returns {*}
 */
function checkTaskDateErrorsAgainstAssignmentForCalendar(data,model){
    let assignment = {};
        assignments.forEach((ass) => {
        if(ass.id === model.assignment){
            assignment = ass;
        }
    });
    model["startDate"] = new Date(data.start);
    if(data.end === null){
        model["endDate"] = new Date(data.start);
    }else{
        model["endDate"] = new Date(data.end);
    }
    if(taskDateHasErrorsWithAssignmentDates(model,assignment)){
        showToastError("Task start and end date must be between assignment start and due dates!");
    }
    return taskDateHasErrorsWithAssignmentDates(model,assignment);
}

calendar.render();