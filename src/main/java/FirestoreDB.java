

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
// [START fs_include_dependencies]
// [START firestore_setup_dependencies]
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
// [END firestore_setup_dependencies]
// [END fs_include_dependencies]
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // [START fs_initialize]
        // [START firestore_setup_client_create]
        Firestore db = FirestoreOptions.getDefaultInstance().getService();
        // [END firestore_setup_client_create]
        // [END fs_initialize]
        this.db = db;
    }

    public static synchronized FirestoreDB getInstance() {
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }
}


