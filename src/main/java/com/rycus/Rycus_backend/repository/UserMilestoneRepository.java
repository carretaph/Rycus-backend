package com.rycus.Rycus_backend.repository;

import com.rycus.Rycus_backend.milestone.MilestoneType;
import com.rycus.Rycus_backend.milestone.UserMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMilestoneRepository
        extends JpaRepository<UserMilestone, Long> {

    Optional<UserMilestone> findByUserEmailAndMilestoneType(
            String userEmail,
            MilestoneType milestoneType
    );
}
