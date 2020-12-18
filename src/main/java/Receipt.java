import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Receipt{
    private final IExtractable extractor;
    private  IParamable params;
    public ReceiptData receiptData;

    // дата добавления чека в бд
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date currentDate = new Date();
    private String addition_date = dateFormat.format(currentDate);

    public String receiptId;


    public Receipt(IExtractable ex, IParamable params) throws IOException, InterruptedException {
        this.extractor = ex;
        this.params = params;
        this.receiptData = extractor.getDetails(params.getParams());
    }

    private void setReceiptId(String receiptId){
        this.receiptId = receiptId;
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

    public void receiptToDatabase(String chatId)
    {
        // чек на сумму:
        double sum = this.receiptData.data.get("totalSum").asDouble()/100;
        // дата покупки
        String ticketDate = this.receiptData.data.get("dateTime").asText();

        Firestore db = FirestoreDB.getInstance().db;
        DocumentReference receipt = db.collection("users").document(chatId).collection("receipts").document();
        Map<String, Object> receiptData = new HashMap<>();
        receiptData.put("added", this.addition_date);
        receiptData.put("ticket date", ticketDate
                .replaceAll("([-])", "/").replaceAll("([T])", " "));
        receiptData.put("sum", sum);
        receiptData.put("QR-code", "QR-code");
        // удален чек из базы или нет
        receiptData.put("deleted", false);
        // погашен чек или нет
        receiptData.put("canceled", false);
        receipt.set(receiptData);

        CollectionReference goods = receipt.collection("goods");
        List<JsonNode> jsonNodesGoods = this.receiptData.getJsonNodeItems();
        for (JsonNode jsonNodesGood : jsonNodesGoods)
        {
            DocumentReference good = goods.document(jsonNodesGood.get("name").asText().
                    replaceFirst("([0-9:*]+)", ""));
            Map<String, Object> goodData = new HashMap<>();
            goodData.put("price", jsonNodesGood.get("price").asDouble() / 100);
            goodData.put("quantity", jsonNodesGood.get("quantity").asDouble());
            goodData.put("owner", "owner");
            good.set(goodData);
        }
        this.setReceiptId(receipt.getId());
    }

    public void addParticipants(String chatId, String[] debtors_usernames){
        ArrayList<String> debtors = getDebtors(debtors_usernames);
        Firestore db = FirestoreDB.getInstance().db;
        DocumentReference participants = db.collection("users").document(chatId).collection("receipts")
                .document(this.receiptId);
        Map<String, Object> updateReceiptData = new HashMap<>();
        updateReceiptData.put("participants", debtors);
        participants.set(updateReceiptData, SetOptions.merge());
    }

    public ArrayList<String> getDebtors(String[] debtors_usernames){
        Firestore db = FirestoreDB.getInstance().db;
        ArrayList<String> debtors = new ArrayList<String>();
        QuerySnapshot querySnapshotUsers = null;
        try {
            querySnapshotUsers = db.collection("users").get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert querySnapshotUsers != null;
        List<QueryDocumentSnapshot> documents = querySnapshotUsers.getDocuments();
        for (String debtor_username : debtors_usernames) {
            for (QueryDocumentSnapshot document : documents)
            {
                if (Objects.equals(document.getString("username"), debtor_username))
                {
                    debtors.add(document.getId());
                }
            }
        }
        System.out.println("debtors: " + debtors);
        return debtors;
    }

    public double divideSum(String user, int usersQuantity)
    {
        Firestore db = FirestoreDB.getInstance().db;
        DocumentSnapshot documentSnapshot = null;
        try {
            documentSnapshot = db.collection("users").document(user).collection("receipts")
                    .document(this.receiptId).get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert documentSnapshot != null;
        double debt = documentSnapshot.getDouble("sum")/(usersQuantity+1);
        System.out.println(debt);
        return debt;
    }

    public void deleteReceipt(Object addDate, DocumentReference documentReference){
        // ...
        // ...
        }
















}