package com.niit.memory.service;

import com.niit.memory.entity.Couple;
import com.niit.memory.repository.CoupleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoupleService {

    private final CoupleRepository coupleRepository;

    public CoupleService(CoupleRepository coupleRepository) {
        this.coupleRepository = coupleRepository;
    }

    public Map<String, Object> getCoupleInfo() {
        Couple couple = coupleRepository.findFirst();
        Map<String, Object> result = new HashMap<>();
        result.put("couple", couple);
        if (couple != null && couple.getLoveStartDate() != null) {
            long days = ChronoUnit.DAYS.between(couple.getLoveStartDate(), LocalDate.now());
            result.put("loveDays", days);
        }
        return result;
    }

    public Couple update(Couple couple) {
        coupleRepository.update(couple);
        return coupleRepository.findById(couple.getId());
    }
}
