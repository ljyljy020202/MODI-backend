package kuit.modi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import kuit.modi.domain.Diary;
import kuit.modi.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DiaryQueryRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Diary> findAllByMemberId(Long memberId) {
        return em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.tone " +
                                "LEFT JOIN FETCH d.location " +
                                "LEFT JOIN FETCH d.style s " +
                                "LEFT JOIN FETCH s.frame " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.member.id = :memberId " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .getResultList();
    }

    /*
     * diaryId에 해당하는 Diary를 연관 엔티티와 함께 조회
     * - Emotion, Tone, Location, Image, Tag 포함 fetch
     */
    public Optional<Diary> findDetailById(Long diaryId) {
        List<Diary> result = em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.tone " +
                                "LEFT JOIN FETCH d.location " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.id = :diaryId", Diary.class)
                .setParameter("diaryId", diaryId)
                .getResultList();

        return result.stream().findFirst();
    }

    /*
     * 특정 날짜의 일기를 createdAt 기준으로 정렬하여 조회
     */
    public List<Diary> findDiariesByDate(Long memberId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.member.id = :memberId AND d.date BETWEEN :start AND :end " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getResultList();
    }

    /*
     * 일별 보기 기능을 위한 월별 조회
     */
    public List<Diary> findByYearMonthForDaily(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.member.id = :memberId " +
                                "AND FUNCTION('YEAR', d.date) = :year " +
                                "AND FUNCTION('MONTH', d.date) = :month " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    /*
     * 특정 연&월의 일기 목록을 시간 기준으로 정렬하여 조회
     * - Image와 Emotion 포함 fetch
     * - 작성 순 정렬
     */
    public List<Diary> findByYearMonth(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.emotion " +
                                "WHERE d.member.id = :memberId " +
                                "AND FUNCTION('YEAR', d.date) = :year " +
                                "AND FUNCTION('MONTH', d.date) = :month " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //즐겨찾기한 일기 목록 조회 - 로그인한 사용자의 favorite=true 조건
    public List<Diary> findFavorites(Long memberId) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE d.member.id = :memberId " +
                                "AND d.favorite = true " +
                                "ORDER BY d.createdAt DESC", Diary.class
                )
                .setParameter("memberId", memberId)
                .getResultList();
    }

    //감정별 통계 조
    public List<Object[]> findEmotionStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT e.name, COUNT(d) FROM Diary d " +
                                "JOIN d.emotion e " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY e.name ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

   //어투별 통계 조회
    public List<Object[]> findToneStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT t.name, COUNT(d) FROM Diary d " +
                                "JOIN d.tone t " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY t.name ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //위치별 통계 조회
    public List<Object[]> findLocationStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT l.address, COUNT(d) FROM Diary d " +
                                "JOIN d.location l " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY l.address ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //전체 일기 수 카운트
    public long countMonthlyDiaries(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM Diary d " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month", Long.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getSingleResult();
    }

    //특정 태그에 해당하는 사용자의 일기 조회- 작성일 기준 오름차순
    public List<Object[]> findByTagIdWithImage(Long memberId, Long tagId) {
        return em.createQuery(
                        "SELECT d.date, i.url FROM Diary d " +
                                "JOIN d.image i " +
                                "JOIN d.diaryTags dt " +
                                "WHERE d.member.id = :memberId " +
                                "AND dt.tag.id = :tagId " +
                                "ORDER BY d.date ASC, d.createdAt ASC", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("tagId", tagId)
                .getResultList();
    }

    // member + tagName(대소문자 무시)로 일기/이미지 조회 (이미지 없으면 null)
    public List<Object[]> findByMemberAndTagNameWithImage(Long memberId, String tagNameNorm) {
        return em.createQuery(
                        """
                        select d.id, d.date, i.url
                        from Diary d
                        left join d.image i
                        where d.member.id = :memberId
                          and exists (
                               select 1
                               from DiaryTag dt
                               join dt.tag t
                               where dt.diary = d
                                 and lower(t.name) = :tagName
                          )
                        order by d.date asc, d.createdAt asc, d.id asc
                        """, Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("tagName", tagNameNorm.toLowerCase())
                .getResultList();
    }

    // diaryId 리스트로 날짜/이미지 가져오기 (이미지 없을 수 있으니 LEFT JOIN)
    public List<Object[]> findByDiaryIdsWithImage(List<Long> diaryIds) {
        return em.createQuery(
                        "select d.id, d.date, i.url " +
                                "from Diary d " +
                                "left join d.image i " +
                                "where d.id in :ids " +
                                "order by d.date asc, d.createdAt asc, d.id asc", Object[].class)
                .setParameter("ids", diaryIds)
                .getResultList();
    }

    // 특정 태그 이름에 해당하는 사용자의 일기 조회
    // tagName 기반: 날짜/작성일 오름차순
    public List<Object[]> findByTagNameWithImage(Long memberId, String tagName) {
        return em.createQuery(
                        "SELECT d.id, d.date, i.url " +
                                "FROM Diary d " +
                                "JOIN d.diaryTags dt " +
                                "JOIN dt.tag t " +
                                "LEFT JOIN d.image i " + // 이미지 없을 수도 있으면 LEFT JOIN
                                "WHERE d.member.id = :memberId " +
                                "AND LOWER(t.name) = LOWER(:tagName) " +
                                "ORDER BY d.date ASC, d.createdAt ASC, d.id ASC", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("tagName", tagName.trim())
                .getResultList();
    }

    /*
     * 지도 범위 내 위/경도(lat/lng)에 해당하는 일기 목록 조회
     * - Location 포함 fetch
     */
    public List<Diary> findNearbyDiaries(Long memberId, double swLat, double swLng, double neLat, double neLng) {
        return em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "JOIN FETCH d.location l " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE l.latitude BETWEEN :swLat AND :neLat " +
                                "AND l.longitude BETWEEN :swLng AND :neLng " +
                                "AND d.member.id = :memberId", Diary.class)
                .setParameter("swLat", swLat)
                .setParameter("neLat", neLat)
                .setParameter("swLng", swLng)
                .setParameter("neLng", neLng)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    /*
     * 지정한 위도/경도 기준 반경 m 이내의 일기를 조회 (Location 포함)
     * member에 대한 처리 안함
     * 현재 사용X
     */
    public List<Diary> findDiariesWithinRadius(double latitude, double longitude, double radiusInMeters) {
        // 위도/경도 기준 100m 범위 내 사각형 영역으로 계산
        // 근사치로 계산, 정밀한 계산을 원하면 수정 가능
        double earthRadius = 6371000; // meters
        double deltaLat = Math.toDegrees(radiusInMeters / earthRadius);
        double deltaLng = Math.toDegrees(radiusInMeters / (earthRadius * Math.cos(Math.toRadians(latitude))));

        double minLat = latitude - deltaLat;
        double maxLat = latitude + deltaLat;
        double minLng = longitude - deltaLng;
        double maxLng = longitude + deltaLng;

        return em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "JOIN FETCH d.location l " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE l.latitude BETWEEN :minLat AND :maxLat " +
                                "AND l.longitude BETWEEN :minLng AND :maxLng", Diary.class)
                .setParameter("minLat", minLat)
                .setParameter("maxLat", maxLat)
                .setParameter("minLng", minLng)
                .setParameter("maxLng", maxLng)
                .getResultList();
    }

    //일기 월별 조회 페이징처리
    public List<Diary> findByYearMonthPaged(Long memberId, int year, int month, Pageable pageable) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.emotion " +
                                "WHERE d.member.id = :memberId " +
                                "AND FUNCTION('YEAR', d.date) = :year " +
                                "AND FUNCTION('MONTH', d.date) = :month " +
                                "ORDER BY d.createdAt DESC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    //즐겨찾기 조회 페이징
    public List<Diary> findFavoritesPaged(Long memberId, Pageable pageable) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE d.member.id = :memberId " +
                                "AND d.favorite = true " +
                                "ORDER BY d.createdAt DESC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 즐겨찾기 일기 총 개수 카운트
    public long countFavorites(Long memberId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM Diary d " +
                                "WHERE d.member.id = :memberId " +
                                "AND d.favorite = true", Long.class
                )
                .setParameter("memberId", memberId)
                .getSingleResult();
    }

    // tagId 기반 페이징 조회
    // - 특정 태그가 지정된 일기를 페이징하여 조회
    // - createdAt 내림차순 정렬
    // - 이미지 포함 LEFT JOIN
    public List<Diary> findByTagIdPaged(Long memberId, Long tagId, Pageable pageable) {
        return em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE d.member.id = :memberId " +
                                "AND EXISTS (" +
                                "  SELECT 1 FROM DiaryTag dt " +
                                "  WHERE dt.diary = d AND dt.tag.id = :tagId" +
                                ") " +
                                "ORDER BY d.date DESC, d.createdAt DESC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("tagId", tagId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // tagId 기반 일기 총 개수 카운트
    // - 특정 태그가 지정된 일기의 개수 조회
    public long countByTagId(Long memberId, Long tagId) {
        return em.createQuery(
                        "SELECT COUNT(DISTINCT d) FROM Diary d " +
                                "WHERE d.member.id = :memberId " +
                                "AND EXISTS (" +
                                "  SELECT 1 FROM DiaryTag dt " +
                                "  WHERE dt.diary = d AND dt.tag.id = :tagId" +
                                ")", Long.class
                )
                .setParameter("memberId", memberId)
                .setParameter("tagId", tagId)
                .getSingleResult();
    }

    // 월별 조회 - createdAt+id 복합 커서 기반
    public List<Diary> findByYearMonthCursor(Long memberId, int year, int month,
            LocalDateTime lastCreatedAt, Long lastId, int size) {
        String jpql = "SELECT d FROM Diary d " +
                "LEFT JOIN FETCH d.image " +
                "LEFT JOIN FETCH d.emotion " +
                "WHERE d.member.id = :memberId " +
                "AND FUNCTION('YEAR', d.date) = :year " +
                "AND FUNCTION('MONTH', d.date) = :month ";
        if (lastCreatedAt != null) {
            jpql += "AND (d.createdAt < :lastCreatedAt " +
                    "OR (d.createdAt = :lastCreatedAt AND d.id < :lastId)) ";
        }
        jpql += "ORDER BY d.createdAt DESC, d.id DESC";

        TypedQuery<Diary> query = em.createQuery(jpql, Diary.class)
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .setMaxResults(size + 1);
        if (lastCreatedAt != null) {
            query.setParameter("lastCreatedAt", lastCreatedAt);
            query.setParameter("lastId", lastId);
        }
        return query.getResultList();
    }

    // 즐겨찾기 조회 - createdAt+id 복합 커서 기반
    public List<Diary> findFavoritesCursor(Long memberId, LocalDateTime lastCreatedAt, Long lastId, int size) {
        String jpql = "SELECT d FROM Diary d " +
                "LEFT JOIN FETCH d.image " +
                "WHERE d.member.id = :memberId " +
                "AND d.favorite = true ";
        if (lastCreatedAt != null) {
            jpql += "AND (d.createdAt < :lastCreatedAt " +
                    "OR (d.createdAt = :lastCreatedAt AND d.id < :lastId)) ";
        }
        jpql += "ORDER BY d.createdAt DESC, d.id DESC";

        TypedQuery<Diary> query = em.createQuery(jpql, Diary.class)
                .setParameter("memberId", memberId)
                .setMaxResults(size + 1);
        if (lastCreatedAt != null) {
            query.setParameter("lastCreatedAt", lastCreatedAt);
            query.setParameter("lastId", lastId);
        }
        return query.getResultList();
    }

    // 태그 ID 기반 조회 - createdAt+id 복합 커서 기반
    public List<Diary> findByTagIdCursor(Long memberId, Long tagId,
            LocalDateTime lastCreatedAt, Long lastId, int size) {
        String jpql = "SELECT DISTINCT d FROM Diary d " +
                "LEFT JOIN FETCH d.image " +
                "WHERE d.member.id = :memberId " +
                "AND EXISTS (" +
                "  SELECT 1 FROM DiaryTag dt " +
                "  WHERE dt.diary = d AND dt.tag.id = :tagId) ";
        if (lastCreatedAt != null) {
            jpql += "AND (d.createdAt < :lastCreatedAt " +
                    "OR (d.createdAt = :lastCreatedAt AND d.id < :lastId)) ";
        }
        jpql += "ORDER BY d.createdAt DESC, d.id DESC";

        TypedQuery<Diary> query = em.createQuery(jpql, Diary.class)
                .setParameter("memberId", memberId)
                .setParameter("tagId", tagId)
                .setMaxResults(size + 1);
        if (lastCreatedAt != null) {
            query.setParameter("lastCreatedAt", lastCreatedAt);
            query.setParameter("lastId", lastId);
        }
        return query.getResultList();
    }

}
