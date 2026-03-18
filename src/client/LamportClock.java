package lamport;

public class LamportClock {

    private int clock = 0;

    // We add 'synchronized' to prevent errors if two things happen at once
    public synchronized int tick() {
        clock++;
        return clock;
    }

    public synchronized int receiveAction(int receivedTime) {
        clock = Math.max(clock, receivedTime) + 1;
        return clock;
    }

    public synchronized int getTime() {
        return clock;
    }
}