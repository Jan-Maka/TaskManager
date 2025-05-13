//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Get the current user logged in
const user = $('#_principal').data("principal");
const chartEl = $("#activityOnTasks");

/**
 * Makes ajax call for getting task and group task completion
 * @returns {*[]}
 */
function getChartData(){
    let taskData = [];
    $.ajax({
        type:"GET",
        url:"/api/task/user/"+user+"/num-completed",
        async: false,
        success: function(res){
            taskData = res;
        },
        error: function(){
            console.log("Error!");
        }
    });
    let groupData = [];
    $.ajax({
        type:"GET",
        url:"/api/task/group/user/"+user+"/num-completed",
        async: false,
        success: function(res){
            groupData = res;
        },
        error: function(){
            console.log("Error!");
        }
    });
    for (let i = 0; i < taskData.length; i++) {
        taskData[i] = groupData[i] + taskData[i];
    }
    return taskData;
}

let chart = new Chart(chartEl, {
    type: 'line',
    data: {
        labels: getPreviousDaysFromNow(),
        datasets: [{
            label: '# Tasks Completed',
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
    }
});