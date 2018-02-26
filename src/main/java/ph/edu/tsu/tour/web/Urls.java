package ph.edu.tsu.tour.web;

public interface Urls {

    String ACCESS_MANAGEMENT = "/access-management";
    String ADMINISTRATOR = ACCESS_MANAGEMENT + "/administrator";

    String LOCATION = "/location";
    String CHURCH_LOCATION = LOCATION + "/church";
    String MAP = "/map";
    String USER = "/user";
    String TOUR = "/tour";

    String REST_PREFIX_V1 = "/api/v1";
    String REST_V1_LOCATION = REST_PREFIX_V1 + LOCATION;
    String REST_V1_CHURCH_LOCATION = REST_PREFIX_V1 + CHURCH_LOCATION;
    String REST_V1_MAP = REST_PREFIX_V1 + MAP;
    String REST_V1_USER = REST_PREFIX_V1 + USER;
    String REST_V1_TOUR = REST_PREFIX_V1 + TOUR;

}
