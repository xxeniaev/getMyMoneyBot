import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Receipt{
    private final IExtractable extractor;

    public Receipt(IExtractable ex) {
        this.extractor = ex;
    }

    public ReceiptData getData(HashMap<String,String> map) throws IOException, InterruptedException {
        return extractor.getDetails(map);
    }

    public String createReceiptForUser(ReceiptData receiptData) {
        List<JsonNode> jsonNodesItems = receiptData.getJsonNodeItems();
        StringBuilder s = new StringBuilder();
        for (JsonNode jsonNodesItem : jsonNodesItems) {
            s.append(jsonNodesItem.get("name").asText())
                    .append(": ")
                    .append(jsonNodesItem.get("price").asDouble() / 100)
                    .append("\n");
        }
        s.append("Итог: ").append(receiptData.data.get("totalSum").asDouble()/100);
        return s.toString();
    }
}