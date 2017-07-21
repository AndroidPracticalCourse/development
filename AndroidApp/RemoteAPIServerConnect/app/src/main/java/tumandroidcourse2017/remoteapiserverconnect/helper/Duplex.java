package tumandroidcourse2017.remoteapiserverconnect.helper;

/*
 * Generic type class
 */

public class Duplex<S, T> {

    private S first;
    private T second;

    public Duplex(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public Duplex() {

    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
