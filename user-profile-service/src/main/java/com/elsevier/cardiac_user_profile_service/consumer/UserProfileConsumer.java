package com.elsevier.cardiac_user_profile_service.consumer;

import com.elsevier.cardiac_user_profile_service.entity.Profile;
import com.elsevier.cardiac_user_profile_service.event.UserProfileEvent;
import com.elsevier.cardiac_user_profile_service.exception.InvalidUserProfileEventException;
import com.elsevier.cardiac_user_profile_service.exception.ProfilePersistenceException;
import com.elsevier.cardiac_user_profile_service.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserProfileConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserProfileConsumer.class);

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;


    public UserProfileConsumer(
            ProfileRepository profileRepository,
            ObjectMapper objectMapper
    ) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(
            topics = "user.registered",
            groupId = "user-profile-service"
    )
    public void consumeUserProfile(String message) {

        UserProfileEvent event;

        try {
            event = objectMapper.readValue(message, UserProfileEvent.class);
        } catch (Exception e) {
            throw new InvalidUserProfileEventException(
                    "Malformed user.registered event, skipping: " + message,
                    e
            );
        }

        try {

            // Prevent duplicate profile creation
            if (profileRepository.existsById(event.getUserId())) {

                log.info(
                        "Profile already exists for user: {}",
                        event.getUserId()
                );

                return;
            }


            Profile profile = new Profile();

            profile.setUserId(event.getUserId());
            profile.setFirstName(event.getFirstName());
            profile.setLastName(event.getLastName());
            profile.setContact(event.getContactNumber());
            profile.setDepartment(event.getDepartment());


            profileRepository.save(profile);


            log.info(
                    "User profile saved successfully: {}",
                    event.getUserId()
            );

        } catch (DataAccessException e) {
            throw new ProfilePersistenceException(
                    "Failed to save profile for user: " + event.getUserId(),
                    e
            );
        }
    }
}
