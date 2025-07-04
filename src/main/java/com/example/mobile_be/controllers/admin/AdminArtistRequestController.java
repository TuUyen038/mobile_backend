package com.example.mobile_be.controllers.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.repository.ArtistRequestRepository;
import com.example.mobile_be.repository.UserRepository;

import lombok.Generated;

@RequestMapping({"/api/artist-request"})
@RestController
public class AdminArtistRequestController {
   private final UserRepository userRepository;
   private final ArtistRequestRepository artistRequestRepository;

   @Generated
   public AdminArtistRequestController(final UserRepository userRepository, final ArtistRequestRepository artistRequestRepository) {
      this.userRepository = userRepository;
      this.artistRequestRepository = artistRequestRepository;
   }
}