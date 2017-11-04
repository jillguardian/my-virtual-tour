package ph.edu.tsu.tour.web;

public interface Urls {

    String POI = "/poi";
    String ACCESS_MANAGEMENT = "/access-management";
    String MAP = "/map";
    String USER = "/user";

    String REST_PREFIX = "/api";
    String REST_POI = REST_PREFIX + POI;
    String REST_MAP = REST_PREFIX + MAP;
    String REST_USER = REST_PREFIX + USER;

}
