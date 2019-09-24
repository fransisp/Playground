package io.dropwizard.employeeapiauth.model;

import org.hibernate.validator.constraints.Length;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author fransiskus.prayuda
 * Small model class to hold user data
 */
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class User {

    @Length(max = 32)
    private String username;
    @Length(max = 128)
    private String password;

    public User( String username, String password) {
        this.username = username;
        this.password = password;
    }

}
