package br.com.ecommerce.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.ecommerce.auth.exception.InvalidTokenException;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;


    public String validateToken(String token) {
        String cleanToken = token.replace("Bearer ", "");
        
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.secret);
            return JWT.require(algorithm)
                .withIssuer("ecommerce")
                .build()
                .verify(cleanToken)
                .getSubject();
                
        } catch (JWTVerificationException exception) {
            throw new InvalidTokenException();
        }
    }
}
