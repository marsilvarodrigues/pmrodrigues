package test.com.pmrodrigues.users.bdd.stepdefs;

import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.dtos.StateDTO;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import test.com.pmrodrigues.users.helper.HelperPage;

import java.util.List;
import java.util.Map;

import static com.pmrodrigues.users.specifications.SpecificationAddress.owner;
import static org.junit.jupiter.api.Assertions.*;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.*;

@RequiredArgsConstructor
public class AddressStepsConfigurations  extends AbstractStepsConfiguration<Address> {

    public static final String ADDRESSES = "/addresses";
    private final AddressRepository addressRepository;

    private final StateRepository stateRepository;

    private final UserService userService;

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
                    .neightboor(entry.get("neightboor"))
                    .city(entry.get("city"))
                    .build();
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

    @When("I delete my address")
    public void whenIDeleteMyAddressByProperty() {
        delete(ADDRESSES + "/" + get(ADDRESS_ID));
    }

    @Then("My Address needs to be empty")
    public void thenIDontHaveAListOfAddress() {

        val addresses = addressRepository.findAll(owner((User) get(USER)));
        assertTrue(addresses.isEmpty());

    }

    @And("Owner is the who logged")
    public void ownerIsTheWhoLogged() {
        val address = (Address)get(ADDRESS);
        val logged = (User)get(USER);

        assertEquals(logged, address.getOwner());
    }

    @Given("a list of address as")
    public void givenMyListOfAddress(List<Address> addresses) {
        addresses.stream()
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

        val address = new AddressDTO(null, null, null, null, null, null, null, null);
        val entity = new HttpEntity<>(address);
        super.searchBySample("/addresses", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<Address>>(){});
    }

    @Then("my address list is")
    public void willReturn(List<Address> expectedAddress) {

        val founded = super.listEntity();

        assertEquals(expectedAddress.size(), founded.size());
        assertTrue(founded.stream().allMatch(u -> expectedAddress.contains(u)));
    }

    @After
    @Transactional
    public void afterAll() {

        val user = (User)get(USER);
        val addresses = addressRepository.findAll(owner(user));
        addressRepository.deleteAll(addresses);
        userService.delete(user);

    }

    @When("I search by state {state}")
    public void iSearchByStateRJ(State state) {
        val stateDTO = StateDTO.builder()
                .id(state.getId())
                .code(state.getCode())
                .name(state.getName())
                .build();
        val address = AddressDTO.builder().state(stateDTO).build();
        val entity = new HttpEntity<>(address);
        super.searchBySample("/addresses", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<Address>>(){});
    }

    @When("I search by addressType {addressType}")
    public void iSearchByAddressType(AddressType addressType) {
        val address = AddressDTO.builder().addressType(addressType).build();
        val entity = new HttpEntity<>(address);
        super.searchBySample("/addresses", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<Address>>(){});
    }

    @When("I search by city {string}")
    public void iSearchByCity(String city) {
        val address = AddressDTO.builder().city(city).build();
        val entity = new HttpEntity<>(address);
        super.searchBySample("/addresses", HttpMethod.GET, entity, new ParameterizedTypeReference<HelperPage<Address>>(){});
    }
}
