package com.softserve.academy.event.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.academy.event.annotation.PageableDefault;
import com.softserve.academy.event.dto.*;
import com.softserve.academy.event.entity.Survey;
import com.softserve.academy.event.entity.SurveyQuestion;
import com.softserve.academy.event.entity.enums.SurveyStatus;
import com.softserve.academy.event.service.db.SurveyService;
import com.softserve.academy.event.service.mapper.SaveQuestionMapper;
import com.softserve.academy.event.service.mapper.SurveyMapper;
import com.softserve.academy.event.util.DuplicateSurveySettings;
import com.softserve.academy.event.util.Page;
import com.softserve.academy.event.util.Pageable;
import com.softserve.academy.event.util.Sort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Api(value = "/survey")
@RestController
@RequestMapping("survey")
@Slf4j
public class SurveyController {

    private final SaveQuestionMapper saveQuestionMapper;
    private final SurveyService service;
    private final SurveyMapper surveyMapper;
    private final QuestionService questionService;

    @Autowired
    public SurveyController(SurveyService service, SurveyMapper surveyMapper, SaveQuestionMapper saveQuestionMapper, QuestionService questionService) {
        this.saveQuestionMapper = saveQuestionMapper;
        this.service = service;
        this.surveyMapper = surveyMapper;
        this.questionService = questionService;
    }

