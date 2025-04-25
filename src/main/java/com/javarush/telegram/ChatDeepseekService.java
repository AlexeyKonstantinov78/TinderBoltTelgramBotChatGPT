package com.javarush.telegram;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Properties;

@Slf4j
public class ChatDeepseekService {
    private static final Logger log = LoggerFactory.getLogger(ChatDeepseekService.class);
    String url = "https://api.deepseek.com/v1/chat/completions";
    String urlOpenrouter = "https://openrouter.ai/api/v1/chat/completions";
    String model = "deepseek-chat";
    String modelOpenrouter = "deepseek/deepseek-r1";
    HttpURLConnection connection;
    public static String DEEPSEEK_BOT_TOKEN;
    String fullResponse = "";

    static {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input); // Загружаем файл

            // Получаем значения
            DEEPSEEK_BOT_TOKEN = props.getProperty("deepseek.ai.token");

        } catch (IOException e) {
            log.error("Ошибка загрузки config.properties: " + e.getMessage());
            //System.err.println("Ошибка загрузки config.properties: " + e.getMessage());
        }
    }

    public ChatDeepseekService() throws IOException {
        String authToken = "Bearer " + DEEPSEEK_BOT_TOKEN;
        URL urlopen = new URL(url);
        connection = (HttpURLConnection) urlopen.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
    }

    public void sendMessageDeepseek(String message) throws IOException {
        try {
            try (OutputStream os = connection.getOutputStream()){
                os.write(getJSONObject(message).toString().getBytes());
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line = br.readLine();
                System.out.println(line);
                JSONObject resJson = new JSONObject(line);
                fullResponse = resJson.toString();
            }
            System.out.println(fullResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(fullResponse);
        }

    }

    public JSONObject getJSONObject(String mess) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", model);
        requestJson.put("stream", false);
        JSONArray messagesArray = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", mess);
        messagesArray.put(userMessage);
        requestJson.put("messages", messagesArray);
        return requestJson;
    }

    public static void main(String[] args) {
        try {
            ChatDeepseekService deepseekService = new ChatDeepseekService();
            deepseekService.sendMessageDeepseek("Кто ты?");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
