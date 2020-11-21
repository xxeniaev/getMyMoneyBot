import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Commands {
    public ModelBot modelBot;

    public Commands(ModelBot modelBot) {this.modelBot = modelBot;}
    /*проверяем, есть ли он базе*/
    /*если есть, то просто передаем управление состоянию
     * Choose_command, иначе, создаем запись в БД*/
    public void signUp() {
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void waitPhoto() {
        this.modelBot.setCurrentStateUser(State.WAIT_PHOTO);
    }

    public void viewReceipts() {
        // ...
        // ...
        this.modelBot.setBufferAnswer("Вот твои чеки, пользуйся: ");
        this.modelBot.setCurrentStateUser(State.VIEW_RECEIPTS);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void viewStatistic() {
        // ...
        // ...
        this.modelBot.setBufferAnswer("надо придумать, какую статистику ведём)");
        this.modelBot.setCurrentStateUser(State.VIEW_STATISTIC);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void addReceipt(String linkPhoto) throws IOException, InterruptedException {
        this.modelBot.setCurrentStateUser(State.MAKE_RECEIPT);
        HashMap<String, String> params = this.getQRCode(linkPhoto); // это хэшМапа содержащая key-value параметры из QR кода

        // создает экземпляр http клиента, отправляет запрос и получает json
        System.out.print(HttpClientPost.sendPost(params)); // это json

        String jsonStr = HttpClientPost.sendPost(params); // создает json строку
        Receipt receipt = Json.parseReceipt(jsonStr); // это создаёт класс чека и засовывает data и code туда
        JsonNode data = receipt.data.get("json");

        // создает массив json'ов, содержащих информацию о товаре
        List<JsonNode> jsonNodesItems = StreamSupport
                .stream(data.get("items").spliterator(), false)
                .collect(Collectors.toList());

        // сколько товаров в чеке
        int n = jsonNodesItems.size();

        // создаёт строку с красивым чеком для пользователя
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < n; i++){
            s.append(jsonNodesItems.get(i).get("name").asText())
                    .append(": ")
                    .append(jsonNodesItems.get(i).get("price")
                    .asDouble()/100)
                    .append("\n")
                    .append("Итог: ")
                    .append(data.get("totalSum").asDouble()/100);
        }
        this.modelBot.setBufferAnswer(s.toString());

//          печатает каждый item отдельно
//        if (items.isArray()) {
//            for ( JsonNode objNode : items) {
//                System.out.println(objNode);
//            }
//        }

        this.getProducts();
        this.calculateCost();
        this.updateStatistic();
        this.modelBot.setCurrentStateUser(State.NOTIFY_MADE_RECEIPT);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    private HashMap<String, String> getQRCode(String linkPhoto) throws FileNotFoundException, UnsupportedEncodingException {
        String decodeText;
        BarCodeDecode dec = new BarCodeDecode();
        URL url = null;
        try {
            url = new URL(linkPhoto);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        decodeText = dec.getQRString(url);
        if (decodeText == null) {
            throw new FileNotFoundException("Не смогли получить QR");
        }
        return splitQuery(decodeText);
    }

    public static HashMap<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        // сплитит query полученный из QR кода и получает на выходе HashMap
        HashMap<String, String> query_pairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.toString()), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.toString()));
        }
        return query_pairs;
    }

    private void getProducts() {
    }

    private void calculateCost() {
    }

    private void updateStatistic() {
    }
}
