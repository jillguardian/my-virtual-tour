package ph.edu.tsu.tour.runtime.mvc.converter;

import org.springframework.core.convert.converter.Converter;
import ph.edu.tsu.tour.core.map.Profile;

public class ProfileConverter implements Converter<String, Profile> {

    @Override
    public Profile convert(String source) {
        return Profile.valueOf(source.toUpperCase());
    }

}
