package test.com.pmrodrigues.users.bdd.stepdefs;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.service.AddressService;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.*;

public class AddressStepsConfigurations  extends AbstractStepsConfiguration<Address> {

    public static final String ADDRESSES = "/addresses";
    @Autowired
    private AddressService addressService;

    @Autowired
    private StateRepository stateRepository;

    @ParameterType(value = ".*", name = "addressType")
    public AddressType getAddressType(String addressType) {
        return AddressType.valueOf(addressType);
    }

    @ParameterType(value = ".*", name = "state")
    public State getState(String state) {
        return stateRepository.findByCode(state).get();
    }


    @Given("I save my address as {state}, {addressType}, {string}, {string}, {string}, {string}")
    public void createMyAddress(State state, AddressType addressType, String street, String zipcode, String neightboor, String city) {
        val address = Address.builder().state(state).addressType(addressType).address1(street).zipcode(zipcode).neightboor(neightboor).city(city).build();

        postForLocation(ADDRESSES, address, ADDRESS_ID , ADDRESS);

    }

    @Then("Address has a {string} defined")
    public void addressHasADefined(String propertyName) {
        getForEntity(ADDRESSES + "/" + get(ADDRESS_ID), ADDRESS_ID, ADDRESS);
        val address = (Address)get(ADDRESS);
        assertNotNull(getValue(propertyName, address));
    }

    @When("I change {string} to {string}")
    public void updateAddressField(String propertyName, String value) {
        val address = (Address)get(ADDRESS);
        setValue(propertyName, value, address);

        super.updateEntity(ADDRESSES + "/" + get(ADDRESS_ID), address);
    }

    @Then("Address with {string} is equals to {string}")
    public void addressWithPropertyIsEqualsTo(String propertyName, String expectedValue) {
        getForEntity(ADDRESSES + "/" + get(ADDRESS_ID), ADDRESS_ID, ADDRESS);
        val address = (Address)get(ADDRESS);
        val value = getValue(propertyName, address);
        assertNotNull(value);
        assertEquals(expectedValue, value);
    }

    @When("I delete my address by {string}")
    public void whenIDeleteMyAddressByProperty(String propertyName) {

    }

    @Then("My Address needs to be empty")
    public void thenIDontHaveAListOfAddress() {
    }

    @And("Owner is the who logged")
    public void ownerIsTheWhoLogged() {
        val address = (Address)get(ADDRESS);
        val logged = (User)get(USER);

        assertEquals(logged, address.getOwner());
    }
}
