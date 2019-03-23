package de.teast.atroops;

public class ATroops {
    ATroop troop;
    int count;

    public ATroops(ATroop troop, int count){
        this.troop = troop;
        this.count = count;
    }

    public int troopCount(){
        return this.count;
    }
    public int addTroop(int count){
        this.count += count;
        return this.count;
    }
    public int removeTroops(int count){
        this.count = Math.min(0, this.count - count);
        return count;
    }

    public ATroop troop(){
        return troop;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ATroops)
            return hashCode() == obj.hashCode() && count == ((ATroops) obj).count;
        return super.equals(obj);
    }
    @Override
    public int hashCode() {
        return troop.getClass().hashCode();
    }
}
