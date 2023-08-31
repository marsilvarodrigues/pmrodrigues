package test.com.pmrodrigues.users.bdd.integrations;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.enums.AddressType;
import lombok.Getter;
import lombok.val;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.UUID;

public class AddressRestClient extends AbstractRestClient<Address> {

    private final String API_URL = "http://localhost:8143/addresses";

    @Getter
    private List<Address> addresses;

    public AddressRestClient(String serverUrl, String realm, String clientId, String clientSecret) {
        super(serverUrl, realm, clientId, clientSecret);
    }

    @Override
    public String getURL() {
        return API_URL;
    }

    public AddressRestClient create(State state, AddressType addressType, String street, String zipcode, String neighbor, String city) {
        val address = Address.builder()
                .state(state)
                .addressType(addressType)
                .address1(street)
                .zipcode(zipcode)
                .neighbor(neighbor)
                .city(city)
                .build();

        this.setEntity(this.post(address));
        return this;
    }

    public AddressRestClient getById(UUID id) {
        super.get(id);
        return this;
    }

    public AddressRestClient delete() {
        super.deleteEntity();
        return this;
    }

    public AddressRestClient search(Address sample) {
        this.addresses = this.search(new HttpEntity<>(sample));
        return this;
    }
}
