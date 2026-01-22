package kuit.modi.repository;

import kuit.modi.domain.Diary;
import kuit.modi.domain.Location;
import kuit.modi.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteAllByMemberId(Long memberId);

    // === 커서 기반 페이징 추가 === //
    @Query("""
        select d from Diary d
        where d.member.id = :memberId
            and d.location.id in :locationIds
            and (
                :cursorCreatedAt is null
                or d.createdAt < :cursorCreatedAt
                or (d.createdAt = :cursorCreatedAt and d.id < :cursorId)
                )
        order by d.createdAt desc, d.id desc
    """)
    List<Diary> findPagedDiariesByLocations(
            @Param("memberId") Long memberId,
            @Param("locationIds") List<Long> locationIds,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    List<Diary> findByMemberAndLocationInOrderByDateDesc(
            Member member,
            List<Location> locations
    );
}