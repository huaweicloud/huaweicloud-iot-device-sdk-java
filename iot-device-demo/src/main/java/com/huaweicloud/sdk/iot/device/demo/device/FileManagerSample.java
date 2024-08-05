/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.demo.device;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.demo.device.connect.DefaultX509TrustManager;
import com.huaweicloud.sdk.iot.device.filemanager.FileManagerService;
import com.huaweicloud.sdk.iot.device.filemanager.FileMangerListener;
import com.huaweicloud.sdk.iot.device.filemanager.request.OpFileStatusRequest;
import com.huaweicloud.sdk.iot.device.filemanager.request.UrlRequest;
import com.huaweicloud.sdk.iot.device.filemanager.response.UrlResponse;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 文件上传下载样例
 */
public class FileManagerSample {
    private static final Logger log = LogManager.getLogger(FileManagerSample.class);

    public static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    // Upload files to OBS
    private static final String UP_LOAD_FILE_NAME = "upload.jpg";

    // Download the file stored in OBS
    private static final String DOWNLOAD_FILE_NAME = "download.jpg";

    public static void main(String[] args) throws IOException {
        // 用户请替换为自己的接入地址。
        String serverUri = "ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String password = "password";

        // Load the CA certificate of the IoT platform and perform server verification
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = FileManagerSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // Create device
        IoTDevice device = new IoTDevice(serverUri, deviceId, password, tmpCAFile);
        if (device.init() != 0) {
            return;
        }

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .sslSocketFactory(createSSLSocketFactory(), new DefaultX509TrustManager())
            .hostnameVerifier((s, sslSession) -> {
                return true;
            })
            .build();

        final FileManagerService fileManagerService = device.getFileManagerService();

        // 监听平台下发的文件上传/下载临时URL事件
        fileManagerService.setFileMangerListener(new FileMangerListener() {
            @Override
            public void onUploadUrl(UrlResponse param) {
                // 上传文件到OBS
                upLoadFile(param, okHttpClient, fileManagerService);
            }

            @Override
            public void onDownloadUrl(UrlResponse param) {
                // 从OBS下载文件
                downLoadFile(param, okHttpClient, fileManagerService);
            }
        });

        // 设备上报获取文件上传URL请求
        getUploadUrl(fileManagerService);

        // 设备上报获取文件下载URL请求
        getDownloadUrl(fileManagerService);
    }

