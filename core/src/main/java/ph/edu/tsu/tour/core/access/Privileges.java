package ph.edu.tsu.tour.core.access;

public interface Privileges {

    interface Location {

        String PREFIX = "LOCATION";
        String WRITE = PREFIX + "_" + "WRITE";

    }

    interface Access {

        String PREFIX = "ACCESS";
        String READ = PREFIX + "_" + "READ";
        String WRITE = PREFIX + "_" + "WRITE";

    }

    interface User {

        String PREFIX = "USER";
        String WRITE = PREFIX + "_" + "WRITE";

    }

}