    @ApiOperation(value = "Get all surveys")
    @GetMapping
    public ResponseEntity<Page<SurveyDTO>> findAllSurveys(
            @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, name = "status") String status) {
        return ResponseEntity.ok(
                service.findAllByPageableAndStatus(pageable, status)
        );
    }

    @ApiOperation(value = "Duplicates a survey")
    @PostMapping
    public ResponseEntity<SurveyDTO> duplicateSurvey(@RequestBody DuplicateSurveySettings settings) {
        return ResponseEntity.ok(
                surveyMapper.toDTO(service.duplicateSurvey(settings))
        );
    }

    @ApiOperation(value = "Change the title of the survey")
    @PutMapping
    public ResponseEntity<Boolean> updateTitle(@RequestParam Long id, @RequestParam String title) {
        service.updateTitle(id, title);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/status/active")
    public ResponseEntity<Boolean> setStatusActive(@RequestParam Long id) {
        service.updateStatus(id, SurveyStatus.ACTIVE);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/status/done")
    public ResponseEntity<Boolean> setStatusDone(@RequestParam Long id) {
        service.updateStatus(id, SurveyStatus.DONE);
        return ResponseEntity.ok(true);
    }

    @ApiOperation(value = "Delete a survey")
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteSurvey(@RequestParam Long id) {
        service.delete(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(value = "/createNewSurvey")
    public ResponseEntity saveSurvey(@RequestBody SaveSurveyDTO saveSurveyDTO) throws JsonProcessingException {
        Survey survey = new Survey();
        survey.setTitle(saveSurveyDTO.getTitle());
        survey.setImageUrl(saveSurveyDTO.getSurveyPhotoName());
        List<SurveyQuestion> surveyQuestions = new ArrayList<>();
        for (SurveyQuestionDTO x : saveSurveyDTO.getQuestions()) {
            surveyQuestions.add(saveQuestionMapper.toEntity(x));
        }
        return ResponseEntity.ok(service.saveSurveyWithQuestions(survey, surveyQuestions));
        List<SurveyQuestion> surveyQuestions = getQuestionsEntities(saveSurveyDTO.getQuestions());
        return ResponseEntity.ok(service.saveSurveyWithQuestions(survey, surveyQuestions));
    }

    /**
      Method gets list of Question DTO and made list of entities with correct variant of answers
      Mapper can't make string from list, so i set it through object mapper
      @return List<SurveyQuestion> - list of entities but without established survey
    */
    private List<SurveyQuestion> getQuestionsEntities(List<SurveyQuestionDTO> surveyQuestionsDTO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        for (SurveyQuestionDTO surveyQuestionDTO : surveyQuestionsDTO) {
            SurveyQuestion surveyQuestion = saveQuestionMapper.toEntity(surveyQuestionDTO);
            String answers = mapper.writeValueAsString(surveyQuestionDTO.getChoiceAnswers());
            surveyQuestion.setChoiceAnswers(answers);
            surveyQuestions.add(surveyQuestion);
    }


    @ApiOperation(value = "Get a survey and get user access to edit him", response = SaveSurveyDTO.class)
    @GetMapping(value = "/edit/{id}")
    public ResponseEntity loadForEditSurvey(@PathVariable(name = "id") Long surveyId) throws IOException {
        List<SurveyQuestion> questions = questionService.findBySurveyId(surveyId);
        List<EditSurveyQuestionDTO> questionsDTO = new ArrayList<>();
        for (SurveyQuestion x : questions) {
            questionsDTO.add(saveQuestionMapper.toEditDTO(x));
        }
        EditSurveyDTO editSurveyDTO = new EditSurveyDTO(questionsDTO);
        Survey survey = service.findFirstById(surveyId).orElseThrow(SurveyNotFound::new);
        editSurveyDTO = setSurveysTitleAndPhotoName(survey, editSurveyDTO);
        editSurveyDTO = setSurveysPhotos(editSurveyDTO);
        if (questionsDTO.isEmpty())
            return new ResponseEntity(editSurveyDTO, HttpStatus.BAD_REQUEST);
        return new ResponseEntity(editSurveyDTO, HttpStatus.OK);
    }

    private EditSurveyDTO setSurveysTitleAndPhotoName(Survey survey, EditSurveyDTO editSurveyDTO) {
        editSurveyDTO.setTitle(survey.getTitle());
        editSurveyDTO.setSurveyPhotoName(survey.getImageUrl());
        return editSurveyDTO;
    }

    private EditSurveyDTO setSurveysPhotos(EditSurveyDTO editSurveyDTO) throws IOException {
        Properties properties = new Properties();
        InputStream propertiesFile = FileUploadController.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(propertiesFile);

        ObjectMapper objectMapper = new ObjectMapper();
//        for (EditSurveyQuestionDTO questionDTO : editSurveyDTO.getQuestions()) {
//            if (questionDTO.getType().toString().equals("RADIO_PICTURE") || questionDTO.getType().toString().equals("CHECKBOX_PICTURE")) {
//                for (String x : questionDTO.getChoiceAnswers()) {
//                    File file = new File(properties.getProperty("image.upload.dir") + File.separator + x);
////                    questionDTO.getFiles().add(file);
//                    List<File> files = questionDTO.getFiles();
//                    files.add(file);
//                }
//            }
//        }

        for (EditSurveyQuestionDTO question : editSurveyDTO.getQuestions()) {
            List<String> nameOfPictures = new ArrayList<>();
            if (question.getType().toString().equals("RADIO_PICTURE") || question.getType().toString().equals("CHECKBOX_PICTURE")) {
                List<File> files = new ArrayList<>();
                for (String pictureName : question.getChoiceAnswers()) {
                    files.add(new File(properties.getProperty("image.upload.dir") + File.separator + pictureName));
                }
            }
        }
        return editSurveyDTO;
    }


        @ApiOperation(value = "Get a survey and get user access to edit him", response = SaveSurveyDTO.class)
        @PostMapping(value = "/update/{id}")
        public ResponseEntity updateSurvey (@RequestBody SaveSurveyDTO saveSurveyDTO, @PathVariable("id") String id) throws
        JsonProcessingException {
            List<SurveyQuestion> questions = new ArrayList<>();
            for (SurveyQuestionDTO x : saveSurveyDTO.getQuestions()) {
                questions.add(saveQuestionMapper.toEntity(x));
            }
            return ResponseEntity.ok(service.editSurvey(Long.parseLong(id), questions));
        }
    }
