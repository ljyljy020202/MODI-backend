package kuit.modi.service;

import kuit.modi.domain.CharacterType;
import kuit.modi.domain.Member;
import kuit.modi.dto.member.MemberRequest;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.MemberExceptionResponseStatus;
import kuit.modi.repository.CharacterTypeRepository;
import kuit.modi.repository.DiaryRepository;
import kuit.modi.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private CharacterTypeRepository characterTypeRepository;
    @Mock private DiaryRepository diaryRepository;
    @InjectMocks private MemberService memberService;

    @Test
    void update_success() {
        Member member = new Member("test@example.com", null);
        member.setId(1L);
        member.setNickname("old");
        CharacterType characterType = new CharacterType(2L, "cat");
        MemberRequest request = new MemberRequest("newNick", "cat");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(characterTypeRepository.findByName("cat")).thenReturn(Optional.of(characterType));
        when(memberRepository.save(member)).thenReturn(member);

        Member result = memberService.update(1L, request);

        assertEquals("newNick", result.getNickname());
        assertEquals(characterType, result.getCharacterType());
    }

    @Test
    void deleteById_memberNotFound_throwsException() {
        when(memberRepository.existsById(10L)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> memberService.deleteById(10L));
        assertEquals(MemberExceptionResponseStatus.MEMBER_NOT_FOUND, ex.getStatus());
        verify(diaryRepository, never()).deleteAllByMemberId(anyLong());
        verify(memberRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_success_deletesDiariesThenMember() {
        when(memberRepository.existsById(3L)).thenReturn(true);

        memberService.deleteById(3L);

        verify(diaryRepository).deleteAllByMemberId(3L);
        verify(memberRepository).deleteById(3L);
    }
}
