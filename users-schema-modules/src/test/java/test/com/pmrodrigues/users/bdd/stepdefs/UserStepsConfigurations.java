package test.com.pmrodrigues.users.bdd.stepdefs;


import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import test.com.pmrodrigues.users.helper.HelperPage;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@CucumberContextConfiguration
public class UserStepsConfigurations extends AbstractStepsConfiguration {

    private RestTemplate rest;
    private URI response;
    @Autowired
    private UserService userService;
    private ResponseEntity returned;
    private UUID userId;


    @Given("An {string} user")
    public void anAdmin(String userType) {
        if ("admin".equalsIgnoreCase(userType)) {
            this.rest = generateToken("admin", "admin");
        }
    }

    @DataTableType
    public User userEntry(Map<String, String> entry) {

        return User.builder()
                .email(entry.get("email"))
                .firstName(entry.get("firstName"))
                .lastName(entry.get("lastName"))
                .build();
    }

    @Transactional
    @Given("the following users")
    public void givenAListOfUsers(List<User> users) {
        users.stream().forEach(user -> userService.createNewUser(user));
    }

    @When("Create a new user with email {string} firstName {string} and lastName {string}")
    public void createANewUserWithEmailFirstNameAndLastName(String email, String firstName, String lastName) {

        val user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        this.response = rest.postForLocation("/users", user);

    }

    @SneakyThrows
    @When("Update {string} to {string}")
    public void updateFistNameToChangeName(String propertyName, String newValue) {
        val returned = rest.getForEntity(API_URL + response, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());

        val user = returned.getBody();
        setValue(propertyName, newValue, user);

        val entity = new HttpEntity(user);
        this.returned = rest.exchange(API_URL + response, HttpMethod.PUT, entity, User.class);
    }

    @SneakyThrows
    @When("I filter by {string} as {string}")
    public void askToFilterByEmailAs(String propertyName, String value) {

        val user = new UserDTO();
        setValue(propertyName, value, user);

        val entity = new HttpEntity<>(user);

        this.returned = rest.exchange("/users", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<User>>(){} );
    }

    @SneakyThrows
    @When("List all users")
    public void getListWithoutFilter() {

        val user = new UserDTO();
        val entity = new HttpEntity<>(user);

        this.returned = rest.exchange("/users", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<User>>(){} );
    }


    @SneakyThrows
    @Then("User has a {string} defined")
    public void checkIfWasCreated(String propertyName) {

        val returned = rest.getForEntity(API_URL + this.response, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());
        val user = returned.getBody();

        assertNotNull(getValue(propertyName, user));
    }

    @Then("Check if statusCode is {int}")
    public void checkIfStatusCodeIsStatusCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        assertEquals(httpStatus, returned.getStatusCode());
    }

    @Then("User has firstName equals to {string} and lastName equals to {string} and email equals to {string}")
    public void checkIfUserReturnedCorrectly(String firstName, String lastName, String email) {

        val returned = rest.getForEntity(API_URL + this.response, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());
        val user = (User)returned.getBody();

        assertEquals(user.getFirstName(), firstName);
        assertEquals(user.getLastName(), lastName);
        assertEquals(user.getEmail(), email);
    }


    @After
    @Transactional
    public void afterAll() {

        val emails = List.of("to_insert@test.com", "to_get@test.com", "to_update@test.com");
        emails.stream()
                .map(email -> User.builder().email(email).build())
                .map( user -> userService.findAll(user, PageRequest.of(0,1)))
                .filter(p -> !p.isEmpty())
                .map(p -> p.stream().findFirst().get())
                .forEach(user -> userService.delete(user));


    }


    @Then("returned users list as")
    public void willReturn(List<User> expectedUsers) {

        val founded = ((HelperPage<User>) this.returned.getBody()).getContent();

        assertTrue(founded.stream().allMatch(u -> expectedUsers.contains(u)));
    }

    @Given("Id by {string} of {string}")
    public void idOf(String propertyName, String value) {
        val user = new User();
        setValue(propertyName, value, user);

        this.userId = userService.findAll(user, PageRequest.of(0,1))
                .stream()
                .findFirst().map(User::getId)
                .orElse(null);
    }

    @When("Delete user")
    public void deleteUser() {
        this.rest.delete( "/users/" + this.userId);
        getListWithoutFilter();

    }

    @SneakyThrows
    @Then("User has {string} equals to {string}")
    public void checkValue(String propertyName, String value) {
        val returned = rest.getForEntity("/users/" + this.userId, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());

        val user = returned.getBody();
        val read = getValue(propertyName, user);
        assertEquals(value, read);
    }
}
