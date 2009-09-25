package wine;
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
}
