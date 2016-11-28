import java.net.Socket;

public class Message {
    final private static String PING_TYPE = "ping";
    final private static String REGISTER_TYPE = "register";
    final private static String LOGIN_TYPE = "login";
    final private static String TEST_TYPE = "test";



    private Socket from = null;
    private String type = null;
    private String textMessage = null;
    Message(String allTextMessage) {
        int indOfPref = allTextMessage.indexOf(' ');
        if (indOfPref > 0) {
            type = allTextMessage.substring(0, indOfPref - 1);
            textMessage = allTextMessage.substring(indOfPref + 1);
        }
    }
    Message(String allTextMessage, Socket from) {
        if (allTextMessage != null) {
            int indOfPref = allTextMessage.indexOf(' ');
            if (indOfPref > 0) {
                type = allTextMessage.substring(0, indOfPref - 1);
                textMessage = allTextMessage.substring(indOfPref + 1);
            }
        }
        this.from = from;
    }

    public Socket getFrom() {
        return from;
    }
    public String getType() {
        return type;
    }
    public String getText() {
        return textMessage;
    }

    public void setType(String newType) {
        type = newType;
    }
    public void setTextMessage(String newTextMessage) {
        textMessage = newTextMessage;
    }
    public void setFrom(Socket from) {
        this.from = from;
    }

    public String getStringMessage() {
        if (type != null && textMessage != null) {
            return type + ' ' + textMessage;
        }
        return null;
    }


    public void applyMessage() {
        switch (type) {
            case REGISTER_TYPE:
                applyRegister();
                break;
            case LOGIN_TYPE:
                applyLogin();
                break;
            case TEST_TYPE:
                applyTest();
                break;
        }
    }
    
    private void applyRegister() {
        //// TODO: 28.11.2016  
    }
    private void applyLogin() {
        //// TODO: 28.11.2016  
    }
    private void applyTest() {
        System.out.println("test");
    }
}
