import java.io.IOException;
import java.util.HashMap;

public class Commands {
    public ModelBot modelBot;

    public Commands(ModelBot modelBot) {this.modelBot = modelBot;}
    /*проверяем, есть ли он базе*/
    /*если есть, то просто передаем управление состоянию
     * Choose_command, иначе, создаем запись в БД*/
    public void signUp() {
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void waitPhoto() {
        this.modelBot.setCurrentStateUser(State.WAIT_PHOTO);
    }

    public void viewReceipts() {
        // ...
        // ...
        this.modelBot.setBufferAnswer("Вот твои чеки, пользуйся: ");
        this.modelBot.setCurrentStateUser(State.VIEW_RECEIPTS);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void viewStatistic() {
        // ...
        // ...
        this.modelBot.setBufferAnswer("надо придумать, какую статистику ведём)");
        this.modelBot.setCurrentStateUser(State.VIEW_STATISTIC);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    public void addReceipt(String linkPhoto) throws IOException, InterruptedException {
        this.modelBot.setCurrentStateUser(State.MAKE_RECEIPT);

        QRParamsReader qrParamsReader = new QRParamsReader(linkPhoto);
        IExtractable apiExtractor = new DetailsAPIExtractor();
        Receipt receipt = new Receipt(apiExtractor, qrParamsReader);

        this.modelBot.setBufferAnswer(receipt.createReceiptForUser());

        this.getProducts();
        this.calculateCost();
        this.updateStatistic();
        this.modelBot.setCurrentStateUser(State.NOTIFY_MADE_RECEIPT);
        this.modelBot.setCurrentStateUser(State.CHOOSE_COMMAND);
    }

    private void getProducts() {
    }

    private void calculateCost() {
    }

    private void updateStatistic() {
    }
}
