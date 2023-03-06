package test.com.pmrodrigues.users.bdd.stepdefs;


import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import test.com.pmrodrigues.users.helper.HelperPage;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.USER;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.USER_ID;

@ExtendWith({SpringExtension.class})
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStepsConfigurations extends AbstractStepsConfiguration<User> {

    @Autowired
    private UserService userService;

    @DataTableType
    public User userEntry(Map<String, String> entry) {

        return User.builder()
                .email(entry.get("email"))
                .firstName(entry.get("firstName"))
                .lastName(entry.get("lastName"))
                .build();
    }

    @ParameterType(value = ".*", name = "email")
    public User getByEmail(String email){
        return userService.findAll(User.builder().email(email).build(), PageRequest.of(0,1))
                .getContent()
                .stream()
                .findFirst()
                .get();
    }

    @Given("An {string} user")
    public void givenAnNewUserAsUserType(String userType) {
        generateToken("admin", "admin");
    }

    @Transactional
    @Given("the following users")
    public void givenAListOfUsers(List<User> users) {
        users.stream().forEach(user -> userService.createNewUser(user));
    }

    @Given("Id by {string} of {string}")
    public void givenThenIdOfUserByPropertyValue(String propertyName, String value) {
        val user = new User();
        setValue(propertyName, value, user);
        val id = userService.findAll(user, PageRequest.of(0,1))
                .stream()
                .findFirst().map(User::getId)
                .orElse(null);

        super.put(USER_ID, id);

    }

    @Given("a new user as {string} , {string} and {string}")
    public void givenANewUserAs(String email, String firstName , String lastName) {
        var user = User.builder().firstName(firstName).lastName(lastName).email(email).build();
        user = userService.createNewUser(user);
        put(USER , user);
        put(USER_ID, user.getId());
        generateToken(user.getEmail(), user.getPassword());

    }

    @When("Create a new user with email {string} firstName {string} and lastName {string}")
    public void whenCreateANewUserAs(String email, String firstName, String lastName) {

        val user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        super.postForLocation("/users", user, USER_ID, USER);


    }

    @SneakyThrows
    @When("Update {string} to {string}")
    public void whenUpdatePropertyOfUser(String propertyName, String newValue) {
        val returned = super.getForEntity("/users/" + get(USER_ID), USER_ID, USER);
        assertEquals(HttpStatus.OK, returned.getStatusCode());

        val user = returned.getBody();
        setValue(propertyName, newValue, user);

        super.updateEntity("/users/" + get(USER_ID), user);

    }

    @SneakyThrows
    @When("I filter by {string} as {string}")
    public void whenISearchAnUserBy(String propertyName, String value) {

        val user = new UserDTO();
        setValue(propertyName, value, user);
        super.searchBySample("/users", HttpMethod.GET, new HttpEntity(user), new ParameterizedTypeReference<HelperPage<User>>(){});

    }

    @SneakyThrows
    @When("List all users")
    public void whenIListAllUser() {

        val user = new UserDTO();
        val entity = new HttpEntity<>(user);
        super.searchBySample("/users",HttpMethod.GET, new HttpEntity(user), new ParameterizedTypeReference<HelperPage<User>>(){});
    }



    @When("Delete user")
    public void deleteUser() {
        super.delete("/users/" + get(USER_ID));
        whenIListAllUser();

    }


    @SneakyThrows
    @Then("User has a {string} defined")
    public void thenCheckIfThePropertyWasSet(String propertyName) {

        var user = (User)get(USER);

        getForEntity("/users/" + user.getId(), USER_ID, USER);
        checkIfStatusCodeIsStatusCode(200);
        user = super.getEntity();

        assertNotNull(getValue(propertyName, user));
    }



    @Then("User has firstName equals to {string} and lastName equals to {string} and email equals to {string}")
    public void checkIfUserReturnedCorrectly(String firstName, String lastName, String email) {

        getForEntity("/users/" + get(USER_ID), USER_ID, USER);
        checkIfStatusCodeIsStatusCode(200);
        val user = (User)get(USER);

        assertEquals(user.getFirstName(), firstName);
        assertEquals(user.getLastName(), lastName);
        assertEquals(user.getEmail(), email);
    }

    @Then("returned users list as")
    public void willReturn(List<User> expectedUsers) {

        val founded = super.listEntity();

        assertTrue(founded.stream().allMatch(u -> expectedUsers.contains(u)));
    }

    @SneakyThrows
    @Then("User has {string} equals to {string}")
    public void checkValue(String propertyName, String value) {

        super.getForEntity("/users/" + get(USER_ID), USER_ID, USER);
        checkIfStatusCodeIsStatusCode(200);
        val user = super.getEntity();

        val read = getValue(propertyName, user);
        assertEquals(value, read);
    }

    @After
    @Transactional
    public void afterAll() {

        userService.findAll(User.builder().email("test.com").build(), PageRequest.of(0,1000))
                .stream()
                .forEach(user -> userService.delete(user));


    }

    @Then("Check if statusCode is {int}")
    public void checkIfStatusCodeIsStatusCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        assertEquals(httpStatus, super.getStatusCode());
    }

}
