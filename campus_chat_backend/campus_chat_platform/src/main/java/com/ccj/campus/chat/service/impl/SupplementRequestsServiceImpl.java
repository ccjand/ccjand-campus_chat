package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.SupplementRequests;
import com.ccj.campus.chat.mapper.SupplementRequestsMapper;
import com.ccj.campus.chat.service.SupplementRequestsService;
import org.springframework.stereotype.Service;

@Service
public class SupplementRequestsServiceImpl extends ServiceImpl<SupplementRequestsMapper, SupplementRequests> implements SupplementRequestsService {
}
