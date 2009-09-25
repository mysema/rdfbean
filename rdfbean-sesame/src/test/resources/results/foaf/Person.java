package foaf;
@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Person extends wordnet.Person {
    @Predicate(ln="currentProject")
    private owl.Thing foafCurrentProject;
    @Predicate(ln="family_name")
    private String foafFamily_name;
    @Predicate(ln="firstName")
    private String foafFirstName;
    @Predicate(ln="geekcode")
    private String foafGeekcode;
    @Predicate(ln="img")
    private Image foafImg;
    @Predicate(ln="interest")
    private Document foafInterest;
    @Predicate(ln="knows")
    private Person foafKnows;
    @Predicate(ln="myersBriggs")
    private String foafMyersBriggs;
    @Predicate(ln="pastProject")
    private owl.Thing foafPastProject;
    @Predicate(ln="plan")
    private String foafPlan;
    @Predicate(ln="publications")
    private Document foafPublications;
    @Predicate(ln="schoolHomepage")
    private Document foafSchoolHomepage;
    @Predicate(ln="surname")
    private String foafSurname;
    @Predicate(ln="topic_interest")
    private owl.Thing foafTopic_interest;
    @Predicate(ln="workInfoHomepage")
    private Document foafWorkInfoHomepage;
    @Predicate(ln="workplaceHomepage")
    private Document foafWorkplaceHomepage;
}
