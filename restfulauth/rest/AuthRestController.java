package com.example.restfulauth.rest;

import com.example.restfulauth.entity.Roles;
import com.example.restfulauth.entity.Status;
import com.example.restfulauth.entity.User;
import com.example.restfulauth.repository.UserRepository;
import com.example.restfulauth.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AuthRestController{

    private PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    private final AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthRestController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest authRequest){

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User doesnt exist"));
        String token = "";
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            token = jwtTokenProvider.creatToken(authRequest.getEmail(), user.getRoles().name());
            Map<Object, Object> response = new HashMap<>();
            response.put("username", authRequest.getEmail());
            response.put("token", token);
            return ResponseEntity.ok(response);
        }catch (AuthenticationException exc){
            return new ResponseEntity<>("Invalid email/password combination", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/api/v1/logout")
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.logout(httpServletRequest, httpServletResponse, null);
    }

    @PostMapping("/api/v1/registration")
    public ResponseEntity signUp(@RequestBody SUPRequest supRequest){

        String token = "";
        if(!userRepository.findByEmail(supRequest.getEmail()).isEmpty()){
            return new ResponseEntity("User already exist", HttpStatus.FORBIDDEN);
        }else {
            User user = new User();
            user.setName(supRequest.getName());
            user.setEmail(supRequest.getEmail());
            user.setPassword(passwordEncoder().encode(supRequest.getPassword()));
            user.setRoles(Roles.USER);
            user.setStatus(Status.ACTIVE);
            token = jwtTokenProvider.creatToken(user.getEmail(), user.getRoles().name());
            Map<Object, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("token", token);
            this.userRepository.save(user);
            return ResponseEntity.ok(response);
        }
    }
}
