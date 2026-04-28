package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ActivityResponse;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Transactional
    public void logActivity(Integer managerId, Activity.ActivityType type, String description) {
        Activity activity = Activity.builder()
                .managerId(managerId)
                .type(type)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();

        activityRepository.save(activity);
    }

    public Page<ActivityResponse> list(
            Integer managerId,
            Activity.ActivityType type,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {

        Specification<Activity> spec = Specification.where(ActivitySpecifications.managerIdEquals(managerId));

        if (type != null) {
            spec = spec.and(ActivitySpecifications.typeEquals(type));
        }

        if (from != null && to != null) {
            spec = spec.and(ActivitySpecifications.timestampBetween(from, to));
        } else if (from != null) {
            spec = spec.and(ActivitySpecifications.timestampGreaterThanEqual(from));
        } else if (to != null) {
            spec = spec.and(ActivitySpecifications.timestampLessThanEqual(to));
        }

        return activityRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private ActivityResponse toResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .managerId(a.getManagerId())
                .type(a.getType())
                .description(a.getDescription())
                .timestamp(a.getTimestamp())
                .build();
    }
}
