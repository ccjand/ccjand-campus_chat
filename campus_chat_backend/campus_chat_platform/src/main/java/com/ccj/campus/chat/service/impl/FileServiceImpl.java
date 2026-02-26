package com.ccj.campus.chat.service.impl;

import com.ccj.campus.chat.cache.UserCache;
import com.ccj.campus.chat.dao.UserDao;
import com.ccj.campus.chat.dto.FileUploadResp;
import com.ccj.campus.chat.dto.MinioBuckets;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.exception.BusinessException;
import com.ccj.campus.chat.service.FileService;
import com.ccj.campus.chat.utils.MediaUtil;
import com.ccj.campus.chat.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author ccj
 * @Date 2026-01-16 19:04
 * @Description
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCache userCache;


    @Override
    public FileUploadResp uploadFile(Long uid, MultipartFile file) {
        try {
            String fileNameMd5 = MediaUtil.getFileNameMd5ByStream(file.getInputStream());
            String extension = MediaUtil.getExtension(file.getOriginalFilename());
            String fileUrl = MediaUtil.getUploadFullFileName(fileNameMd5, extension, uid);
            String mineType = MediaUtil.getMineType(extension);
            minioUtil.upload(MinioBuckets.USER_AVATAR_BUCKET, fileUrl, mineType, file.getInputStream());
            return new FileUploadResp(MinioBuckets.USER_AVATAR_BUCKET + "/" + fileUrl);
        } catch (IOException e) {
            throw new BusinessException("上传图片失败");
        }
    }

    @Override
    public FileUploadResp uploadAvatar(Long uid, MultipartFile file) {
        String avatarUrl = this.uploadFile(uid, file).getFileUrl();
        Users update = new Users();
        update.setId(uid);
        update.setAvatar(avatarUrl);
        userDao.updateById(update);
        userCache.userInfoChange(uid);
        return new FileUploadResp(update.getAvatar());
    }
}
