import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class GetMyMoneyBot extends TelegramLongPollingBot implements IObserver {
    public ModelBot modelBot;
    public Map<State, String> answersForStates;

    public GetMyMoneyBot()
    {
        this.modelBot = new ModelBot();
        this.modelBot.addObserver(this);
        this.answersForStates = new HashMap<>();
        this.initializationAnswers();
    }

    public void onUpdateReceived(Update update) {

        Message message = null;
        Long chatId = null;

        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = update.getMessage().getChatId();
            this.modelBot.setCurrentStateUser(chatId);
            // We check if the update has a message and the message has text
            //this.sendMessage(chatId, "Current" + modelBot.getCurrentState());
            if (message.hasText()) {
                // Set variables
                switch (message.getText()) {
                    case "/start":
                        sendMessage(chatId, this.answersForStates.get(State.SIGN_UP));
                        this.modelBot.commands.signUp();
                        break;
                    case "/add_receipt": this.modelBot.commands.waitPhoto();
                        break;
                    case "/my_receipts": this.modelBot.commands.viewReceipts();
                        break;
                    case "/my_stats": this.modelBot.commands.viewStatistic();
                        break;
                    case "/authors": sendMessage(chatId, "С вами были @xxxeny и @donilg");
                        break;
                    default:
                        sendMessage(chatId, "К сожалению, я не понимаю Вас.");
                }
            }
            else if (message.hasPhoto() && this.modelBot.getCurrentState() == State.WAIT_PHOTO) {
                this.modelBot.commands.addReceipt();
            }
            else {
                sendMessage(chatId, "К сожалению, я не понимаю Вас.");
            }
            //this.sendMessage(chatId, "Current" + modelBot.getCurrentState());
        }
    }

    public void sendMessage(Long chat_id, String message_text) {
        SendMessage message = new SendMessage() // Create a message object object
                .setChatId(chat_id)
                .setText(message_text);
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        // TODO
        return "getPoorStudentsMoneyBot";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "1371432192:AAHo_acUpWsEfA8v9_xKq40hR0JRIY6kmfM";
    }

    public void modelIsChange(Long chatId) {
        String completeText = this.modelBot.getBufferAnswer();
        if (completeText != null)
            this.sendMessage(chatId, completeText);
        else if (answersForStates.containsKey(this.modelBot.getCurrentState()))
            this.sendMessage(chatId, this.answersForStates.get(this.modelBot.getCurrentState()));
    }

    public void initializationAnswers()
    {
        String text_sign = "Приветы! Используй клавиатуру ниже, чтобы " +
                "вызывать команды :) \n\n" +
                "Хочешь увидеть авторов?\nИспользуй /authors";
        String text_wait = "Отправь фотографию QR-кода с чека сюда)";
        String text_up_stat = "Мы получили твой список покупок!" +
                "\nЧто дальше?";
        this.answersForStates.put(State.SIGN_UP, text_sign);
        this.answersForStates.put(State.WAIT_PHOTO, text_wait);
        this.answersForStates.put(State.NOTIFY_MADE_RECEIPT, text_up_stat);
    }
}