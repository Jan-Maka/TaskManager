//Get token so that ajax requests will work
const token = $('#_csrf').attr('csrf_content');
const header = $('#_csrf_header').attr('csrf_content');
$(document).ajaxSend(function (e, xhr) {
    xhr.setRequestHeader(header, token);
});

//Clock updates every second
updateDate();
setInterval(updateDate, 1000);
/**
 * Handles the clock in the html page
 */
function updateDate(){
    let date = new Date();
    $("#date").text(formatTime(date));

    function formatTime(date){
        let day = date.getDate();
        let year = date.getFullYear();
        let month = date.getMonth()+1;
        let hours = date.getHours();
        let minutes = date.getMinutes();
        let seconds = date.getSeconds();
        let amOrPm = hours >= 12 ? "pm" : "am";

        hours = (hours % 12) || 12;

        hours = formatZeros(hours);
        minutes = formatZeros(minutes);
        seconds = formatZeros(seconds);

        return `Date: ${day}/${month}/${year} ${hours}:${minutes}:${seconds} ${amOrPm}`;
    }
    function formatZeros(time){
        time = time.toString();
        return time.length < 2 ? "0" +time : time;
    }
}

/**
 * Converts ratings to emojicons
 * @param dataValues
 * @returns {*}
 */
function convertToEmojis(dataValues) {
    const emojis = ['😁', '😊', '😑', '😣', '😤', '😠'];
    return dataValues.map(value => emojis[value]);
}

/**
 * Gets user mood ratings over last seven days
 * @returns {*[]}
 */
function getMoodRatingsForPast7Days(){
    let moods = [];
    $.ajax({
        type:"GET",
        url:"/api/user/mood-ratings",
        async: false,
        success: function (res){
            moods = res;
        }
    });
    if(moods.length !== 7){
        const spacesToFill = 7 - moods.length;
        for (let i = 0; i < spacesToFill; i++) {
            moods.unshift(-1);
        }
    }
    return moods;
}

const chartMood = $("#moodGraph");
const moodChart = new Chart(chartMood, {
    type: 'line',
    data: {
        labels: getPreviousDaysFromNow(),
        datasets: [{
            label: 'Mood Rating in the Past Week',
            data: getMoodRatingsForPast7Days(),
            borderWidth: 1,
            backgroundColor: ['rgba(0, 123, 255, 1)'],
            borderColor: 'rgba(75, 192, 192, 1)',
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                reverse: true,
                beginAtZero: true,
                min:0,
                max:5,
                ticks: {
                    stepSize:1,
                    callback: function(value){
                        return convertToEmojis([value])[0];
                    }
                }
            }
        },
        maintainAspectRatio: false,
    }
});

/**
 * Gets info on task progression for the data which is displayed on the pie chart
 * @returns {*[]}
 */
function getTaskProgressForToday(){
    let data = [];
    $.ajax({
        type:"GET",
        url:"/api/user/task-completion-info",
        async:false,
        success: function (res){
            data.push(res.notStarted);
            data.push(res.inProgress);
            data.push(res.completed);
        }
    });
    //Done so a pie chart does appear
    let sum = 0;
    data.forEach((num) => num+sum);
    if(sum === 0){
        data[0] = 1;
    }
    return data;
}

const pieChart = $("#activityChart");
const pieChartCompletion = new Chart(pieChart,{
    type:'pie',
    data: {
        labels: ['Not Started', 'In Progress', 'Completed'],
        datasets: [{
            label: 'Colors',
            data: getTaskProgressForToday(),
            backgroundColor: ['red', 'blue', 'green'],
            borderWidth: 1
        }]
    },
    options: {}
});

//If user just logged then process the mood rating given
let moodRating;

/**
 * Handles event of selecting mood
 */
$(document).on("click", ".mood", function(){
    $(".mood").removeClass("enlarged");
    $(".mood").addClass("text-muted");
    $(this).removeClass("text-muted");
    $(this).addClass("enlarged");
    moodRating = $(this).data("rating");
});

/**
 * Submits the mood user has inputted
 */
$("#saveMood").on("click", function(){
    $.ajax({
        type:"PATCH",
        url:"/api/user/update/mood-rating",
        data:{
            "rating":moodRating
        },
        async: false,
        success: function (){
            showToastSuccess("Mood analyzed and submitted");
        },
        error:function () {
            showToastError("Server Error Occurred!");
        }
    });
    $("#userMoodModal").modal("toggle");
    window.location.reload();
});