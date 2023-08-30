package test.com.pmrodrigues.users.bdd.stepdefs;


import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.model.User;
import io.cucumber.java.After;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import test.com.pmrodrigues.users.bdd.integrations.UserRestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {UserApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor
public class UserStepsConfigurations extends AbstractStepsConfiguration{

    private final UserRestClient userRestClient = userRestClient();

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
        val user = User.builder().email(email).build();
        return this.userRestClient.search(user)
                .getUsers()
                .stream()
                .findFirst()
                .get();

    }

    @Given("An {string} user")
    public void givenAnNewUserAsUserType(String userType) {
        userRestClient.auth(userType);
    }

    @Transactional
    @Given("the following users")
    public void givenAListOfUsers(List<User> users) {
        users.stream().forEach(user -> userRestClient.create(user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    @Given("Id by {string} of {string}")
    public void givenThenIdOfUserByPropertyValue(String propertyName, String value) {
        var user = new User();
        userRestClient.setValue(user, propertyName, value);
        user = userRestClient.search(user).getUsers()
                .stream()
                .findFirst().orElse(new User());

        userRestClient.setEntity(user);
        userRestClient.setId(user.getId());

    }

    @Given("a new user as {string} , {string} and {string}")
    public void givenANewUserAs(String email, String firstName , String lastName) {
        userRestClient.create(email, firstName, lastName);

    }

    @When("Create a new user with email {string} firstName {string} and lastName {string}")
    public void whenCreateANewUserAs(String email, String firstName, String lastName) {
        userRestClient.create(email, firstName, lastName);
    }

    @SneakyThrows
    @When("Update {string} to {string}")
    public void whenUpdatePropertyOfUser(String propertyName, String newValue) {
        this.userRestClient.getById(this.userRestClient.getId());
        assertEquals(HttpStatus.OK, userRestClient.getHttpStatus());
        userRestClient.update(propertyName, newValue);

    }

    @SneakyThrows
    @When("I filter by {string} as {string}")
    public void whenISearchAnUserBy(String propertyName, String value) {

        val user = new User();
        userRestClient.setValue(user, propertyName, value);
        userRestClient.search(user);

    }

    @SneakyThrows
    @When("List all users")
    public void whenIListAllUser() {

        val user = new User();
        userRestClient.search(user);
    }



    @When("Delete user")
    public void deleteUser() {
        this.userRestClient.delete();
        whenIListAllUser();

    }


    @SneakyThrows
    @Then("User has a {string} defined")
    public void thenCheckIfThePropertyWasSet(String propertyName) {
        userRestClient.getById(userRestClient.getEntity().getId());
        checkIfStatusCodeIsStatusCode(200);
        assertNotNull(userRestClient.getValue(propertyName));
    }



    @Then("User has firstName equals to {string} and lastName equals to {string} and email equals to {string}")
    public void checkIfUserReturnedCorrectly(String firstName, String lastName, String email) {

        checkIfStatusCodeIsStatusCode(200);
        val user = (User)this.userRestClient.getEntity();
        assertEquals(user.getFirstName(), firstName);
        assertEquals(user.getLastName(), lastName);
        assertEquals(user.getEmail(), email);
    }

    @Then("returned users list as")
    public void willReturn(List<User> expectedUsers) {

        val founded = userRestClient.getUsers();
        assertTrue(founded.stream().allMatch(u -> expectedUsers.contains(u)));
    }

    @SneakyThrows
    @Then("User has {string} equals to {string}")
    public void checkValue(String propertyName, String value) {

        val read = userRestClient.getValue(propertyName);
        assertEquals(value, read);
    }

    @After
    @Transactional
    public void afterAll() {

        val user = new User();
        userRestClient.setValue(user, "email", "test.com");
        val users = userRestClient.search(user).getUsers();
        users.forEach( u -> userRestClient.delete(u));


    }

    @Then("Check if statusCode is {int}")
    public void checkIfStatusCodeIsStatusCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        assertEquals(httpStatus, userRestClient.getHttpStatus());
    }

}
