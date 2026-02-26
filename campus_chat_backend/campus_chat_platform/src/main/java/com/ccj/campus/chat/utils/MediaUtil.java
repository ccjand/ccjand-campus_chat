package com.ccj.campus.chat.utils;

import com.ccj.campus.chat.exception.BusinessException;
import com.ccj.campus.chat.exception.CommonErrorEnum;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author ccj
 * @Date 2023-05-08 11:53
 * @Description 媒体资源工具类
 */
@Slf4j
public class MediaUtil {

    /**
     * 根据扩展名获取资源的媒体类型
     *
     * @return 返回扩展名对应的媒体类型
     */
    public static String getMineType(String extension) {
        //文件可能没有扩展名, ContentInfoUtil.findExtensionMatch(null)会有空指针异常
        if (extension == null) {
            extension = "";
        }

        ContentInfo contentInfo = ContentInfoUtil.findExtensionMatch(extension);
        String mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (contentInfo != null) mediaType = contentInfo.getMimeType();
        return mediaType;
    }

    public static String getMineTypeByName(String fileName) {
        String extension = getExtension(fileName);

        ContentInfo contentInfo = ContentInfoUtil.findExtensionMatch(extension);
        String mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (contentInfo != null) mediaType = contentInfo.getMimeType();
        return mediaType;
    }

    public static String getExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件名不能为空");
        }

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(dotIndex);
        }

        throw new BusinessException("文件扩展名错误");
    }

    /**
     * 根据日期生成文件上传的目录
     */
    public static String getUploadFolder(Long uid) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/");
        return format.format(new Date()) + uid + "/";
    }


    /**
     * 根据日期生成文件上传的目录/文件名
     */
    public static String getUploadFullFileName(String fileNameByMd5, String fileExtension, Long uid) {
        String uploadFolder = getUploadFolder(uid);
        return uploadFolder + fileNameByMd5 + fileExtension;
    }

    /**
     * 获取文件后缀 (带点)
     */
    public static String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件名不能为空");
        }
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new BusinessException("不支持的图片的类型");
        }
        return fileName.substring(index);
    }


    /**
     * 根据流生成md5摘要信息,作为资源名
     */
    public static String getFileNameMd5ByStream(InputStream inputStream) {
        String objectName;
        try {
            objectName = DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            log.error("生成md5文件名失败, 原因:{}", e.getMessage(), e);
            throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
        }
        return objectName;
    }

    public static String getFileNameMd5ByStream(byte[] data) {
        String objectName = "";
        try {
            objectName = DigestUtils.md5Hex(data);
        } catch (Exception e) {
            log.error("生成md5文件名失败, 原因:{}", e.getMessage(), e);
            throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
        }
        return objectName;
    }


    /**
     * 根据流的数据(字节数组)生成md5摘要信息,作为资源名
     */
    public static String getFileNameMd5ByBytes(byte[] content) {
        String md5Hex = "";
        try {
            md5Hex = DigestUtils.md5Hex(content);
        } catch (Exception e) {
            log.error("生成md5文件名失败, 原因:{}", e.getMessage(), e);
            throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
        }

        return md5Hex;
    }


    public static String generateMd5ByStream(InputStream stream) {
        byte[] buffer = new byte[1024 * 1024 * 3];

        int len = -1;

        MessageDigest md5Digest = DigestUtils.getMd5Digest();

        StringBuilder md5 = new StringBuilder();
        try {
            while ((len = stream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, len);
            }

            byte[] hash = md5Digest.digest();
            for (byte b : hash) {
                md5.append(String.format("%02x", b & 0xff));
            }
        } catch (IOException e) {
            log.error("生成md5文件名失败, 原因:{}", e.getMessage(), e);
            throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
        }
        return md5.toString();
    }

}
