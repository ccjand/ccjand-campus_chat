package com.ccj.campus.chat.utils;

import com.ccj.campus.chat.exception.BusinessException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * @Author ccj
 * @Date 2024-06-04 19:24
 * @Description
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioAsyncClient minioAsyncClient;

    public CompletableFuture<GetObjectResponse> getObjectResponse(String bucket, String objectName) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try {
            return minioAsyncClient.getObject(getObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public CompletableFuture<ObjectWriteResponse> uploadAsync(String bucket, String objectName, String contentType, InputStream stream) {
        System.out.println("键为 = " + objectName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .stream(stream, stream.available(), -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();

            return minioAsyncClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传文件失败, bucket:{}, object:{}, contentType:{}, 原因: {}", bucket, objectName, contentType, e.getMessage(), e);
            return null;
        }
    }


    public CompletableFuture<ObjectWriteResponse> uploadAsync(String bucket, String objectName, String contentType, byte[] compressedData) {
        System.out.println("键为 = " + objectName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .stream(new ByteArrayInputStream(compressedData), compressedData.length, -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();

            return minioAsyncClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传文件失败, bucket:{}, object:{}, contentType:{}, 原因: {}", bucket, objectName, contentType, e.getMessage(), e);
            throw new BusinessException("上传文件失败");
        }
    }


    public boolean upload(String bucket, String objectName, String contentType, byte[] compressedData) {
        System.out.println("键为 = " + objectName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .stream(new ByteArrayInputStream(compressedData), compressedData.length, -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("上传文件失败, bucket:{}, object:{}, contentType:{}, 原因: {}", bucket, objectName, contentType, e.getMessage(), e);
            throw new BusinessException("上传文件失败");
        }
    }

    /**
     * 文件上传到minio(分布式文件系统)
     *
     * @param bucket      桶
     * @param objectName  文件对象名(在桶中对应的文件名字)
     * @param contentType 内容(媒体)类型
     * @param stream      文件流
     * @return 返回是否上传成功
     */
    public boolean upload(String bucket, String objectName, String contentType, InputStream stream) {
        System.out.println("键为 = " + objectName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .stream(stream, stream.available(), -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("上传文件失败, bucket:{}, object:{}, contentType:{}, 原因: {}", bucket, objectName, contentType, e.getMessage(), e);
            throw new BusinessException("上传文件失败");
        }
    }


}
