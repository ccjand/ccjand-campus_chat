package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.entity.Exams;
import com.ccj.campus.chat.service.ExamsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/exams")
public class ExamController {

    @Autowired
    private ExamsService examService;

}