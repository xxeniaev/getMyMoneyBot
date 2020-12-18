public class DataCommand {
    private final String textMessage;
    private final Long chatID;
    private final String username;

    public DataCommand(String textMessage, Long chatId, String username)
    {
        this.textMessage = textMessage;
        this.chatID = chatId;
        this.username = username;
    }

    public Long getChatID() {
        return chatID;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getUsername() {
        return username;
    }
}
