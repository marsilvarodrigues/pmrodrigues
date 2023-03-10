package test.com.pmrodrigues.users.bdd.runners;

import com.pmrodrigues.users.UserApplication;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-report.html", "summary"},
        dryRun = true,
        monochrome = true,
        features = "src/test/resources/features/addresses.feature",
        glue = {"test.com.pmrodrigues.users.bdd","test.com.pmrodrigues.users.bdd.runners","test.com.pmrodrigues.users.bdd.stepdefs"})
@ExtendWith({SpringExtension.class})
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ITAddressCucumberRunner {
}
