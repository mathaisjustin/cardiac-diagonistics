package com.elsevier.cardiac_user_profile_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileTest {

    @Test
    void onCreate_generatesProfileIdAndStampsTimestamps() {
        Profile profile = new Profile();

        profile.onCreate();

        assertThat(profile.getProfileId()).isNotBlank().hasSize(12);
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isEqualTo(profile.getCreatedAt());
    }

    @Test
    void onUpdate_bumpsUpdatedAtOnly() {
        Profile profile = new Profile();
        profile.onCreate();
        var createdAt = profile.getCreatedAt();

        profile.onUpdate();

        assertThat(profile.getCreatedAt()).isEqualTo(createdAt);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }
}
