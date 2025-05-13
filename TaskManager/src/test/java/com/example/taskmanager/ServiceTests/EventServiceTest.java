package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Events.StudySessionRepository;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.EventsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    MyUser user;
    StudySession studySession;
    StudySessionDTO studySessionDTO;

    MockPrincipal mockPrincipal;


    @InjectMocks
    EventsService eventsService;

    @Mock
    StudySessionRepository studySessionRepository;

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommonUtils utils;

    @BeforeEach
    public void setUp(){
        this.user = new MyUser("foo@bar.com","foo", "bar","password", "foofoo");
        user.setId(1L);
        this.studySession = new StudySession("Test","Info",new Date(),new Date(),new Date(),false,"location",user);
        studySession.setId(1L);
        user.getStudySessions().add(studySession);
        this.studySessionDTO = new StudySessionDTO(studySession.getId(), studySession.getTitle(), studySession.getInfo(),studySession.getStartDate(),studySession.getEndDate(),studySession.getModified(), studySession.isOnline(),studySession.getLocation(),studySession.getOrganiser().getId(),studySession.getOrganiser().getUsername());
        studySessionDTO.setAssignment(-1L);
        this.mockPrincipal = new MockPrincipal(user.getEmail());
    }

    @DisplayName("Test if study session exists by id")
    @Test
    public void testIfStudySessionExistById(){
        when(studySessionRepository.existsById(1L)).thenReturn(true);
        assertTrue(eventsService.studySessionExistsById(1L));
        verify(studySessionRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test creating study session DTO")
    @Test
    public void testCreatingStudySessionDTO(){
        StudySessionDTO result = eventsService.createStudySessionDTO(studySession);
        assertEquals(studySession.getId(),result.getId());
        assertEquals(studySession.getOrganiser().getId(),result.getOrganiser());
        assertEquals(studySession.getOrganiser().getUsername(),result.getUser());
        assertEquals(studySession.getTitle(),result.getTitle());
        assertEquals(studySession.getInfo(),result.getInfo());
        assertEquals(studySession.isOnline(),result.isOnline());
        assertEquals(studySession.getStartDate(),result.getStartDate());
        assertEquals(studySession.getEndDate(),result.getEndDate());
        assertEquals(studySession.getLocation(),result.getLocation());
    }

    @DisplayName("Test creating study session")
    @Test
    public void testCreatingStudySession(){
        StudySession test = new StudySession(studySessionDTO.getTitle(),studySessionDTO.getInfo(),studySessionDTO.getStartDate(),studySessionDTO.getEndDate(),studySessionDTO.getModified(),studySessionDTO.isOnline(),studySessionDTO.getLocation(),user);
        test.setId(2L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(test);
        eventsService.createStudySession(studySessionDTO);
        verify(studySessionRepository,times(1)).save(any(StudySession.class));
        verify(userRepository,times(1)).save(user);
        assertTrue(user.getStudySessions().contains(test));
    }

    @DisplayName("Test getting study session by id")
    @Test
    public void testGetStudySessionById(){
        when(studySessionRepository.findById(1L)).thenReturn(Optional.ofNullable(studySession));
        assertEquals(studySession,eventsService.getStudySessionById(1L));
        verify(studySessionRepository,times(1)).findById(1L);
    }

    @DisplayName("Test getting user study sessions")
    @Test
    public void testGetUserStudySessions(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        assertEquals(user.getStudySessions(),eventsService.getUserStudySessions(mockPrincipal));
    }

    @DisplayName("Test getting user study sessions for current date")
    @Test
    public void testGetUserStudySessionForCurrentDate(){
        Date current = new Date();
        Date start = getStartOfDay(current);
        Date end = getEndOfDay(current);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        List<StudySession> result = eventsService.getUserStudySessionsForCurrentDate(user.getStudySessions());
        assertTrue(result.contains(studySession));
    }

    @DisplayName("Test getting study sessions in date range")
    @Test
    public void testGetStudySessionsInDateRange(){
        Date current = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.DAY_OF_MONTH,-3);
        Date start = cal.getTime();
        cal.clear();
        cal.setTime(current);
        cal.add(Calendar.DAY_OF_MONTH,5);
        Date end = cal.getTime();
        List<StudySession> testList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            cal.clear();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH,i);
            Date startDate = cal.getTime();
            cal.clear();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH,i+1);
            Date endDate = cal.getTime();
            StudySession test = new StudySession("test","test",startDate,endDate,new Date(),false,"test",user);
            test.setId(i+1L);
            testList.add(test);
        }
        user.setStudySessions(testList);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(utils.getStartDate(start)).thenReturn(getStartOfDay(start));
        when(utils.getEndDate(end)).thenReturn(getEndOfDay(end));
        List<StudySessionDTO> result = eventsService.getStudySessionsInDateRange(mockPrincipal,start,end);
        assertEquals(5,result.size());
    }

    @DisplayName("Test user is study session organiser")
    @Test
    public void testUserIsStudySessionOrganiser(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(studySessionRepository.findById(1L)).thenReturn(Optional.ofNullable(studySession));
        assertTrue(eventsService.isStudySessionOrganiser(1L,mockPrincipal));
    }

    @DisplayName("Test deleting study session by id")
    @Test
    public void testDeletingStudySessionById(){
        when(studySessionRepository.findById(1L)).thenReturn(Optional.ofNullable(studySession));
        eventsService.deleteStudySessionById(1L);
        verify(userRepository,times(1)).save(user);
        verify(studySessionRepository,times(1)).delete(studySession);
        assertTrue(!user.getStudySessions().contains(studySession));
    }

    @DisplayName("Test updating study session details")
    @Test
    public void testUpdatingStudySession(){
        StudySessionDTO test = new StudySessionDTO(1L,"test","test",new Date(),new Date() ,new Date(), false,"location",user.getId(),user.getUsername());
        when(studySessionRepository.findById(1L)).thenReturn(Optional.ofNullable(studySession));
        eventsService.updateStudySession(test);
        verify(studySessionRepository,times(1)).save(studySession);
        assertEquals(test.getTitle(),studySession.getTitle());
        assertEquals(test.getInfo(),studySession.getInfo());
        assertEquals(test.getStartDate(),studySession.getStartDate());
        assertEquals(test.getEndDate(),studySession.getEndDate());
        assertEquals(test.getLocation(),studySession.getLocation());
    }

