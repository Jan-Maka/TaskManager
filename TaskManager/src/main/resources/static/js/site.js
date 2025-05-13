/**
 * This file is mainly used as a way to minimize code that may be re-used since this script is used on every page.
 */

$(document).ready(function () {
    $('#navbar-logout-btn').click(function (e) {
        e.preventDefault();
        $('#navbar-logout-form').submit();
    });
});

function showToastSuccess(msg) {
    iziToast.show({
        title: "Success",
        titleSize: '18',
        message: msg,
        messageSize: '18',
        position: 'bottomRight',
        color: '#1C69F9',
        transitionIn: 'bounceInRight',
        progressBar: true,
        progressBarColor: 'black',
        icon: 'bi bi-exclamation-circle',
        theme: 'dark',
        drag: true,
    });
}

function showToastInfo(msg) {
    iziToast.show({
        title: "Info",
        titleSize: '18',
        message: msg,
        messageSize: '18',
        position: 'bottomRight',
        color: '#F4FC05',
        transitionIn: 'bounceInRight',
        progressBar: true,
        progressBarColor: 'black',
        icon: 'bi bi-info-circle-fill',
        theme: 'light',
        drag: true,
    });
}

function showToastError(msg) {
    iziToast.show({
        title: "Error",
        titleSize: '18',
        message: msg,
        messageSize: '18',
        position: 'bottomRight',
        color: '#dc3545',
        transitionIn: 'bounceInRight',
        progressBar: true,
        progressBarColor: 'black',
        icon: 'bi bi-exclamation-circle-fill',
        theme: 'dark',
        drag: true,
    });
}

const userRoles = $('#_user_roles').data("roles").split(",");
let maxFileSize = 0;
if(userRoles.includes("MEMBER")){
    maxFileSize = 5 * (1024 * 1024);
}else{
    maxFileSize = 2 * (1024 * 1024);
}


/**
 * Helper function to get the time for a date
 * @param date
 * @returns {string}
 */
function getFormattedTime(date) {
    let d = new Date(date).toLocaleTimeString();
    if(d.includes("M")){
        return d.slice(0, -6);
    }
    return d.slice(0, -3);
}

/**
 * Helper function to get date string in a particular way
 * @param model
 * @returns {string}
 */
