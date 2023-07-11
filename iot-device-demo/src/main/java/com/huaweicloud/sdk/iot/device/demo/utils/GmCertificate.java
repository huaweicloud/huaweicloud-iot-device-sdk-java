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

package com.huaweicloud.sdk.iot.device.demo.utils;

public final class GmCertificate {
    /*
     * 证书别名
     */
    private String certAlias;

    /*
     * 证书文件
     */
    private String certFile;

    /*
     * 私钥别名
     */
    private String keyAlias;

    /*
     * 私钥文件
     */
    private String keyFile;

    /*
     * 密码
     */
    private String password;

    public GmCertificate(String certAlias, String certFile, String keyAlias, String keyFile, String password) {
        this.certAlias = certAlias;
        this.certFile = certFile;
        this.keyAlias = keyAlias;
        this.keyFile = keyFile;
        setPassword(password);
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkValid() {
        if (certAlias == null || certFile == null || keyAlias == null || keyFile == null) {
            return false;
        }
        return true;
    }
}
