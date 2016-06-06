// Copyright (c) 2015 D1SM.net

package net.fs.client;

import net.fs.rudp.Route;
import net.fs.utils.JsonUtils;
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

public class ClientApplication {
    private static final String CONFIG_FILE_PATH = "client_config.json";
    private final String mSystemName = System.getProperty("os.name").toLowerCase();
    private MapClient mMapClient;
    private ClientConfig mConfig;
    private boolean mOsxFwPf = false;
    private boolean mOsxFwIpfw = false;

    public ClientApplication() {
        MLog.info("System: " + mSystemName + " " + System.getProperty("os.version"));
        loadConfig();

        boolean tcpEnvSuccess=true;
        checkFireWallOn();

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
                if (mSystemName.contains("windows")) {
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
        boolean success = true;
        if (mSystemName.contains("os x")) {
            String runFirewall = "ipfw";
            try {
                Runtime.getRuntime().exec(runFirewall, null);
                mOsxFwIpfw = true;
            } catch (IOException ignored) {}
            runFirewall = "pfctl";
            try {
                Runtime.getRuntime().exec(runFirewall, null);
                mOsxFwPf = true;
            } catch (IOException ignored) {}
            success = mOsxFwIpfw | mOsxFwPf;
        } else if (mSystemName.contains("windows")) {
            String runFirewall = "netsh advfirewall set allprofiles state on";
            try {
                Process p = Runtime.getRuntime().exec(runFirewall, null);
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
                                success = false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        exit();
                        break;
                    }
                }
                is = p.getErrorStream();
                localBufferedReader = new BufferedReader(new InputStreamReader(is));
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
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        if (!success) {
            MLog.println("启动防火墙失败.");
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

    public boolean isOsxFwPf() {
        return mOsxFwPf;
    }

    public boolean isOsxFwIpfw() {
        return mOsxFwIpfw;
    }

    public static void run() {
        new ClientApplication();
    }
}
