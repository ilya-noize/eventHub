package com.event.manager.domain.event;

import com.event.manager.db.EventEntity;
import com.event.manager.db.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchEventStatusUpdaterService {
    private final EventRepository eventRepository;

    public Slice<EventEntity> findAllMustBeStartedEvents(Pageable pageable) {
        return eventRepository.findAllByStatusWaitStartAndDateBeforeNow(pageable);
    }

    public Slice<EventEntity> findAllMustBeFinishedEvents(Pageable pageable) {
        return eventRepository.findAllByStatusStartedAndDateExpired(pageable);
    }

    @Transactional
    @Async
    public int updateStatusByIdsIn(String status, List<Long> ids) {
        return eventRepository.updateStatusByIdsIn(status, ids);
    }
}
