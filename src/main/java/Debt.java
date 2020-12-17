import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Debt {
    // должник
    public String debtor;
    // кому должен
    public String user;
    // сумма долга
    public double sum;
    public String receiptId;

    public Debt(String debtor, String user, String receiptId, int usersQuantity) {
        this.debtor = debtor;
        this.user = user;
        this.receiptId = receiptId;
        this.sum = divideSum(receiptId, usersQuantity);
    }

    public void debtToDatabase()
    {
        Firestore db = FirestoreDB.getInstance().db;
        DocumentReference debt = db.collection("users").document(this.debtor).collection("debts")
                .document(this.receiptId);
        Map<String, Object> debtData = new HashMap<>();
        debtData.put("chatID", this.user);
        debtData.put("sum", this.sum);
        debtData.put("exists", true);
        debt.set(debtData);
    }

    private double divideSum(String receiptId, int usersQuantity)
    {
        Firestore db = FirestoreDB.getInstance().db;
        DocumentSnapshot documentSnapshot = null;
        try {
            documentSnapshot = db.collection("users").document(this.user).collection("receipts")
                    .document(receiptId).get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert documentSnapshot != null;
        double debt = documentSnapshot.getDouble("sum")/(usersQuantity+1);
        System.out.println(debt);
        return debt;
    }
}
