package ph.edu.tsu.tour.web.common.validator;

public final class Validators {

    private Validators() {
        throw new AssertionError("Intentionally unimplemented");
    }

    public static final String VALID_PLACE_NAME_PATTERN =
            "^([a-zA-Z\\u0080-\\u024F]+(?:. |-| |'))*[a-zA-Z\\u0080-\\u024F]*$";
    public static final String VALID_NAME_PATTERN = "^\\p{L}+[\\p{L}\\p{Pd}\\p{Zs}']*\\p{L}+$|^\\p{L}+$";
    public static final String VALID_USERNAME_PATTERN = "\\p{Alnum}+";

}
