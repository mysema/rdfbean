package foaf;
@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Document extends wordnet.Document {
    @Predicate(ln="primaryTopic")
    private owl.Thing foafPrimaryTopic;
    @Predicate(ln="sha1")
    private Object foafSha1;
    @Predicate(ln="topic")
    private owl.Thing foafTopic;
}
