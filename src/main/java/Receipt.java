import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Receipt{
    private final IExtractable extractor;
    private IParamable params;
    public ReceiptData receiptData;


    public Receipt(IExtractable ex, IParamable params) throws IOException, InterruptedException {
        this.extractor = ex;
        this.params = params;
        this.receiptData = extractor.getDetails(params.getParams());
    }

    public String createReceiptForUser() {
        List<JsonNode> jsonNodesItems = this.receiptData.getJsonNodeItems();
        StringBuilder s = new StringBuilder();
        int j = 1;
        for (JsonNode jsonNodesItem : jsonNodesItems) {
            s.append(jsonNodesItem.get("name").asText().replaceFirst("([0-9:*]+)", j+". "))
                    .append(".\n")
                    .append("Цена: ")
                    .append(jsonNodesItem.get("price").asDouble() / 100)
                    .append("р * ")
                    .append(jsonNodesItem.get("quantity").asDouble())
                    .append(" = ")
                    .append(jsonNodesItem.get("sum").asDouble() / 100)
                    .append("р")
                    .append("\n");
            j++;
        }
        s.append("------------------------------------\n").append("Итог: ")
                .append(this.receiptData.data.get("totalSum").asDouble()/100).append(" рублей");
        return s.toString();
    }

    public ArrayList<String[]> giveElementsReceipt()
    {
        List<JsonNode> jsonNodesItems = this.receiptData.getJsonNodeItems();
        ArrayList<String[]> arrayList = new ArrayList<>();
        int j = 1;
        for (JsonNode jsonNodesItem : jsonNodesItems) {
            String[] s = new String[4];
            s[0] = jsonNodesItem.get("name").asText().replaceFirst("([0-9:*]+)", j+". ");
            s[1] = String.valueOf((jsonNodesItem.get("price").asDouble() / 100));
            s[2] = jsonNodesItem.get("quantity").asText();
            s[3] = String.valueOf((jsonNodesItem.get("sum").asDouble() / 100));
            arrayList.add(s);
            j++;
        }
        return arrayList;
    }

    public void receiptToDatabase(String chatId)
    {
        System.out.print("На базу !!!!\n");

        // чек на сумму:
        double sum = this.receiptData.data.get("totalSum").asDouble()/100;
        // дата добавления чека в бд
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date currentDate = new Date();
        // дата покупки
        String ticketDate = this.receiptData.data.get("dateTime").asText();

        Firestore db = FirestoreDB.getInstance().db;
        DocumentReference receipt = db.collection("users").document(chatId).collection("receipts").document();
        Map<String, Object> receiptData = new HashMap<>();
        receiptData.put("added", dateFormat.format(currentDate));
        receiptData.put("ticket data", ticketDate
                .replaceAll("([-])", "/").replaceAll("([T])", " "));
        receiptData.put("sum", sum);
        receiptData.put("QR-code", "QR-code");
        receiptData.put("deleted", false);
        receipt.set(receiptData);


        CollectionReference goods = receipt.collection("goods");
        List<JsonNode> jsonNodesGoods = this.receiptData.getJsonNodeItems();
        for (JsonNode jsonNodesGood : jsonNodesGoods)
        {
            DocumentReference good = goods.document(jsonNodesGood.get("name").asText().replaceFirst("([0-9:*]+)", ""));
            Map<String, Object> goodData = new HashMap<>();
            goodData.put("price", jsonNodesGood.get("price").asDouble() / 100);
            goodData.put("quantity", jsonNodesGood.get("quantity").asDouble());
            goodData.put("owner", "owner");
            good.set(goodData);
        }
    }

    public void deleteReceipt(){
        // ...
        // ...

    }

    public Double divideBetweenUsers(){
        // ...
        // ...
        return null;
    }
}