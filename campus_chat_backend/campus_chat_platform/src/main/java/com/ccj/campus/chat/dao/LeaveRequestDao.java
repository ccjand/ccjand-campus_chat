package com.ccj.campus.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.LeaveRequest;
import com.ccj.campus.chat.mapper.LeaveRequestMapper;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2026-01-16 17:13
 * @Description
 */
@Service
public class LeaveRequestDao extends ServiceImpl<LeaveRequestMapper, LeaveRequest> {



}
