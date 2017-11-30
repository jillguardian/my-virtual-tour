package ph.edu.tsu.tour.web;

public interface Urls {

    String LOCATION = "/location";
    String ACCESS_MANAGEMENT = "/access-management";
    String MAP = "/map";
    String USER = "/user";
    String TOUR = "/tour";

    String REST_PREFIX_V1 = "/api/v1";
    String REST_V1_LOCATION = REST_PREFIX_V1 + LOCATION;
    String REST_V1_MAP = REST_PREFIX_V1 + MAP;
    String REST_V1_USER = REST_PREFIX_V1 + USER;
    String REST_V1_TOUR = REST_PREFIX_V1 + TOUR;

}
