abstract interface IObserver {
    public void modelIsChange(Long chatId);

    public void sendNotification(Long[] chats_id, String[] messages);
}
