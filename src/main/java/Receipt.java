import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Receipt{
    private final IExtractable extractor;
    private IParamable params;
    public ReceiptData receiptData;


    public Receipt(IExtractable ex, IParamable params) throws IOException, InterruptedException {
        this.extractor = ex;
        this.params = params;
        this.receiptData = extractor.getDetails(params.getParams());
    }

    public String createReceiptForUser() {
        List<JsonNode> jsonNodesItems = this.receiptData.getJsonNodeItems();
        StringBuilder s = new StringBuilder();
        int j = 1;
        for (JsonNode jsonNodesItem : jsonNodesItems) {
            s.append(jsonNodesItem.get("name").asText().replaceFirst("([0-9:*]+)", j+". "))
                    .append(".\n")
                    .append("Цена: ")
                    .append(jsonNodesItem.get("price").asDouble() / 100)
                    .append("р * ")
                    .append(jsonNodesItem.get("quantity").asDouble())
                    .append(" = ")
                    .append(jsonNodesItem.get("sum").asDouble() / 100)
                    .append("р")
                    .append("\n");
            j++;
        }
        s.append("------------------------------------\n").append("Итог: ")
                .append(this.receiptData.data.get("totalSum").asDouble()/100).append(" рублей");
        return s.toString();
    }
}