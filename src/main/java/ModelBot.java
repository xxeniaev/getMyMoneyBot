import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ModelBot{
    public ArrayList<IObserver> observers;
    private final HashMap<String, State> botStateMap;
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
        this.botStateMap = new HashMap<String, State>();
        initializationStateBotMap(this.botStateMap);
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
        this.usersStates.replace(chatId, state);
        this.notifyObservers(chatId);
    }

    public State getBotStateMap(String command) {
        return botStateMap.get(command);
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

    private void initializationStateBotMap(HashMap<String, State> botStates)
    {
        botStates.put("/start", State.SIGN_UP);
        botStates.put("/add_receipt", State.PRESS_ADD_RECEIPT);
        botStates.put("/my_receipts", State.VIEW_RECEIPTS);
        botStates.put("/my_stats", State.VIEW_STATISTIC);
        botStates.put("/authors", State.VIEW_AUTHORS);
    }

    private void initializationStateFunctionMap(HashMap<State, BiConsumer<ModelBot, DataCommand>> hm)
    {
        hm.put(State.SIGN_UP, Commands::signUp);
        hm.put(State.WAIT_PHOTO, Commands::addReceipt);
        hm.put(State.VIEW_RECEIPTS, Commands::viewReceipts);
        hm.put(State.VIEW_STATISTIC, Commands::viewStatistic);
        hm.put(State.WAIT_CHECK_RECEIPT, Commands::addDataBase);
    }

    private void initializationStateAfterFunction(HashMap<State, State[]> hm)
    {
        hm.put(State.SIGN_UP, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.PRESS_ADD_RECEIPT, new State[]{State.WAIT_PHOTO});
        hm.put(State.WAIT_PHOTO, new State[]{State.WAIT_CHECK_RECEIPT});
        hm.put(State.WAIT_CHECK_RECEIPT, new State[]{State.NOTIFY_MADE_RECEIPT, State.CHOOSE_COMMAND});
        hm.put(State.FAIL_CHECK_RECEIPT, new State[]{State.PRESS_ADD_RECEIPT, State.WAIT_PHOTO});
        hm.put(State.VIEW_STATISTIC, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.VIEW_RECEIPTS, new State[]{State.CHOOSE_COMMAND});
        hm.put(State.VIEW_AUTHORS, new State[]{State.CHOOSE_COMMAND});
    }
}
