package bot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

public class ReinvyBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().equals("/menu")) {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setText("Nampilin Menu");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                System.out.println(update.getMessage().getText());
                try {
                    String result = chatOpenAI(update.getMessage().getText());
                    if (result != null) {
                        message.setText(result);
                    } else {
                        message.setText("Gagal menjawab pertanyaan anda XD, coba lagi ya");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public String getBotUsername() {
        return "BOT_USERNAME";
    }

    @Override
    public String getBotToken() {
        return "BOT_TOKEN";
    }

    public String chatOpenAI(String prompt) throws Exception {
        URL apiUrl = new URL("https://api.openai.com/v1/completions");
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization",
                "Bearer " + "OPEN_AI_TOKEN");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.createObjectNode()
                .put("model", "text-davinci-003")
                .put("prompt", prompt)
                .put("max_tokens", 200)
                .put("temperature", 0)
                .put("top_p", 1)
                .put("n", 1)
                .put("stream", false)
                .put("logprobs", (String) null);
        wr.writeBytes(jsonData.toString());
        wr.flush();
        wr.close();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            String text = (String) ((JSONObject) ((JSONArray) json.get("choices")).get(0)).get("text");

            System.out.println(response.toString());
            return text;
        } else {
            System.out.println("Error: " + connection);
            return null;
        }
    }

}
