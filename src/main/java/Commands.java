import com.google.api.core.ApiFuture;
import com.google.cloud.Tuple;
import com.google.cloud.firestore.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands {
    public ModelBot modelBot;

    public Commands(ModelBot modelBot) {this.modelBot = modelBot;}
    public static void signUp(ModelBot modelBot, DataCommand dataCommand)
    {
        /* тута я получаю ник и id для последующего записывания в базу,
        * если это необходимо */
        String username = dataCommand.getUsername();
        String chatId = dataCommand.getChatID().toString();

        Firestore db = FirestoreDB.getInstance().db;

        DocumentReference docRef = db.collection("users").document(chatId);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("chatID", chatId);
        docRef.set(data);
        //asynchronously write data
        // ApiFuture<WriteResult> result = docRef.set(data);
    }

    public static void viewReceipts(ModelBot modelBot, DataCommand dataCommand) {
        Firestore db = FirestoreDB.getInstance().db;
        CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                .collection("receipts");

        List<QueryDocumentSnapshot> receiptsDocuments = getReceiptsDocuments(receipts);
        // составлять сообщение с юзера
        // сделать, чтобы чеки выводились сортированно по  датам, а не рандомно
        StringBuilder s = new StringBuilder();
        s.append("Вот твои чеки, пользуйся :)\n\n");
        int i = 1;
        for (QueryDocumentSnapshot receiptDocument: receiptsDocuments){
            s.append("\u25aa\ufe0fЧек ").append("/").append(i).append(" от ");
            String date = receiptDocument.getString("added").split(" ")[0];
            s.append(date).append("\n");
            i++;
        }
        modelBot.setBufferAnswer(s.toString());

    }

    private static List<QueryDocumentSnapshot> getReceiptsDocuments(CollectionReference receipts)
    {
        QuerySnapshot future = null;
        try {
            future = receipts.get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return future.getDocuments();
    }

    public static void viewSpecificReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        StringBuilder text = new StringBuilder();
        StringBuilder s = new StringBuilder(dataCommand.getTextMessage()).deleteCharAt(0);
        int position = Integer.parseInt(s.toString()) - 1;
        Firestore db = FirestoreDB.getInstance().db;
        CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                .collection("receipts");
        QueryDocumentSnapshot receiptDocument = getReceiptsDocuments(receipts).get(position);
        CollectionReference goods = receipts.document(receiptDocument.getId()).collection("goods");

        List<QueryDocumentSnapshot> goodsDocuments = getReceiptsDocuments(goods);
        text.append("Чек №").append(position + 1).append("\n");
        String date = receiptDocument.getString("added").split(" ")[0];
        text.append("Дата добавления чека: ").append(date).append("\n").append("\n");
        int i = 1;
        for (QueryDocumentSnapshot good: goodsDocuments
             ) {
            text.append(i).append(". ").append(good.getId()).append(".\n").append(good.getDouble("price")*good.getDouble("quantity")).append(" р").append("\n");
            i++;
        }
        modelBot.setBufferAnswer(text.toString());

    }

    public static void viewStatistic(ModelBot modelBot, DataCommand dataCommand) {
        // ...
        // ...
    }

    public static void addReceipt(ModelBot modelBot, DataCommand dataCommand) {
        String linkPhoto = dataCommand.getTextMessage();
        System.out.println(linkPhoto);
        QRParamsReader qrParamsReader;
        try {
            qrParamsReader = new QRParamsReader(linkPhoto);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            modelBot.setUsersFault(dataCommand.getChatID(), Boolean.TRUE);
            System.out.println("Fail");
            return;
        }
        IExtractable apiExtractor = new DetailsAPIExtractor();
        try {
            Receipt receipt = new Receipt(apiExtractor, qrParamsReader);
            modelBot.setUsersReceipt(dataCommand.getChatID(), receipt);
            modelBot.setBufferAnswer(receipt.createReceiptForUser());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            modelBot.setUsersFault(dataCommand.getChatID(), Boolean.TRUE);
        }
    }

    public static void addDataBase(ModelBot modelBot, DataCommand dataCommand)
    {
        if (!dataCommand.getTextMessage().equals("Да")) {
            modelBot.setCurrentStateUser(dataCommand.getChatID(), State.FAIL_CHECK_RECEIPT);
        }
        else {
            System.out.print(dataCommand.getChatID().toString()+"\n");
            Receipt receipt = modelBot.getUserReceipt(dataCommand.getChatID());
            receipt.receiptToDatabase(dataCommand.getChatID().toString());
        }

        // а тута всё, можно метод для базы данных
        /* modelBot.getUserReceipt(dataCommand.getChatID()); - обращение
        к словарю, который содержит чек.
        написал метод, чтобы чек можно было представить в виде листа из массивов string,
        где индексы
        0 - имя продукта
        1 - цена за один
        2 - количество
        3 - сумма*/
    }

    private static QueryDocumentSnapshot getReceiptElement(DataCommand dataCommand)
    {
        Pattern pattern = Pattern.compile("Чек №(\\d+?)\\n");
        Matcher matcher = pattern.matcher(dataCommand.getTextMessage());
        if (matcher.find()) {
            int position = Integer.parseInt(matcher.group(1)) - 1;
            Firestore db = FirestoreDB.getInstance().db;
            CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                    .collection("receipts");
            QueryDocumentSnapshot receiptDocument = getReceiptsDocuments(receipts).get(position);
            return receiptDocument;
        }
        return null;
    }

    public static void areThereFriends(ModelBot modelBot, DataCommand dataCommand)
    {
        if (dataCommand.getTextMessage().equals("Нет")) {
            modelBot.setCurrentStateUser(dataCommand.getChatID(), State.NO_CHECK_SHARE);
        }
        else if (!dataCommand.getTextMessage().equals("Да"))
        {
            modelBot.setUsersFault(dataCommand.getChatID(), Boolean.TRUE);
        }
    }

    public static void quenchReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        QueryDocumentSnapshot queryDocumentSnapshot = getReceiptElement(dataCommand);
    }

    public static void deleteReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        QueryDocumentSnapshot queryDocumentSnapshot = getReceiptElement(dataCommand);
    }

    public static void shareReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        Long user = dataCommand.getChatID();
        Receipt currentReceipt = modelBot.getUserReceipt(user);

        // получаю юзернеймы должников
        ArrayList<String> debtorsUsernames = parseDebtorsString(dataCommand.getTextMessage());

        if (!checkIfAllUsersExist(debtorsUsernames, modelBot.getUserReceipt(dataCommand.getChatID()))) {
            modelBot.setCurrentStateUser(dataCommand.getChatID(), State.INCORRECT_USERNAMES);
            return;
        }

        // добавляю участников в чек
        currentReceipt.addParticipants(user.toString(), debtorsUsernames);
        ArrayList<String> debtors = currentReceipt.getDebtorsIds(debtorsUsernames);

        String[] debt_text = new String[debtors.size()];
        int i = 1;
        Long[] ids = new Long[debtors.size()];

        // for every debtor
        for (String debtor : debtors) {
            BigDecimal sum = currentReceipt.divideSum(user.toString(), debtors.size());
            ids[i] = Long.parseLong(debtor);
            debt_text[i] = "\u2757\ufe0f" +
                    " Привет-велосипед \u2757\ufe0f\n" +
                    "Время платить по долгам\n\ud83d\udd2a Ты должен " + sum + " руб. \ud83d\udd2a\n" +
                    "С любовью, Ваш покорный слуга @" + dataCommand.getUsername() ;
            i++;

        }
        modelBot.sendNotification(ids, debt_text);

    }

    private static ArrayList<String> parseDebtorsString(String string)
    {
        //тут должна быть проверка ника в бд
        return new ArrayList<>(Arrays.asList(string.replaceAll("@", "")
                .split(" ")));
    }

    private static boolean checkIfAllUsersExist(ArrayList<String> usernames, Receipt receipt)
    {
        // проверяет все ли пользователи есть в бд
        ArrayList<String> debtorsIds = receipt.getDebtorsIds(usernames);
        return usernames.size() == debtorsIds.size();
    }

//    private static boolean checkIfUserExists(String debtorUsername){
//        Firestore db = FirestoreDB.getInstance().db;
//        // тут надо переделать, вместо debtorUsername надо его chatId
//        DocumentReference docRef = db.collection("users").document(debtorUsername);
//        ApiFuture<DocumentSnapshot> future = docRef.get();
//        DocumentSnapshot user = null;
//        try {
//            user = future.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        if (user.exists()) {
//            System.out.println("СУЩЕСТВУЕТ");
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
}
