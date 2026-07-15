package br.com.ecommerce.auth.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ecommerce.auth.exception.UserNotFoundException;
import br.com.ecommerce.auth.service.TokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private final JdbcTemplate jdbcTemplate;

    public AuthController(TokenService tokenService, JdbcTemplate jdbcTemplate) {
        this.tokenService = tokenService;
        this.jdbcTemplate = jdbcTemplate;
    }


    @GetMapping
    public ResponseEntity<UserDTO> getUserIdAndRoleByToken(@RequestHeader("Authorization") String token) {
        String username = this.tokenService.validateToken(token);
        UserDTO userData = this.findUser(username);
        return ResponseEntity.ok().body(userData);
    }

    private UserDTO findUser(String username) {
        String query = "SELECT id, username, role FROM users WHERE username = ?";
        
        try {
            return this.jdbcTemplate.queryForObject(
                query,
                (rs, rowNum) -> new UserDTO(
                    String.valueOf(rs.getLong("id")),
                    rs.getString("username"),
                    rs.getString("role")
                ),
                username
            );
            
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            throw new UserNotFoundException(ex);
        }
    }
}
