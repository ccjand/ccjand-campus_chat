package com.ccj.campus.chat.controller;

import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.service.SegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaf")
public class IdController {

    //号段ID
    @Autowired
    private SegmentService segmentService;

    @RequestMapping("/segment/{segmentTag}")
    public Result getSegmentId(@PathVariable("segmentTag") String segmentTag) {

        //获取snowflake分布式ID
        Result r = segmentService.getId(segmentTag);

        //判断是否成功，成功返回具体的id，不成功返回错误提示
        if (r.getStatus() == Status.SUCCESS) {
            return r;
        }

        return new Result(-1, Status.EXCEPTION);
    }
}