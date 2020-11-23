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
        QRParamsReader qrParamsReader = new QRParamsReader(linkPhoto);
        HashMap<String, String> params = qrParamsReader.getParams(); // это хэшМапа содержащая key-value параметры из QR кода

        IExtractable apiExtractor = new DetailsAPIExtractor();
        Receipt receipt = new Receipt(apiExtractor);
        ReceiptData receiptData = receipt.getData(params);

        this.modelBot.setBufferAnswer(receipt.createReceiptForUser(receiptData));

        this.getProducts();
        this.calculateCost();
        this.updateStatistic();
        this.modelBot.setCurrentStateUser(State.NOTIFY_MADE_RECEIPT);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    private void getProducts() {
    }

    private void calculateCost() {
    }

    private void updateStatistic() {
    }
}
