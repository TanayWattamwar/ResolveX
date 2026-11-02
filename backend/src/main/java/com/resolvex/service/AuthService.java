package com.resolvex.service;
import com.resolvex.dto.ApiDtos.*; import com.resolvex.entity.*; import com.resolvex.repository.UserRepository; import com.resolvex.security.JwtService; import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.web.server.ResponseStatusException;
@Service public class AuthService {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt;
 public AuthService(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
 public AuthResponse register(RegisterRequest r){if(users.existsByEmail(r.email().toLowerCase()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered");User u=new User();u.setName(r.name().trim());u.setEmail(r.email().toLowerCase());u.setPassword(encoder.encode(r.password()));u.setRole(Role.EMPLOYEE);users.save(u);return response(u);}
 public AuthResponse login(LoginRequest r){User u=users.findByEmail(r.email().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));if(!encoder.matches(r.password(),u.getPassword()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");return response(u);}
 private AuthResponse response(User u){return new AuthResponse(jwt.generate(u),u.getId(),u.getName(),u.getEmail(),u.getRole());}
}
