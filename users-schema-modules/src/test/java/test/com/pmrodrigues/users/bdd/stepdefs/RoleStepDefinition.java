package test.com.pmrodrigues.users.bdd.stepdefs;

import com.pmrodrigues.users.clients.RoleClient;
import com.pmrodrigues.users.dtos.RoleDTO;
import com.pmrodrigues.users.repositories.UserRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

@RequiredArgsConstructor
public class RoleStepDefinition   extends AbstractStepsConfiguration<RoleDTO>{

    public static final String ROLES = "/roles";

    private final UserRepository userRepository;

    private final RoleClient roleClient;

    @When("add {string} to {string} role")
    public void addToGroup(String email, String role) {
        val user = userRepository.findByEmail(email).get().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("role", role);
        map.add("user", user.toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        super.setReturn(getRest().postForEntity( API_URL + ROLES, map , String.class ));

        
    }

    @Then("Check if role {string} returns list with {string}")
    public void checkIfReturnedListContains(String roleName, String email) {
        val response = roleClient.getRole(roleName);
        val role = Optional.of(response.getBody())
                .orElse(new RoleRepresentation());

        



    }
}
