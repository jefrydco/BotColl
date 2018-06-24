package id.jefrydco.botcoll.message.model;

public class BotMessage extends BaseMessage {

    private BaseSender baseSender;

    public BotMessage(String id, String message, long createdAt) {
        super(id, message, createdAt);
        baseSender = new BaseSender("BotColl", "");
    }

    public BaseSender getSender() {
        return baseSender;
    }
}
