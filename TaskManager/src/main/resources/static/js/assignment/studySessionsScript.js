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

/**
 * Makes API call for getting study sessions in date range from the Flatpickr
 * calendar
 * @param selectedDates
 * @returns {*[]}
 */
function getStudySessionsForDateRange(selectedDates){
    let sessions = [];
    $.ajax({
        type:"GET",
        url:"/api/events/study-sessions/for-dates",
        contentType: "application/json; charset=utf-8",
        async: false,
        data:{
            "date1": new Date(selectedDates[0]),
            "date2": new Date(selectedDates[1])
        },
        success: function(res){
            sessions = res;
        },
        error: function(){
            console.log("Error!");
        }
    });
    return sessions;
}

/**
 * Gets the body of a study-session card
 * @param session
 * @returns {string}
 */
function getStudySessionCard(session){
    return `<div class="card highlight-card m-3 fade-left session" data-session="${session.id}">
              <div class="card-body">
                <h4 id="sessionTitle" class="card-title">${session.title}</h4>
                <h6 id="sessionDates" class="card-text">Date 📅: ${dateDetails(session)}</h6>
                <hr>
                <h6 id="sessionLocation" class="card-text">Location 📍: ${session.location}</h6>
             </div>
            </div>`;
}

/**
 * Updates a study session card if its on displayed after its been updated
 * @param session
 */
function updateStudySessionCard(session){
    const target = $(".session[data-session="+session.id+"]");
    target.find("#sessionTitle").text(session.title);
    target.find("#sessionDates").text("Date 📅: "+ dateDetails(session));
    target.find("#sessionLocation").text("Location 📍: "+session.location);
}

/**
 * Renders study sessions for the dates selected
 * @param selectedDates
 * @param dateStr
 */
function renderStudySessionsForDateSelected(selectedDates, dateStr){
    $("#studySessions").html("");
    $("#dateTitle").text("Study Sessions For: "+dateStr);
    const sessions = getStudySessionsForDateRange(selectedDates);
    if(sessions.length !== 0){
        sessions.forEach((session) => {
            $("#studySessions").append(getStudySessionCard(session));
        });
        showToastSuccess("Study sessions found for date range!");
    }else{
        $("#studySessions").html(`<h4 class="fade-left m-2">No study sessions found for this date range!</h4>`);
        showToastError("No Study sessions found for the date range!");
    }

}

const chartEl = $("#sessionChart");

/**
 * Makes API call to get all the data needed for bar chart
 * @returns {*[]}
 */
