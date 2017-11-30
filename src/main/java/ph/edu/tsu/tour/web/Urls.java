package ph.edu.tsu.tour.web;

public interface Urls {

    String LOCATION = "/location";
    String ACCESS_MANAGEMENT = "/access-management";
    String MAP = "/map";
    String USER = "/user";

    String REST_PREFIX = "/api";
    String REST_LOCATION = REST_PREFIX + LOCATION;
    String REST_MAP = REST_PREFIX + MAP;
    String REST_USER = REST_PREFIX + USER;

}
