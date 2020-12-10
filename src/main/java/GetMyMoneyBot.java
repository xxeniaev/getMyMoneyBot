import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GetMyMoneyBot extends TelegramLongPollingBot implements IObserver {
    public ModelBot modelBot;
    private final Map<State, String> answersForStates;

    public GetMyMoneyBot()
    {
        this.modelBot = new ModelBot();
        this.modelBot.addObserver(this);
        this.answersForStates = new HashMap<>();
        this.initializationAnswers();
    }

    public void onUpdateReceived(Update update) {
        Message message;
        Long chatId;
        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = update.getMessage().getChatId();
            State lastState = this.modelBot.getLastCurrentStateUser(chatId);
            System.out.println(lastState);
            this.actionCommand(message, chatId, lastState);
            this.actionFunction(message,chatId);
            System.out.println(this.modelBot.getUsersFault(chatId));
            this.actionAfterFunction(chatId);
        }
    }

    private void actionCommand(Message message, Long chatId, State lastState)
    {
        if (message.isCommand()) {
            this.modelBot.setCurrentStateUser(chatId, this.modelBot.getBotStateMap(message.getText()));
            System.out.println("1 " + this.modelBot.getCurrentStateUser(chatId));
        }
        else if (lastState != State.WAIT_PHOTO && lastState != State.WAIT_CHECK_RECEIPT
            && lastState != State.WAIT_CHECK_SHARE && lastState != State.WAIT_USERNAMES_FRIENDS) {
            this.modelBot.setUsersFault(chatId, Boolean.TRUE);
        }
    }

    private void actionFunction(Message message, Long chatId) {
        if (this.modelBot.getStateFunctionHashMap(this.modelBot.getCurrentStateUser(chatId)) != null) {
            DataCommand data = null;
            if (this.modelBot.getCurrentStateUser(chatId) != State.WAIT_PHOTO)
                data = new DataCommand(message.getText(), chatId, message.getChat().getUserName());
            else if (message.hasPhoto())
                data = new DataCommand(this.getLinkPhoto(message), chatId, message.getChat().getUserName());
            else {
                this.modelBot.setUsersFault(chatId, Boolean.TRUE);
            }
            this.modelBot.getStateFunctionHashMap(
                    this.modelBot.getCurrentStateUser(chatId)).accept(this.modelBot, data);
            System.out.println("2 " + this.modelBot.getCurrentStateUser(chatId));
        }
    }

    private void actionAfterFunction(Long chatId)
    {
        if (!this.modelBot.getUsersFault(chatId))
        {
            State[] states = this.modelBot.getStateAfterFunction(this.modelBot.getCurrentStateUser(chatId));
            for (State state: states) {
                this.modelBot.setCurrentStateUser(chatId, state);
                System.out.println("3 " + this.modelBot.getCurrentStateUser(chatId));
            }
        }
        else {
            sendMessage(chatId, "произошёл эксепшион, давай ещё раз");
        }
    }

    private void sendMessage(Long chat_id, String message_text) {
        SendMessage message = new SendMessage() // Create a message object object
                .setChatId(chat_id)
                .setText(message_text);
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getLinkPhoto(Message message) {
        List<PhotoSize> photos = message.getPhoto();
        String photo_id = message.getPhoto().get(photos.size() - 1).getFileId();
        GetFile gf = new GetFile();
        gf.setFileId(photo_id);
        File file = null;
        try {
            file = this.execute(gf);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return "https://api.telegram.org/file/bot" + this.getBotToken() +
                "/"+file.getFilePath();
    }

    public String getBotUsername() {
        // TODO
        Properties prop = new Properties();
        try {
            prop.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop.getProperty("botUsername");
    }

    @Override
    public String getBotToken() {
        // TODO
        Properties prop = new Properties();
        try {
            prop.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop.getProperty("botToken");
    }

    public void modelIsChange(Long chatId) {
        String completeText = this.modelBot.getBufferAnswer();
        if (completeText != null)
            this.sendMessage(chatId, completeText);
        if (answersForStates.containsKey(this.modelBot.getCurrentStateUser(chatId)))
            this.sendMessage(chatId, this.answersForStates.get(this.modelBot.getCurrentStateUser(chatId)));
    }

    private void initializationAnswers()
    {
        String text_sign = "Приветы! Используй клавиатуру ниже, чтобы " +
                "вызывать команды :) \n\n" +
                "Хочешь увидеть авторов?\nИспользуй /authors";
        String text_wait = "Отправляй фотографию QR-кода сюда :)";
        String text_wait_share = "Мы записали твой чек!" +
                "\nДелить его между друзьями?\n(Да/Нет)";
        String text_up_stat = "Мы уведомили твоих компаньонов о том, " +
                "что они должны тебе вернуть тебе червонец.";
        String text_authors = "C вами были @xxxeny и @donilg";
        String text_check_receipt = "Верно? (Да/Нет)";
        String text_fail_check_receipt = "Нет? Видимо что-то пошло не так. Попробуем снова!!!";
        String text_no_share = "Самостоятельный, да? Что будем делать дальше?";
        String text_send_friends = "Отправь нам usernames твоих компаньонов. " +
                "Отправляй username через запятую.\n" +
                "Пример: @xxxeny, @donilg\n" +
                "Также учти, что твои друзья должны быть подписаны на этого бота.";
        this.answersForStates.put(State.SIGN_UP, text_sign);
        this.answersForStates.put(State.PRESS_ADD_RECEIPT, text_wait);
        this.answersForStates.put(State.FAIL_CHECK_RECEIPT, text_fail_check_receipt);
        this.answersForStates.put(State.NOTIFY_MADE_RECEIPT, text_up_stat);
        this.answersForStates.put(State.WAIT_USERNAMES_FRIENDS, text_send_friends);
        this.answersForStates.put(State.WAIT_CHECK_RECEIPT, text_check_receipt);
        this.answersForStates.put(State.VIEW_AUTHORS, text_authors);
        this.answersForStates.put(State.WAIT_CHECK_SHARE, text_wait_share);
        this.answersForStates.put(State.NO_CHECK_SHARE, text_no_share);
    }
}