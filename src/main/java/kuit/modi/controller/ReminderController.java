package kuit.modi.controller;

import jakarta.validation.Valid;
import kuit.modi.domain.Member;
import kuit.modi.dto.reminder.*;
import kuit.modi.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    // 리마인더 알림 생성 API (POST /api/reminders)
    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @RequestBody ReminderCreateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        log.info("리마인더 알림 생성 API 호출: memberId={}", member.getId());
        ReminderResponse reminder = reminderService.createReminder(member, request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    // 다이어리 기록 조회 API (GET /api/reminders?address=)
    @GetMapping
    public ResponseEntity<ReminderPagedResponse> getRemindersByAddress(
            @Valid @ModelAttribute ReminderQueryParams params,
            @AuthenticationPrincipal Member member
    ) {
        log.info("리마인더 위치 기반 기록 조회 API 호출: memberId={}, address={}", member.getId(), params.address());
        ReminderPagedResponse response = reminderService.getRemindersByAddress(member, params);
        return ResponseEntity.ok(response);
    }

    // 최근 알림 내역 조회 API (GET /api/reminders/recent)
    @GetMapping("/recent")
    public ResponseEntity<List<ReminderResponse>> getRecentReminders(
            @AuthenticationPrincipal Member member) {
        log.info("최근 알림 내역 조회 API 호출: memberId={}", member.getId());
        List<ReminderResponse> reminders = reminderService.getRecentReminders(member);
        return ResponseEntity.ok(reminders);
    }
}
