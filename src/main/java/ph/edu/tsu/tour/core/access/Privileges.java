package ph.edu.tsu.tour.core.access;

public interface Privileges {

    interface PointOfInterest {

        String PREFIX = "POI";
        String WRITE = PREFIX + "_" + "WRITE";

    }

    interface Access {

        String PREFIX = "ACCESS";
        String READ = PREFIX + "_" + "READ";
        String WRITE = PREFIX + "_" + "WRITE";

    }

}
