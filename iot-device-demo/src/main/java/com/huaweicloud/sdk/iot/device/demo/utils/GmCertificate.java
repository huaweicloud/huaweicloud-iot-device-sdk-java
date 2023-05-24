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
