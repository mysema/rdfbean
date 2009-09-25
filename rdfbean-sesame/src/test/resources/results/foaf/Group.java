package foaf;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Group extends Agent {

    @Predicate(ln="member")
    private Agent foafMember;

    public Agent getFoafMember(){
        return foafMember;
    }

    public void setFoafMember(Agent foafMember){
        this.foafMember = foafMember;
    }

}
