package com.softserve.academy.event.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.academy.event.annotation.PageableDefault;
import com.softserve.academy.event.dto.SaveSurveyDTO;
import com.softserve.academy.event.dto.SimpleSurveyDTO;
import com.softserve.academy.event.dto.SurveyQuestionDTO;
import com.softserve.academy.event.entity.Survey;
import com.softserve.academy.event.entity.SurveyQuestion;
import com.softserve.academy.event.response.ServerResponse;
import com.softserve.academy.event.service.db.SurveyService;
import com.softserve.academy.event.service.mapper.SaveQuestionMapper;
import com.softserve.academy.event.util.Page;
import com.softserve.academy.event.util.Pageable;
import com.softserve.academy.event.util.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("survey")
public class SurveyController {

    private final SaveQuestionMapper saveQuestionMapper;
    private final SurveyService service;

    @Autowired
    public SurveyController(SaveQuestionMapper saveQuestionMapper, SurveyService service) {
        this.saveQuestionMapper = saveQuestionMapper;
        this.service = service;
    }

    @GetMapping
    public ServerResponse<Page<SimpleSurveyDTO>> findAllSurveys(
            @PageableDefault(sort = {"creationDate"}, direction = Sort.Direction.DESC) Pageable pageable,
            @RequestBody(required = false) Map<String, Map<String, Object>> filters) {
        return ServerResponse.success(service.findAllFiltered(pageable, filters));
    }

    @PostMapping
    public ServerResponse<SimpleSurveyDTO> duplicateSurvey(Long id) {
        return ServerResponse.success(service.duplicateSurvey(id));
    }

    @PutMapping
    public ServerResponse<String> updateTitle(Long id, String title) {
        return ServerResponse.success(service.setTitleForSurvey(id, title));
    }

    @DeleteMapping
    public ServerResponse<Long> deleteSurvey(Survey survey) {
        service.delete(survey);
        return ServerResponse.success(survey.getId());
    }

    @PostMapping(value = "/createNewSurvey")
    public ServerResponse<Survey> saveSurvey(@RequestBody SaveSurveyDTO saveSurveyDTO) throws JsonProcessingException {
        Survey survey = new Survey();
        survey.setTitle(saveSurveyDTO.getTitle());
        long userID = saveSurveyDTO.getUserID();
        List<SurveyQuestion> surveyQuestions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for(SurveyQuestionDTO surveyQuestionDTO : saveSurveyDTO.getQuestions()){
            SurveyQuestion surveyQuestion = saveQuestionMapper.toEntity(surveyQuestionDTO);
            String  answers =  mapper.writeValueAsString(surveyQuestionDTO.getAnswers());
            surveyQuestion.setAnswers(answers);
            surveyQuestions.add(surveyQuestion);
        }
        return ServerResponse.success(service.saveSurveyWithQuestions(survey, userID, surveyQuestions));
    }
}
