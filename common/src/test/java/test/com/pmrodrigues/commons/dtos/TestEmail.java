package test.com.pmrodrigues.commons.dtos;

import com.pmrodrigues.commons.dtos.Email;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestEmail {

    @Test
    public void prepareEmail() {

        val path = "templates/newUser.vm";

        val email = new Email().template(path)
                .set("fullName","Marcelo da Silva Rodrigues")
                .set("password","123456");

        assertNotNull(email.getFormatMessage());

    }

}