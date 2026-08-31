package com.elsevier.cardiac_user_profile_service.repository;

import com.elsevier.cardiac_user_profile_service.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {

}