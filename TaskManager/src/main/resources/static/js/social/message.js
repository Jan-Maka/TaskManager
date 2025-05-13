//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Get the current user logged in
const user = $('#_principal').data("principal");
const username = $("#_username").data("username");

//Get current chat and type
let currentChat;
let chatType;

//File attachment for message
let file;

let stompClient = null;

/**
 * Connects user to websocket
 */
function connect() {
    let socket = new SockJS("/chat");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected);
}

/**
 * Once connected it will check for messages
 */
function onConnected(){
    stompClient.subscribe(`/user/${username}/queue/messages`, onMessageReceived);
}

/**
 * Whenever a user sends or receives a message the chat will be moved to the top
 * of the list
 * @param id
 */
function moveChatToTopFromLatestMessage(msg){
    if(msg.conversation){
        $(".conversation[data-conversation="+msg.conversation+"]").prependTo("#chats");
    }else{
        $(".group-chat[data-groupchat="+msg.groupChat+"]").prependTo("#chats");
    }
}

/**
 * Handles the event of sending messages to either a conversation or group chat
 */
function sendMessage(message){
    if(chatType === "conversation"){
        stompClient.send(`/app/chat.sendMessage/conversation/${currentChat}`,{}, JSON.stringify(message));
    }else{
        stompClient.send(`/app/chat.sendMessage/group/${currentChat}`,{}, JSON.stringify(message));
    }
    moveChatToTopFromLatestMessage(message);
    displayMessage(message);
}

/**
 * Handles what to do when receiving a message
 * @param message
 */
