package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.diary.request.CreateDiaryRequest;
import kuit.modi.dto.diary.request.UpdateDiaryRequest;
import kuit.modi.dto.diary.response.*;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;
import kuit.modi.service.DiaryQueryService;
import kuit.modi.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryQueryService diaryQueryService;

    // 일기 생성
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDiary(
            @AuthenticationPrincipal Member member,
            @RequestPart("data") CreateDiaryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        log.info("일기 생성 API 호출: memberId={}", member.getId());
        Long createdId = diaryService.createDiary(member, request, image);
        return ResponseEntity.ok(new DiaryCreateResponse(createdId, "기록 생성이 완료되었습니다."));
    }

    // 일기 수정
    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDiary(
            @PathVariable Long diaryId,
            @RequestPart("data") UpdateDiaryRequest request,   // JSON 부분
            @RequestPart(value = "image", required = false) MultipartFile imageFile  // 파일 부분
    ) {
        log.info("일기 수정 API 호출: diaryId={}", diaryId);
        diaryService.updateDiary(diaryId, request, imageFile);
        return ResponseEntity.ok(new DiaryUpdateResponse(diaryId, "기록 수정이 완료되었습니다."));
    }

    // 일기 즐겨찾기 설정
    @PostMapping("/{diaryId}/favorite")
    public ResponseEntity<?> updateFavorite(
            @PathVariable Long diaryId,
            @RequestParam boolean favorite
    ) {
        log.info("일기 즐겨찾기 API 호출: diaryId={}", diaryId);
        diaryService.updateFavorite(diaryId, favorite);
        return ResponseEntity.ok(
                new FavoriteUpdateResponse("즐겨찾기 " + (favorite ? "등록" : "해제") + " 완료되었습니다.")
        );
    }

    // 일기 삭제
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(
            @PathVariable Long diaryId
    ) {
        log.info("일기 삭제 API 호출: diaryId={}", diaryId);
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok(new DiaryDeleteResponse("기록 삭제가 완료되었습니다."));
    }

    // 홈화면 전체 일기 조회용
    @GetMapping
    public ResponseEntity<?> getDiaryAll(
            @AuthenticationPrincipal Member member
    ) {
        log.info("홈화면 전체 일기 조회 API 호출: memberId={}", member.getId());
        DiaryAllResponse response = diaryQueryService.getDiaryAll(member);
        return ResponseEntity.ok(response);
    }

    // 일기 상세 조회
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponse> getDiaryDetail(
            @AuthenticationPrincipal Member member,
            @PathVariable Long diaryId
    ) {
        log.info("일기 상세 조회 API 호출: diaryId={}", diaryId);
        DiaryDetailResponse response = diaryQueryService.getDiaryDetail(diaryId, member);
        return ResponseEntity.ok(response);
    }

    // 일별 기록 조회용 월 전체 기록 응답
    @GetMapping("/daily")
    public ResponseEntity<DiaryAllResponse> getDailyDiaryDetail(
            @AuthenticationPrincipal Member member,
            @RequestParam int year,
            @RequestParam int month
    ) {
        log.info("일별 기록 조회 API 호출: memberId={}, year={}, month={}", member.getId(), year, month);
        DiaryAllResponse response = diaryQueryService.getDailyDetailMonthly(year, month, member);
        return ResponseEntity.ok(response);
    }

    // 특정 연/월의 일기 목록 조회 (월별 보기)
    /*@GetMapping(params = {"year", "month"})
    public ResponseEntity<List<DiaryMonthlyItemResponse>> getMonthlyDiaries(
            @AuthenticationPrincipal Member member,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (year == null || month == null) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_YEAR_MONTH);
        }

        List<DiaryMonthlyItemResponse> diaries = diaryQueryService.getMonthlyDiaries(year, month, member);
        return ResponseEntity.ok(diaries);
    }*/

    @GetMapping(params = {"year", "month"})
    public ResponseEntity<CursorPageResponse<DiaryMonthlyItemResponse>> getMonthlyDiaries(
            @AuthenticationPrincipal Member member,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("월별 기록 조회 API 호출: memberId={}, year={}, month={}", member.getId(), year, month);
        if (year <= 0 || month < 1 || month > 12) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_YEAR_MONTH);
        }

        CursorPageResponse<DiaryMonthlyItemResponse> response =
                diaryQueryService.getMonthlyDiariesCursor(year, month, member, cursor, size);
        return ResponseEntity.ok(response);
    }

    // 즐겨찾기한 일기 목록 조회 (커서 페이징)
    @GetMapping("/favorites")
    public ResponseEntity<CursorPageResponse<FavoriteDiaryItemResponse>> getFavoriteDiaries(
            @AuthenticationPrincipal Member member,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("즐겨찾기 일기 목록 조회 API 호출: memberId={}", member.getId());
        CursorPageResponse<FavoriteDiaryItemResponse> response =
                diaryQueryService.getFavoriteDiariesCursor(member, cursor, size);
        return ResponseEntity.ok(response);
    }

    // 월간 통계 조회 API
    @GetMapping("/statistics")
    public ResponseEntity<DiaryStatisticsResponse> getMonthlyStatistics(
            @AuthenticationPrincipal Member member,
            @RequestParam int year,
            @RequestParam int month
    ) {
        log.info("월간 통계 조회 API 호출: memberId={}, year={}, month={}", member.getId(), year, month);
        DiaryStatisticsResponse response = diaryQueryService.getMonthlyStatistics(year, month, member);
        return ResponseEntity.ok(response);
    }

    // 특정 태그 이름 기반으로 검색 (날짜별 그룹 + 일기별 이미지 매핑)
    @GetMapping(params = "tagName")
    public ResponseEntity<List<DiaryTagSearchItemResponse>> getDiariesByTagName(
            @AuthenticationPrincipal Member member,
            @RequestParam String tagName
    ) {
        log.info("태그 이름 기반 검색 API 호출: memberId={}, tagname={}", member.getId(), tagName);
        // 핸들러가 실제로 매칭되는지, 값이 뭘로 들어오는지 확인
        //log.info("GET /diaries?tagName={} (memberId={})", tagName, member != null ? member.getId() : null);
        return ResponseEntity.ok(diaryQueryService.getDiariesByTagName(tagName, member));
    }

    // 태그 ID 기반 일기 검색 (커서 페이징)
    @GetMapping("/by-tag")
    public ResponseEntity<CursorPageResponse<DiaryTagSearchItemResponse>> getDiariesByTagId(
            @AuthenticationPrincipal Member member,
            @RequestParam Long tagId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("태그 Id 기반 검색 API 호출: memberId={}, tagId={}", member.getId(), tagId);
        if (size > 100) size = 100;

        CursorPageResponse<DiaryTagSearchItemResponse> response =
                diaryQueryService.getDiariesByTagIdCursor(member, tagId, cursor, size);
        return ResponseEntity.ok(response);
    }

    // 많이 쓰이는 태그 조회
    @GetMapping("/tags/popular")
    public ResponseEntity<List<String>> getPopularTags(
            @AuthenticationPrincipal Member member
    ) {
        log.info("많이 쓰이는 태그 조회 API 호출: memberId={}", member.getId());
        List<String> tags = diaryQueryService.getPopularTags(member.getId());
        return ResponseEntity.ok(tags);
    }

    // 지도 조회
    @GetMapping("/nearby")
    public ResponseEntity<List<DiaryNearbyResponse>> getNearbyDiaries(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @AuthenticationPrincipal Member member
    ) {
        log.info("지도 조회 API 호출: memberId={}, swLat={}, swLng={}, neLat={}, neLng={}", member.getId(), swLat, swLng, neLat, neLng);
        List<DiaryNearbyResponse> diaries = diaryQueryService.getNearbyDiaries(swLat, swLng, neLat, neLng, member);
        return ResponseEntity.ok(diaries);
    }

}



