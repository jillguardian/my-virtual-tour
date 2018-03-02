package ph.edu.tsu.tour;

public final class Project {

    private static final String NAME = "${project.parent.name}";
    private static final String VERSION = "${project.parent.version}";
    private static final String DESCRIPTION = "${project.parent.description}";

    public static String getName() {
        return NAME;
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getDescription() {
        return DESCRIPTION;
    }

}
