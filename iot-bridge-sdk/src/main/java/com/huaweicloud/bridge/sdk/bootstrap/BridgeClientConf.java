package com.huaweicloud.bridge.sdk.bootstrap;

import java.io.File;

/**
 * 网桥客户端配置
 */
public class BridgeClientConf {

    /**
     * 平台接入地址变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SERVER_IP = "NET_BRIDGE_SERVER_IP";


    /**
     * 平台接入端口变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SERVER_PORT = "NET_BRIDGE_SERVER_PORT";

    /**
     * 网桥ID环境变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_ID = "NET_BRIDGE_ID";

    /**
     * 网桥密码环境变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SECRET = "NET_BRIDGE_SECRET";


    /**
     * 连接IoT平台的地址 样例：xxxxxx.iot-mqtts.cn-north-4.myhuaweicloud.com
     *
     */
    private String serverIp;

    /**
     * 连接IoT平台的端口
     *
     */
    private String serverPort;

    /**
     * 连接IoT平台的网桥ID.
     *
     */
    private String bridgeId;

    /**
     * 连接IoT平台的网桥密码
     *
     */
    private String bridgeSecret;

    /**
     * 连接IoT平台的CA文件路径， 用于设备侧校验平台
     *
     */
    private File caFilePath;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public String getBridgeSecret() {
        return bridgeSecret;
    }

    public void setBridgeSecret(String bridgeSecret) {
        this.bridgeSecret = bridgeSecret;
    }

    public File getCaFilePath() {
        return caFilePath;
    }

    public void setCaFilePath(File caFilePath) {
        this.caFilePath = caFilePath;
    }

    public static BridgeClientConf fromEnv() {
        BridgeClientConf conf = new BridgeClientConf();
        conf.setServerIp(System.getenv(ENV_NET_BRIDGE_SERVER_IP));
        conf.setServerPort(System.getenv(ENV_NET_BRIDGE_SERVER_PORT));
        conf.setBridgeId(System.getenv(ENV_NET_BRIDGE_ID));
        conf.setBridgeSecret(System.getenv(ENV_NET_BRIDGE_SECRET));

        return conf;
    }
}
