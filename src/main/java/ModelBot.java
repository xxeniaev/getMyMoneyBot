import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelBot{
    public ArrayList<IObserver> observers;
    private final HashMap<String, State> botAllMenuStateMap;
    private final HashMap<String, State> botOnlyMenuStateMap;
    private final HashMap<String, State> botStateViewReceiptMap;
    private ConcurrentHashMap<Long, State> usersStates;
    private ConcurrentHashMap<Long, Boolean> usersFault;
    private HashMap<Long, Receipt> usersReceipt;
    private final HashMap<State, BiConsumer<ModelBot, DataCommand>> stateFunctionHashMap;
    private final HashMap<State, State[]> stateAfterFunction;
    private String bufferAnswer;

    public ModelBot() {
        this.observers = new ArrayList<>();
        this.bufferAnswer = null;
        this.usersStates = new ConcurrentHashMap<Long, State>();
        this.usersFault = new ConcurrentHashMap<Long, Boolean>();
        this.usersReceipt = new HashMap<Long, Receipt>();
        this.botAllMenuStateMap = new HashMap<String, State>();
        initializationAllMenuStateBotMap(this.botAllMenuStateMap);
        this.botOnlyMenuStateMap = new HashMap<String, State>();
        initializationOnlyMenuStateBotMap(this.botOnlyMenuStateMap);
        this.botStateViewReceiptMap = new HashMap<String, State>();
        initializationViewReceiptStateBotMap(this.botStateViewReceiptMap);
        this.stateFunctionHashMap = new HashMap<State, BiConsumer<ModelBot, DataCommand>>();
        initializationStateFunctionMap(stateFunctionHashMap);
        this.stateAfterFunction =  new HashMap<State, State[]>();
        initializationStateAfterFunction(this.stateAfterFunction);
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObservers(Long chatId) {
        for (IObserver observer: observers) {
            observer.modelIsChange(chatId);
        }
    }

    public void sendNotification(Long[] chatsId, String[] messages) {
        for (IObserver observer: observers) {
            observer.sendNotification(chatsId, messages);
        }
    }

    public State getLastCurrentStateUser(Long userId)
    {
        if (!usersStates.containsKey(userId))
            usersStates.put(userId, State.NONE);
        this.setUsersFault(userId, Boolean.FALSE);
        return usersStates.get(userId);
    }

    public Boolean getUsersFault(Long userId) {
        return usersFault.get(userId);
    }

    public void setUsersFault(Long userId, Boolean bool) {
        if (!usersFault.containsKey(userId))
            usersFault.put(userId, Boolean.FALSE);
        this.usersFault.replace(userId, bool);
    }

    public void setCurrentStateUser(Long chatId, State state) {
        if (state == null)
            return;
        this.usersStates.replace(chatId, state);
        this.notifyObservers(chatId);
    }

    public State getBotStateMap(String command, State state) {
        if (state == State.WAIT_SELECT_RECEIPT) {
            Pattern pattern = Pattern.compile("^/\\d+?$");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find())
                return botStateViewReceiptMap.get("/view_specific_receipt");
            if (command.equals("quench") || command.equals("delete")) {
                return botStateViewReceiptMap.get(command);
            }
            return botOnlyMenuStateMap.get(command);
        }
        else if (state == State.CHOOSE_COMMAND || state == State.NONE)
            return botAllMenuStateMap.get(command);
        return botOnlyMenuStateMap.get(command);
    }

    public BiConsumer<ModelBot, DataCommand> getStateFunctionHashMap(State state)
    {
        if (this.stateFunctionHashMap.containsKey(state))
            return this.stateFunctionHashMap.get(state);
        return null;
    }

    public State getCurrentStateUser(Long chatId){
        return this.usersStates.get(chatId);
    }

    public Receipt getUserReceipt(Long userId)
    {
        return this.usersReceipt.get(userId);
    }

    public void setUsersReceipt(Long userId, Receipt receipt)
    {
        if (!this.usersReceipt.containsKey(userId))
            this.usersReceipt.put(userId, receipt);
        else
            this.usersReceipt.replace(userId, receipt);
    }

    public State[] getStateAfterFunction(State state)
    {
        return this.stateAfterFunction.get(state);
    }

    public void setBufferAnswer(String text) {
        this.bufferAnswer = text;
    }

    public String getBufferAnswer() {
        String answer = this.bufferAnswer;
        if (answer != null)
            this.bufferAnswer = null;
        return answer;
    }

    private void initializationAllMenuStateBotMap(HashMap<String, State> botStates)
    {
        botStates.put("/start", State.SIGN_UP);
        botStates.put("Добавить чек", State.PRESS_ADD_RECEIPT);
        botStates.put("Мои чеки", State.VIEW_RECEIPTS);
        botStates.put("Моя статистика", State.VIEW_STATISTIC);
        botStates.put("/authors", State.VIEW_AUTHORS);
    }

    private void initializationOnlyMenuStateBotMap(HashMap<String, State> botStates)
    {
        botStates.put("Меню", State.GO_MENU);
    }

    private void initializationViewReceiptStateBotMap(HashMap<String, State> botStates) {
        botStates.put("/view_specific_receipt", State.VIEW_SPECIFIC_RECEIPT);
        botStates.put("quench", State.QUENCH_SPECIFIC_RECEIPT);
        botStates.put("delete", State.DELETE_SPECIFIC_RECEIPT);
    }

    private void initializationStateFunctionMap(HashMap<State, BiConsumer<ModelBot, DataCommand>> hm)
    {
        hm.put(State.SIGN_UP, Commands::signUp);
        hm.put(State.WAIT_PHOTO, Commands::addReceipt);
        hm.put(State.VIEW_RECEIPTS, Commands::viewReceipts);
        hm.put(State.VIEW_STATISTIC, Commands::viewStatistic);
        hm.put(State.WAIT_CHECK_RECEIPT, Commands::addDataBase);
        hm.put(State.WAIT_CHECK_SHARE, Commands::areThereFriends);
        hm.put(State.WAIT_USERNAMES_FRIENDS, Commands::shareReceipt);
        hm.put(State.VIEW_SPECIFIC_RECEIPT, Commands::viewSpecificReceipt);
        hm.put(State.QUENCH_SPECIFIC_RECEIPT, Commands::quenchReceipt);
        hm.put(State.DELETE_SPECIFIC_RECEIPT, Commands::deleteReceipt);
    }

    private void initializationStateAfterFunction(HashMap<State, State[]> hm)
    {
        hm.put(State.SIGN_UP, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.PRESS_ADD_RECEIPT, new State[]{State.WAIT_PHOTO});
        hm.put(State.WAIT_PHOTO, new State[]{State.WAIT_CHECK_RECEIPT});
        hm.put(State.WAIT_CHECK_RECEIPT, new State[]{State.WAIT_CHECK_SHARE});
        hm.put(State.FAIL_CHECK_RECEIPT, new State[]{State.PRESS_ADD_RECEIPT, State.WAIT_PHOTO});
        hm.put(State.NO_CHECK_SHARE, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.WAIT_CHECK_SHARE, new State[]{State.WAIT_USERNAMES_FRIENDS});
        hm.put(State.WAIT_USERNAMES_FRIENDS, new State[]{State.NOTIFY_MADE_RECEIPT, State.CHOOSE_COMMAND});
        hm.put(State.INCORRECT_USERNAMES, new State[]{State.WAIT_USERNAMES_FRIENDS});
        hm.put(State.VIEW_STATISTIC, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.VIEW_RECEIPTS, new State[]{State.WAIT_SELECT_RECEIPT});
        hm.put(State.VIEW_SPECIFIC_RECEIPT, new State[]{State.GIVE_VIEW_SPECIFIC_RECEIPT,
                State.WAIT_SELECT_RECEIPT});
        hm.put(State.DELETE_SPECIFIC_RECEIPT, new State[]{State.WAIT_SELECT_RECEIPT});
        hm.put(State.QUENCH_SPECIFIC_RECEIPT, new State[]{State.WAIT_SELECT_RECEIPT});
        hm.put(State.VIEW_AUTHORS, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.GO_MENU, new State[]{State.CHOOSE_COMMAND});
    }
}
