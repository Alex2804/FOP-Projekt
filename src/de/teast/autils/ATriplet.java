package de.teast.autils;

/**
 * Class to store 3 values
 * @author Alexander Muth
 * @param <F> Generic type for the first element
 * @param <S> Generic type for the second element
 * @param <T> Generic type for the third element
 */
public class ATriplet<F, S, T> {
    protected F first;
    protected S second;
    protected T third;

    public ATriplet(F first, S second, T third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }
    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }
    public void setSecond(S second) {
        this.second = second;
    }

    public T getThird() {
        return third;
    }
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
