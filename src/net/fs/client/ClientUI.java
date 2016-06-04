// Copyright (c) 2015 D1SM.net

package net.fs.client;

import net.fs.rudp.Route;
import net.fs.utils.JsonUtils;
import net.fs.utils.LogOutputStream;
import net.fs.utils.MLog;
import org.pcap4j.core.Pcaps;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ClientUI {
    private static final String CONFIG_FILE_PATH = "client_config.json";

    MapClient mMapClient;

    private ClientConfig mConfig;

    public static ClientUI ui;

    private boolean success_firewall_windows = true;

    private boolean success_firewall_osx = true;

    private String systemName = null;

    public boolean osx_fw_pf = false;

    public boolean osx_fw_ipfw = false;

    public boolean isVisible = true;

    private LogOutputStream los;

    public ClientUI() {
        setVisible(isVisible);

        if(isVisible){
             los=new LogOutputStream(System.out);
             System.setOut(los);
             System.setErr(los);
        }


        systemName = System.getProperty("os.name").toLowerCase();
        MLog.info("System: " + systemName + " " + System.getProperty("os.version"));
        ui = this;
        loadConfig();


        boolean tcpEnvSuccess=true;
        checkFireWallOn();
        if (!success_firewall_windows) {
            tcpEnvSuccess=false;
            MLog.println("启动windows防火墙失败,请先运行防火墙服务.");
        }
        if (!success_firewall_osx) {
            tcpEnvSuccess=false;
            MLog.println("启动ipfw/pfctl防火墙失败,请先安装.");
        }

        {
            boolean success = false;
            try {
                Pcaps.findAllDevs();
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!success) {
                tcpEnvSuccess=false;
                String msg = "启动失败,请先安装libpcap,否则无法使用tcp协议";
                if (systemName.contains("windows")) {
                    msg = "启动失败,请先安装winpcap,否则无法使用tcp协议";
                }
                MLog.println(msg);
            }
        }

        try {
            mMapClient = new MapClient(this,tcpEnvSuccess);
        } catch (final Exception e1) {
            e1.printStackTrace();
        }

        mMapClient.setUi(this);

        mMapClient.setMapServer(
                mConfig.getServerAddress(),
                mConfig.getServerPort(),
                0,
                null,
                null,
                mConfig.isDirectCn(),
                mConfig.getProtocol().equals("tcp"),
                null);

        Route.localDownloadSpeed = mConfig.getDownloadSpeed();
        Route.localUploadSpeed = mConfig.getUploadSpeed();
    }

    private void checkFireWallOn() {
        if (systemName.contains("os x")) {
            String runFirewall = "ipfw";
            try {
                Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_ipfw = true;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            runFirewall = "pfctl";
            try {
                Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_pf = true;
            } catch (IOException e) {
               // e.printStackTrace();
            }
            success_firewall_osx = osx_fw_ipfw | osx_fw_pf;
        } else if (systemName.contains("linux")) {
            String runFirewall = "service iptables start";
        } else if (systemName.contains("windows")) {
            String runFirewall = "netsh advfirewall set allprofiles state on";
            Thread standReadThread = null;
            Thread errorReadThread = null;
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                standReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getInputStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    if (line.contains("Windows")) {
                                        success_firewall_windows = false;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                exit();
                                break;
                            }
                        }
                    }
                };
                standReadThread.start();

                errorReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getErrorStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    System.out.println("error" + line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                exit();
                                break;
                            }
                        }
                    }
                };
                errorReadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                success_firewall_windows = false;
            }
            if (standReadThread != null) {
                try {
                    standReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (errorReadThread != null) {
                try {
                    errorReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void exit() {
        System.exit(0);
    }

    private void loadConfig() {
        mConfig = new ClientConfig();
        if (!new File(CONFIG_FILE_PATH).exists()) {
            try {
                saveFile(JsonUtils.clientConfigToJson(mConfig).getBytes("UTF-8"), CONFIG_FILE_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            String content = readFileUtf8(CONFIG_FILE_PATH);
            mConfig = JsonUtils.jsonToClientConfig(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFileUtf8(String path) throws Exception {
        String str = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            File file = new File(path);

            int length = (int) file.length();
            byte[] data = new byte[length];

            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            dis.readFully(data);
            str = new String(data, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return str;
    }

    private void saveFile(byte[] data, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    public boolean login() {
        return false;
    }

    public boolean updateNode(boolean testSpeed) {
        return true;
    }

    public boolean isOsx_fw_pf() {
        return osx_fw_pf;
    }

    public void setOsx_fw_pf(boolean osx_fw_pf) {
        this.osx_fw_pf = osx_fw_pf;
    }

    public boolean isOsx_fw_ipfw() {
        return osx_fw_ipfw;
    }

    public void setOsx_fw_ipfw(boolean osx_fw_ipfw) {
        this.osx_fw_ipfw = osx_fw_ipfw;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}
