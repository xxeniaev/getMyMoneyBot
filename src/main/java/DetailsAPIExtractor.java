import java.io.IOException;
import java.util.HashMap;

public class DetailsAPIExtractor implements IExtractable {

    @Override
    public ReceiptData getDetails(HashMap<String, String> map) throws IOException, InterruptedException {
        // возвращает класс информации чека и засовывает data и code туда
        String jsonStr = HttpClientPost.sendPost(map); // создает json строку
        return Json.parseReceipt(jsonStr);
    }
}
