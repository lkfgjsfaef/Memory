package com.niit.memory.service;

import com.niit.memory.config.UserContext;
import com.niit.memory.entity.Couple;
import com.niit.memory.entity.DailyRecord;
import com.niit.memory.repository.CoupleRepository;
import com.niit.memory.repository.DailyRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyRecordService {

    private final DailyRecordRepository repository;
    private final CoupleRepository coupleRepository;

    public DailyRecordService(DailyRecordRepository repository, CoupleRepository coupleRepository) {
        this.repository = repository;
        this.coupleRepository = coupleRepository;
    }

    public List<DailyRecord> findAll(Integer year, Integer month) {
        return repository.findAll(year, month);
    }

    public DailyRecord create(DailyRecord record) {
        record.setUserId(UserContext.getUserId());
        repository.insert(record);
        return record;
    }

    public DailyRecord update(DailyRecord record) {
        repository.update(record);
        return repository.findById(record.getId());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate now = LocalDate.now();
        stats.put("monthCount", repository.countByMonth(now.getYear(), now.getMonthValue()));
        stats.put("totalCount", repository.countAll());
        try {
            Couple couple = coupleRepository.findFirst();
            if (couple != null && couple.getLoveStartDate() != null) {
                stats.put("loveDays", ChronoUnit.DAYS.between(couple.getLoveStartDate(), LocalDate.now()));
            }
        } catch (Exception e) {
            stats.put("loveDays", 0);
        }
        return stats;
    }
}
