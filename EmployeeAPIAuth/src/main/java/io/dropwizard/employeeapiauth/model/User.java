package io.dropwizard.employeeapiauth.model;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
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
