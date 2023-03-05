package test.com.pmrodrigues.users.bdd.stepdefs;

import com.pmrodrigues.users.service.AddressService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class AddressStepsConfigurations  extends AbstractStepsConfiguration {

    @Autowired
    private AddressService addressService;

    @Given("I save my address as {string}, {string}, {string}, {string}, {string}, {string}")
    public void iAskToSaveMyAddressAs(String state, String addressType, String address, String zipcode, String neightboor, String city) {
    }

    @Then("Address has a {string} defined")
    public void addressHasADefined(String propertyName) {
    }

    @When("I change {string} to {string}")
    public void iChangeTo(String propertyName, String value) {

    }

    @Then("Address with {string} is equals to {string}")
    public void addressWithPropertyIsEqualsTo(String propertyName, String value) {
    }

    @When("I delete my address by {string}")
    public void whenIDeleteMyAddressByProperty(String propertyName) {

    }

    @Then("My Address needs to be empty")
    public void thenIDontHaveAListOfAddress() {
    }
}
