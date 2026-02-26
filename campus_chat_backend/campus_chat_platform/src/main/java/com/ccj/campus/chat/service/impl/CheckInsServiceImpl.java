package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.CheckIns;
import com.ccj.campus.chat.mapper.CheckInsMapper;
import com.ccj.campus.chat.service.CheckInsService;
import org.springframework.stereotype.Service;

@Service
public class CheckInsServiceImpl extends ServiceImpl<CheckInsMapper, CheckIns> implements CheckInsService {
}
