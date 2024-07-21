package com.cydeo.review;

import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.entity.User;
import com.cydeo.exception.TicketingProjectException;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import com.cydeo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private ProjectService projectService;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TaskService taskService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    User user;
    UserDTO userDTO;

//    @BeforeAll
//    public static void SetUpBeforeClass() throws Exception{
//
//    }
//    @AfterAll
//    public static void tearDownAfterClass() throws Exception{
//
//    }
    @BeforeEach
    public void setUp(){
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("user");
        user.setPassWord("Abc1");
        user.setEnabled(true);
        user.setRole(new Role("Manager"));

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("user");
        userDTO.setPassWord("Abc1");
        userDTO.setEnabled(true);

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setDescription("Manager");

        userDTO.setRole(roleDTO);
    }

    private List<User> getUsers(){

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Emily");

        return List.of(user, user2);
    }
    private List<UserDTO> getUserDTOs(){

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("Emily");

        return List.of(userDTO, userDTO2);
    }

    private User getUserWithRole(String role){
        User user3 = new User();

        user3.setUserName("user3");
        user3.setPassWord("Abc1");
        user3.setEnabled(true);
        user3.setIsDeleted(false);
        user3.setRole(new Role(role));

        return user3;
    }
    @AfterEach
    public void tearDown(){

    }

    @Test
    public void test(){

    }

    @Test
    public void should_list_all_users(){
        // Given - Preparation
        when(userRepository.findAllByIsDeletedOrderByFirstNameDesc(false)).thenReturn(getUsers());

        when(userMapper.convertToDto(user)).thenReturn(userDTO);
        when(userMapper.convertToDto(getUsers().get(1))).thenReturn(getUserDTOs().get(1));
        //when(userMapper.convertToDto(any(User.class))).thenReturn(userDto, getUserDTOs().get(1)); // this line can replace the two lines above

        List<UserDTO> expectedList = getUserDTOs();

        // When - Action
        List<UserDTO> actualList = userService.listAllUsers();

        // Then - Assertion/Verification
        //assertEquals(expectedList, actualList);

        // AssertJ
        assertThat(actualList).usingRecursiveComparison().isEqualTo(expectedList);

        verify(userRepository, times(1)).findAllByIsDeletedOrderByFirstNameDesc(false);
        verify(userRepository, never()).findAllByIsDeletedOrderByFirstNameDesc(true);
    }

    @Test
    public void should_throw_noSuchElementException_when_user_not_found(){
        // Given
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(null);
//        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);

        // when + then
//        Throwable actualException = assertThrows(NoSuchElementException.class, () -> userService.findByUserName("SomeUsername"));
//        Throwable actualException = assertThrows(RuntimeException.class, () -> userService.findByUserName("SomeUsername"));
        Throwable actualException = assertThrowsExactly(NoSuchElementException.class, () -> userService.findByUserName("SomeUsername"));
        assertEquals("User not found.", actualException.getMessage());

        //AssertJ
//        Throwable actualException = catchThrowable(() -> userService.findByUserName("SomeUsername"));
    }


    @Test
    public void should_encode_user_password_on_save_operation(){
        // Given
        when(userMapper.convertToEntity(any(UserDTO.class))).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);
        when(passwordEncoder.encode(anyString())).thenReturn("some-Password");

        String expectedPassword = "some-Password";

        // When
        UserDTO savedUser = userService.save(userDTO);

        // Then
        assertEquals(expectedPassword, savedUser.getPassWord());

        // verify that passwordEncoder is executed
        verify(passwordEncoder, times(1)).encode(anyString());

    }
    // 	User Story 2: As an admin, I shouldn't be able to delete a manager user,
    // 	if that manager has projects linked to them to prevent data loss.
    //
    //	Acceptance Criteria:
    //
    //	1 - The system should prevent a manager user from being deleted
    //	if they have projects linked to them.
    //	2 - An error message should be displayed to the user if they attempt
    //	to delete a manager user with linked projects.
    //
    //	User Story 3: As an admin, I shouldn't be able to delete an employee user,
    //	if that employee has tasks linked to them to prevent data loss.
    //
    //	Acceptance Criteria:
    //
    //	1 - The system should prevent an employee user from being deleted
    //	if they have tasks linked to them.
    //	2 - An error message should be displayed to the user if they attempt
    //	to delete an employee user with linked tasks.
    @Test
    public void should_encode_user_password_on_update_operation(){
        // Given
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);
        when(userMapper.convertToEntity(any(UserDTO.class))).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);
        when(passwordEncoder.encode(anyString())).thenReturn("some-Password");

        String expectedPassword = "some-Password";

        // When
        UserDTO updatedUser = userService.update(userDTO);

        // Then
        assertEquals(expectedPassword, updatedUser.getPassWord());

        // verify that passwordEncoder is executed
        verify(passwordEncoder, times(1)).encode(anyString());

    }

    @Test
    void should_delete_manager() throws TicketingProjectException {
        // Given - Preparation
        User managerUser = getUserWithRole("Manager");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(managerUser);
        when(userRepository.save(any())).thenReturn(managerUser);
        when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());

        // When - Action
        userService.delete(managerUser.getUserName());

        //Then - Assertion / verification
        assertTrue(managerUser.getIsDeleted());
        assertNotEquals("user3", managerUser.getUserName());
    }

    @Test
    void should_delete_employee() throws TicketingProjectException {
        // Given - Preparation
        User employeeUser = getUserWithRole("Employee");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(employeeUser);
        when(userRepository.save(any())).thenReturn(employeeUser);
        when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());

        // When - Action
        userService.delete(employeeUser.getUserName());

        //Then - Assertion / verification
        assertTrue(employeeUser.getIsDeleted());
        assertNotEquals("user3", employeeUser.getUserName());
    }

}
