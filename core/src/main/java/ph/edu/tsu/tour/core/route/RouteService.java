package ph.edu.tsu.tour.core.route;

import java.util.List;
import java.util.Set;

public interface RouteService<S, D> {

    D getNearestDestination(Profile profile, S source, Set<D> destinations);

    List<D> sortDestinations(Profile profile, S source, Set<D> destinations);

}
