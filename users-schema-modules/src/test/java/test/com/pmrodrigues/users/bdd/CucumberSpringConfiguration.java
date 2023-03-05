package test.com.pmrodrigues.users.bdd;

import com.pmrodrigues.users.UserApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = UserApplication.class)
public class CucumberSpringConfiguration {
}