function dateDetails(model){
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
 * Helper function used for labels for x-axis on a chart
 * @returns {string[]}
 */
function getWeekDaysFromNow(){
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const today = new Date().getDay();
    return days.slice(today).concat(days.slice(0, today));
}

/**
 * Helper function used for labels for z-axis on a chart
 * @returns {string[]}
 */
function getPreviousDaysFromNow(){
    return getWeekDaysFromNow().reverse();
}

/**
 * Helper function used to get the status of something
 * @param model
 * @returns {string}
 */
function getStatus(inProgress,complete){
    if(inProgress){
        return `🚧`;
    }else if(complete){
        return `✅`;
    }else{
        return `❌`;
    }
}

/**
 * Helper function to create the progress bar for
 * assignments or tasks.
 * @param model
 * @returns {string}
 */
function getProgressBar(progress){
    return `<div class="mb-2">
                 <div class="progress">
                     <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-label="Example with label" style="width: ${progress}%;" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100">${progress}%</div>
                 </div>
             </div>`;
}

/**
 * Helper function to return in string form what importance is a task or assignment
 * @param model
 * @returns {string}
 */
function getType(model){
    if(model.urgent && model.important){
        return "Urgent/Important 🔥";
    }else if(model.urgent && !model.important){
        return "Urgent ⏰";
    }else if(model.important){
        return "Important ‼";
    }else{
        return "Not important or Urgent 🛏️";
    }
}

/**
 * Used to reperesnt workload rating using emojis
 * @param workload
 * @returns {string}
 */
function getWorkloadRating(workload){
    switch (workload){
        case 0:
            return `<p class="fs-1 mb-1 ms-2">🔧</p>`;
        case 1:
            return `<p class="fs-1 mb-1 ms-2">🔨</p>`;
        case 2:
            return `<p class="fs-1 mb-1 ms-2">🛠️</p>`;
        case 3:
            return `<p class="fs-1 mb-1 ms-2">⚙</p>`;
        case 4:
            return `<p class="fs-1 mb-1 ms-2">🧰</p>`;
        case 5:
            return `<p class="fs-1 mb-1 ms-2">🛑</p>`;
    }
}

/**
 * Gets emoticon for visual representation on mood
 * @param mood
 * @returns {string}
 */
function getMoodRating(mood){
    switch (mood){
        case 0:
            return `<p class="fs-1 mb-1 ms-2">🤷</p>`;
        case 1:
            return `<p class="fs-1 mb-1 ms-2">😐</p>`;
        case 2:
            return `<p class="fs-1 mb-1 ms-2">😶</p>`;
        case 3:
            return `<p class="fs-1 mb-1 ms-2">😣</p>`;
        case 4:
            return `<p class="fs-1 mb-1 ms-2">😤</p>`;
        case 5:
            return `<p class="fs-1 mb-1 ms-2">😡</p>`;
    }
}

/**
 * Helper function used to check if there are date errors in input
 * @param taskModel
 * @returns {boolean}
 */
function hasDateErrors(model) {
    return model['startDate'] > model['endDate'];
}

/**
 * Checks if an assignment tasks dates are between the assignment start and end date.
 * @param task
 * @param assignment
 */
function taskDateHasErrorsWithAssignmentDates(task,assignment){
    const taskStartDate = new Date(task.startDate);
    const taskEndDate = new Date(task.endDate);
    const assignmentStartDate = new Date(assignment.startDate);
    const assignmentEndDate = new Date(assignment.endDate);
    if (taskStartDate < assignmentStartDate || taskStartDate > assignmentEndDate){
        return true;
    }else if(taskEndDate > assignmentEndDate || taskEndDate < assignmentStartDate){
        return true;
    }
    return false;
}


/**
 * Gets file attachment body
 * @param files
 */
function getFileAttachmentBody(files){
    let filesBody = "";
    if(files.length === 0){
        filesBody = "<h5 class='m-2'>No Files Attached!</h5>";
    }
    files.forEach((file) => {
       filesBody += `<div class="card text-dark bg-light">
                        <div class="card-body">
                            <div class="d-flex flex-column align-items-center flex-sm-row">
                                <i class="fs-3 bi bi-file-earmark-break-fill me-auto"></i>
                                <span class="fs-5">${file.title.slice(0,15)}..</span>
                                <a class="btn btn-primary ms-auto" href="/api/files/${file.id}/download">
                                    <i class="bi bi-download"></i>
                                </a>
                            </div>
                        </div>
                    </div>`;
    });
    return `<div class="border border-3 overflow-auto rounded" style="max-height: 150px;">
                ${filesBody}
            </div>`;
}

/**
 * Helper function to create the img element
 * @param pfp
 * @returns {string|string}
 */
function getUserPfpBody(pfp){
    return pfp != null ?
        `<img class="img-fluid img-thumbnail rounded-circle m-1" src="${pfp}" alt="profile picture" style="height: 60px; width: 60px;">`:
        `<img class="img-fluid img-thumbnail rounded-circle m-1" src="/images/defaultPfp.jpg" alt="profile picture" style="height: 60px; width: 60px;">`;
}

/**
 * Helper function used to get a users pfp based on their user id
 * @param userId
 * @returns {*}
 */
function getUserPfpString(userId){
    let pfp = null;
    $.ajax({
        type:"GET",
        url:"/api/user/"+userId+"/pfp",
        async: false,
        contentType: "application/json; charset=utf-8",
        success: function (res){
            if(res.trim() !== ""){
                pfp = res;
            }
        }
    });
    return pfp;
}

/**
 * Helper function used to check if a file is of valid size (2MB or 5MB)
 * @param file
 * @returns {boolean}
 */
function isFileValidSize(file){
    const fileSize = file.size;
    if(fileSize > maxFileSize){
        return false;
    }
    return true;
}

/**
 * Helper function to check if file attachments of a task are what they are supposed to be
 * @param attachments
 * @param filesToAttach
 */
function isFileAttachmentSizeValid(attachments, filesToAttach){
    let totalSizeOfAttachments = 0;
    let totalSizeOfFilesToAttach = 0;
    attachments.forEach((file) => totalSizeOfAttachments += file.size);
    filesToAttach.forEach((file) => totalSizeOfFilesToAttach += file.size);
    if((totalSizeOfAttachments+totalSizeOfFilesToAttach) <= maxFileSize){
        return true;
    }
    return false;

}
/**
 * Helper function used when editing a task to show workload ratings
 * @param workloadRating
 * @returns {string}
 */
function getWorkloadEmoticons(workloadRating){
    let emoticons = "";
    const workloads = ["🔧","🔨","🛠️","⚙","🧰","🛑"];
    for (let i = 0; i < workloads.length; i++) {
        if(i === workloadRating){
            emoticons += `<span class="ms-4 fs-1 workload-emoticon enlarged" data-rating="${i}">${workloads[i]}</span>`;
            continue;
        }
        emoticons += `<span class="ms-4 text-muted fs-1 workload-emoticon" data-rating="${i}">${workloads[i]}</span>`;
    }
    return emoticons;
}


/**
 * Helper function used when editing a task to show mood ratings
 * @param moodRating
 * @returns {string}
 */
function getEmoticons(moodRating){
    let emoticons = "";
    const moods = ["🤷‍","😐","😶","😣","😤","😡"];
    for (let i = 0; i < moods.length; i++) {
        if(i === moodRating){
            emoticons += `<span class="ms-4 fs-1 emoticon enlarged" data-rating="${i}">${moods[i]}</span>`;
            continue;
        }
        emoticons += `<span class="ms-4 text-muted fs-1 emoticon" data-rating="${i}">${moods[i]}</span>`;
    }
    return emoticons;
}

/**
 * Gets minute difference between two dates
 * @param start
 * @returns {number}
 */
function getMinutesDifference(start){
    let currentDate = new Date();
    const currentTimestamp = currentDate.getTime();
    const startTimestamp = start.getTime();
    const diffInMilliseconds = Math.abs(currentTimestamp - startTimestamp);
    const minutes = Math.floor(diffInMilliseconds / (1000 * 60));
    return minutes;
}

/**
 * Helper click event for when user is selecting a mood or workload rating
 */
$(document).on("click", ".emoticon", function (){
   $(".emoticon").removeClass("enlarged");
    $(".emoticon").addClass("text-muted");
   $(this).addClass("enlarged");
   $(this).removeClass("text-muted");
});

$(document).on("click", ".workload-emoticon", function (){
    $(".workload-emoticon").removeClass("enlarged");
    $(".workload-emoticon").addClass("text-muted");
    $(this).addClass("enlarged");
    $(this).removeClass("text-muted");
});

/**
 * Creates the onscreen notification for a task/assignment/study-session
 * @param model
 */
function createNotificationHTML(notification){
    let taskBody = "";
    notification.tasks.forEach((task) => {
        taskBody += `<a class="m-2 card highlight-card" href="/tasks/personal/${task.id}" style="text-decoration: none">
                            <div class="card-body">
                                <h5 class="card-title">📝 ${task.title}</h5>
                                <p class="card-text">📆: ${dateDetails(task)}</p>
                                <p class="text-muted card-text">🚨 Minutes from start: ${getMinutesDifference(new Date(task.startDate))}</p>  
                            </div>
                        </a>`;
    });
    let groupTaskBody = "";
    notification.groupTasks.forEach((task) => {
        groupTaskBody += `<a class="m-2 card highlight-card" href="/tasks/group/${task.id}" style="text-decoration: none">
                                <div class="card-body">
                                    <h5 class="card-title">🕴️ ${task.title}</h5>
                                    <p class="card-text">📆: ${dateDetails(task)}</p>  
                                    <p class="text-muted card-text">🚨 Minutes from start: ${getMinutesDifference(new Date(task.startDate))}</p>  
                                </div>
                            </a>`;
    });

   let assignmentBody = "";
   notification.assignments.forEach((assignment) => {
       assignmentBody +=  `<a class="m-2 card highlight-card" href="/academic/assignments/${assignment.id}" style="text-decoration: none">
                            <div class="card-body">
                                <h5 class="card-title">📒️ ${assignment.title}</h5>
                                <p class="card-text">📆: ${dateDetails(assignment)}</p>
                                <p class="text-muted card-text">🚨 Minutes from start: ${getMinutesDifference(new Date(assignment.startDate))}</p>  
                            </div>
                           </a>`;
   });
   let assignmentTaskBody = "";
   notification.assignmentTasks.forEach((task) => {
       assignmentTaskBody += `<a class="m-2 card highlight-card" href="/academic/assignments/${task.assignment}/task/${task.id}" style="text-decoration: none">
                                <div class="card-body">
                                    <h5 class="card-title">📃 ${task.title}</h5>
                                    <p class="card-text">📆: ${dateDetails(task)}</p>
                                    <p class="text-muted card-text">🚨 Minutes from start: ${getMinutesDifference(new Date(task.startDate))}</p>  
                                </div>
                              </a>`;
   });
   let studySessionBody = "";
   notification.studySessions.forEach((session) => {
      studySessionBody += `<a class="m-2 card highlight-card" href="/academic/study-sessions/${session.id}" style="text-decoration: none">
                            <div class="card-body">
                                <h5 class="card-title">🎓 ${session.title}</h5>
                                <p class="card-text">📆: ${dateDetails(session)}</p>
                                <p class="text-muted card-text">🚨 Minutes from start: ${getMinutesDifference(new Date(session.startDate))}</p>  
                            </div> 
                           </a>`;
   });

    return `<div class="notification-box">
                <div class="notification-header">
                    <h5>Upcoming ⏰!</h5>
                    <button type="button" class="btn-close ms-auto dismiss-notification"  aria-label="Close"></button>
                </div>
                <div class="notification-content">
                    ${taskBody}
                    ${groupTaskBody}
                    ${assignmentBody}
                    ${assignmentTaskBody}
                    ${studySessionBody}
                </div>
            </div>`;
}

/**
 * If user has in system notifications on this will make calls every 15 minutes
 * for things to be started in 30 minutes
 */
if($("#notificationArea").length !== 0){
    setInterval(checkNotifications,900000);
    function checkNotifications(){
        $.ajax({
            type:"POST",
            url:"/api/user/notifications",
            statusCode:{
                200: function (res){
                    $("#notificationArea").append(createNotificationHTML(res));
                },
                204: function (){
                }
            }
        });
    }
}

/**
 * Removes notification of page
 */
$(document).on("click", ".dismiss-notification", function (){
    $(this).parent().parent().remove();
});

/**
 * Checks if task/assignments that are not complete end date is before current date
 * @param model
 */
function isOverdue(model){
    if(model.complete){
        return false;
    }
    return new Date(model.endDate) < new Date();
}