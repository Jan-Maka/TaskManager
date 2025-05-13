package com.example.taskmanager.component;

import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CommonUtils {

    /**
     * Helper function which is used to sort lists based on importance and urgency
     * @param important
     * @param urgent
     * @return
     */
    public int getPriority(boolean important, boolean urgent){
        if (important && urgent) {
            return 3; // Highest priority
        } else if (important) {
            return 2; // Medium priority
        } else if (urgent) {
            return 1; // Low priority
        }
        return 0; // Default priority
    }

    /**
     * Helper function to get days till completion for tasks
     * @param endDate
     * @return
     */
    public long getDaysTillCompletion(Date endDate){
        long millisecondsInDay = 24 * 60 * 60 * 1000;
        long currentDateMills = new Date().getTime();
        long taskEndDateMills = endDate.getTime();
        long timeDifference = taskEndDateMills - currentDateMills;
        long daysTillCompletion = timeDifference / millisecondsInDay;
        return daysTillCompletion;
    }

    /**
     * Helper function used to get suggested tasks (assignment and group as well) based on users mood, task workload and completion date.
     * @param isComplete
     * @param user
     * @param taskMood
     * @param taskWorkload
     * @param taskStartDate
     * @param taskEndDate
     * @return
     */
    public boolean shouldSuggestTask(boolean isComplete, MyUser user, short taskMood,short taskWorkload,Date taskStartDate,Date taskEndDate){
        if(isComplete){
            return false;
        }
        //If task to be complete within 2 days then recommend or if the task is overdue for completion
        if(getDaysTillCompletion(taskEndDate) <= 2 || taskEndDate.before(new Date())){
            return true;
        }
        int userMood = user.getMoodRating();
        List<Integer> ratings = new ArrayList<>();
        switch (userMood) {
            case 0 -> ratings = Arrays.asList(0, 1, 2, 3, 4, 5);
            case 1 -> ratings = Arrays.asList(0, 1, 2, 3, 4);
            case 2 -> ratings = Arrays.asList(0, 1, 2, 3);
            case 3 -> ratings = Arrays.asList(0, 1, 2);
            case 4 -> ratings = Arrays.asList(0, 1);
            case 5 -> ratings = List.of(0);
        }
        boolean containsTaskMood = ratings.contains((int)taskMood);
        boolean containsWorkload = ratings.contains((int)taskWorkload);
        //If taskMood and taskWorkload rating is within the ratings user can handle then it will recommend
        return containsTaskMood && containsWorkload && getDaysTillCompletion(taskEndDate) <= 7;
    }


    /**
     * Sorts all tasks in terms of importance and urgency
     * @param tasks
     * @return
     */
    public List<Object> getTasksSortedBasedOnImportanceAndUrgency(List<Object> tasks){
        Comparator<Object> comparator = Comparator.comparingInt(obj -> {
            if (obj instanceof Task) {
                return getPriority(((Task) obj).isImportant(), ((Task) obj).isUrgent());
            } else if (obj instanceof GroupTask) {
                return getPriority(((GroupTask) obj).isImportant(), ((GroupTask) obj).isUrgent());
            } else if (obj instanceof AssignmentTask) {
                return getPriority(((AssignmentTask) obj).isImportant(), ((AssignmentTask) obj).isUrgent());
            } else {
                throw new IllegalArgumentException("Unsupported object type: " + obj.getClass());
            }
        });
        tasks.sort(comparator);
        return tasks;
    }

    /**
     * Converts start date with time to just date
     * @param startDate
     * @return
     */
    public Date getStartDate(Date startDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        Date startOfDay = calendar.getTime();
        return startOfDay;
    }

    /**
     * Converts end date with time to just date
     * @param endDate
     * @return
     */
    public Date getEndDate(Date endDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();
        return endOfDay;
    }

    /**
     * Gets the date range need for graphs
     * @param currentDate
     * @return
     */
    public List<Date> getDateRange(Date currentDate) {
        Calendar cal = Calendar.getInstance();
        List<Date> dateRange = IntStream.range(0,7).mapToObj((i) -> {
            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }).collect(Collectors.toList());
        Collections.reverse(dateRange);
        return dateRange;
    }

    /**
     * Gets 30 days ago from current date mainly used in the scheduling service
     * @return
     */
    public Date get30daysAgo(){
        Calendar thirtyDaysAgo = new GregorianCalendar();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
        return thirtyDaysAgo.getTime();
    }

    /**
     * Gets Date object 30 Minutes from now
     * @param currentDate
     * @return
     */
    public Date get30MinutesFromNow(Date currentDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.MINUTE, 30);
        Date thirtyMinutesFromNow = cal.getTime();
        return thirtyMinutesFromNow;
    }
}