    private static void getDownloadUrl(FileManagerService fileManagerService) {
        Map<String, Object> downLoadFileAttributes = new HashMap<>();
        UrlRequest urlRequest = new UrlRequest();
        urlRequest.setFileName(DOWNLOAD_FILE_NAME);
        urlRequest.setFileAttributes(downLoadFileAttributes);
        fileManagerService.getDownloadUrl(urlRequest, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.debug("Succeed to getDownloadUrl");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("Failed to getDownloadUrl, error={}", ExceptionUtil.getBriefStackTrace(var2));
            }
        });
    }

    private static void getUploadUrl(FileManagerService fileManagerService) {
        Map<String, Object> uploadFileAttributes = new HashMap<>();
        UrlRequest gettingUpLoadUrlDTO = new UrlRequest();
        gettingUpLoadUrlDTO.setFileName(UP_LOAD_FILE_NAME);
        gettingUpLoadUrlDTO.setFileAttributes(uploadFileAttributes);
        fileManagerService.getUploadUrl(gettingUpLoadUrlDTO, new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.debug("Succeed to getUploadUrl");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.warn("Failed to getUploadUrl, error={}", ExceptionUtil.getBriefStackTrace(var2));
                }
            }
        );
    }

    private static void downLoadFile(UrlResponse param, OkHttpClient okHttpClient,
        FileManagerService fileManagerService) {
        if (!isUrlResponseValid(param)) {
            log.warn("param is inValid");
            return;
        }
        String url = param.getUrl();
        Request request = new Request.Builder()
            .url(url)
            .header("Content-Type", "text/plain")
            .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.warn("Failed to upload file to OBS, error={}",
                    ExceptionUtil.getBriefStackTrace(e));
                OpFileStatusRequest downLoadFileStatusRequest = new OpFileStatusRequest();
                downLoadFileStatusRequest.setObjectName(param.getObjectName());
                downLoadFileStatusRequest.setStatusCode(1);
                reportDownLoadFileStatus(fileManagerService, downLoadFileStatusRequest);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                OpFileStatusRequest downLoadFileStatusRequest = new OpFileStatusRequest();
                downLoadFileStatusRequest.setObjectName(param.getObjectName());
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body();
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(DOWNLOAD_FILE_NAME))) {
                        byte[] bytes = new byte[1024 * 10];
                        int len;
                        while ((len = bufferedInputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, len);
                            fileOutputStream.flush();
                        }
                        log.info("Succeed to download file from OBS");
                        downLoadFileStatusRequest.setResultCode(0);
                    } catch (Exception e) {
                        log.warn("Failed to download file from OBS,e={}", ExceptionUtil.getBriefStackTrace(e));
                        downLoadFileStatusRequest.setResultCode(1);
                    }
                } else {
                    log.warn("Failed to download file from OBS, code={}, message={}", response.code(),
                        response.message());
                    downLoadFileStatusRequest.setResultCode(1);
                    downLoadFileStatusRequest.setStatusCode(response.code());
                    downLoadFileStatusRequest.setStatusDescription(response.message());
                }
                reportUploadFileStatus(fileManagerService, downLoadFileStatusRequest);
            }
        });
    }

    private static void upLoadFile(UrlResponse param, OkHttpClient okHttpClient,
        FileManagerService fileManagerService) {
        if (!isUrlResponseValid(param)) {
            log.warn("param is inValid");
            return;
        }
        String url = param.getUrl();
        File uploadFile = new File(UP_LOAD_FILE_NAME);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),
            uploadFile);
        Request request = new Request.Builder()
            .url(url)
            .put(requestBody)
            .header("Content-Type", "text/plain")
            .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.warn("Failed to upload file to OBS, error={}",
                    ExceptionUtil.getBriefStackTrace(e));
                OpFileStatusRequest uploadFileStatusRequest = new OpFileStatusRequest();
                uploadFileStatusRequest.setObjectName(param.getObjectName());
                uploadFileStatusRequest.setResultCode(1);
                reportUploadFileStatus(fileManagerService, uploadFileStatusRequest);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                OpFileStatusRequest uploadFileStatusRequest = new OpFileStatusRequest();
                uploadFileStatusRequest.setObjectName(param.getObjectName());
                if (response.isSuccessful()) {
                    log.info("Succeed to upload file to OBS");
                    uploadFileStatusRequest.setResultCode(0);
                } else {
                    log.warn("Failed to upload file to OBS, code={}, message={}", response.code(), response.message());
                    uploadFileStatusRequest.setResultCode(1);
                    uploadFileStatusRequest.setStatusCode(response.code());
                    uploadFileStatusRequest.setStatusDescription(response.message());
                }
                reportUploadFileStatus(fileManagerService, uploadFileStatusRequest);
            }
        });
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            DefaultX509TrustManager defaultX509TrustManager = new DefaultX509TrustManager();
            sc.init(null, new TrustManager[] {defaultX509TrustManager}, SecureRandom.getInstanceStrong());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("create SSL Socket Factory error" + e.getMessage());
        }

        return ssfFactory;
    }

    private static boolean isUrlResponseValid(UrlResponse urlResponse) {
        if (urlResponse == null) {
            return false;
        }

        if (urlResponse.getUrl() == null || urlResponse.getUrl().isEmpty()) {
            return false;
        }

        return true;
    }

    private static void reportUploadFileStatus(FileManagerService fileManagerService,
        OpFileStatusRequest uploadFileStatusRequest) {
        fileManagerService.reportUploadFileStatus(uploadFileStatusRequest, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.debug("Succeed to reportUploadFileStatus");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("Failed to reportUploadFileStatus, error={}",
                    ExceptionUtil.getBriefStackTrace(var2));
            }
        });
    }

    private static void reportDownLoadFileStatus(FileManagerService fileManagerService,
        OpFileStatusRequest uploadFileStatusRequest) {
        fileManagerService.reportDownloadFileStatus(uploadFileStatusRequest, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.debug("Succeed to reportDownLoadFileStatus");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("Failed to reportDownLoadFileStatus, error={}",
                    ExceptionUtil.getBriefStackTrace(var2));
            }
        });
    }
}
