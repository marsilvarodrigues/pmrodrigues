package test.com.pmrodrigues.users.bdd.stepdefs;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import io.cucumber.java.After;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import test.com.pmrodrigues.users.bdd.integrations.AddressRestClient;
import test.com.pmrodrigues.users.bdd.integrations.UserRestClient;

import java.util.List;
import java.util.Map;

import static com.pmrodrigues.users.specifications.SpecificationAddress.owner;
import static org.junit.jupiter.api.Assertions.*;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.USER;

@RequiredArgsConstructor
public class AddressStepsConfiguration extends AbstractStepsConfiguration<Address> {

    private final AddressRepository addressRepository;

    private final StateRepository stateRepository;

    private final AddressRestClient addressRestClient = addressRestClient();
    private final UserRestClient userRestClient = userRestClient();

    @ParameterType(value = ".*", name = "addressType")
    public AddressType getAddressType(String addressType) {
        return AddressType.valueOf(addressType);
    }

    @ParameterType(value = ".*", name = "state")
    public State getState(String state) {
        return stateRepository.findByCode(state).get();
    }

    @DataTableType
    public Address addressEntry(Map<String, String> entry) {

            return Address.builder().state(this.getState(entry.get("state")))
                    .addressType(this.getAddressType(entry.get("addressType")))
                    .address1(entry.get("address"))
                    .zipcode(entry.get("zipcode"))
                    .neighbor(entry.get("neighbor"))
                    .city(entry.get("city"))
                    .build();
    }


    @When("I save my address as {state}, {addressType}, {string}, {string}, {string}, {string}")
    public void createMyAddress(State state, AddressType addressType, String street, String zipcode, String neighbor, String city) {
        addressRestClient.create(state, addressType, street, zipcode, neighbor, city);

    }

    @Then("Address has a {string} defined")
    public void addressHasADefined(String propertyName) {

        addressRestClient.getById(addressRestClient.getId());
        assertNotNull(addressRestClient.getValue(propertyName));
    }

    @When("I change {string} to {string}")
    public void updateAddressField(String propertyName, String value) {
        addressRestClient.getById(addressRestClient.getId());
        addressRestClient.setValue(propertyName, value);
    }

    @Then("Address with {string} is equals to {string}")
    public void addressWithPropertyIsEqualsTo(String propertyName, String expectedValue) {
        addressRestClient.getById(addressRestClient.getId());
        val value = addressRestClient().getValue(propertyName);
        assertNotNull(value);
        assertEquals(expectedValue, value);
    }

    @When("I delete my address")
    public void whenIDeleteMyAddressByProperty() {
        addressRestClient.delete();
    }

    @Then("My Address needs to be empty")
    public void thenIDontHaveAListOfAddress() {
        val addresses = addressRepository.findAll(owner((User) get(USER)));
        assertTrue(addresses.isEmpty());

    }

    @And("Owner is the who logged")
    public void ownerIsTheWhoLogged() {
        val address = addressRestClient.getEntity();
        val logged = userRestClient.getEntity();

        assertEquals(logged, address.getOwner());
    }

    @Given("a list of address as")
    public void givenMyListOfAddress(List<Address> addresses) {
        addresses
                .forEach(a -> {
                    val user = (User) get(USER);
                    a.setOwner(user);
                    a.setCreatedBy(user.getId());
                    a.setUpdatedBy(user.getId());
                    addressRepository.save(a);
                });

    }

    @When("I ask to list my address will return")
    public void returnMyListOfAddress() {

        val address = Address.builder().build();
        addressRestClient.search(address);

    }

    @Then("my address list is")
    public void willReturn(List<Address> expectedAddress) {

        val founded = addressRestClient.getAddresses();

        assertEquals(expectedAddress.size(), founded.size());
        assertTrue(expectedAddress.containsAll(founded));
    }

    @After
    @Transactional
    public void afterAll() {

        val addresses = addressRepository.findAll(owner(userRestClient.getEntity()));
        addressRepository.deleteAll(addresses);

        val user = User.builder().email("test@test_address.com").build();
        val users = userRestClient.search(user).getUsers();
        users.forEach(userRestClient::delete);

    }

    @When("I search by state {state}")
    public void iSearchByStateRJ(State state) {

        val address = Address.builder().state(state).build();
        addressRestClient.search(address);
    }

    @When("I search by addressType {addressType}")
    public void iSearchByAddressType(AddressType addressType) {
        val address = Address.builder().addressType(addressType).build();
        addressRestClient.search(address);
    }

    @When("I search by city {string}")
    public void iSearchByCity(String city) {
        val address = Address.builder().city(city).build();
        addressRestClient.search(address);
    }

}
