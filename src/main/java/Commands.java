import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Commands {
    public ModelBot modelBot;

    public Commands(ModelBot modelBot) {this.modelBot = modelBot;}
    public static void signUp(ModelBot modelBot, DataCommand dataCommand)
    {
        /* тута я получаю ник и id для последующего записывания в базу,
        * если это необходимо */
        String username = dataCommand.getUsername();
        Long chatId = dataCommand.getChatID();

        Firestore db = FirestoreDB.getInstance().db;

        DocumentReference docRef = db.collection("users").document(username);
        // Add document data  with id "alovelace" using a hashmap
        Map<String, Object> data = new HashMap<>();
        data.put("chatID", chatId);
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    public static void viewReceipts(ModelBot modelBot, DataCommand dataCommand) {
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
        QRParamsReader qrParamsReader = null;
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

    public static void addBaseData(ModelBot modelBot, DataCommand dataCommand)
    {
        if (!dataCommand.getTextMessage().equals("Да")) {
            modelBot.setCurrentStateUser(dataCommand.getChatID(), State.FAIL_CHECK_RECEIPT);
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

    private static void getProducts() {
    }

    private static void calculateCost() {
    }

    private static void updateStatistic() {
    }
}
