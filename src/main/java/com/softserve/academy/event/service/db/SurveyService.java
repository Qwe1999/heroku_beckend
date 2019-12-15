package com.softserve.academy.event.service.db;

import com.softserve.academy.event.entity.Survey;
import com.softserve.academy.event.entity.enums.SurveyStatus;
import com.softserve.academy.event.util.DuplicateSurveySettings;
import com.softserve.academy.event.util.Page;
import com.softserve.academy.event.util.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Map;

public interface SurveyService {

    Page<Survey> findAll(Pageable pageable);

    Page<Survey> findAllFiltered(Pageable pageable, Map<String, Map<String, Object>> filters);

    HttpStatus updateTitle(Long id, String title);

    HttpStatus updateStatus(Long id, SurveyStatus status);

    Survey duplicateSurvey(DuplicateSurveySettings settings);

    void delete(Survey entity);

}
