import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
        ArrayList<Receipt> myReceipts = new ArrayList<>();
        CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                .collection("receipts");
        ApiFuture<QuerySnapshot> future = receipts.get();
        QuerySnapshot querySnapshotReceipts = null;
        try {
            querySnapshotReceipts = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        List<QueryDocumentSnapshot> receiptsDocuments = querySnapshotReceipts.getDocuments();

        // составлять сообщение с юзера
        // сделать, чтобы чеки выводились сортированно по  датам, а не рандомно
        StringBuilder s = new StringBuilder();
        s.append("Вот твои чеки, пользуйся\u2665\ufe0f\n\n");
        int i = 0;
        for (QueryDocumentSnapshot receiptDocument: receiptsDocuments){
            s.append("/").append(i).append(" ");
            String date = receiptDocument.getString("ticket date");
            s.append(date).append("\n");
            i++;
        }
        // ...
        // ...

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

    public static void shareReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        Long user = dataCommand.getChatID();
        Receipt currentReceipt = modelBot.getUserReceipt(user);

        // получаю юзернеймы должников
        ArrayList<String> debtorsUsernames = parseDebtorsString(dataCommand.getTextMessage());

        // добавляю участников в чек
        currentReceipt.addParticipants(user.toString(), debtorsUsernames);
        ArrayList<String> debtors =currentReceipt.getDebtorsIds(debtorsUsernames);

        String[] debt_text = new String[debtors.size()];
        int i = 0;
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

    private static boolean checkIfAllUsersExist(String string, Receipt receipt)
    {
        // проверяет все ли пользователи есть в бд
        ArrayList<String> debtorsUsernames = parseDebtorsString(string);
        ArrayList<String> debtorsIds = receipt.getDebtorsIds(debtorsUsernames);
        return debtorsUsernames.size() <= debtorsIds.size();
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
