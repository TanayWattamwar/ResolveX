package com.resolvex.repository;
import com.resolvex.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<User,Long>{Optional<User> findByEmail(String email); boolean existsByEmail(String email); List<User> findByRole(Role role);}
