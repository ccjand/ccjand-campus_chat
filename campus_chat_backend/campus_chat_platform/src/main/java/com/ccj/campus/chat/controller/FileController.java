package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.dto.FileUploadResp;
import com.ccj.campus.chat.service.FileService;
import com.ccj.campus.chat.util.RequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author ccj
 * @Date 2026-01-16 19:03
 * @Description
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 通用文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ApiResult<FileUploadResp> uploadFile(@RequestPart("file") MultipartFile file) {
        Long uid = RequestHolder.get().getUid();
        FileUploadResp resp = fileService.uploadFile(uid, file);
        return ApiResult.success(resp);
    }

    /**
     * 头像上传
     */
    @PostMapping("/avatar/upload")
    public ApiResult<FileUploadResp> uploadAvatar(@RequestPart("file") MultipartFile file) {
        Long uid = RequestHolder.get().getUid();
        FileUploadResp resp = fileService.uploadAvatar(uid, file);
        return ApiResult.success(resp);
    }


}
