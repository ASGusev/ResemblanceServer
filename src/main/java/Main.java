public class Main {
    public static void main(String[] args) {
        //Starting network part

        Object waitingObject = new Object();
        while (true) {
            try {
                waitingObject.wait();
            } catch (InterruptedException e) {}
        }
    }
}
