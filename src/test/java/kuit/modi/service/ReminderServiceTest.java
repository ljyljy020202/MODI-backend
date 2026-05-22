package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.reminder.ReminderPagedResponse;
import kuit.modi.dto.reminder.ReminderQueryParams;
import kuit.modi.repository.DiaryRepository;
import kuit.modi.repository.LocationRepository;
import kuit.modi.repository.ReminderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock private LocationRepository locationRepository;
    @Mock private DiaryRepository diaryRepository;
    @Mock private ReminderRepository reminderRepository;
    @InjectMocks private ReminderService reminderService;

    @Test
    void getRemindersByAddress_emptyLocations_returnsEmptyResponse() {
        Member member = new Member("test@example.com", null);
        member.setId(1L);

        ReminderQueryParams params = new ReminderQueryParams("Seoul", null, null, 20, null);
        when(locationRepository.findAllByAddress("Seoul")).thenReturn(List.of());

        ReminderPagedResponse response = reminderService.getRemindersByAddress(member, params);

        assertTrue(response.getItems().isEmpty());
        assertNull(response.getNextCursor());
    }

    @Test
    void getRemindersByAddress_withCursorAndOverflow_setsNextCursor() {
        Member member = new Member("test@example.com", null);
        member.setId(1L);

        Location location = new Location(10L, "Seoul", 37.5, 127.0);
        Emotion emotion = new Emotion(1L, "HAPPY");
        Tone tone = new Tone(1L, "calm");
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);

        Diary diary1 = Diary.create("c1", "s1", null, now, member, emotion, tone, location, now, now);
        diary1.setId(101L);
        Diary diary2 = Diary.create("c2", "s2", null, now.minusHours(1), member, emotion, tone, location, now.minusHours(1), now.minusHours(1));
        diary2.setId(100L);
        Diary diary3 = Diary.create("c3", "s3", null, now.minusHours(2), member, emotion, tone, location, now.minusHours(2), now.minusHours(2));
        diary3.setId(99L);

        String cursorRaw = "{\"id\":200,\"created_at\":\"2026-05-21T00:00:00\"}";
        String cursor = Base64.getEncoder().encodeToString(cursorRaw.getBytes());
        ReminderQueryParams params = new ReminderQueryParams("Seoul", Instant.now(), Instant.now(), 2, cursor);

        when(locationRepository.findAllByAddress("Seoul")).thenReturn(List.of(location));
        when(diaryRepository.findPagedDiariesByLocations(eq(1L), eq(List.of(10L)), any(), eq(200L), any(Pageable.class)))
                .thenReturn(List.of(diary1, diary2, diary3));

        ReminderPagedResponse response = reminderService.getRemindersByAddress(member, params);

        assertEquals(2, response.getItems().size());
        assertNotNull(response.getNextCursor());
    }

    @Test
    void getRecentReminders_mapsToResponse() {
        Member member = new Member("test@example.com", null);
        member.setId(1L);
        Location location = new Location(1L, "Busan", 35.1, 129.0);
        Emotion emotion = new Emotion(1L, "CALM");
        Reminder reminder = new Reminder(
                11L,
                LocalDateTime.ofInstant(Instant.ofEpochSecond(1_700_000_000L), ZoneOffset.UTC),
                location,
                LocalDateTime.of(2026, 5, 21, 9, 0),
                emotion,
                member
        );

        when(reminderRepository.findTop10ByMemberAndCreatedAtAfterOrderByCreatedAtDesc(eq(member), any(LocalDateTime.class)))
                .thenReturn(List.of(reminder));

        var responses = reminderService.getRecentReminders(member);

        assertEquals(1, responses.size());
        assertEquals(11L, responses.get(0).getId());
        assertEquals("Busan", responses.get(0).getAddress());
        assertEquals("CALM", responses.get(0).getEmotion());
    }
}
