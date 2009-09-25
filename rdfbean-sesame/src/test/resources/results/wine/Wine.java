package wine;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#")
public class Wine extends food.PotableLiquid {

    @Predicate(ln="hasBody")
    private WineBody body;

    @Predicate(ln="hasColor")
    private WineColor color;

    @Predicate(ln="hasFlavor")
    private WineFlavor flavor;

    @Predicate
    private Region locatedIn;

    @Predicate
    private WineGrape madeFromGrape;

    @Predicate(ln="hasMaker")
    private Winery maker;

    @Predicate(ln="hasSugar")
    private WineSugar sugar;

    @Predicate(ln="hasWineDescriptor")
    private WineDescriptor wineDescriptor;

    public WineBody getBody(){
        return body;
    }

    public void setBody(WineBody body){
        this.body = body;
    }

    public WineColor getColor(){
        return color;
    }

    public void setColor(WineColor color){
        this.color = color;
    }

    public WineFlavor getFlavor(){
        return flavor;
    }

    public void setFlavor(WineFlavor flavor){
        this.flavor = flavor;
    }

    public Region getLocatedIn(){
        return locatedIn;
    }

    public void setLocatedIn(Region locatedIn){
        this.locatedIn = locatedIn;
    }

    public WineGrape getMadeFromGrape(){
        return madeFromGrape;
    }

    public void setMadeFromGrape(WineGrape madeFromGrape){
        this.madeFromGrape = madeFromGrape;
    }

    public Winery getMaker(){
        return maker;
    }

    public void setMaker(Winery maker){
        this.maker = maker;
    }

    public WineSugar getSugar(){
        return sugar;
    }

    public void setSugar(WineSugar sugar){
        this.sugar = sugar;
    }

    public WineDescriptor getWineDescriptor(){
        return wineDescriptor;
    }

    public void setWineDescriptor(WineDescriptor wineDescriptor){
        this.wineDescriptor = wineDescriptor;
    }

}
