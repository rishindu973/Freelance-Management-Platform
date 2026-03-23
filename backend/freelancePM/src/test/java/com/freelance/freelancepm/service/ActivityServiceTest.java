package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.ActivityResponse;
import com.freelance.freelancepm.entity.Activity;
import com.freelance.freelancepm.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void testFetchAllActivities() {
        Activity act = Activity.builder()
                .id(1L)
                .managerId(1L)
                .type(Activity.ActivityType.PROJECT_CREATED)
                .description("Created new repo")
                .timestamp(LocalDateTime.now())
                .build();

        Page<Activity> mockPage = new PageImpl<>(List.of(act));
        when(activityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<ActivityResponse> result = activityService.list(1L, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("Created new repo", result.getContent().get(0).getDescription());
    }

    @Test
    void testFilterByType() {
        Activity act = Activity.builder()
                .id(2L)
                .managerId(1L)
                .type(Activity.ActivityType.MEMBER_ADDED)
                .description("Added John Doe")
                .timestamp(LocalDateTime.now())
                .build();

        Page<Activity> mockPage = new PageImpl<>(List.of(act));
        when(activityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<ActivityResponse> result = activityService.list(
                1L, 
                Activity.ActivityType.MEMBER_ADDED, 
                null, 
                null, 
                PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals(Activity.ActivityType.MEMBER_ADDED, result.getContent().get(0).getType());
    }

    @Test
    void testFilterByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        Activity act = Activity.builder()
                .id(3L)
                .managerId(1L)
                .type(Activity.ActivityType.INVOICE_SENT)
                .description("Sent Invoice #123")
                .timestamp(LocalDateTime.now())
                .build();

        Page<Activity> mockPage = new PageImpl<>(List.of(act));
        when(activityRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<ActivityResponse> result = activityService.list(
                1L, 
                null, 
                start, 
                end, 
                PageRequest.of(0, 5));

        assertEquals(1, result.getContent().size());
        assertEquals(Activity.ActivityType.INVOICE_SENT, result.getContent().get(0).getType());
    }
}
