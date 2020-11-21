import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;

public class Json {
    private static final ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper(){
        return new ObjectMapper();
    }

    public static JsonNode parseNode(String src) throws IOException {
        return objectMapper.readTree(src);
    }

    public static Receipt parseReceipt(String json) throws IOException {
        return objectMapper.readValue(json, Receipt.class);
    }
}
class Receipt{
    int code;
    JsonNode data;

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public int getCode() {
        return this.code;
    }

    public JsonNode getData() {
        return this.data;
    }
}

class Data{
    JsonNode json;

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }
}

class JsonClass {
    public String items;
    public int nds10;
    public String userInn;
    public String dateTime;
    public String kktRegId;
    public String operator;
    public int totalSum;
    public int fiscalSign;
    public int receiptCode;
    public int shiftNumber;
    public int cashTotalSum;
    public int taxationType;
    public int ecashTotalSum;
    public int operationType;
    public int requestNumber;
    public String fiscalDriveNumber;
    public int fiscalDocumentNumber;
    public String html;

    public int getNds10() {
        return nds10;
    }

    public String getItems() {
        return items;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getKktRegId() {
        return kktRegId;
    }

    public String getUserInn() {
        return userInn;
    }

    public int getCashTotalSum() {
        return cashTotalSum;
    }

    public int getEcashTotalSum() {
        return ecashTotalSum;
    }

    public String getOperator() {
        return operator;
    }

    public int getFiscalDocumentNumber() {
        return fiscalDocumentNumber;
    }

    public int getFiscalSign() {
        return fiscalSign;
    }

    public int getOperationType() {
        return operationType;
    }

    public int getReceiptCode() {
        return receiptCode;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public int getTaxationType() {
        return taxationType;
    }

    public int getTotalSum() {
        return totalSum;
    }

    public String getFiscalDriveNumber() {
        return fiscalDriveNumber;
    }

    public String getHtml() {
        return html;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public void setCashTotalSum(int cashTotalSum) {
        this.cashTotalSum = cashTotalSum;
    }

    public void setKktRegId(String kktRegId) {
        this.kktRegId = kktRegId;
    }

    public void setNds10(int nds10) {
        this.nds10 = nds10;
    }

    public void setEcashTotalSum(int ecashTotalSum) {
        this.ecashTotalSum = ecashTotalSum;
    }

    public void setFiscalDocumentNumber(int fiscalDocumentNumber) {
        this.fiscalDocumentNumber = fiscalDocumentNumber;
    }

    public void setUserInn(String userInn) {
        this.userInn = userInn;
    }

    public void setFiscalDriveNumber(String fiscalDriveNumber) {
        this.fiscalDriveNumber = fiscalDriveNumber;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setFiscalSign(int fiscalSign) {
        this.fiscalSign = fiscalSign;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public void setReceiptCode(int receiptCode) {
        this.receiptCode = receiptCode;
    }

    public void setRequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public void setTaxationType(int taxationType) {
        this.taxationType = taxationType;
    }

    public void setTotalSum(int totalSum) {
        this.totalSum = totalSum;
    }
}

class Item{
    public int sum;
    public String name;
    public int price;
    public int quantity;

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSum() {
        return sum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}


