import com.google.cloud.firestore.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class Commands {
    public ModelBot modelBot;

    public Commands(ModelBot modelBot) {this.modelBot = modelBot; }
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
    }

    public static void viewReceipts(ModelBot modelBot, DataCommand dataCommand) {
        Firestore db = FirestoreDB.getInstance().db;
        CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                .collection("receipts");
        receipts.orderBy("addition date", Query.Direction.ASCENDING);

        List<QueryDocumentSnapshot> receiptsDocuments = getReceiptsDocuments(receipts);
        sortReceipts(receiptsDocuments);
        // составлять сообщение с юзера
        // сделать, чтобы чеки выводились сортированно по  датам, а не рандомно
        StringBuilder s = new StringBuilder();
        s.append("Вот твои чеки, пользуйся :)\n\n");
        int i = 1;
        for (QueryDocumentSnapshot receiptDocument: receiptsDocuments){
            if (receiptDocument.getBoolean("deleted"))
                continue;
            s.append("\u25aa\ufe0fЧек ").append("/").append(i).append(" от ");
            String date;
            if (receiptDocument.getString("addition date") == null)
                date = "???????????";
            else
                date = createBeautifulDate(receiptDocument.getString("addition date").split(" ")[0]);
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

    private static QueryDocumentSnapshot getElement(int position, String id)
    {
        Firestore db = FirestoreDB.getInstance().db;
        CollectionReference receipts = db.collection("users").document(id)
                .collection("receipts");
        List<QueryDocumentSnapshot> receiptsDocumetns = getReceiptsDocuments(receipts);
        sortReceipts(receiptsDocumetns);

        int i = 0;
        for (QueryDocumentSnapshot receiptDocument: receiptsDocumetns){
            if (receiptDocument.getBoolean("deleted"))
                continue;
            if (i==position){
                return receiptDocument;
            }
            i++;
        }
        return null;
    }

    private static void sortReceipts(List<QueryDocumentSnapshot> receipts)
    {

    }

    public static void viewSpecificReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        StringBuilder text = new StringBuilder();
        StringBuilder s = new StringBuilder(dataCommand.getTextMessage()).deleteCharAt(0);
        int position = Integer.parseInt(s.toString()) - 1;
        Firestore db = FirestoreDB.getInstance().db;
        CollectionReference receipts = db.collection("users").document(dataCommand.getChatID().toString())
                .collection("receipts");
        QueryDocumentSnapshot receiptDocument = getElement(position, dataCommand.getChatID().toString());


        CollectionReference goods = receipts.document(receiptDocument.getId()).collection("goods");
        List<QueryDocumentSnapshot> goodsDocuments = getReceiptsDocuments(goods);

        text.append("Чек №").append(position + 1).append("\n");
        String date = "?????????";
        if (receiptDocument.getString("addition date") != null)
            date = createBeautifulDate(receiptDocument.getString("addition date").split(" ")[0]);
        text.append("Дата добавления чека: ").append(date).append("\n");
        String quited = "????????????";
        if (receiptDocument.getBoolean("quited") != null)
            quited = (receiptDocument.getBoolean("quited")) ? "Погашен \u2705" : "Не погашен";
        text.append(quited).append("\n").append("\n");

        int i = 1;
        for (QueryDocumentSnapshot good: goodsDocuments
             ) {
            BigDecimal price = BigDecimal.valueOf(good.getDouble("price")*good.getDouble("quantity"))
                    .setScale(2, ROUND_HALF_UP);
            text.append(i).append(". ").append(good.getId()).append(".\n").append(price).append(" р").append("\n");
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
        QRParamsReader qrParamsReader;
        try {
            qrParamsReader = new QRParamsReader(linkPhoto);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            modelBot.setUsersFault(dataCommand.getChatID(), Boolean.TRUE);
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

    private static DocumentReference getReceiptElement(DataCommand dataCommand)
    {
        Pattern pattern = Pattern.compile("Чек №(\\d+?)\\n");
        Matcher matcher = pattern.matcher(dataCommand.getTextMessage());
        if (matcher.find()) {
            int position = Integer.parseInt(matcher.group(1)) - 1;
            return getElement(position, dataCommand.getChatID().toString()).getReference();
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

    public static void quitReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        DocumentReference documentReference = getReceiptElement(dataCommand);

        Map<String, Object> updateReceiptData = new HashMap<>();
        updateReceiptData.put("quited", true);
        documentReference.set(updateReceiptData, SetOptions.merge());    }

    public static void deleteReceipt(ModelBot modelBot, DataCommand dataCommand)
    {
        DocumentReference documentReference = getReceiptElement(dataCommand);

        Map<String, Object> updateReceiptData = new HashMap<>();
        updateReceiptData.put("deleted", true);
        documentReference.set(updateReceiptData, SetOptions.merge());;

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

    private static boolean checkIfAllUsersExist(ArrayList<String> usernames, Receipt receipt)
    {
        // проверяет все ли пользователи есть в бд
        ArrayList<String> debtorsIds = receipt.getDebtorsIds(usernames);
        return usernames.size() == debtorsIds.size();
    }

    private static String createBeautifulDate(String date){
        String[] splitDate = date.split("/");
        GregorianCalendar beautifulDate = new GregorianCalendar(Integer.parseInt(splitDate[0]),
                Integer.parseInt(splitDate[1])-1, Integer.parseInt(splitDate[2]));
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.format(beautifulDate.getTime());
    }
}
