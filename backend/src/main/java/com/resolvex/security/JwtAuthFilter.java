package com.resolvex.security;
import com.resolvex.repository.UserRepository; import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.List;
@Component public class JwtAuthFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserRepository users;
 public JwtAuthFilter(JwtService jwt,UserRepository users){this.jwt=jwt;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String header=req.getHeader("Authorization"); if(header!=null&&header.startsWith("Bearer ")){try{String email=jwt.extractEmail(header.substring(7));var user=users.findByEmail(email).orElse(null);if(user!=null&&jwt.isValid(header.substring(7),email)&&SecurityContextHolder.getContext().getAuthentication()==null){var auth=new UsernamePasswordAuthenticationToken(email,null,List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name())));SecurityContextHolder.getContext().setAuthentication(auth);}}catch(Exception ignored){}}
  chain.doFilter(req,res);
 }
}
