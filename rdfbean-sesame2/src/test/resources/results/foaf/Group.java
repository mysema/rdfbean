package foaf;
@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Group extends Agent {
    @Predicate(ln="member")
    private Agent foafMember;
}
