import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ReceiptData {
    int code;
    JsonNode data;

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(JsonNode data) {
        this.data = data.get("json");
    }

    public List<JsonNode> getJsonNodeItems() {
//          печатает каждый item отдельно
//        System.out.print("\n");
//        System.out.print(data.get("items"));
//        if (this.data.get("items").isArray()) {
//            for ( JsonNode objNode : this.data.get("items")) {
//                System.out.println(objNode);
//            }
//        }
        // возвращает массив json'ов, содержащих информацию о товаре
        return StreamSupport
                .stream(this.data.get("items").spliterator(), false)
                .collect(Collectors.toList());
    }
}