function getChartData(){
    let data = [];
    $.ajax({
        type:"GET",
        url:"/api/events/study-sessions/count",
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

let chart = new Chart(chartEl, {
    type: 'bar',
    data: {
        labels: getWeekDaysFromNow(),
        datasets: [{
            label: '# of Study Sessions',
            data: getChartData(),
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
        plugins: {
            title: {
                display: true,
                text: 'Number of Study Sessions Over the Next 7 Days',
                font: {
                    size: 16
                }
            }
        }
    }
});

let dateRangeSelected = [new Date().setHours(0, 0, 0, 0), new Date().setHours(23, 59, 59, 999)];

$("#calendar-input").flatpickr({
    dateFormat: "Y-m-d H:i",
    mode: "range",
    dateFormat: "Y-m-d",
    inline: true,
    weekNumbers: true,
    onChange: function(selectedDates, dateStr, instance){
        if(selectedDates.length === 2){
            dateRangeSelected = selectedDates;
            renderStudySessionsForDateSelected(selectedDates,dateStr);
        }
    }
});

/**
 * Gets a study session details from the id
 * @param id
 * @returns {{}}
 */
function getSessionById(id){
    let session = {};
    $.ajax({
        type:"GET",
        url:baseUrlWithoutPath+"/api/events/study-sessions/"+id,
        async: false,
        dataType: 'json',
        success: function (res){
            session = res;
        },
        error: function (){
            console.log("Not Found");
        }
    });
    return session;
}


$(document).on("change","input[name='studyPurpose']", function (){
    $("#toggleAssignments").toggle();
});

/**
 * Handles the creation of a study session
 */
$("#createSession").on("submit", function(event){
    event.preventDefault();
    let sessionModel = {};
    const data = $("#createSession").serializeArray();
    const isForAssignment = $("#addAssignment").prop("checked");
    $(data).each(function(i,field){
        const key = field.name;
        const val = field.value;
        if(key === "assignment"){
            if(isForAssignment && $("#addAssignment").length !== 0){
                sessionModel[key] = $('#assignment option:selected').data("assignment");
            }
        }else if(key === "type"){
            if(val === "Online"){
                sessionModel["isOnline"] = true;
            }else{
                sessionModel["isOnline"] = false;
            }
        }else{
            sessionModel[key] = val;
        }
    });
    sessionModel["organiser"] = user;
    sessionModel["modified"] = new Date();
    if(!sessionModel.hasOwnProperty("assignment")){
        sessionModel["assignment"] = -1;
    }

    if(hasDateErrors(sessionModel["startDate"],sessionModel["endDate"])){
        showToastError("Start date has to be before the end date!");
        return;
    }
    $.ajax({
        type:"POST",
        url:baseUrlWithoutPath+"/api/events/study-sessions",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(sessionModel),
        success:function(res){
            $("#createSession")[0].reset();
            $("#toggleAssignments").hide();
            chart.data.datasets[0].data = getChartData();
            chart.update();
            if((new Date(sessionModel["startDate"]) < dateRangeSelected[1] && new Date(sessionModel["endDate"]) > dateRangeSelected[0]) || new Date(sessionModel["startDate"]) == dateRangeSelected[0]){
                $("#studySessions").prepend(getStudySessionCard(res));
            }
            showToastSuccess("Created a study session!");
        }
    });
    $("#addingSession").modal("toggle");
});

/**
 * Gets embedded google maps string
 * @param location
 * @returns {string}
 */
function mapLocation(location){
    const locationQuery = location.replace(/\s/g, '%20');
    return `<iframe width="100%" height="350px" id="gmap_canvas" src="https://maps.google.com/maps?q=${locationQuery}&t=&z=13&ie=UTF8&iwloc=&output=embed"></iframe>`;
}

/**
 * Gets the body of study session details
 * @param session
 * @returns {string}
 */
function getSessionBody(session){
    let location = `<svg width="100%" height="350px" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg" stroke-width="3" stroke="#000000" fill="none"><path d="M50.28,23.29V43.94a1.74,1.74,0,0,1-1.74,1.74H11.3a1.74,1.74,0,0,1-1.74-1.74V17.85a1.74,1.74,0,0,1,1.74-1.74H37.07"/><line x1="9.56" y1="39.19" x2="50.28" y2="39.19"/><path d="M22.23,52.54a5.72,5.72,0,0,0,3-6.86"/><path d="M38.38,52.54a5.73,5.73,0,0,1-3.05-6.86"/><line x1="17.45" y1="52.54" x2="42.39" y2="52.54" stroke-linecap="round"/><circle cx="22.13" cy="25.21" r="3.53"/><path d="M29.28,39.19a7.15,7.15,0,0,0-7.15-7.14h0A7.14,7.14,0,0,0,15,39.19Z"/><path d="M53.58,23.29h-8.4L40.1,26.88a.09.09,0,0,1-.14-.07l0-3.52H37.93a.87.87,0,0,1-.86-.86V12.32a.86.86,0,0,1,.86-.86H53.58a.86.86,0,0,1,.86.86V22.43A.87.87,0,0,1,53.58,23.29Z"/></svg>`;
    let locationStr = `<a href="${session.location}">${session.location}</a>`;
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
                            <p class="fw-bold mx-2">For Assignment:</p>
                            <p id="forAssignment"></p>
                          </div>`;
        owner = `<hr>
                 <div id="sessionOrganiser">
                    <p class="my-2 fw-bold">Organiser: </p>
                    <p>${session.user}</p>
                 </div> `;
    }

    return `<div>
                ${assignmentName}
                ${location}
                <hr>
                <p id="sessionDate" class="fw-bold my-2">
                    ${dateDetails(session)}
                </p>
                <div id="edit-session-date">
                    <input id="session-edit-startdate" class="form-select" name="startDate" type="datetime-local" placeholder="start date" required>
                    <input id="session-edit-enddate" class="form-select" name="endDate" type="datetime-local" placeholder="end date date" required>
                </div>
                <hr>
                <p class="my-2 fw-bold">Location: </p>
                <p class="my-2" contenteditable="false" id="session-detail-location">${locationStr}</p>
                <hr>
                <p class="my-2 fw-bold">Details: </p>
                <p contenteditable="false" id="session-details"></p>
                ${owner}  
            </div>`;
}

/**
 * Handles the clicking of a study session card which displays all if its details
 */
$(document).on("click", ".session", function (){
    const id = $(this).data("session");
    const session = getSessionById(id);

    const title = `<h1 id="session-title" class="modal-title fs-5 text-white" contenteditable="false"></h1>
                   <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>`;

    const body = getSessionBody(session);

    const footer = `<span class="fade-left edit-session-btn" data-session="${session.id}">
                        <i class="bi bi-pen-fill"></i>
                     </span>
                     <button class="btn btn-danger fade-left del-session-btn mx-1" style="display: none;" data-session="${session.id}">
                        <i class="bi bi-trash"></i>
                     </button>
                     <button class="btn btn-success fade-left save-session-btn mx-1" style="display: none;" data-session="${session.id}">
                        <i class="bi bi-save"></i>
                     </button>
                     <button class="btn btn-secondary fade-left cancel-session-edit mx-1" style="display: none;" data-session="${session.id}">
                        <i class="bi bi-x-square"></i>
                     </button>`;

    $("#sessionHeader").html(title);
    $("#session-title").text(session.title);
    $("#sessionBody").html(body);
    $("#forAssignment").text(session.assignmentName);
    $("#session-details").text(session.info);
    if(user === session.organiser){
        $("#sessionFooter").html(footer);
    }else{
        $("#sessionFooter").remove();
    }
    $("#sessionDetails").modal("toggle");
});

/**
 * Hnadles the edit mode of a study session
 * @param event
 */
function toggleEditSessionMode(event){
    let value = $("#session-title").attr("contenteditable");
    if(value === "false" || value === null){
        value = "true";
    }else{
        value = "false";
    }

    if(event != null){
        const sessionId = event.data("session");
        const session = getSessionById(sessionId);
        const dateTimeString = $("#sessionDate").text().split(",");
        const startTime = dateTimeString[1].trim().split(" ")[0];
        let endTime;

        if(new Date(session.endDate).getDate() === new Date(session.startDate).getDate() &&
            new Date(session.endDate).getMonth() === new Date(session.startDate).getMonth() &&
            new Date(session.startDate).getFullYear() === new Date(session.endDate).getFullYear()){
            endTime = dateTimeString[1].trim().split(" ")[2];
        } else{
            endTime = dateTimeString[1].trim().split(" ")[6];
        }

        let startYear = new Date(session.startDate).getFullYear();
        let startMonth =  new Date(session.startDate).getMonth() +1;
        let startDate = new Date(session.startDate).getDate();

        let endYear = new Date(session.endDate).getFullYear();
        let endMonth = new Date(session.endDate).getMonth() +1;
        let endDate = new Date(session.endDate).getDate();

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
        $("#session-edit-startdate").val(startDateVal);
        $("#session-edit-enddate").val(endDateVal);
    }

    $("#sessionDate").toggle();

    $("#session-title").attr("contenteditable", value);
    $("#edit-session-date").toggle();
    $("#session-detail-location").attr("contenteditable", value);
    $("#session-details").attr("contenteditable",value);

    $(".edit-session-btn").toggle();
    $(".del-session-btn").toggle();
    $(".save-session-btn").toggle();
    $(".cancel-session-edit").toggle();
}

$(document).on("click", ".edit-session-btn", function(){
    toggleEditSessionMode($(this));
});

$(document).on("click", ".cancel-session-edit", function(){
    const sessionId = $(this).data("session");
    const session = getSessionById(sessionId);
    toggleEditSessionMode();
    $("#session-title").text(session.title);
    $("#sessionBody").html(getSessionBody(session));
    $("#forAssignment").text(session.assignmentName);
    $("#session-details").text(session.info);
    showToastInfo("No changes to study session made!");
});

$(document).on("click", ".del-session-btn", function(){
    const sessionId = $(this).data("session");
    const confirmed = confirm("Are you sure you want to delete this study session?");
    if(confirmed){
        $.ajax({
            type:"DELETE",
            url:baseUrlWithoutPath+"/api/events/study-sessions/"+sessionId,
            success: function(){
                $("#sessionDetails").modal("toggle");
                $('[data-session="'+sessionId+'"]').remove();
                chart.data.datasets[0].data = getChartData();
                chart.update();
                showToastSuccess("Successfully deleted study session!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});

/**
 * If updates have been made then this will handle the event of saving updates
 */
$(document).on("click", ".save-session-btn", function(){
    let sessionModel = {};
    const sessionId = $(this).data("session");
    sessionModel["id"] = sessionId;
    sessionModel["title"] =  $("#session-title").text();
    sessionModel["startDate"] = $("#session-edit-startdate").val();
    sessionModel["endDate"] = $("#session-edit-enddate").val();
    sessionModel["location"] = $("#session-detail-location").text();
    sessionModel["info"] = $("#session-details").text();
    sessionModel["modified"] = new Date();

    let errors = false;
    if(hasDateErrors(sessionModel)){
        showToastError("Start date has to be before the end date!");
        errors = true;
    }

    for (const [k,v] of Object.entries(sessionModel)){
        if(k === "title" && v.trim() === ""){
            showToastError("Title field cannot be empty!");
            errors = true;
        }
        if(k === "info" && v.trim() === ""){
            showToastError("Description field cannot be empty!");
            errors = true;
        }
        if(k === "location" && v.trim() === ""){
            showToastError("Location field cannot be empty!");
        }
    }

    if(!errors){
        $.ajax({
            type:"PUT",
            url:"/api/events/study-sessions/"+sessionId,
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(sessionModel),
            success: function(){
                const session = getSessionById(sessionId);
                toggleEditSessionMode();
                $("#session-title").text(session.title);
                $("#sessionBody").html(getSessionBody(session));
                $("#forAssignment").text(session.assignmentName);
                $("#session-details").text(session.info);
                if($('.session[data-session="'+sessionId+'"]').length !== 0){
                    if((new Date(sessionModel["startDate"]) < dateRangeSelected[1] && new Date(sessionModel["endDate"]) > dateRangeSelected[0]) || new Date(sessionModel["startDate"]) == dateRangeSelected[0]){
                        updateStudySessionCard(session);
                    }else{
                        $('[data-session="'+sessionId+'"]').remove();
                    }
                }
                chart.data.datasets[0].data = getChartData();
                chart.update();
                showToastSuccess("Successfully updated study session!");
            },
            error: function(){
                showToastError("Server Error Occurred!");
            }
        });
    }
});