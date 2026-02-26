package com.ccj.campus.chat.service;

import com.ccj.campus.chat.dto.FileUploadResp;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author ccj
 * @Date 2026-01-16 19:04
 * @Description
 */
public interface FileService {
    FileUploadResp uploadFile(Long uid, MultipartFile file);

    FileUploadResp uploadAvatar(Long uid, MultipartFile file);
}
