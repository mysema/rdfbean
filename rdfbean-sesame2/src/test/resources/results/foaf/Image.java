package foaf;
@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Image extends wordnet.Document {
    @Predicate(ln="depicts")
    private owl.Thing foafDepicts;
    @Predicate(ln="thumbnail")
    private Image foafThumbnail;
}
