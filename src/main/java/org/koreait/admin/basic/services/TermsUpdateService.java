package org.koreait.admin.basic.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.koreait.global.entities.CodeValue;
import org.koreait.global.entities.Terms;
import org.koreait.global.repositories.CodeValueRepository;
import org.koreait.global.services.CodeValueService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class TermsUpdateService {

    private final CodeValueService service;

    /**
     * 약관 저장
     * @param terms
     */
    public void save(Terms terms) {
        String code = String.format("term_%s", terms.getCode());
        service.save(code, terms);
    }
}