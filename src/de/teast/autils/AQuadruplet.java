package de.teast.autils;

/**
 * Class to store 4 values
 * @author Alexander Muth
 * @param <F> Generic type for the first element
 * @param <S> Generic type for the second element
 * @param <T> Generic type for the third element
 * @param <L> Generic type for the fourth element
 */
public class AQuadruplet<F, S, T, L> extends ATriplet<F, S, T>{
    private L fourth;

    public AQuadruplet(F first, S second, T third,L fourth){
        super(first, second, third);
        this.fourth = fourth;
    }

    public L getFourth() {
        return fourth;
    }
    public void setFourth(L fourth) {
        this.fourth = fourth;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AQuadruplet){
            AQuadruplet other = (AQuadruplet) obj;
            return other.first.equals(first) && other.second.equals(second)
                    && other.third.equals(third) && other.fourth.equals(fourth);
        }
        return super.equals(obj);
    }
}
