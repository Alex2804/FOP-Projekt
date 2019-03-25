package de.teast.atroops;

/**
 * This class saves {@link ATroop} and a troop count
 * @author Alexander Muth
 */
public class ATroops {
    ATroop troop;
    int count;

    public ATroops(ATroop troop, int count){
        this.troop = troop;
        this.count = count;
    }

    /**
     * @return the troop count saved by this
     */
    public int troopCount(){
        return this.count;
    }
    /**
     * @param count the troop count to add
     * @return the new troop count
     */
    public int addTroop(int count){
        this.count += count;
        return this.count;
    }
    /**
     * @param count the troop count to remove
     * @return the new troop count
     */
    public int removeTroops(int count){
        setTroopCount(this.count - count);
        return this.count;
    }
    /**
     * @param count the new troop count (troop count never gets negative)
     */
    public void setTroopCount(int count){
        this.count = Math.max(0, count);
    }

    /**
     * @return the {@link ATroop} object
     */
    public ATroop troop(){
        return troop;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ATroops)
            return troop.equals(obj) && count == ((ATroops) obj).count;
        return super.equals(obj);
    }

    @Override
    public ATroops clone() {
        return new ATroops(troop.clone(), count);
    }
}
