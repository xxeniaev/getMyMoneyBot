import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple Quick start application demonstrating how to connect to Firestore
 * and add and query documents.
 */
public class FirestoreDB {

    private static FirestoreDB instance;
    public Firestore db;

    /**
     * Initialize Firestore using default project ID.
     */
    public FirestoreDB() {
        this.db = FirestoreOptions.getDefaultInstance().getService();
    }

    public static synchronized FirestoreDB getInstance() {
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }
}


