package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.diary.request.CreateDiaryRequest;
import kuit.modi.dto.diary.request.UpdateDiaryRequest;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;
import kuit.modi.exception.S3ExceptionResponseStatus;
import kuit.modi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock private DiaryRepository diaryRepository;
    @Mock private EmotionRepository emotionRepository;
    @Mock private ToneRepository toneRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private FrameRepository frameRepository;
    @Mock private TagRepository tagRepository;
    @Mock private DiaryTagRepository diaryTagRepository;
    @Mock private S3Service s3Service;
    @Mock private MultipartFile imageFile;

    @InjectMocks private DiaryService diaryService;

    private Member member;
    private Emotion emotion;
    private Tone tone;
    private Frame frame;
    private Location location;

    @BeforeEach
    void setUp() {
        member = new Member("test@example.com", null);
        member.setId(1L);

        emotion = new Emotion(1L, "HAPPY");
        tone = new Tone(1L, "calm");
        frame = new Frame(1L, "FRAME_A");
        location = new Location(1L, "Seoul", 37.5, 127.0);
    }

    @Test
    void createDiary_success() {
        CreateDiaryRequest request = new CreateDiaryRequest(
                "content", "summary", "2026-05-22T10:15:30",
                "Seoul", 37.5, 127.0, "HAPPY", "calm",
                List.of("travel", "food"), "Pretendard", "FRAME_A"
        );

        when(emotionRepository.findByName("HAPPY")).thenReturn(Optional.of(emotion));
        when(toneRepository.findByName("calm")).thenReturn(Optional.of(tone));
        when(frameRepository.findByName("FRAME_A")).thenReturn(Optional.of(frame));
        when(locationRepository.findByAddressAndLatitudeAndLongitude("Seoul", 37.5, 127.0))
                .thenReturn(Optional.of(location));
        when(tagRepository.findByName("travel")).thenReturn(Optional.empty());
        when(tagRepository.findByName("food")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(diaryRepository.save(any(Diary.class)))
                .thenAnswer(invocation -> {
                    Diary saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        Long createdId = diaryService.createDiary(member, request, null);

        assertEquals(100L, createdId);
        ArgumentCaptor<Diary> captor = ArgumentCaptor.forClass(Diary.class);
        verify(diaryRepository).save(captor.capture());
        Diary saved = captor.getValue();

        assertEquals("content", saved.getContent());
        assertEquals("summary", saved.getSummary());
        assertEquals(LocalDateTime.parse("2026-05-22T10:15:30"), saved.getDate());
        assertEquals(emotion, saved.getEmotion());
        assertEquals(tone, saved.getTone());
        assertEquals(location, saved.getLocation());
        assertNotNull(saved.getStyle());
        assertEquals("Pretendard", saved.getStyle().getFont());
        assertEquals(frame, saved.getStyle().getFrame());
        assertEquals(2, saved.getDiaryTags().size());
        verify(s3Service, never()).uploadFile(any());
    }

    @Test
    void updateDiary_missingFrame_throwsException() {
        UpdateDiaryRequest request = new UpdateDiaryRequest(
                "new content", "new summary", "2026-05-22T12:00:00",
                "Seoul", 37.5, 127.0, "HAPPY", "calm",
                List.of("tag"), "font", null
        );
        Diary diary = Diary.create(
                "old", "old", null, LocalDateTime.now(),
                member, emotion, tone, location, LocalDateTime.now(), LocalDateTime.now()
        );
        diary.setStyle(Style.create("old-font", diary, frame));

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(emotionRepository.findByName("HAPPY")).thenReturn(Optional.of(emotion));
        when(toneRepository.findByName("calm")).thenReturn(Optional.of(tone));
        when(locationRepository.findByAddressAndLatitudeAndLongitude("Seoul", 37.5, 127.0))
                .thenReturn(Optional.of(location));
        when(tagRepository.findByName("tag")).thenReturn(Optional.of(new Tag(1L, "tag", List.of())));

        CustomException ex = assertThrows(CustomException.class,
                () -> diaryService.updateDiary(1L, request, null));
        assertEquals(DiaryExceptionResponseStatus.MISSING_FRAME, ex.getStatus());
    }

    @Test
    void deleteDiary_s3Failure_throwsMappedException() {
        Diary diary = Diary.create(
                "content", "summary", null, LocalDateTime.now(),
                member, emotion, tone, location, LocalDateTime.now(), LocalDateTime.now()
        );
        diary.setImage(Image.create("old-key", diary));

        when(diaryRepository.findById(99L)).thenReturn(Optional.of(diary));
        doThrow(new RuntimeException("s3 fail")).when(s3Service).deleteFileFromUrl("old-key");

        CustomException ex = assertThrows(CustomException.class, () -> diaryService.deleteDiary(99L));
        assertEquals(S3ExceptionResponseStatus.S3_DELETE_FAILED, ex.getStatus());
        verify(diaryRepository, never()).delete(any());
    }
}
