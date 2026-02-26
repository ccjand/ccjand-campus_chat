package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.Exams;
import com.ccj.campus.chat.mapper.ExamsMapper;
import com.ccj.campus.chat.service.ExamsService;
import org.springframework.stereotype.Service;

@Service
public class ExamsServiceImpl extends ServiceImpl<ExamsMapper, Exams> implements ExamsService {
}
