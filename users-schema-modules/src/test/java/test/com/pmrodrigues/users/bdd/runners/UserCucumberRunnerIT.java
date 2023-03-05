package test.com.pmrodrigues.users.bdd.runners;

import com.pmrodrigues.users.UserApplication;
import io.cucumber.junit.CucumberOptions;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;


@CucumberOptions(plugin = {"pretty", "json:target/cucumber-report.json"},
        dryRun = true,
        features = "classpath:features/users.feature",
        glue = {"test.com.pmrodrigues.users.bdd","test.com.pmrodrigues.users.bdd.stepdefs"},
        monochrome = true)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserCucumberRunnerIT {
}
