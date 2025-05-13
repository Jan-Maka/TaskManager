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
 * Makes API call for data to be displayed on the graph
 * @returns {*[]}
 */
function getChartData(){
    let data = [];
    $.ajax({
        type:"GET",
        url:"/api/assignments/tasks/user/"+user+"/num-completed",
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
