package com.example.mobile_be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;

@Service
public class UserService implements UserDetailsService {
 @Autowired
 private UserRepository userRepository;

 @Autowired
 private PasswordEncoder passwordEncoder;

 
 @Override
 public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
  User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));
  return new UserDetailsImpl(user);
 }

 public User register(RegisterRequest request) {
  User user = new User();
  user.setEmail(request.getEmail());
  user.setFirstName(request.getFirstName());
  user.setLastName(request.getLastName());
  user.setPassword(passwordEncoder.encode(request.getPassword()));
  user.setRole(request.getRole());
  user.setFullName(request.getFirstName() + " " + request.getLastName());
  user.setIsVerifiedArtist(false);
  return userRepository.save(user);
 }

 public User authenticate(String email, String rawPassword) {
  User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

  if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
   throw new BadCredentialsException("Wrong password");
  }

  return user;
 }
}
