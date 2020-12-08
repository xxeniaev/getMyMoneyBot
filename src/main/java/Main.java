import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
//        Firestore db = FirestoreDB.getInstance().db;
//        ApiFuture<QuerySnapshot> query = db.collection("users").get();
//
//        // вывод бд
//        QuerySnapshot querySnapshot = query.get();
//        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
//        for (QueryDocumentSnapshot document : documents) {
//            System.out.println("User: " + document.getId());
//            System.out.println("First: " + document.getString("username"));
//            if (document.contains("name2")) {
//                System.out.println("Middle: " + document.getString("name2"));
//            }
//        }

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new GetMyMoneyBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}