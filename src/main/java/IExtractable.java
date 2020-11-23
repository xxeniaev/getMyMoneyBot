import java.io.IOException;
import java.util.HashMap;

public interface IExtractable {
    ReceiptData getDetails(HashMap<String, String> map) throws IOException, InterruptedException;
}
