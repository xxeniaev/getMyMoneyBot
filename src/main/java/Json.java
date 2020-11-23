import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Json {
    private static final ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper(){
        return new ObjectMapper();
    }

    public static ReceiptData parseReceipt(String json) throws IOException {
        return objectMapper.readValue(json, ReceiptData.class);
    }
}



