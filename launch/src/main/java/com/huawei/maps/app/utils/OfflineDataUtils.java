package com.huawei.maps.app.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.network.file.api.Progress;
import com.huawei.hms.network.file.api.Response;
import com.huawei.hms.network.file.api.exception.NetworkException;
import com.huawei.maps.app.common.utils.SharedPreUtil;
import com.huawei.maps.app.common.utils.ValidateUtil;
import com.huawei.maps.app.offline.api.OfflineDownloadObserver;
import com.huawei.maps.app.offline.api.OfflineMapAreaService;
import com.huawei.maps.app.offline.api.OfflineMapService;
import com.huawei.maps.app.offline.api.OfflineMapsDataBaseCallback;
import com.huawei.maps.auto.petalsdk.PetalSDKManager;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineConstants;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineDataLoadParam;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineHomeRegionResultBean;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineMapsInfo;
import com.huawei.maps.auto.sdk.businessbase.offline.callback.OfflineMapNetworkCallback;
import com.huawei.maps.offlinemap.model.OfflineDataMode;
import com.huawei.maps.offlinemap.utils.AutoOfflineDataUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class OfflineDataUtils {
    private static volatile OfflineDataUtils mOfflineDataUtils;
    private static final String TAG = "KikaISA_OfflineDataUtils";
    private int checkOfflineDataState = -1;

    private long startDownloadTime = 0;

    private long monthTime = 32L * 24L * 60L * 60L * 1000L;

    private Context context;

    private OfflineDownloadObserver mOfflineDownloadObserver;

    public List<OfflineMapsInfo> cloudList = new ArrayList<>();

    private long lastResumeTime = 0;

    private int monthMaxDownloadSize = 2000;

    private OfflineDataUtils() {
    }

    public static OfflineDataUtils getInstance() {
        if (mOfflineDataUtils == null) {
            synchronized (OfflineDataUtils.class) {
                if (mOfflineDataUtils == null) {
                    mOfflineDataUtils = new OfflineDataUtils();
                }
            }
        }
        return mOfflineDataUtils;
    }

    // 新增：安全获取 Context（优先实例字段）
    private Context getSafeContext(Context ctx) {
        return (this.context != null) ? this.context : ctx;
    }

    public String getIsaDataVersion() {
        // 完整替换以加入服务非空保护
        final String[] dataVersion = {""};
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger callbackCount = new AtomicInteger(0);
        LogUtils.getInstance().i(TAG, "start getAllOfflineRecords");

        PetalSDKManager petal = PetalSDKManager.getInstance();
        if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapDataBaseService() == null) {
            LogUtils.getInstance().e(TAG, "getIsaDataVersion petal or services is null");
            return "";
        }

        petal.getOfflineMapService().getMapDataBaseService().getAllOfflineRecords(new OfflineMapsDataBaseCallback() {
            @Override
            public void onQueryAllRecords(List<OfflineMapsInfo> records) {
                try {
                    if (records == null) {
                        LogUtils.getInstance().i(TAG, "onQueryAllRecords records is null");
                        latch.countDown();
                        return;
                    }
                    petal.getOfflineMapService().getMapDataBaseService().queryWorldBasicRecord(new OfflineMapsDataBaseCallback() {
                        @Override
                        public void onQueryGlobalBaseRecord(OfflineMapsInfo globalRecord) {
                            if (records == null || records.isEmpty()) {
                                LogUtils.getInstance().i(TAG, "onQueryGlobalBaseRecord records == null || records.isEmpty()");
                                latch.countDown();
                                return;
                            }
                            try {
                                if (globalRecord != null && globalRecord.getStatus() == OfflineConstants.OfflineDataStatus.FINISH) {
                                    try {
                                        if (!TextUtils.isEmpty(globalRecord.getOfflineMapVersion())) {
                                            LogUtils.getInstance().i(TAG, "globalRecord.getOfflineMapVersion() : " + globalRecord.getOfflineMapVersion());
                                            for (OfflineMapsInfo info : records) {
                                                if (info != null && info.getStatus() == OfflineConstants.OfflineDataStatus.FINISH) {
                                                    LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getOfflineMapVersion() : " + info.getOfflineMapVersion());
                                                    LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getRegionName() : " + info.getRegionName());
                                                    LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getCountryName() : " + info.getCountryName());
                                                    dataVersion[0] = getBiggerVersion(getBiggerVersion(globalRecord.getOfflineMapVersion(), info.getOfflineMapVersion()), dataVersion[0]);
                                                }
                                            }
                                        }
                                    } catch (Exception exception) {
                                        LogUtils.getInstance().e(TAG, "onQueryGlobalBaseRecord exception e = " + Utils.getStackTraceAsString(exception));
                                        latch.countDown();
                                    } finally {
                                        if (callbackCount.incrementAndGet() == records.size()) {
                                            latch.countDown();
                                        }
                                    }
                                } else {
                                    for (OfflineMapsInfo info : records) {
                                        if (info != null && info.getStatus() == OfflineConstants.OfflineDataStatus.FINISH) {
                                            LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getOfflineMapVersion() : " + info.getOfflineMapVersion());
                                            LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getRegionName() : " + info.getRegionName());
                                            LogUtils.getInstance().i(TAG, "onQueryAllRecords, info.getCountryName() : " + info.getCountryName());
                                            dataVersion[0] = getBiggerVersion(dataVersion[0], info.getOfflineMapVersion());
                                        }
                                    }
                                    latch.countDown();
                                }
                            } catch (Exception e) {
                                LogUtils.getInstance().e(TAG, "onQueryGlobalBaseRecord unexpected exception " + Utils.getStackTraceAsString(e));
                                latch.countDown();
                            }
                        }
                    });
                } catch (Exception exception) {
                    LogUtils.getInstance().e(TAG, "onQueryAllRecords exception");
                    latch.countDown();
                }
            }
        });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtils.getInstance().e(TAG, "latch.await exception e = " + Utils.getStackTraceAsString(e));
        }
        LogUtils.getInstance().i(TAG, "dataVersion = " + dataVersion[0]);
        return dataVersion[0];
    }

    public String getBiggerVersion(String version1, String version2) {
        if (version1 == null || "".equals(version1)) return (version2 != null) ? version2 : "";
        if (version2 == null || "".equals(version2)) return version1;
        // 将版本号按点号分割成部分
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        // 获取两个版本号的最大长度
        int maxLength = Math.max(parts1.length, parts2.length);

        // 遍历每个部分进行比较
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 < num2) {
                return version2; // version1 < version2，返回version2
            } else if (num1 > num2) {
                return version1; // version1 > version2，返回version1
            }
        }

        // 如果所有部分都相等，则版本号相等
        return version1; // version1 == version2
    }

    public void startCheckOfflineData() {
        new Thread(() -> {
            checkOfflineDataState = checkOfflineData(LogUtils.getInstance().isaOfflinedtaPath + "/offlinemaps") ? 1 : 0;
            LogUtils.getInstance().i(TAG, "the data check result is " + checkOfflineDataState);
        }).start();
    }

    public boolean checkOfflineData(String directoryPath) {
        File rootDir = new File(directoryPath);
        File hashFile = new File(rootDir, "SHA256.txt");

        // 1. 基础校验
        if (!validateBasicConditions(rootDir, hashFile)) {
            return false;
        }

        // 2. 读取哈希文件
        Map<String, String> hashMap;
        try {
            hashMap = readHashFile(hashFile);
        } catch (IOException e) {
            LogUtils.getInstance().i(TAG, "读取哈希文件失败: " + Utils.getStackTraceAsString(e));
            return false;
        }

        // 3. 遍历验证所有文件
        try {
            boolean result = traverseAndVerify(rootDir, rootDir, hashMap);
            if (!result) {
                LogUtils.getInstance().i(TAG, "traverseAndVerify result is false...");
                return false;
            }
            // 检查是否还有未验证的条目
            if (!hashMap.isEmpty()) {
                LogUtils.getInstance().i(TAG, "存在未找到的注册文件,数量为:" + hashMap.size());
                hashMap.keySet().forEach(path -> LogUtils.getInstance().i(TAG, "未找到: " + path));
                return false;
            }
            return true;
        } catch (IOException | NoSuchAlgorithmException e) {
            LogUtils.getInstance().i(TAG, "校验过程中发生错误: " + Utils.getStackTraceAsString(e));
            return false;
        }
    }

    private boolean validateBasicConditions(File directory, File hashFile) {
        // 校验目录有效性
        if (!directory.exists() || !directory.isDirectory()) {
            LogUtils.getInstance().i(TAG, "目录不存在或不是有效目录");
            return false;
        }

        // 校验哈希文件是否存在
        if (!hashFile.exists() || !hashFile.isFile()) {
            LogUtils.getInstance().i(TAG, "SHA256.txt文件不存在");
            return false;
        }

        return true;
    }

    private Map<String, String> readHashFile(File hashFile) throws IOException {
        Map<String, String> hashMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(hashFile))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    LogUtils.getInstance().e(TAG, "哈希文件格式错误（第" + lineNumber + "行）");
                    continue; // 跳过格式错误行
                }

                String hash = parts[0].trim();
                String relativePath = parts[1].trim().replace("\\", "/").replaceFirst("/", "");

                if (hash.length() != 64 || !hash.matches("[a-f0-9]{64}")) {
                    LogUtils.getInstance().e(TAG, "无效的SHA256格式（第" + lineNumber + "行）");
                    continue; // 跳过无效哈希
                }

                if (hashMap.containsKey(relativePath)) {
                    LogUtils.getInstance().e(TAG, "重复的文件路径（第" + lineNumber + "行）: " + relativePath);
                    continue; // 跳过重复路径
                }
                hashMap.put(relativePath, hash);
            }
        }
        LogUtils.getInstance().i(TAG, "索引的文件总数量为：" + hashMap.size());
        return hashMap;
    }

    private boolean traverseAndVerify(File currentDir, File rootDir, Map<String, String> hashMap) throws IOException, NoSuchAlgorithmException {

        File[] files = currentDir.listFiles();
        if (files == null) return false;

        // 将根目录转换为 Path 对象
        Path rootPath = rootDir.toPath();

        for (File file : files) {
            // 跳过哈希文件
            if (file.getName().equalsIgnoreCase("SHA256.txt")) {
                continue;
            }

            // 获取文件相对于根目录的相对路径（关键步骤）
            Path filePath = file.toPath();
            Path relativePath = rootPath.relativize(filePath); // 生成相对路径
            String relativePathStr = relativePath.toString().replace("\\", "/"); // 统一路径格式

            if (file.isDirectory()) {
                // 递归处理子目录
                if (!traverseAndVerify(file, rootDir, hashMap)) {
                    return false;
                }
            } else {
                // 使用相对路径检查哈希表
                if (!hashMap.containsKey(relativePathStr)) {
                    LogUtils.getInstance().i(TAG, "发现未注册文件: " + relativePathStr);
                    return false;
                }

                // 验证哈希值
                String expectedHash = hashMap.get(relativePathStr);
                String actualHash = calculateSHA256(file);
                if (!expectedHash.equals(actualHash)) {
                    LogUtils.getInstance().i(TAG, "哈希值不匹配: " + relativePathStr);
                    return false;
                }

                // 移除已验证条目
                hashMap.remove(relativePathStr);
            }
        }
        return true;
    }

    private String calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(is)) {

            byte[] buffer = new byte[8192];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public boolean getCheckOfflineDataState() {
        return checkOfflineDataState == 1 ? true : false;
    }

    public void checkDownload(Context context, OfflineMapsInfo currentRegionInfoFinal) {
        Context safeCtx = getSafeContext(context);
        registerDownloadObserver();

        PetalSDKManager petal = PetalSDKManager.getInstance();
        if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapDataBaseService() == null) {
            LogUtils.getInstance().e(TAG, "checkDownload petal or services is null");
            return;
        }

        petal.getOfflineMapService().getMapDataBaseService().getAllOfflineRecords(new OfflineMapsDataBaseCallback() {
            @Override
            public void onQueryAllRecords(List<OfflineMapsInfo> records) {
                // 本地所有数据
                if (ValidateUtil.isEmpty(records)) {
                    LogUtils.getInstance().i(TAG, "checkDownload 获取到的本地离线数据为空");
                    return;
                }

                if (currentRegionInfoFinal == null) {
                    LogUtils.getInstance().i(TAG, "checkDownload 获取到的本区域离线数据信息为空");
                    return;
                }
                for (OfflineMapsInfo info : records) {
                    if (info == null) continue;
                    if (currentRegionInfoFinal.equals(info)) {
                        continue;
                    }
                    double originalSize = info.getOriginalSize();
                    // 状态是下载中或暂停的，继续下载
                    double naviSpace = StorageUtils.getNaviAvailableSpaceMB();
                    double downloadingSpace = StorageUtils.getDownloadingAvailableSpaceMB(safeCtx);
                    if (info.getStatus() != OfflineConstants.OfflineDataStatus.FINISH && (naviSpace <= originalSize || downloadingSpace <= originalSize)) {
                        LogUtils.getInstance().i(TAG, "checkDownload 内存空间不足，下载失败, naviSpace = " + naviSpace + " downloadingSpace = " + downloadingSpace + " originalSize = " + originalSize);
                        continue;
                    }
                    if (info.getStatus() == OfflineConstants.OfflineDataStatus.ERROR) {
                        // 删除未下载完的数据
                        LogUtils.getInstance().i(TAG, "checkDownload 删除未下载完的数据");
                        OfflineDataUtils.getInstance().deleteTempFile(info.getFileId(), safeCtx);
                        info.setUpdateState(OfflineConstants.OfflineDataStatus.NEED_DOWNLOAD);
                        if (petal.getOfflineMapService().getMapDataBaseService() != null) {
                            petal.getOfflineMapService().getMapDataBaseService().insert(info);
                        }
                        LogUtils.getInstance().i(TAG, "checkDownload 状态是下载中或暂停的，继续下载 first error，info = " + GsonUtil.toJson(info));
                        OfflineDataUtils.getInstance().updateToCloudInfo(info, cloudList, 3);
                        if (petal.getOfflineMapService().getMapAreaService() != null) {
                            petal.getOfflineMapService().getMapAreaService().resumeDownloadAreaData(getDownloadingInfo(info));
                        }
                    } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.ON_PROGRESS
                            || info.getStatus() == OfflineConstants.OfflineDataStatus.PAUSE) {
                        // 状态是下载中或暂停的，继续下载
                        LogUtils.getInstance().i(TAG, "checkDownload 状态是下载中或暂停的，继续下载，info = " + GsonUtil.toJson(info));
                        OfflineDataUtils.getInstance().updateToCloudInfo(info, cloudList, 4);
                        if (petal.getOfflineMapService().getMapAreaService() != null) {
                            petal.getOfflineMapService().getMapAreaService().resumeDownloadAreaData(getDownloadingInfo(info));
                        }
                    } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.DOWNLOAD_SUCCESS) {
                        // 下载成功的，解压移动文件
                        LogUtils.getInstance().i(TAG, "checkDownload 下载成功的，解压移动文件，info = " + GsonUtil.toJson(info));
                        if (petal.getOfflineMapService().getMapAreaService() != null) {
                            petal.getOfflineMapService().getMapAreaService().handleSuccessFile(info);
                        }
                    } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.UNZIP_SUCCESS) {
                        // 解压成功的，移动文件
                        LogUtils.getInstance().i(TAG, "checkDownload 解压成功的，移动文件，info = " + GsonUtil.toJson(info));
                        if (petal.getOfflineMapService().getMapAreaService() != null) {
                            petal.getOfflineMapService().getMapAreaService().moveFile(info);
                        }
                    }
                }
            }
        });
    }

    public synchronized void registerDownloadObserver() {
        LogUtils.getInstance().i(TAG, "start registerDownloadObserver..");
        if (mOfflineDownloadObserver == null) {
            mOfflineDownloadObserver = new OfflineDownloadObserver() {
                @Override
                public void onStart(OfflineMapsInfo offlineMapsInfo) {
                }

                @Override
                public void onProgress(OfflineMapsInfo offlineMapsInfo, Progress progress) {
                    if (offlineMapsInfo != null && progress != null) {
                        LogUtils.getInstance().i(TAG, "onProgress info RegionName = " + offlineMapsInfo.getRegionName() + ", CountryName = " + offlineMapsInfo.getCountryName() + " kika download progress = " + progress.getProgress());
                    }
                }

                @Override
                public void onProgress(OfflineMapsInfo offlineMapsInfo) {
                    if (offlineMapsInfo != null) {
                        LogUtils.getInstance().i(TAG, "onProgress info CountryName = " + offlineMapsInfo.getCountryName() + ", getRegionName =" + offlineMapsInfo.getRegionName() + " ,Progress = " + offlineMapsInfo.getDownloadProgress());
                    }
                }

                @Override
                public void onSuccess(OfflineMapsInfo offlineMapsInfo) {
                    LogUtils.getInstance().i(TAG, GsonUtil.toJson(offlineMapsInfo) + "下载成功！！！");
                }

                @Override
                public void onFinish(OfflineMapsInfo offlineMapsInfo) {
                    float size = DownloadPrefs.getMonthDownloadedSize();
                    LogUtils.getInstance().i(TAG, "onFinish monthDownloadedSize = " + size + " ,offlineMapsInfo.getPackageSize() = " + (offlineMapsInfo != null ? offlineMapsInfo.getPackageSize() : "null") + ",offlineMapsInfo = " + GsonUtil.toJson(offlineMapsInfo));
                }

                @Override
                public void onException(Response response, OfflineMapsInfo offlineMapsInfo, NetworkException e) {
                    LogUtils.getInstance().i(TAG, "onException info offlineMapsInfo = " + GsonUtil.toJson(offlineMapsInfo));
                    LogUtils.getInstance().i(TAG, "onException info " + (e != null ? e.getMessage() : "null") + " 具体错误内容 " + Utils.getStackTraceAsString(e));
                    // 如果有异常，代表下载链接失效,可以任务是过期的
                    // 自己决定 删除 还是 重新下载
                    // 1 删除
                    deleteTempFile(offlineMapsInfo != null ? offlineMapsInfo.getFileId() : null, getSafeContext(null));
                }

                @Override
                public void onDownLoadStatus(OfflineMapsInfo offlineMapsInfo, int i) {
                }

                @Override
                public void onDownLoadStatus(long l, OfflineMapsInfo offlineMapsInfo, int i) {
                }
            };

            PetalSDKManager petal = PetalSDKManager.getInstance();
            if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapAreaService() == null) {
                LogUtils.getInstance().e(TAG, "registerDownloadObserver failed: petal or mapAreaService is null");
                return;
            }

            petal.getOfflineMapService().getMapAreaService().addOfflineDataObserver(mOfflineDownloadObserver);
            LogUtils.getInstance().i(TAG, "registerDownloadObserver success..");
        }
    }


    public <T extends Parcelable> void saveToFile(T object, String fileName) {
        if (object == null) {
            LogUtils.getInstance().i(TAG, "saveToFile object is null");
            return;
        }
        if (TextUtils.isEmpty(fileName)) {
            LogUtils.getInstance().i(TAG, "saveToFile fileName is empty");
            return;
        }
        new Thread(() -> {
            // 将对象写入 Parcel
            Parcel parcel = Parcel.obtain();
            try {
                object.writeToParcel(parcel, 0);
                byte[] bytes = parcel.marshall();

                // 1. 获取基础目录（私有文件目录）
                File baseDir = new File(LogUtils.LOG_DIR);
                // 2. 拼接完整路径
                File targetFile = new File(baseDir, fileName);
                // 3. 确保父目录存在
                File parentDir = targetFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean dirsCreated = parentDir.mkdirs();
                    if (!dirsCreated) {
                        LogUtils.getInstance().i(TAG, "无法创建目录: " + parentDir.getAbsolutePath());
                        return;
                    }
                }
                // 4. 写入文件
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(bytes);
                    fos.flush();
                    LogUtils.getInstance().i(TAG, "文件保存成功: " + targetFile.getAbsolutePath());
                } catch (IOException e) {
                    LogUtils.getInstance().i(TAG, "写入失败:  e = " + Utils.getStackTraceAsString(e));
                }
            } catch (Exception e) {
                LogUtils.getInstance().i(TAG, "parcel write error: " + Utils.getStackTraceAsString(e));
            } finally {
                parcel.recycle();
            }
        }).start();
    }

    public <T extends Parcelable> T loadFromFile(String fileName, Parcelable.Creator<T> creator) {
        if (TextUtils.isEmpty(fileName) || creator == null) {
            LogUtils.getInstance().i(TAG, "loadFromFile invalid args");
            return null;
        }
        File file = new File(LogUtils.LOG_DIR, fileName);

        // 检查文件是否存在
        if (!file.exists()) {
            LogUtils.getInstance().i(TAG, "文件不存在: " + fileName);
            return null;
        }

        // 检查文件是否为空
        if (file.length() == 0) {
            LogUtils.getInstance().i(TAG, "文件内容为空: " + fileName);
            return null;
        }

        byte[] bytes;

        // 从文件中读取字节数组
        try (FileInputStream fis = new FileInputStream(file)) {
            int available = fis.available();
            if (available <= 0) {
                LogUtils.getInstance().i(TAG, "file available size <= 0");
                return null;
            }
            bytes = new byte[available];
            int read = fis.read(bytes);
            if (read <= 0) {
                LogUtils.getInstance().i(TAG, "读取字节失败");
                return null;
            }
        } catch (FileNotFoundException e) {
            LogUtils.getInstance().i(TAG, "文件未找到: " + fileName + " e = " + Utils.getStackTraceAsString(e));
            return null;
        } catch (IOException e) {
            LogUtils.getInstance().i(TAG, "读取文件时发生错误: " + fileName + " e = " + Utils.getStackTraceAsString(e));
            return null;
        }

        // 检查读取的字节数组是否为空
        if (bytes == null || bytes.length == 0) {
            LogUtils.getInstance().i(TAG, "读取的字节数组为空: " + fileName);
            return null;
        }

        try {
            // 将字节数组反序列化为对象
            Parcel parcel = Parcel.obtain();
            try {
                parcel.unmarshall(bytes, 0, bytes.length); // 可能会抛出异常
                parcel.setDataPosition(0); // 重置 Parcel 的读取位置

                // 从 Parcel 中创建对象
                T object = creator.createFromParcel(parcel); // 可能会抛出异常
                return object;
            } finally {
                parcel.recycle(); // 释放 Parcel 对象
            }
        } catch (RuntimeException e) {
            LogUtils.getInstance().i(TAG, "反序列化时发生错误: " + e.getMessage() + " e = " + Utils.getStackTraceAsString(e));
            return null;
        }
    }

    public boolean deleteFile(String fileName) {
        // 获取文件路径
        File file = new File(LogUtils.LOG_DIR, fileName);

        // 删除文件
        if (file.exists()) {
            return file.delete();
        } else {
            LogUtils.getInstance().i(TAG, "文件不存在: " + fileName);
            return false;
        }
    }

    public void deleteTempFile(String fileId, Context context) {
        Context safeCtx = getSafeContext(context);
        if (safeCtx == null) {
            LogUtils.getInstance().i(TAG, "deleteTempFile context is null !!!");
            return;
        }
        if (TextUtils.isEmpty(fileId)) {
            return;
        }
        int lastSlash = fileId.lastIndexOf("/");
        int lastDot = fileId.lastIndexOf(".");
        if (lastSlash < 0 || lastDot <= lastSlash) {
            LogUtils.getInstance().i(TAG, "deleteTempFile fileId format is invalid: " + fileId);
            return;
        }
        String fileName = fileId.substring(lastSlash + 1, lastDot); // 截取文件名，不包含 '/'
        File extDir = safeCtx.getExternalFilesDir(null);
        if (extDir == null) {
            LogUtils.getInstance().i(TAG, "deleteTempFile getExternalFilesDir returned null");
            return;
        }
        String offlineDownloadPath = extDir.getAbsolutePath() + "/offlinemaps/tempDownload";
        if (TextUtils.isEmpty(offlineDownloadPath)) {
            return;
        }
        File file = new File(offlineDownloadPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (ValidateUtil.isEmpty(files)) {
                return;
            }
            for (File filee : files) {
                if (filee == null) continue;
                String name = filee.getName();
                if (TextUtils.isEmpty(name)) {
                    continue;
                }
                int indexOf = name.lastIndexOf(".");
                if (indexOf != -1 && fileName.equals(name.substring(0, indexOf))) {
                    boolean deleteFile = filee.delete();
                    LogUtils.getInstance().i(TAG, "deleteUnZipMap: " + deleteFile + " fileId: " + fileId);
                }
            }
        }
    }

    public boolean monthDownloadComplete() {
        startDownloadTime = DownloadPrefs.getStartDownloadTime();
        float monthDownloadedSize = DownloadPrefs.getMonthDownloadedSize();
        long time = System.currentTimeMillis() - startDownloadTime;
        LogUtils.getInstance().i(TAG, "monthDownloadComplete monthDownloadedSize = " + monthDownloadedSize + " ,time = " + time + " ,startDownloadTime = " + startDownloadTime);
        if (time >= monthTime) {
            monthDownloadedSize = 0;
            startDownloadTime = System.currentTimeMillis();
            DownloadPrefs.saveDownloadData(monthDownloadedSize, startDownloadTime);
        }

        if (time < monthTime && monthDownloadedSize >= monthMaxDownloadSize) {
            LogUtils.getInstance().i(TAG, "monthDownloadComplete The download volume has exceeded 2000MB this month!!!");
            return true;//一个月之内
        }
        return false;
    }

    public void checkUpdate(Context context, double lat, double lon) {
        if (context != null) {
            this.context = context;
        } else {
            LogUtils.getInstance().i(TAG, "checkUpdate received null context, using existing instance context");
        }
        Context safeCtx = getSafeContext(context);

        if (monthDownloadComplete()) {
            return;
        }
        updateGlobal();

        PetalSDKManager petal = PetalSDKManager.getInstance();
        if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapDataBaseService() == null) {
            LogUtils.getInstance().e(TAG, "checkUpdate petal or services is null");
            return;
        }

        petal.getOfflineMapService().getMapDataBaseService().getAllOfflineRecords(new OfflineMapsDataBaseCallback() {
            @Override
            public void onQueryAllRecords(List<OfflineMapsInfo> records) {
                if (ValidateUtil.isEmpty(records)) {
                    LogUtils.getInstance().i(TAG, "checkUpdate records is empty");
                    return;
                }
                List<OfflineMapsInfo> localList = new ArrayList<>(records);
                for (OfflineMapsInfo localInfo : localList) {//防止一个国家未下载完，就开始下载另外一个
                    if (localInfo == null) continue;
                    if (localInfo.getStatus() != OfflineConstants.OfflineDataStatus.FINISH && !AutoOfflineDataUtil.isGlobalPackage(localInfo)) {
                        LogUtils.getInstance().i(TAG, "checkUpdate have data is not finished!!!");
                        kikaTryResumeDownload(safeCtx, null);//尝试恢复下载未下载完成的数据
                        return;
                    }
                }
                if (petal.getOfflineMapService().getMapAreaService() == null) {
                    LogUtils.getInstance().e(TAG, "checkUpdate mapAreaService is null");
                    return;
                }
                petal.getOfflineMapService().getMapAreaService().getAllOfflineMapAreaList(new OfflineMapNetworkCallback() {
                    @Override
                    public void onSuccess(List<OfflineMapsInfo> offlineMapsInfoList) {
                        LogUtils.getInstance().i(TAG, "checkUpdate update local list version");
                        if (!localList.stream().anyMatch(mapsInfo -> frontSmaller(mapsInfo.getOfflineMapVersion(), "20250710"))) {//判断版本号是否有小于20250710的
                            LogUtils.getInstance().i(TAG, "checkUpdate No version lower then 20250710...");
                            checkDownloadByLocation(offlineMapsInfoList, localList, safeCtx, lat, lon);
                            return;
                        }
                        cloudList = new ArrayList<>(offlineMapsInfoList);
                        LogUtils.getInstance().i(TAG, "checkUpdate cloud list size " + cloudList.size() + " local list size " + localList.size());

                        List<OfflineMapsInfo> unCheckedRegionList =
                                cloudList.stream().filter(info -> !localList.contains(info)).collect(Collectors.toList());
                        if (ValidateUtil.isEmpty(unCheckedRegionList)) {
                            checkDownloadByLocation(offlineMapsInfoList, localList, safeCtx, lat, lon);
                            LogUtils.getInstance().i(TAG, "checkUpdate unCheckedRegionList is empty !!!");
                            return;
                        }
                        localList.removeAll(unCheckedRegionList);
                        for (OfflineMapsInfo mapsInfo : unCheckedRegionList) {
                            if (AutoOfflineDataUtil.isGlobalPackage(mapsInfo)) {
                                continue;
                            }
                            OfflineMapsInfo offlineMapsInfo = new OfflineMapsInfo();
                            offlineMapsInfo.setCountryId(mapsInfo.getCountryId());
                            offlineMapsInfo.setCountryName(mapsInfo.getCountryName());
                            String fileId = mapsInfo.getFileId();
                            if (TextUtils.isEmpty(fileId)) {
                                continue;
                            }
                            int lastIndex = fileId.lastIndexOf(AutoOfflineDataUtil.PATH_SPLIT);
                            if (lastIndex <= 0) {
                                continue;
                            }
                            String[] elements = AutoOfflineDataUtil.getFileIdElements(fileId);
                            if (ValidateUtil.isEmpty(elements) || elements.length < 3) {
                                continue;
                            }
                            String subString = fileId.substring(0, lastIndex);
                            String countryFileId = subString + AutoOfflineDataUtil.PATH_SPLIT + elements[0] + AutoOfflineDataUtil.FILENAME_UNDERLINE + elements[1] + AutoOfflineDataUtil.ZIP_FILE_SUFFIX;
                            LogUtils.getInstance().i(TAG, "checkUpdate countryName " + mapsInfo.getCountryName() + " regionName " + mapsInfo.getRegionName() + " ,dataVersion = " + mapsInfo.getOfflineMapVersion() + ", countryFileId " + countryFileId);
                            offlineMapsInfo.setFileId(countryFileId);
                            OfflineDataLoadParam param = new OfflineDataLoadParam.Builder()
                                    .setNotCheckNaviFiles(false)
                                    .setNotCheckRenderFiles(OfflineDataMode.FILLING_EHP_PURE_OFFLINE
                                            .equals(AutoOfflineDataUtil.getDataMode()) ? true : false)
                                    .setNotCheckSearchFiles(OfflineDataMode.FILLING_EHP_PURE_OFFLINE
                                            .equals(AutoOfflineDataUtil.getDataMode()) ? true : false)
                                    .build();
                            if (AutoOfflineDataUtil.isFillingOfflineFileExist(param, offlineMapsInfo, false)) {
                                LogUtils.getInstance().i(TAG, "checkUpdate countryName " + mapsInfo.getCountryName() + " regionName " + mapsInfo.getRegionName() + " ,dataVersion = " + mapsInfo.getOfflineMapVersion());
                                OfflineMapsInfo copyMapsInfo = GsonUtil.getGson().fromJson(GsonUtil.toJson(mapsInfo), OfflineMapsInfo.class);
                                copyMapsInfo.setStatus(OfflineConstants.OfflineDataStatus.FINISH);
                                copyMapsInfo.setOfflineMapVersion("20250333");
                                PetalSDKManager.getInstance().getOfflineMapService().getMapDataBaseService().insert(copyMapsInfo);
                                localList.add(copyMapsInfo);
                                LogUtils.getInstance().i(TAG, "copyMapsInfo = " + GsonUtil.toJson(copyMapsInfo));
                                OfflineMapsInfo countryInfo = new OfflineMapsInfo();
                                countryInfo.setCountryId(copyMapsInfo.getCountryId());
                                if (localList.contains(countryInfo)) {
                                    localList.remove(countryInfo);
                                    PetalSDKManager.getInstance().getOfflineMapService().getMapDataBaseService().delete(countryInfo);
                                    LogUtils.getInstance().i(TAG, "checkUpdate delete country info " + GsonUtil.toJson(countryInfo));
                                }
                            }
                        }
                        checkDownloadByLocation(offlineMapsInfoList, localList, safeCtx, lat, lon);
                    }
                });
            }
        });
    }

    public boolean frontSmaller(String version1, String version2) {
        if (TextUtils.isEmpty(version1)) {
            LogUtils.getInstance().e(TAG, "frontSmaller version1 is exception!!!");
            return false;
        }
        try {
            // 转换为 long 类型比较
            if (Long.parseLong(version1) < Long.parseLong(version2)) {
                return true;
            }
        } catch (Exception e) {
            LogUtils.getInstance().e(TAG, "frontSmaller error!!!");
        }
        return false;
    }

    public void checkDownloadByLocation(List<OfflineMapsInfo> offlineMapsInfoList, List<OfflineMapsInfo> list, Context context, double lat, double lon) {
        //LogUtils.getInstance().i(TAG, "checkDownloadByLocation Latitude=" + lat + " , Longitude = " + lon);
        List<OfflineMapsInfo> localList = new ArrayList<>();
        localList.addAll(list);

        PetalSDKManager.getInstance().getOfflineMapService().getOfflineHomeRegion(list1 -> {
            if (list1 == null || list1.isEmpty()) {
                LogUtils.getInstance().i(TAG, "checkDownloadByLocation getOfflineHomeRegion list1 no data...");
                return;
            }

            LogUtils.getInstance().i(TAG, "checkDownloadByLocation getOfflineHomeRegion list1.get(0) = " + GsonUtil.toJson(list1.get(0)) + " ,size = " + list1.size());
            OfflineMapsInfo info = new OfflineMapsInfo(list1.get(0));
            boolean hasFind = false;
            // 查找当前区域
            for (OfflineMapsInfo regionInfo : localList) {
                if (regionInfo.getCountryId().equals(info.getCountryId())) {
                    if (OfflineMapsInfo.DEFAULT_VALUE.equals(info.getRegionId()) || (regionInfo.getSubRegionId() != null &&
                            regionInfo.getSubRegionId().contains(info.getRegionId()) || regionInfo.getRegionId().equals(info.getRegionId()))
                            || (regionInfo.getCnSubRegionId() != null && regionInfo.getCnSubRegionId().contains(info.getRegionId()))) {
                        info = regionInfo;
                        hasFind = true;
                    }
                }
            }
            final OfflineMapsInfo currentRegionInfoFinal = hasFind ? info : null;
            if (!hasFind) {
                LogUtils.getInstance().i(TAG, "checkDownloadByLocation not find the regionInfo");
                return;
            }
            LogUtils.getInstance().i(TAG, "checkDownloadByLocation currentRegionInfoFinal  " + currentRegionInfoFinal.getOfflineMapVersion() + " ,CountryId = "
                    + currentRegionInfoFinal.getCountryId() + ",RegionId = " + currentRegionInfoFinal.getRegionId());
            checkDownload(context, info);

            if (offlineMapsInfoList == null || offlineMapsInfoList.isEmpty()) {
                LogUtils.getInstance().i(TAG, "checkDownloadByLocation getAllOfflineMapAreaList list2 no data...");
                return;
            }
            List<OfflineMapsInfo> newCloudList = new ArrayList<>();
            newCloudList.addAll(offlineMapsInfoList);
            // 检查是否有更新
            LogUtils.getInstance().i(TAG, "checkDownloadByLocation start check the offline data...");
            if (newCloudList.contains(currentRegionInfoFinal)) {
                OfflineMapsInfo cloudInfo = newCloudList.get(newCloudList.indexOf(currentRegionInfoFinal));
                String localVersion = currentRegionInfoFinal.getOfflineMapVersion();
                String cloudVersion = cloudInfo.getOfflineMapVersion();
                LogUtils.getInstance().i(TAG, "checkDownloadByLocation localVersion = " + localVersion + " cloudVersion = " + cloudVersion);
                for (OfflineMapsInfo checkedInfo : newCloudList) {
                    if (checkedInfo.getCountryId().equals(currentRegionInfoFinal.getCountryId())) {
                        int index = localList.indexOf(checkedInfo);
                        if (index >= 0) {
                            OfflineMapsInfo localInfo = localList.get(index);
                            checkStartDownload(localInfo, checkedInfo, "A1 no A0 data check");
                            LogUtils.getInstance().i(TAG, "checkDownloadByLocation start checkStartDownload localInfo = " + GsonUtil.toJson(localInfo));
                        }
                    }
                }
            }
        }, new LatLng(lat, lon));
    }

    public void checkStartDownload(OfflineMapsInfo localInfo, OfflineMapsInfo cloudInfo, String tag) {
        if (monthDownloadComplete()) {
            return;
        }
        String localVersion = localInfo.getOfflineMapVersion();
        String cloudVersion = cloudInfo.getOfflineMapVersion();

        double naviSpace = StorageUtils.getNaviAvailableSpaceMB();
        double downloadingSpace = StorageUtils.getDownloadingAvailableSpaceMB(context);
        LogUtils.getInstance().i(TAG, tag + " checkStartDownload localVersion = " + localVersion + " cloudVersion = " + cloudVersion);
        // 云侧版本大于当前本地版本，升级
        if (localVersion != null && !localVersion.equals(getBiggerVersion(localVersion, cloudVersion)) || localInfo.getUpdateState() == 2) {
            double cloudPackageSize = cloudInfo.getPackageSize();
            double cloudOriginalSize = cloudInfo.getOriginalSize();
            if (naviSpace > cloudOriginalSize && downloadingSpace > cloudOriginalSize) {
                // 开始下载
                LogUtils.getInstance().i(TAG, tag + " checkStartDownload start download the new offline data, naviSpace = " + naviSpace + " downloadingSpace = " + downloadingSpace + " cloudInfo getPackageSize = " + cloudPackageSize + " cloudInfo getOriginalSize = " + cloudOriginalSize);
                localInfo.updateToClould(cloudInfo);
                localInfo.setUpdateState(2);
                if (localInfo.getStatus() == 1 ||//WAITING
                        localInfo.getStatus() == 2) {//ON PROGRESS
                    localInfo.setStatus(3);//set pause
                }
                float monthDownloadedSize = DownloadPrefs.getMonthDownloadedSize();
                if (monthDownloadedSize > 0 && (monthDownloadedSize >= monthMaxDownloadSize || monthDownloadedSize + cloudPackageSize > monthMaxDownloadSize)) {
                    LogUtils.getInstance().i(TAG, "checkStartDownload 每月的下载量已超额，此处终止下载判断 monthDownloadedSize = " + monthDownloadedSize + " localInfo.getPackageSize() = " + cloudPackageSize + " cloudInfo getOriginalSize = " + cloudOriginalSize);
                    return;
                }
                PetalSDKManager.getInstance().getOfflineMapService().getMapAreaService().startDownloadAreaData(getDownloadingInfo(localInfo));
                monthDownloadedSize = (float) (monthDownloadedSize + cloudPackageSize);
                DownloadPrefs.saveMonthDownloadedSize(monthDownloadedSize);
                LogUtils.getInstance().i(TAG, "checkStartDownload 已经开始本次下载 monthDownloadedSize = " + monthDownloadedSize + " cloudPackageSize = " + cloudPackageSize + " cloudInfo getOriginalSize = " + cloudOriginalSize);
                LogUtils.getInstance().i(TAG, "checkStartDownload 开始下载数据 localInfo = " + GsonUtil.toJson(localInfo));
            } else {
                LogUtils.getInstance().i(TAG, tag + "checkStartDownload 内存空间不足，下载失败, naviSpace = " + naviSpace + " downloadingSpace = " + downloadingSpace + "cloudPackageSize = " + cloudPackageSize + " cloudInfo getOriginalSize = " + cloudOriginalSize);
            }
        }
    }

    /**
     * 全球基础包检测更新下载
     */
    private void updateGlobal() {
        PetalSDKManager petal = PetalSDKManager.getInstance();
        if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapDataBaseService() == null) {
            LogUtils.getInstance().e(TAG, "updateGlobal petal or services is null");
            return;
        }
        petal.getOfflineMapService().getMapDataBaseService().getAllOfflineRecords(new OfflineMapsDataBaseCallback() {
            @Override
            public void onQueryAllRecords(List<OfflineMapsInfo> records) {
                if (ValidateUtil.isEmpty(records)) {
                    return;
                }
                if (petal.getOfflineMapService().getMapAreaService() == null) {
                    LogUtils.getInstance().e(TAG, "updateGlobal mapAreaService is null");
                    return;
                }
                petal.getOfflineMapService().getMapAreaService().getOfflineWorldMap(list -> {
                    if (ValidateUtil.isEmpty(records) || ValidateUtil.isEmpty(list)) {
                        return;
                    }
                    for (OfflineMapsInfo info : records) {
                        if (info == null) continue;
                        int index = list.indexOf(info);
                        if (index >= 0 && AutoOfflineDataUtil.isGlobalPackage(info) && info.getStatus() == OfflineConstants.OfflineDataStatus.FINISH) {
                            checkStartDownload(info, list.get(index), "global data check");
                            LogUtils.getInstance().i(TAG, "updateGlobal start checkStartDownload info = " + GsonUtil.toJson(info));
                        }
                    }
                });
            }
        });
    }

    public void kikaTryResumeDownload(Context context, List<OfflineMapsInfo> downloadRecords) {
        if (!notTimeException()) {
            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload timeException");
            return;
        }
        long time = System.currentTimeMillis() - lastResumeTime;
        if (time < 3 * 60 * 1000) {
            LogUtils.getInstance().i(TAG, "HwLocationAndIsaService less than 3 minutes since the last resume,  time = " + time);
            return;
        }
        LogUtils.getInstance().i(TAG, "kikaTryResumeDownload");
        Context safeCtx = getSafeContext(context);
        new Thread(() -> {
            PetalSDKManager petal = PetalSDKManager.getInstance();
            if (petal == null || petal.getOfflineMapService() == null || petal.getOfflineMapService().getMapDataBaseService() == null) {
                LogUtils.getInstance().e(TAG, "kikaTryResumeDownload petal or services is null");
                return;
            }
            petal.getOfflineMapService().getMapDataBaseService().getAllOfflineRecords(new OfflineMapsDataBaseCallback() {
                @Override
                public void onQueryAllRecords(List<OfflineMapsInfo> records) {
                    registerDownloadObserver();
                    // 本地所有数据
                    if (ValidateUtil.isEmpty(records)) {
                        LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 获取到的本地离线数据为空");
                        return;
                    }
                    if (downloadRecords != null) {
                        downloadRecords.clear();
                    }
                    for (OfflineMapsInfo info : records) {
                        if (info == null) continue;
                        if (info.getStatus() == 1 ||//WAITING
                                info.getStatus() == 2) {//ON PROGRESS
                            info.setStatus(3);//set pause
                        }
                        double originalSize = info.getOriginalSize();
                        // 状态是下载中或暂停的，继续下载
                        double naviSpace = StorageUtils.getNaviAvailableSpaceMB();
                        double downloadingSpace = StorageUtils.getDownloadingAvailableSpaceMB(safeCtx);
                        if (info.getStatus() != OfflineConstants.OfflineDataStatus.FINISH && (naviSpace <= originalSize || downloadingSpace <= originalSize)) {
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 内存空间不足，下载失败, naviSpace = " + naviSpace + " downloadingSpace = " +
                                    downloadingSpace + " originalSize = " + originalSize + " info = " + GsonUtil.toJson(info));
                            continue;
                        }
                        if (info.getStatus() == OfflineConstants.OfflineDataStatus.ERROR) {
                            // 删除未下载完的数据
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 删除未下载完的数据");
                            OfflineDataUtils.getInstance().deleteTempFile(info.getFileId(), safeCtx);
                            info.setUpdateState(OfflineConstants.OfflineDataStatus.NEED_DOWNLOAD);
                            if (petal.getOfflineMapService().getMapDataBaseService() != null) {
                                petal.getOfflineMapService().getMapDataBaseService().insert(info);
                            }
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 状态是下载中或暂停的，继续下载 first error，info = " + GsonUtil.toJson(info));
                            OfflineDataUtils.getInstance().updateToCloudInfo(info, cloudList, 5);
                            if (petal.getOfflineMapService().getMapAreaService() != null) {
                                petal.getOfflineMapService().getMapAreaService().resumeDownloadAreaData(getDownloadingInfo(info));
                            }
                        } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.ON_PROGRESS
                                || info.getStatus() == OfflineConstants.OfflineDataStatus.PAUSE
                                || info.getStatus() == OfflineConstants.OfflineDataStatus.WAITING
                                || info.getStatus() == OfflineConstants.OfflineDataStatus.NEED_DOWNLOAD) {
                            if (downloadRecords != null) {
                                downloadRecords.add(info);
                            }
                            OfflineDataUtils.getInstance().updateToCloudInfo(info, cloudList, 6);
                            if (petal.getOfflineMapService().getMapAreaService() != null) {
                                petal.getOfflineMapService().getMapAreaService().resumeDownloadAreaData(getDownloadingInfo(info));
                            }
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 状态是下载中或暂停的，继续下载，info = " + GsonUtil.toJson(info));
                        } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.DOWNLOAD_SUCCESS) {
                            // 下载成功的，解压移动文件
                            if (petal.getOfflineMapService().getMapAreaService() != null) {
                                petal.getOfflineMapService().getMapAreaService().handleSuccessFile(info);
                            }
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 下载成功的，解压移动文件，info = " + GsonUtil.toJson(info));
                        } else if (info.getStatus() == OfflineConstants.OfflineDataStatus.UNZIP_SUCCESS) {
                            // 解压成功的，移动文件
                            if (petal.getOfflineMapService().getMapAreaService() != null) {
                                petal.getOfflineMapService().getMapAreaService().moveFile(info);
                            }
                            LogUtils.getInstance().i(TAG, "kikaTryResumeDownload 解压成功的，移动文件，info = " + GsonUtil.toJson(info));
                        }
                    }
                }
            });
        }).start();
    }

    public OfflineMapsInfo getDownloadingInfo(OfflineMapsInfo info) {
        PetalSDKManager petal = PetalSDKManager.getInstance();
        OfflineMapAreaService service = petal.getOfflineMapService().getMapAreaService();
        List<OfflineMapsInfo> list = service.getDownLoadingList();
        if (service != null && list != null && !list.isEmpty()) {
            int index = list.indexOf(info);
            if (index != -1) {
                OfflineMapsInfo newInfo = list.get(index);
                newInfo.updateToClould(info);
                return newInfo;
            }
        }
        return info;
    }

    /**
     * 检查系统时间是否早于2025年9月23日，若是则返回true
     */
    public boolean notTimeException() {
        // 1. 计算目标日期（2025-09-23 00:00:00）的时间戳（毫秒）
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2025, Calendar.SEPTEMBER, 23, 0, 0, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0); // 忽略毫秒
        long targetTimestamp = targetCalendar.getTimeInMillis();
        // 2. 获取当前系统时间戳（毫秒）
        long currentTimestamp = System.currentTimeMillis();
        boolean notTimeException = currentTimestamp > targetTimestamp;
        LogUtils.getInstance().i(TAG, "notTimeException = " + notTimeException);

        // 3. 比较：当前时间戳 < 目标时间戳 → 系统时间更早
        return notTimeException;
    }

    public void updateToCloudInfo(OfflineMapsInfo info, List<OfflineMapsInfo> cloudList, int state) {
        LogUtils.getInstance().i(TAG, "updateInfo..." + state);
        if (cloudList != null && cloudList.indexOf(info) >= 0) {
            LogUtils.getInstance().i(TAG, "start update" + state);
            info.updateToClould(cloudList.get(cloudList.indexOf(info)));
            LogUtils.getInstance().i(TAG, "end update" + state);
        }
    }

}
