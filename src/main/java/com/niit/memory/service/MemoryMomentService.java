package com.niit.memory.service;

import com.niit.memory.entity.MemoryMoment;
import com.niit.memory.repository.MemoryMomentRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemoryMomentService {

    private final MemoryMomentRepository repository;

    public MemoryMomentService(MemoryMomentRepository repository) {
        this.repository = repository;
    }

    /**
     * 获取时间线数据，按年月分组
     */
    public List<Map<String, Object>> getTimeline() {
        List<MemoryMoment> all = repository.findAll();

        // 按年月分组
        Map<String, List<MemoryMoment>> grouped = new LinkedHashMap<>();
        for (MemoryMoment m : all) {
            String key = m.getYear() + "年" + m.getMonth() + "月";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<MemoryMoment>> entry : grouped.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("label", entry.getKey());
            List<MemoryMoment> moments = entry.getValue();
            if (!moments.isEmpty()) {
                group.put("year", moments.get(0).getYear());
                group.put("month", moments.get(0).getMonth());
            }
            group.put("moments", moments);
            result.add(group);
        }
        return result;
    }

    public MemoryMoment create(MemoryMoment moment) {
        if (moment.getMomentDate() != null) {
            moment.setYear(moment.getMomentDate().getYear());
            moment.setMonth(moment.getMomentDate().getMonthValue());
        }
        repository.insert(moment);
        return moment;
    }

    public MemoryMoment findById(Long id) {
        return repository.findById(id);
    }

    public MemoryMoment update(MemoryMoment moment) {
        if (moment.getMomentDate() != null) {
            moment.setYear(moment.getMomentDate().getYear());
            moment.setMonth(moment.getMomentDate().getMonthValue());
        }
        repository.update(moment);
        return repository.findById(moment.getId());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
