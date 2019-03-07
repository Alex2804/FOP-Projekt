package de.teast.autils;

/**
 * Class to store 3 values
 * @author Alexander Muth
 * @param <F> Generic type for the first element
 * @param <S> Generic type for the second element
 * @param <T> Generic type for the third element
 */
public class ATriplet<F, S, T> {
    private F first;
    private S second;
    private T third;

    public ATriplet(F first, S second, T third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * @return The first element of this triplet
     */
    public F getFirst() {
        return first;
    }
    /**
     * @param first Replacement for the first element of this triplet
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     * @return The second element of this triplet
     */
    public S getSecond() {
        return second;
    }
    /**
     * @param second Replacement for the second element of this triplet
     */
    public void setSecond(S second) {
        this.second = second;
    }

    /**
     * @return The third element of this triplet
     */
    public T getThird() {
        return third;
    }
    /**
     * @param third Replacement for the third element of this triplet
     */
    public void setThird(T third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ATriplet){
            ATriplet other = (ATriplet)obj;
            return other.first.equals(first) && other.second.equals(second) && other.third.equals(third);
        }
        return super.equals(obj);
    }
}
