package es.marta.tfg.carescan.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import es.marta.tfg.carescan.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);



}
