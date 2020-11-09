import java.util.ArrayList;

public class ModelBot{
    public ArrayList<IObserver> observers;
    private Long chatId;
    private State currentState;
    public Commands commands;
    private String bufferAnswer;

    public ModelBot(){
        this.observers = new ArrayList<IObserver>();
        this.currentState = null;
        this.chatId = null;
        this.bufferAnswer = null;

        // всегда в самом конце //
        this.commands = new Commands(this);
        }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObservers() {
        for (IObserver observer: observers) {
            observer.modelIsChange(chatId);
        }
    }

    public void setCurrentStateUser(Long userId)
    {
        /*происходит считывание текущего состояния определенного
        юзера из базы данный, менять State через
        setCurrentState*/
        if (this.getCurrentState() == null)
            this.setCurrentState(State.SIGN_UP);
        this.chatId = userId;
    }

    public void setCurrentStateUser(State state) {
        /*Должен вызываться не чтобы установить State из базы
         * данных, а поменять State из метода и сообщить об этом
         * подписчикам*/
                this.setCurrentState(state);
                this.notifyObservers();
    }

    public State getCurrentState(){
        return this.currentState;
    }

    private void setCurrentState(State state){
        // Где-то должно быть вливание в базу данных, пока не понял
        // Возможно здесь надо будет метод вызывать для этого
        this.currentState = state;
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
}
