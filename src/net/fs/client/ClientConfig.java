// Copyright (c) 2015 D1SM.net

package net.fs.client;

public class ClientConfig {
    private String mServerAddress = "";
    private int mServerPort;
    private int mDownloadSpeed;
    private int mUploadSpeed;
    private boolean mDirectCn = true;
    private int mSocks5Port = 1083;
    private String mProtocol = "tcp";

    public String getServerAddress() {
        return mServerAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.mServerAddress = serverAddress;
    }

    public int getServerPort() {
        return mServerPort;
    }

    public void setServerPort(int serverPort) {
        this.mServerPort = serverPort;
    }

    public boolean isDirectCn() {
        return mDirectCn;
    }

    public void setDirectCn(boolean direct_cn) {
        this.mDirectCn = direct_cn;
    }

    public int getDownloadSpeed() {
        return mDownloadSpeed;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.mDownloadSpeed = downloadSpeed;
    }

    public int getUploadSpeed() {
        return mUploadSpeed;
    }

    public void setUploadSpeed(int uploadSpeed) {
        this.mUploadSpeed = uploadSpeed;
    }

    public int getSocks5Port() {
        return mSocks5Port;
    }

    public void setSocks5Port(int socks5Port) {
        this.mSocks5Port = socks5Port;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public void setProtocol(String protocol) {
        this.mProtocol = protocol;
    }
}
