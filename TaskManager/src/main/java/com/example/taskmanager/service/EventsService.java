package com.example.taskmanager.service;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Events.StudySessionRepository;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class EventsService {

    @Autowired
    private StudySessionRepository studySessionRepo;

    @Autowired
    private AssignmentRepository assignmentRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CommonUtils utils;

    public boolean studySessionExistsById(long id){return studySessionRepo.existsById(id);}

    /**
     * Creates a study session DTO based of study session object
     * @param studySession
     * @return
     */
    public StudySessionDTO createStudySessionDTO(StudySession studySession){
        StudySessionDTO studySessionDTO = new StudySessionDTO(studySession.getId(), studySession.getTitle(),studySession.getInfo(),studySession.getStartDate(),studySession.getEndDate(),studySession.getModified(),studySession.isOnline(),studySession.getLocation(),studySession.getOrganiser().getId(), studySession.getOrganiser().getUsername());
        if(studySession.getAssignment() != null){
            studySessionDTO.setAssignment(studySession.getAssignment().getId());
            studySessionDTO.setAssignmentName(studySession.getAssignment().getTitle());
        }
        return studySessionDTO;
    }

    /**
     * Returns a list of study sessions DTO
     * @param studySessions
     * @return
     */
    public List<StudySessionDTO> getUserStudySessionsDTO(List<StudySession> studySessions){
        List<StudySessionDTO> studySessionDTOS = new ArrayList<>();
        studySessions.forEach((studySession) -> {
            studySessionDTOS.add(createStudySessionDTO(studySession));
        });
        return studySessionDTOS;
    }

    /**
     * Creates a study session for user with provided DTO
     * @param studySessionDTO
     * @return
     */
    public StudySessionDTO createStudySession(StudySessionDTO studySessionDTO){
        MyUser organiser = userRepo.findById(studySessionDTO.getOrganiser()).get();
        StudySession studySession = new StudySession(studySessionDTO.getTitle(),studySessionDTO.getInfo(),studySessionDTO.getStartDate(),studySessionDTO.getEndDate(),studySessionDTO.getModified(),studySessionDTO.isOnline(),studySessionDTO.getLocation(),organiser);
        studySession = studySessionRepo.save(studySession);
        if(studySessionDTO.getAssignment() != -1){
            Assignment assignment = assignmentRepo.findById(studySessionDTO.getAssignment()).get();
            studySession.setAssignment(assignment);
            assignment.getStudySessions().add(studySession);
            for (MyUser user: assignment.getUsers()) {
                user.getStudySessions().add(studySession);
                userRepo.save(user);
            }
            assignmentRepo.save(assignment);
        }else{
            organiser.getStudySessions().add(studySession);
            userRepo.save(organiser);
        }
        return createStudySessionDTO(studySession);
    }

    /**
     * Gets a study session via its id
     * @param id
     * @return
     */
    public StudySession getStudySessionById(long id){return studySessionRepo.findById(id).get();}

    /**
     * Gets every single user study-session
     * @param principal
     * @return
     */
    public List<StudySession> getUserStudySessions(Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return user.getStudySessions();
    }

    /**
     * Gets all study session for a user for the current date
     * @param studySessions
     * @return
     */
    public List<StudySession> getUserStudySessionsForCurrentDate(List<StudySession> studySessions){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return studySessions.stream()
                .filter(studySession -> (studySession.getStartDate().before(endOfDay) && studySession.getEndDate().after(startOfDay)) ||
                        studySession.getStartDate().equals(startOfDay)).toList();
    }

    /**
     * Get study session within the date range provided
     * @param principal
     * @param date1
     * @param date2
     * @return
     */
    public List<StudySessionDTO> getStudySessionsInDateRange(Principal principal, Date date1, Date date2){
        MyUser user = userRepo.findByEmail(principal.getName());
        Date startDate = utils.getStartDate(date1);
        Date endDate = utils.getEndDate(date2);
        List<StudySession> forDates = user.getStudySessions().stream().filter(session ->
                (session.getStartDate().before(endDate) && session.getEndDate().after(startDate) ||
                session.getStartDate().equals(startDate))).toList();
        return getUserStudySessionsDTO(forDates);
    }

    /**
     * Checks if the current logged in user is the owner of a study session found via id
     * @param id
     * @param principal
     * @return
     */
    public boolean isStudySessionOrganiser(long id,Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return getStudySessionById(id).getOrganiser().equals(user);
    }

    /**
     * Handles deletion of a study session via its id
     * @param id
     */
    public void deleteStudySessionById(long id){
        StudySession studySession = getStudySessionById(id);
        if(studySession.getAssignment() != null){
            Assignment assignment = studySession.getAssignment();
            assignment.getUsers().forEach((user) -> {
                user.getStudySessions().remove(studySession);
                userRepo.save(user);
            });
        }else{
            studySession.getOrganiser().getStudySessions().remove(studySession);
            userRepo.save(studySession.getOrganiser());
        }
        studySessionRepo.delete(studySession);
    }

    /**
     * Updates a study sessions details from provided DTO details sent by user
     * @param studySessionDTO
     */
    public void updateStudySession(StudySessionDTO studySessionDTO){
        StudySession studySession = getStudySessionById(studySessionDTO.getId());
        studySession.setTitle(studySessionDTO.getTitle());
        studySession.setStartDate(studySessionDTO.getStartDate());
        studySession.setEndDate(studySessionDTO.getEndDate());
        studySession.setLocation(studySessionDTO.getLocation());
        studySession.setInfo(studySessionDTO.getInfo());
        studySession.setModified(studySessionDTO.getModified());
        studySessionRepo.save(studySession);
    }

    /**
     * Gets a list of numbers containing number of study-sessions that are on during that day within the next 7 days
     * @param principal
     * @return
     */
    public List<Integer> getNumberOfStudySessionForWeek(Principal principal){
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        List<Date> dateRange = IntStream.range(0,7).mapToObj((i) -> {
            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, i);
            return cal.getTime();
        }).collect(Collectors.toList());
        List<Integer> sessionCounts = new ArrayList<>();
        dateRange.forEach((date) -> {
            int count = getStudySessionsInDateRange(principal,date,date).size();
            sessionCounts.add(count);
        });
        return sessionCounts;
    }

    /**
     * Gets a list of user study sessions DTO
     * @param principal
     * @return
     */
    public List<StudySessionDTO> getAllUserStudySessionsDTO(Principal principal){
        return getUserStudySessionsDTO(getUserStudySessions(principal));
    }

    /**
     * If the user moves or resizes a study session in calendar this will update the dates
     * @param id
     * @param start
     * @param end
     */
    public void updateStudySessionDates(Long id, Date start, Date end){
        StudySession studySession = getStudySessionById(id);
        studySession.setStartDate(start);
        studySession.setEndDate(end);
        studySessionRepo.save(studySession);
    }

    /**
     * Gets study sessions that start in 30 minutes
     * Part of notifications system.
     * @param user
     * @return
     */
    public List<StudySessionDTO> getStudySessionsThatBeginIn30Minutes(MyUser user){
        Date currentDate = new Date();
        Date thirtyMinutesFromNow = utils.get30MinutesFromNow(currentDate);
        List<StudySession> sessionsThatStartIn30Minutes = user.getStudySessions().stream().filter((session) -> session.getStartDate().before(thirtyMinutesFromNow) && session.getStartDate().after(currentDate)).toList();
        return getUserStudySessionsDTO(sessionsThatStartIn30Minutes);
    }
}
