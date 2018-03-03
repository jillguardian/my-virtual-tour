package ph.edu.tsu.tour.runtime.mvc.formatter;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.Year;
import java.util.Locale;

public class YearFormatter implements Formatter<Year> {

    @Override
    public Year parse(String s, Locale locale) throws ParseException {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return Year.parse(s);
    }

    @Override
    public String print(Year year, Locale locale) {
        return year == null ? null : year.toString();
    }

}
