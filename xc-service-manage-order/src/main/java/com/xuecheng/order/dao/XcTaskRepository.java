package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable,LocalDateTime localDateTime);
}
