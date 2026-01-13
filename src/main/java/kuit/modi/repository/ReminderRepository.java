package kuit.modi.repository;

import kuit.modi.domain.Member;
import kuit.modi.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findTop10ByMemberAndCreatedAtAfterOrderByCreatedAtDesc(
            Member member,
            LocalDateTime createdAt
    );
}
