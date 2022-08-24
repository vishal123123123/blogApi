package com.blogapi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blogapi.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	Optional<User> findByName(String userName);
	
	Optional<User> findByEmail(String email);

}
