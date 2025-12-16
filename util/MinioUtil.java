package cn.edu.chtholly.util;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class MinioUtil {
    // 配置参数
    private static String ENDPOINT;
    private static String ACCESS_KEY;
    private static String SECRET_KEY;
    private static String BUCKET;

    private static final MinioClient minioClient;

    static {
        // 加载minio.properties配置文件
        Properties props = new Properties();
        try (InputStream inputStream = MinioUtil.class.getClassLoader().getResourceAsStream("minio.properties")) {
            // 校验配置文件是否存在
            if (inputStream == null) {
                throw new RuntimeException("未找到minio.properties配置文件，请检查src/main/resources目录下是否存在");
            }
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("加载minio.properties配置失败：" + e.getMessage(), e);
        }

        // 读取配置项
        ENDPOINT = props.getProperty("minio.endpoint");
        ACCESS_KEY = props.getProperty("minio.access-key");
        SECRET_KEY = props.getProperty("minio.secret-key");
        BUCKET = props.getProperty("minio.bucket");

        // 校验必填配置
        if (ENDPOINT == null || ENDPOINT.isBlank()) {
            throw new RuntimeException("minio.endpoint（MinIO服务地址）未配置");
        }
        if (ACCESS_KEY == null || ACCESS_KEY.isBlank()) {
            throw new RuntimeException("minio.access-key（访问密钥）未配置");
        }
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new RuntimeException("minio.secret-key（密钥）未配置");
        }
        if (BUCKET == null || BUCKET.isBlank()) {
            throw new RuntimeException("minio.bucket（存储桶名称）未配置");
        }

        // 保持原有客户端初始化方式
        minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
    }

    /**
     * 上传头像并生成与数据库存储格式一致的URL
     * 格式：users/[UUID]-avatar.扩展名
     */
    public static String uploadAvatar(Part avatarPart) throws Exception {
        // 1. 获取原始文件名和扩展名
        String originalFileName = avatarPart.getSubmittedFileName();
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".")); // 提取扩展名

        // 2. 生成唯一文件名：UUID + "-avatar" + 扩展名
        String fileName = UUID.randomUUID() + "-avatar" + ext;

        // 3. 存储路径：直接放在users目录下
        String objectName = "users/" + fileName;

        // 4. 上传文件到MinIO
        try (InputStream inputStream = avatarPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName) // 路径：users/UUID-avatar.ext
                            .stream(inputStream, avatarPart.getSize(), -1)
                            .contentType(avatarPart.getContentType())
                            .build()
            );
        }

        // 5. 生成完整访问URL
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }

    /**
     * 上传歌手头像（路径：artists/UUID-blob，无扩展名）
     */
    public static String uploadArtistAvatar(Part avatarPart) throws Exception {
        // 1. 生成歌手头像文件名：UUID + "-blob"
        String fileName = UUID.randomUUID() + "-blob";
        // 2. 存储路径：artists目录下
        String objectName = "artists/" + fileName;

        // 3. 上传到MinIO
        try (InputStream inputStream = avatarPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName) // 最终路径：vibe-music-data/artists/UUID-blob
                            .stream(inputStream, avatarPart.getSize(), -1)
                            .contentType(avatarPart.getContentType())
                            .build()
            );
        }

        // 4. 生成匹配需求的头像URL
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }



    // 上传轮播图 banners/[UUID]-banner.扩展名（与前端返回URL匹配）
    public static String uploadBanner(Part bannerPart) throws Exception {
        // 1. 获取原始文件名和扩展名（如".png"）
        String originalFileName = bannerPart.getSubmittedFileName();
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));

        // 2. 生成唯一文件名：UUID + "-banner" + 扩展名（保持与前端示例一致）
        String fileName = UUID.randomUUID() + "-banner" + ext;

        // 3. 存储路径：banners目录下（与前端URL中的"banners"路径匹配）
        String objectName = "banners/" + fileName;

        // 4. 上传文件到MinIO
        try (InputStream inputStream = bannerPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName)
                            .stream(inputStream, bannerPart.getSize(), -1)
                            .contentType(bannerPart.getContentType()) // 自动识别文件类型
                            .build()
            );
        }

        // 5. 生成完整访问URL（例如：http://localhost:9000/vibe-music-data/banners/xxx.png）
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }

    // 通用文件删除方法 用户头像、歌手头像、轮播图等
    public static void deleteFile(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return; // 空URL直接返回，避免异常
        }

        // 从URL中解析MinIO的对象路径（例如："banners/xxx.png"）
        String objectName = fileUrl.replace(ENDPOINT + "/" + BUCKET + "/", "");

        // 执行删除
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectName)
                        .build()
        );
    }

    // 保留原deleteAvatar方法
    public static void deleteAvatar(String oldAvatarUrl) throws Exception {
        deleteFile(oldAvatarUrl);
    }


    // 上传歌单封面（路径：playlists/UUID-blob，无扩展名，与歌手头像格式一致）
    public static String uploadPlaylistCover(Part coverPart) throws Exception {
        // 1. 生成歌单封面文件名：UUID + "-blob"（与前端示例一致）
        String fileName = UUID.randomUUID() + "-blob";
        // 2. 存储路径：playlists目录下
        String objectName = "playlists/" + fileName;

        // 3. 上传到MinIO
        try (InputStream inputStream = coverPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName) // 最终路径：playlists/UUID-blob
                            .stream(inputStream, coverPart.getSize(), -1)
                            .contentType(coverPart.getContentType())
                            .build()
            );
        }

        // 4. 生成完整访问URL
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }

    // 上传歌曲封面（路径：songCovers/UUID-blob，无扩展名）
    public static String uploadSongCover(Part coverPart) throws Exception {
        // 1. 生成唯一文件名：UUID + "-blob"
        String fileName = UUID.randomUUID() + "-blob";
        // 2. 存储路径：songCovers目录下
        String objectName = "songCovers/" + fileName;

        // 3. 上传到MinIO
        try (InputStream inputStream = coverPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName)
                            .stream(inputStream, coverPart.getSize(), -1)
                            .contentType(coverPart.getContentType())
                            .build()
            );
        }

        // 4. 生成完整访问URL
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }

    // 上传歌曲音频（路径：songs/UUID-原始文件名.mp3）
    public static String uploadSongAudio(Part audioPart) throws Exception {
        // 1. 获取原始文件名
        String originalFileName = audioPart.getSubmittedFileName();
        // 2. 生成唯一文件名：UUID + "-" + 原始文件名
        String fileName = UUID.randomUUID() + "-" + originalFileName;
        // 3. 存储路径：songs目录下
        String objectName = "songs/" + fileName;

        // 4. 上传到MinIO
        try (InputStream inputStream = audioPart.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName)
                            .stream(inputStream, audioPart.getSize(), -1)
                            .contentType(audioPart.getContentType())
                            .build()
            );
        }

        // 5. 生成完整访问URL
        return ENDPOINT + "/" + BUCKET + "/" + objectName;
    }

}