package test.com.pmrodrigues.users.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "json:target/cucumber-report.json"},
        dryRun = true,
        features = "classpath:features",
        glue = {"test.com.pmrodrigues.users.bdd.stepdefs"})
public class CucumberRunnerIT {
}
