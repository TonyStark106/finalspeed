package net.fs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fs.client.ClientConfig;
import net.fs.client.MapRule;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static JsonElement parseJson(String jsonStr) {
        JsonParser parser = new JsonParser();
        return parser.parse(jsonStr);
    }

    private static String jsonToString(JsonElement json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(json);
    }

    public static ClientConfig jsonToClientConfig(String jsonString) {
        try {
            JsonObject json = parseJson(jsonString).getAsJsonObject();
            ClientConfig config = new ClientConfig();
            config.setServerAddress(json.get(Names.ClientConfig.SERVER_ADDRESS).getAsString());
            config.setServerPort(json.get(Names.ClientConfig.SERVER_PORT).getAsInt());
            if (json.has(Names.ClientConfig.DIRECT_CN)) {
                config.setDirect_cn(json.get(Names.ClientConfig.DIRECT_CN).getAsBoolean());
            }
            config.setDownloadSpeed(json.get(Names.ClientConfig.DOWNLOAD_SPEED).getAsInt());
            config.setUploadSpeed(json.get(Names.ClientConfig.UPLOAD_SPEED).getAsInt());
            if (json.has(Names.ClientConfig.SOCKS5_PORT)) {
                config.setSocks5Port(json.get(Names.ClientConfig.SOCKS5_PORT).getAsInt());
            }
            if (json.has(Names.ClientConfig.PROTOCOL)) {
                config.setProtocol(json.get(Names.ClientConfig.PROTOCOL).getAsString());
            }
            return config;
        } catch (Exception e) {
            System.out.println("Error while loading config file, using defaults.");
            return new ClientConfig();
        }
    }

    public static String clientConfigToJson(ClientConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty(Names.ClientConfig.SERVER_ADDRESS, config.getServerAddress());
        json.addProperty(Names.ClientConfig.SERVER_PORT, config.getServerPort());
        json.addProperty(Names.ClientConfig.DIRECT_CN, config.isDirect_cn());
        json.addProperty(Names.ClientConfig.DOWNLOAD_SPEED, config.getDownloadSpeed());
        json.addProperty(Names.ClientConfig.UPLOAD_SPEED, config.getUploadSpeed());
        json.addProperty(Names.ClientConfig.SOCKS5_PORT, config.getSocks5Port());
        json.addProperty(Names.ClientConfig.PROTOCOL, config.getProtocol());
        return jsonToString(json);
    }

    public static List<MapRule> jsonToMapRuleList(String jsonString) {
        List<MapRule> result = new ArrayList<>();
        try {
            JsonObject json = parseJson(jsonString).getAsJsonObject();
            JsonArray jsonArray = json.getAsJsonArray(Names.MapRuleList.MAP_LIST);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                MapRule rule = new MapRule();
                rule.setName(jsonObject.get(Names.MapRule.NAME).getAsString());
                rule.setListen_port(jsonObject.get(Names.MapRule.LISTEN_PORT).getAsInt());
                rule.setDst_port(jsonObject.get(Names.MapRule.DST_PORT).getAsInt());
                result.add(rule);
            }
        } catch (Exception e) {
            System.out.println("Error while loading map rules.");
        }
        return result;
    }

    public static String mapRuleListToJson(List<MapRule> ruleList) {
        JsonArray jsonArray = new JsonArray();
        for (MapRule rule : ruleList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Names.MapRule.NAME, rule.getName());
            jsonObject.addProperty(Names.MapRule.LISTEN_PORT, rule.getListen_port());
            jsonObject.addProperty(Names.MapRule.DST_PORT, rule.getDst_port());
            jsonArray.add(jsonObject);
        }
        JsonObject json = new JsonObject();
        json.add(Names.MapRuleList.MAP_LIST, jsonArray);
        return jsonToString(json);
    }

    private static class Names {
        private static class ClientConfig {
            private static final String SERVER_ADDRESS = "server_address";
            private static final String SERVER_PORT = "server_port";
            private static final String DIRECT_CN = "direct_cn";
            private static final String DOWNLOAD_SPEED = "download_speed";
            private static final String UPLOAD_SPEED = "upload_speed";
            private static final String SOCKS5_PORT = "socks5_port";
            private static final String PROTOCOL = "protocol";
        }

        private static class MapRuleList {
            private static final String MAP_LIST = "map_list";
        }

        private static class MapRule {
            private static final String NAME = "name";
            private static final String LISTEN_PORT = "listen_port";
            private static final String DST_PORT = "dst_port";
        }
    }
}