async function onMessageReceived(payload) {
    const msg = JSON.parse(payload.body);
    moveChatToTopFromLatestMessage(msg);
    if(msg.sender !== $("#userFriend").data("user") && msg.conversation){
        $(".conversation[data-conversation="+msg.conversation+"] .notification").removeClass("d-none");
    }else if(msg.groupChat !== $("#groupChat").data("group") && msg.groupChat){
        $(".group-chat[data-groupchat="+msg.groupChat+"] .notification").removeClass("d-none");
    }else{
        displayMessage(msg);
    }
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
 * Body of the title of conversation if a user clicks on a conversation
 * @param userFriend
 * @returns {string}
 */
function getUserChatTitleBody(userFriend){
    return `<div class="d-flex align-items-center fade-from-bottom" id="userFriend" data-user="${userFriend.id}">
                ${getUserPfpBody(userFriend.profilePic)}
                <h2 class="mx-2">@${userFriend.username}</h2>
            </div>`
}

/**
 * Body of the title of a group chat that contains the title and the
 * users
 * @param users
 * @param title
 * @returns {string}
 */
function getGroupChatTitleBody(users, group){
    let usersBody = "";
    users.forEach((participant) => {
        usersBody += `<p class="text-muted ms-1">@${participant.username}</p>`;
    });
    let href = ""
    if(group.assignment){
        href = `href="/academic/assignments/${group.assignment}"`;
    }else{
        href = `href="/tasks/group/${group.groupTask}"`;
    }

    return `<div class="d-flex flex-column align-items-start fade-from-bottom" id="groupChat" data-group="${group.id}">
                <h2 class="m-1">${group.groupName}<a class="mx-2" ${href}><i class="bi bi-arrow-up-right-square"></i></a></h2>
                <div class="d-flex flex-row flex-wrap">
                    <p class="text-muted fw-semibold ms-1">Users: </p>
                    ${usersBody}
                </div>
            </div>`;
}

/**
 * Gets a task by id
 * @param id
 * @returns {{}}
 */
function getTaskById(id){
    let task = {};
    $.ajax({
        type:"GET",
        url:"/api/task/"+id,
        async: false,
        contentType: "application/json; charset=utf-8",
        success: function (res){
            task = res;
        },
        error: function (){
            console.log("Error!");
        }
    });
    return task;
}

/**
 * Gets string for time or date a message is sent
 * if it's sent in the same day then it will show the time
 * only
 * @param sentDate
 * @returns {string}
 */
function getSentDate(sentDate){
    const endDateTimes = getFormattedTime(new Date(sentDate));
    if(new Date().getDate() === new Date(sentDate).getDate()){
        return `Sent: ${endDateTimes}`;
    }else{
        return `Sent: ${new Date(sentDate).getDate()}:${endDateTimes}`;
    }
}

/**
 * Whenever this function is called it will display messages on the user
 * chat
 * @param message
 */
function displayMessage(message){
    let taskBody = "";
    if(message.task !== 0 && message.task !== undefined){
        const task = getTaskById(message.task);
        taskBody = `<div class="card w-100">                            
                            <div class="card-body">
                               <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                               <h6 class="card-subtitle mb-2 text-body-secondary">Category: ${task.category}</h6>
                               <p class="card-text">📆: ${dateDetails(task)}</p>
                               <div class="progress">
                                   <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-label="Example with label" style="width: ${task.progress}%;" aria-valuenow="${task.progress}" aria-valuemin="0" aria-valuemax="100">${task.progress}%</div>
                               </div>
                           </div>
                        </div>`;
    }
    let fileBody = "";
    if(message.file !== null){
        const fileAttach = message.file;
        fileBody = `<div class="card text-dark bg-light d-flex flex-row">
                        <a href="/api/files/${fileAttach.id}/download" class="card-body highlight-card" style="text-decoration: none">
                            <div class="d-flex align-items-center">
                                <i class="fs-3 bi bi-file-earmark-break-fill me-3"></i>
                                <span class="card-text fs-6 text-primary">${fileAttach.title.slice(0,10)}..</span>
                            </div>
                        </a>
                    </div>`;
    }
    const messageElString = `<p class="text-white m-0"></p>`;
    const messageEl = $(messageElString);
    messageEl.text(message.content);
    if(message.sender === user) {
        const body = `<div class="d-flex m-2 justify-content-end">
                        <div class="user-message m-0">
                            <p class="m-0 mb-1 text-black-50">@${message.senderUsername}</p>
                            ${taskBody}
                            ${fileBody}
                            ${messageEl.prop("outerHTML")}
                            <p class="small m-0 mt-2 text-white-50">${getSentDate(message.sent)}</p>
                        </div>
                        ${getUserPfpBody(getUserPfpString(message.sender))}
                  </div>`;
        $("#chat").append(body);
    }else{
        const body = `<div class="d-flex m-2 justify-content-start">
                      ${getUserPfpBody(getUserPfpString(message.sender))}
                      <div class="sender-message m-0">
                        <p class="m-0 mb-1 text-secondary">@${message.senderUsername}</p>
                        ${fileBody}
                        ${taskBody}
                        <p class="m-0 text-dark">${message.content}</p>
                        <p class="small m-0 mt-2 text-black-50">${getSentDate(message.sent)}</p>
                      </div>
                  </div>`;
        $("#chat").append(body);
    }
    $("#chat").scrollTop($("#chat")[0].scrollHeight);
}

/**
 * Handles event for opening a conversation which gets all messages for that conversation
 */
$(document).on("click",".conversation", function (){
    $(".conversation").removeClass("bg-lightgray");
    $(".group-chat").removeClass("bg-lightgray");
    $(this).addClass("bg-lightgray");
    currentChat = $(this).data("conversation");
    const isFriend = $(this).data("isfriend");
    $("#chat").html("");
    $.ajax({
       type:"GET",
       url:"/api/user/chat/conversations/"+currentChat+"/messages",
       contentType: "application/json; charset=utf-8",
       success: function(conversation){
           if(isFriend){
               $("#sendBtn").prop("disabled",false);
               $("#attachBtn").prop("disabled",false);
               $("#attachments").html("");
               $("#messageInput").prop("disabled", false);
           }else{
               $("#sendBtn").prop("disabled",true);
               $("#attachBtn").prop("disabled",true);
               $("#attachments").html("");
               $("#messageInput").prop("disabled", true);
           }
           const messages = conversation.messages;
           const userFriend = conversation.participants.filter(person => person.id !== user)[0];
           $("#userChatTitle").html(getUserChatTitleBody(userFriend));
           messages.forEach((msg) => displayMessage(msg));
           chatType = "conversation";
           $("#chat").scrollTop($("#chat")[0].scrollHeight);
           $(".conversation[data-conversation="+currentChat+"] .notification").addClass("d-none");
       },
       error: function(){
           console.log("Error!");
       }
    });
});

/**
 * Handles event for opening a group chat and getting all messages for that
 * group chat
 */
$(document).on("click", ".group-chat",function (){
    $(".group-chat").removeClass("bg-lightgray");
    $(".conversation").removeClass("bg-lightgray");
    $(this).addClass("bg-lightgray");
    currentChat = $(this).data("groupchat");
    $("#chat").html("");
    $.ajax({
        type:"GET",
        url:"/api/user/chat/group-chat/"+currentChat+"/messages",
        contentType: "application/json; charset=utf-8",
        success: function (groupChat){
            $("#sendBtn").prop("disabled",false);
            $("#attachBtn").prop("disabled",false);
            $("#attachments").html("");
            $("#messageInput").prop("disabled", false);
            const messages = groupChat.messages;
            const users = groupChat.participants;
            messages.forEach((msg) => displayMessage(msg));
            $("#userChatTitle").html(getGroupChatTitleBody(users,groupChat));
            chatType = "group";
            $("#chat").scrollTop($("#chat")[0].scrollHeight);
            $(".group-chat[data-groupchat="+currentChat+"] .notification").addClass("d-none");
        },
        error: function(){
            console.log("Error!");
        }
    });
});

/**
 * Gets a list of users tasks that can be used to attach to a message
 * @returns {*[]}
 */
function getUserTasks(){
    let tasks = [];
    $.ajax({
        type:"GET",
        url:"/api/task/user/tasks",
        async: false,
        contentType: "application/json; charset=utf-8",
        success: function(res){
            tasks = res;
        },
        error: function(){
            console.log("No tasks for user!");
        }
    });
    return tasks;
}

/**
 * Html body for the task body for the message
 * @param task
 * @returns {string}
 */
function getAttachCardBody(task){
    return `<div class="d-flex align-items-center m-2">
                <div class="card w-100">                            
                    <div class="card-body">
                        <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                        <h6 class="card-subtitle mb-2 text-body-secondary">Category: ${task.category}</h6>
                        <p class="card-text">📆: ${dateDetails(task)}</p>
                        <div class="progress">
                            <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-label="Example with label" style="width: ${task.progress}%;" aria-valuenow="${task.progress}" aria-valuemin="0" aria-valuemax="100">${task.progress}%</div>
                        </div>
                    </div>
                </div>
                <button class="ms-2 attach-task-btn btn btn-outline-success" data-task="${task.id}" data-task-title="${task.title}">
                    <i class="bi bi-clipboard2-plus"></i>
                </button>
            </div>`;
}

/**
 * Renders all user tasks that they can attach to a message
 * @param tasks
 */
function renderUserTasksForAttach(tasks){
    $("#userTasks").html("");
    tasks.forEach((task) => {
        const body = getAttachCardBody(task);
        $("#userTasks").append(body);
    });
}

/**
 * Renders the file attach body
 * @returns {string}
 */
function fileAttachBody(){
    return `<h5 for="attachmentFile" class="form-label">Select File to attach</h5>
                <div class="d-flex">
                    <input class="form-control form-control-lg" id="attachmentFile" type="file">
                    <button class="btn btn-outline-success ms-1 attach-file-btn">
                        <i class="bi bi-file-earmark-diff"></i>
                    </button>
                </div>`;
}

/**
 * Handles event for attaching task/ files
 */
$("#attachBtn").on("click", function(){
    if(chatType === "conversation"){
        const body = `<ul class="nav nav-pills mb-3" id="attachmentType" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active" id="attachTask" data-bs-toggle="pill" data-bs-target="#tasks" type="button" role="tab" aria-selected="true">Attach Task</button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="attachFile" data-bs-toggle="pill" data-bs-target="#filesAttach" type="button" role="tab" aria-selected="false">Attach File</button>
                        </li>
                      </ul>
                      <div class="tab-content" id="pills-tabContent">
                        <div class="tab-pane fade show active" id="tasks" role="tabpanel" aria-labelledby="attachTask" tabindex="0">
                            <form class="d-flex" id="searchTasks">
                                <input id="taskSearch" class="form-control me-2" type="search" placeholder="Search" aria-label="Search">
                                <button class="btn btn-success" type="submit">
                                    <i class="bi bi-search"></i>
                                </button>
                            </form>
                            <div id="userTasks"></div>
                        </div>
                        <div class="tab-pane fade" id="filesAttach" role="tabpanel" aria-labelledby="filesAttach" tabindex="0">
                            ${fileAttachBody()}
                        </div>
                      </div>`;
        $("#attachBody").html(body);
        renderUserTasksForAttach(getUserTasks());
    }else{
        $("#attachBody").html(fileAttachBody());
    }
});

/**
 * Used to filter tasks a user wants to attach to a message
 */
$(document).on("submit", "#searchTasks" ,function (event){
    event.preventDefault();
    const search = $("#taskSearch").val();
    $.ajax({
        type:"GET",
        url:"/api/task/search",
        data:{
            "search":search
        },
        contentType: "application/json; charset=utf-8",
        success: function (res){
            renderUserTasksForAttach(res);
        },
        error: function (){
            $("#userTasks").html("");
            $("#userTasks").append(`<p class="m-2 fw-semibold">No results</p>`);
        }
    });
});

/**
 * If the search input field is empty then undo search
 */
$(document).on("input", "#taskSearch",function (){
    let inputVal = $("#taskSearch").val();
    if(inputVal.trim() == ""){
        renderUserTasksForAttach(getUserTasks());
    }
});

/**
 * Handles event for attaching files to a message which will appear above input box
 */
$(document).on("click", ".attach-task-btn", function (){
   const taskTitle = $(this).data("task-title");
   const taskId = $(this).data("task");
   const attachment = `<div class="d-flex align-items-center bg-light border border-1 rounded-pill text-center p-2 m-2" data-task="${taskId}">
                        <p class="m-0 fw-semibold">${taskTitle}</p>
                        <a class="btn-close remove-attachment-btn ms-auto" disabled aria-label="cancel"></a>
                       </div>`;
   $("#attachments").append(attachment);
   $("#attachBtn").prop("disabled",true);
   $("#attachmentModal").modal("toggle");
});

/**
 * Handles event of user attach a file
 */
$(document).on("click", ".attach-file-btn", function(){
    file = $("#attachmentFile").prop("files")[0];
    if(file){
        if(isFileValidSize(file)){
            const attachment = `<div class="d-flex align-items-center bg-light border border-1 rounded-pill text-center p-2 m-2" data-file="${file}">
                                <p class="m-0 fw-semibold">${file.name}</p>
                                <a class="btn-close remove-attachment-btn ms-auto" disabled aria-label="cancel"></a>
                            </div>`;
            $("#attachments").append(attachment);
            $("#attachBtn").prop("disabled",true);
            $("#attachmentModal").modal("toggle");
        }else{
            showToastError("File attached is too big! (2MB MAX)");
        }
    }else{
        showToastInfo("There is no file to attach!");
    }
});

/**
 * In case a user wants to remove a attachment from a message
 */
$(document).on("click", ".remove-attachment-btn", function (){
    $("#attachments").html("");
    $("#attachBtn").prop("disabled", false);
})

/**
 * If there is a file to attach to a message this will
 * make a file attachment object and will return the DTO of it
 */
function createFileAttachment(){
    let fileDTO = null;
    const formData = new FormData();
    if(file) {
        formData.append("file", file, file.name);
        $.ajax({
            type: "POST",
            url: "/api/files/create",
            async:false,
            processData: false,
            contentType: false,
            data: formData,
            success: function (res) {
                fileDTO = res;
            },
            error: function (){
                console.log("Error");
            }
        });
    }
    return fileDTO;
}

/**
 * Handles the event of sending a message checking if the message has nothing then don't send
 * also checks whether the message should be sent to a group chat or conversation
 */
$("#sendMessage").on("submit",function(event){
    event.preventDefault();
    const messageContent = $("#messageInput").val();
    const task = $("#attachments").children(':first').data("task");
    const fileAttach = createFileAttachment();
    $("#attachments").html("");
    $("#attachBtn").prop("disabled",false);
    if(chatType === "conversation" && (messageContent.length !== 0 || file || task)){
        const recipient = $("#userFriend").data("user");
        const messageBody = {
            content: messageContent,
            sender: user,
            recipient: recipient,
            conversation: currentChat,
            task:task,
            senderUsername: username,
            sent: new Date(),
            file: fileAttach
        }
        sendMessage(messageBody);
    }else if(chatType === "group" && (messageContent.length !== 0 || file)){
        const messageBody = {
            content: messageContent,
            sender: user,
            groupChat: currentChat,
            senderUsername: username,
            sent: new Date(),
            file: fileAttach
        }
        sendMessage(messageBody);
    }
    file = null;
    $("#messageInput").val("");
});

/**
 * Gets the card body of group task
 * @param task
 * @returns {string}
 */
function getGroupTaskCard(task){
    return `<div class="d-flex align-items-center m-2">
                <div class="card w-100">                            
                    <div class="card-body">
                        <h5 class="card-title">${task.title} ${getStatus(task.inProgress,task.complete)}</h5>
                        <h6 class="card-subtitle mb-2 text-body-secondary">Number of users: ${task.users.length}</h6>
                        <p class="card-text">📆: ${dateDetails(task)}</p>
                        <div class="progress">
                            <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-label="Example with label" style="width: ${task.progress}%;" aria-valuenow="${task.progress}" aria-valuemin="0" aria-valuemax="100">${task.progress}%</div>
                        </div>
                    </div>
                </div>
                <button class="ms-2 create-group-task-chat btn btn-outline-success" data-task="${task.id}">
                    <i class="bi bi-sign-intersection"></i>
                </button>
            </div>`;
}

/**
 * Gets card body of an assignment from search
 * @param assignment
 * @returns {string}
 */
function getAssignmentCard(assignment){
    return `<div class="d-flex align-items-center m-2">
                <div class="card w-100">
                    <div class="card-body">
                        <h5 class="card-title">${assignment.title} ${getStatus(assignment.inProgress,assignment.complete)}</h5>
                        <h6 class="card-subtitle mb-2 text-body-secondary">Courses ${assignment.course}</h6>
                        <div class="d-flex">
                            <h6 class="card-text me-2">Users: ${assignment.users.length} |</h6>
                            <h6 class="card-text ">Tasks: ${assignment.tasks.length}</h6>   
                        </div>
                        <p class="card-text">📆: ${new Date(assignment.endDate).toDateString()}</p>
                        <div class="progress">
                            <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-label="Example with label" style="width: ${assignment.progress}%;" aria-valuenow="${assignment.progress}" aria-valuemin="0" aria-valuemax="100">${assignment.progress}%</div>
                        </div>
                    </div>
                </div>
                <button  class="ms-2 create-group-assignment-chat btn btn-outline-success" data-assignment="${assignment.id}">
                    <i class="bi bi-sign-intersection"></i>
                </button>
            </div>`;
}

/**
 * Handles event for searching group tasks or assignments that are
 * available for chat depending on search query
 */
$("#groupProjSearch").on("submit", function(event){
   event.preventDefault();
   $("#results").html("");
   const searchOption = $("#searchType").val();
   const query = $("#searchProj").val();
   if(searchOption === "group"){
       $.ajax({
           type:"GET",
           url:"/api/task/group/search/available-tasks-for-chat",
           data:{
               "query":query
           },
           contentType: "application/json; charset=utf-8",
           success: function (res){
               res.forEach((task) => $("#results").append(getGroupTaskCard(task)));
           },
           error: function (){
               $("#results").append(`<p class="m-2 fw-semibold">No Results Found!</p>`);
           }
       });
   }else{
       $.ajax({
           type:"GET",
           url:"/api/assignments/search/available-assignments-for-chat/",
           data:{
               "query":query
           },
           contentType: "application/json; charset=utf-8",
           success: function (res){
               res.forEach((assignment) => $("#results").append(getAssignmentCard(assignment)));
           },
           error: function (){
               $("#results").append(`<p class="m-2 fw-semibold">No Results Found!</p>`);
           }
       });
   }
});

/**
 * Handles event of rending group chats card
 * @param chat
 */
function renderGroupChat(chat){
    let users = "";
    chat.participants.forEach((u) => {
        if(u.id !== user){
            users += `<p class="ms-1 text-muted">${u.username}</p>`;
        }
    });
    const body = `<div class="border-bottom d-flex highlight-card group-chat" data-groupchat="${chat.id}">
                        <div class="m-1 d-flex">
                          <img class="img-fluid img-thumbnail rounded-circle m-1" src="/images/groupImg.png" style="height: 60px; width: 60px;" alt="group picture">
                          <div class="d-flex flex-column m-1 d-none d-md-block">
                            <h4 class="m-0">${chat.groupName}</h4>
                            <div class="d-flex flex-row">
                              ${users}
                            </div>
                          </div>
                        </div>
                        <div class="d-flex align-items-center ms-auto me-3 notification d-none">
                          <div class="notif">
                            <i class="fa-solid bi-chat-text text-white"></i>
                          </div>
                        </div>
                  </div>`;
    $("#chats").prepend(body);
}

/**
 * Handles rendering of conversation cards
 * @param chat
 */
function renderConversation(chat){
    const friend = chat.participants.filter(person => person.id !== user)[0];
    return `<div  class="border-bottom d-flex highlight-card conversation" data-conversation="${chat.id}" >
                ${getUserPfpBody(friend.profilePicture)}
                <h4 class="m-1 d-none d-md-block">@${friend.username}</h4>
                <div class="d-flex align-items-center ms-auto me-3 notification d-none">
                    <div class="notif">
                        <i class="fa-solid bi-chat-text text-white"></i>
                    </div>
                </div>
             </div>`;
}

/**
 * Handles event when user wants to create a group chat for a task
 */
$(document).on("click", ".create-group-task-chat", function(){
    const confirmed = confirm("Are you sure you want to create a group chat for this group task?");
    if(confirmed){
        const id = $(this).data("task");
        $.ajax({
            type:"POST",
            url:"/api/user/chat/group-chat/group-task/"+id,
            success: function (res){
                showToastSuccess("Group-Chat Created!");
                $("#createChatModal").modal("toggle");
                renderGroupChat(res);
            },
            error: function (){
                showToastError("Error Occurred!");
            }
        });
    }
});

/**
 * Handles event for when a user wants to create a group chat for an assignment
 */
$(document).on("click",".create-group-assignment-chat", function (){
    const confirmed = confirm("Are you sure you want to create a group chat for this assignment?");
    if(confirmed){
        const id = $(this).data("assignment");
        $.ajax({
            type:"POST",
            url:"/api/user/chat/group-chat/assignment/"+id,
            success: function (res){
                showToastSuccess("Group-Chat Created!");
                $("#createChatModal").modal("toggle");
                renderGroupChat(res);
            },
            error: function (){
                showToastError("Error Occurred!");
            }
        });
    }
});

/**
 * If search field is cleared then retrieve all chats
 * @returns {*[]}
 */
function getUserChats(){
    let chats = [];
    $.ajax({
        type:"GET",
        url:"/api/user/chat/all",
        contentType: "application/json; charset=utf-8",
        async: false,
        success: function(res){
            chats = res;
        }
    });
    return chats;
}

/**
 * Renders chat cards
 * @param chats
 */
function renderChatCards(chats){
    $("#chats").html("");
    chats.forEach((chat) => {
        //Check if its a group chat or conversation
       if(chat.hasOwnProperty("groupName")){
           $("#chats").append(renderGroupChat(chat));
       }else{
           $("#chats").append(renderConversation(chat));
       }
    });
}

/**
 * Handles event of when a user wants to search for a chat
 */
$("#chatSearch").on("submit", function(event){
    event.preventDefault();
    const query = $("#search-input").val();
    $.ajax({
        type:"GET",
        url:"/api/user/chat/search",
        data:{
            "query":query
        },
        contentType: "application/json; charset=utf-8",
        success: function (res){
            renderChatCards(res);
            showToastSuccess("Chats matching search found!");
        },
        error: function (){
            $("#chats").append(`<p class="m-2 fw-semibold">No Results Found!</p>`);
            showToastInfo("No results found!");
        }
    });
});

/**
 * Handles an event where if the search input is empty then it will reload all
 * chats again
 */
$("#search-input").on("input", function (){
    const inputVal = $("#search-input").val();
    if(inputVal.trim() == ""){
        renderChatCards(getUserChats());
    }
});

//Connect to websocket
connect();