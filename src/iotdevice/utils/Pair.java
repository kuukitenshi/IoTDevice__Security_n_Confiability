package iotdevice.utils;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class used to represent a pair of values.
 */
public class Pair<T1, T2> {

    private final T1 first;
    private final T2 second;

    /**
     * Constructor of the class.
     * 
     * @param first  the first value.
     * @param second the second value.
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value.
     * 
     * @return the first value.
     */
    public T1 getFirst() {
        return this.first;
    }

    /**
     * Returns the second value.
     * 
     * @return the second value.
     */
    public T2 getSecond() {
        return this.second;
    }
}