//    @DisplayName("Test getting number of study sessions for week")
//    @Test
//    public void testGetNumberOfStudySessionsForWeek(){
//        List<StudySession> testList = new ArrayList<>();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.set(Calendar.HOUR_OF_DAY,12);
//        cal.set(Calendar.MINUTE,30);
//        Date current = cal.getTime();
//        System.out.println(current);
//        for (int i = 0; i < 7; i++) {
//            StudySession test = new StudySession("test","test",cal.getTime(),new Date(),new Date(),false,"test",user);
//            test.setId(i+1L);
//            cal.clear();
//            cal.setTime(current);
//            if(i % 3 == 0){
//                cal.add(Calendar.DAY_OF_MONTH,i);
//                test.setStartDate(cal.getTime());
//                cal.add(Calendar.DAY_OF_MONTH,1);
//                test.setEndDate(cal.getTime());
//                testList.add(test);
//            }
//        }
//        user.setStudySessions(testList);
//        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
//        when(utils.getStartDate(any(Date.class))).thenReturn(getStartOfDay(new Date()));
//        when(utils.getEndDate(any(Date.class))).thenReturn(getEndOfDay(new Date()));
//        List<Integer> result = eventsService.getNumberOfStudySessionForWeek(mockPrincipal);
//        System.out.println(result.toString());
//    }

    @DisplayName("Test updating study session dates")
    @Test
    public void testUpdatingStudySessionDates(){
        Date start = getStartOfDay(new Date());
        Date end = getEndOfDay(new Date());
        when(studySessionRepository.findById(1L)).thenReturn(Optional.ofNullable(studySession));
        eventsService.updateStudySessionDates(1L,start,end);
        verify(studySessionRepository,times(1)).save(studySession);
        assertEquals(start,studySession.getStartDate());
        assertEquals(end,studySession.getEndDate());
    }

    @DisplayName("Test getting study sessions that start within 30 minutes")
    @Test
    public void testGetStudySessionsThatBeginIn30Minutes(){
        Date current = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        List<StudySession> testList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            cal.add(Calendar.MINUTE,10);
            StudySession test = new StudySession("test","test",cal.getTime(),new Date(),new Date(),false,"test",user);
            test.setId(i+1L);
            testList.add(test);
        }
        user.setStudySessions(testList);
        when(utils.get30MinutesFromNow(any(Date.class))).thenReturn(getThirtyMinutesFromNow(current));
        List<StudySessionDTO> result = eventsService.getStudySessionsThatBeginIn30Minutes(user);
        assertEquals(2,result.size());
    }


    //Helper method
    private Date getStartOfDay(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return  cal.getTime();
    }
    //Helper method
    private Date getEndOfDay(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE,59);
        cal.set(Calendar.SECOND,59);
        return  cal.getTime();
    }

    //Helper method
    private Date getThirtyMinutesFromNow(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.MINUTE, 30);
        Date thirtyMinutesFromNow = cal.getTime();
        return thirtyMinutesFromNow;
    }

    private static class MockPrincipal implements java.security.Principal {
        private String name;

        public MockPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
