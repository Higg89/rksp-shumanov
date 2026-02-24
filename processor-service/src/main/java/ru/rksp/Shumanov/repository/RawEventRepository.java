package ru.rksp.Shumanov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rksp.Shumanov.entity.RawEvent;

@Repository
public interface RawEventRepository extends JpaRepository<RawEvent, Long> {
}
