package kuit.modi.controller;

import jakarta.validation.Valid;
import kuit.modi.domain.Member;
import kuit.modi.dto.reminder.*;
import kuit.modi.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    // 리마인더 생성 API (POST /api/reminders)
    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @RequestBody ReminderCreateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        ReminderResponse reminder = reminderService.createReminder(member, request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    // 다이어리 기록 조회 API (GET /api/reminders?address=)
    @GetMapping
    public ResponseEntity<ReminderPagedResponse> getRemindersByAddress(
            @Valid @ModelAttribute ReminderQueryParams params,
            @AuthenticationPrincipal Member member
    ) {
        ReminderPagedResponse response = reminderService.getRemindersByAddress(member, params);
        return ResponseEntity.ok(response);
    }

    // 최근 알림 내역 조회 API (GET /api/reminders/recent)
    @GetMapping("/recent")
    public ResponseEntity<List<ReminderResponse>> getRecentReminders(
            @AuthenticationPrincipal Member member) {
        List<ReminderResponse> reminders = reminderService.getRecentReminders(member);
        return ResponseEntity.ok(reminders);
    }
}
