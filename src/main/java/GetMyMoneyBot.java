import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class GetMyMoneyBot extends TelegramLongPollingBot implements IObserver {
    public ModelBot modelBot;
    private final Map<State, String> answersForStates;
    private final HashMap<State, BiConsumer<ReplyKeyboardMarkup, SendMessage>> replyKeyboardForStates;
    private final HashMap<State, BiConsumer<InlineKeyboardMarkup, SendMessage>> inlineKeyboardForStates;

    public GetMyMoneyBot()
    {
        this.modelBot = new ModelBot();
        this.modelBot.addObserver(this);
        this.answersForStates = new HashMap<>();
        this.replyKeyboardForStates = new HashMap<>();
        this.inlineKeyboardForStates = new HashMap<>();
        this.initializationAnswers();
        this.initializationReplyKeyboards(this.replyKeyboardForStates);
        this.initializationInlineKeyboards(this.inlineKeyboardForStates);
    }

    public void onUpdateReceived(Update update) {
        Message message;
        CallbackQuery query;
        Long chatId;
        if (update.hasMessage()) {
            message = update.getMessage();
            chatId = update.getMessage().getChatId();
            State lastState = this.modelBot.getLastCurrentStateUser(chatId);
            System.out.println(lastState);
            this.actionCommand(message.getText(), chatId, lastState);
            this.actionFunction(message, chatId);
            System.out.println(this.modelBot.getUsersFault(chatId));
            this.actionAfterFunction(chatId);
        }
        else if (update.hasCallbackQuery()) {
            sendCallBack(update.getCallbackQuery().getId());
            query = update.getCallbackQuery();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            State lastState = this.modelBot.getLastCurrentStateUser(chatId);
            this.actionCommand(query.getData(), chatId, lastState);
            this.actionFunction(query, chatId);
            this.actionAfterFunction(chatId);
        }
    }

    private void setBotReplyKeyboard(SendMessage sendMessage, Long chatId)
    {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        this.replyKeyboardForStates.get(modelBot.getCurrentStateUser(chatId)).accept(replyKeyboardMarkup, sendMessage);
    }

    private void setBotInlineKeyboard(SendMessage sendMessage, Long chatId)
    {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        this.inlineKeyboardForStates.get(modelBot.getCurrentStateUser(chatId)).accept(inlineKeyboardMarkup, sendMessage);
    }

    private static void setInlineKeyboard(InlineKeyboardMarkup keyboardMarkup, SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button1 = new ArrayList<>();
        button1.add(new InlineKeyboardButton().setText("Загасить\ud83d\udcb8").setCallbackData("quench"));
        button1.add(new InlineKeyboardButton().setText("Удалить\ud83e\udde8").setCallbackData("delete"));
        buttons.add(button1);

        keyboardMarkup.setKeyboard(buttons);
    }

    private static void setAllMenuKeyboard(ReplyKeyboardMarkup replyKeyboardMarkup, SendMessage sendMessage)
    {
        ArrayList<KeyboardRow> buttons = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow secondRow = new KeyboardRow();

        firstRow.add("Добавить чек");
        secondRow.add("Мои чеки");
        secondRow.add("Моя статистика");


        buttons.add(firstRow);
        buttons.add(secondRow);

        replyKeyboardMarkup.setKeyboard(buttons);
    }

    private static void setYesOrNoKeyboard(ReplyKeyboardMarkup replyKeyboardMarkup, SendMessage sendMessage)
    {
        ArrayList<KeyboardRow> buttons = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow buttonMenu = new KeyboardRow();

        firstRow.add("Да");
        firstRow.add("Нет");
        buttonMenu.add("Меню");

        buttons.add(firstRow);
        buttons.add(buttonMenu);

        replyKeyboardMarkup.setKeyboard(buttons);
    }

    private static void setOnlyMenuKeyboard(ReplyKeyboardMarkup replyKeyboardMarkup, SendMessage sendMessage)
    {
        ArrayList<KeyboardRow> buttons = new ArrayList<>();

        KeyboardRow buttonMenu = new KeyboardRow();
        buttonMenu.add("Меню");
        buttons.add(buttonMenu);

        replyKeyboardMarkup.setKeyboard(buttons);
    }

    private void actionCommand(String command, Long chatId, State lastState)
    {
        State newState = this.modelBot.getBotStateMap(command, lastState);
        if (newState != null) {
            this.modelBot.setCurrentStateUser(chatId, newState);
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
            if (this.modelBot.getCurrentStateUser(chatId) != State.WAIT_PHOTO) {
                data = new DataCommand(message.getText(), chatId, message.getChat().getUserName());
            }
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

    private void actionFunction(CallbackQuery query, Long chatId)
    {
        if (this.modelBot.getStateFunctionHashMap(this.modelBot.getCurrentStateUser(chatId)) != null) {
            DataCommand data = null;
            data = new DataCommand(query.getMessage().getText(), query.getMessage().getChatId(), query.getFrom().getUserName());
            this.modelBot.getStateFunctionHashMap(
                    this.modelBot.getCurrentStateUser(chatId)).accept(this.modelBot, data);
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
            sendMessage(chatId, "произошёл эксепшион, давай ещё раз", false);
        }
    }

    private void sendMessage(Long[] chats_id, String[] messages_text) {
        if (chats_id.length != messages_text.length)
            throw new IllegalArgumentException("неправильные аргументы");
        for(int i = 0; i < chats_id.length; i++) {
            sendMessage(chats_id[i], messages_text[i], true);
        }
    }

    private void sendCallBack(String id)
    {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(id);
        answerCallbackQuery.setText("понял-принял");
        answerCallbackQuery.setShowAlert(true);
        try
        {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chat_id, String message_text, Boolean key) {
        SendMessage message = new SendMessage() // Create a message object object
                .setChatId(chat_id)
                .setText(message_text);
        try {
            if (!key) {
                State currentState = this.modelBot.getCurrentStateUser(chat_id);
                if (this.replyKeyboardForStates.containsKey(currentState))
                    setBotReplyKeyboard(message, chat_id);
                if (this.inlineKeyboardForStates.containsKey(currentState))
                    setBotInlineKeyboard(message, chat_id);
            }
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
        assert file != null;
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

    @Override
    public void sendNotification(Long[] chats_id, String[] messages) {
        System.out.println("________ЗЗЗЗЗЗЗЗЗЗЗ");
        this.sendMessage(chats_id, messages);
    }

    public void modelIsChange(Long chatId) {
        String completeText = this.modelBot.getBufferAnswer();
        if (completeText != null)
            this.sendMessage(chatId, completeText, false);
        if (answersForStates.containsKey(this.modelBot.getCurrentStateUser(chatId)))
            this.sendMessage(chatId, this.answersForStates.get(this.modelBot.getCurrentStateUser(chatId)), false);
    }

    private void initializationReplyKeyboards(HashMap<State, BiConsumer<ReplyKeyboardMarkup, SendMessage>> hm){
        State[] statesOnlyMenu = new State[]{State.PRESS_ADD_RECEIPT, State.WAIT_USERNAMES_FRIENDS,
                State.VIEW_SPECIFIC_RECEIPT, State.WAIT_SELECT_RECEIPT};
        State[] statesYesOrNo = new State[]{State.WAIT_CHECK_RECEIPT, State.WAIT_CHECK_SHARE,
                State.INCORRECT_USERNAMES};
        State[] statesAllMenu = new State[]{State.SIGN_UP,
                State.NOTIFY_MADE_RECEIPT, State.VIEW_AUTHORS, State.NO_CHECK_SHARE, State.GO_MENU};
        for (State state: statesAllMenu
             ) {
            hm.put(state, GetMyMoneyBot::setAllMenuKeyboard);
        }
        for (State state: statesOnlyMenu
        ) {
            hm.put(state, GetMyMoneyBot::setOnlyMenuKeyboard);
        }
        for (State state: statesYesOrNo
        ) {
            hm.put(state, GetMyMoneyBot::setYesOrNoKeyboard);
        }
    }

    private void initializationInlineKeyboards(HashMap<State, BiConsumer<InlineKeyboardMarkup, SendMessage>> hm) {
        hm.put(State.GIVE_VIEW_SPECIFIC_RECEIPT, GetMyMoneyBot::setInlineKeyboard);
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
                "Отправляй username через пробел.\n" +
                "Пример: @xxxeny @donilg\n" +
                "Также учти, что твои друзья должны быть подписаны на этого бота.";
        String text_incorrect_users = "Мы не нашли твоих друзей в базе данных.\n" +
                "У нас есть два варианта: либо ты неправильно написал их usernames, " +
                "либо они не подписаны на нашего бота.\n" +
                "Проверь, пожалуйста, ещё раз.";
        this.answersForStates.put(State.SIGN_UP, text_sign);
        this.answersForStates.put(State.PRESS_ADD_RECEIPT, text_wait);
        this.answersForStates.put(State.FAIL_CHECK_RECEIPT, text_fail_check_receipt);
        this.answersForStates.put(State.NOTIFY_MADE_RECEIPT, text_up_stat);
        this.answersForStates.put(State.WAIT_USERNAMES_FRIENDS, text_send_friends);
        this.answersForStates.put(State.WAIT_CHECK_RECEIPT, text_check_receipt);
        this.answersForStates.put(State.VIEW_AUTHORS, text_authors);
        this.answersForStates.put(State.WAIT_CHECK_SHARE, text_wait_share);
        this.answersForStates.put(State.NO_CHECK_SHARE, text_no_share);
        this.answersForStates.put(State.INCORRECT_USERNAMES, text_incorrect_users);
        this.answersForStates.put(State.GO_MENU, "Хорошо, мой дорогой друг" + "\nЧто будем делать дальше?");
    }
}