package test.com.pmrodrigues.users.bdd.integrations;

import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.model.User;
import lombok.Getter;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserRestClient extends AbstractRestClient<User> {
    private final String API_URL = "http://localhost:8143/users";

    @Getter private List<User> users;

    public UserRestClient(String serverUrl, String realm, String clientId, String clientSecret) {
        super(serverUrl, realm, clientId, clientSecret);
    }

    @Override
    public String getURL() {
        return API_URL;
    }
    public UserRestClient create(String email, String firstName , String lastName) {
        val user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        return create(user);
    }

    private UserRestClient create(User user) {
        val entity = super.post(user);
        this.setEntity(entity);
        this.setId(entity.getId());
        return this;
    }

    public UserRestClient update(String propertyName, String propertyValue) {
        setEntity(super.get());
        this.setValue(propertyName, propertyValue);
        this.put();
        return this;
    }

    public UserRestClient getById(UUID id) {
        super.get(id);
        return this;
    }

    public UserRestClient delete() {
        super.deleteEntity();
        return this;
    }

    public UserRestClient search(User sample) {
        this.users = this.search(new HttpEntity<>(sample));
        return this;
    }

    public UserRestClient auth(String username, String password) {
        this.generateToken(username, password);
        return this;
    }

    public UserRestClient auth(String role){
        switch (role){
            case Security.SYSTEM_ADMIN:
                this.generateToken("admin", "admin");
                return this;
            default:
                return null;
        }
    }

    public UserRestClient create(String email, String firstName, String lastName, String password) {
        val user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(password)
                .build();

        create(user);
        val entity = this.getEntity();
        entity.setPassword(user.getPassword());
        this.setEntity(entity);
        return this;
    }
}
