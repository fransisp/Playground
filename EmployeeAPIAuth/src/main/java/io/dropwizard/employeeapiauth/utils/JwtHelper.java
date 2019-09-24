package io.dropwizard.employeeapiauth.utils;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.KeyGenerator;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import lombok.extern.log4j.Log4j;

@Log4j
public class JwtHelper {
	private static JwtHelper jwTokenHelper = null;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long EXPIRATION_LIMIT = 30;
    private static final int KEY_BIT_SIZE = 256;
    private final Key secret;

    private KeyGenerator keyGenerator;

    private JwtHelper() {
    	try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(KEY_BIT_SIZE, secureRandom);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error generating key", e);
			throw new ExceptionInInitializerError(e);
		}
    	secret = keyGenerator.generateKey();
    }

    public static JwtHelper getInstance() {
        if (jwTokenHelper == null)
            jwTokenHelper = new JwtHelper();
        return jwTokenHelper;
    }

    public String issueToken(String login, String issuerURI) {
        return JWT.create()
                .withSubject(login)
                .withIssuer(issuerURI)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(EXPIRATION_LIMIT, ChronoUnit.MINUTES)))
                .sign(Algorithm.HMAC512(secret.getEncoded()));
    }
    
    public String checkToken (String token) {
        return JWT.require(Algorithm.HMAC512(secret.getEncoded()))
        		.build()
        		.verify(token)
        		.getSubject();
    }
}
