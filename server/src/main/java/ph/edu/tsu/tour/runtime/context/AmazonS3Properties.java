package ph.edu.tsu.tour.runtime.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
@EqualsAndHashCode
class AmazonS3Properties {

    @Value("${storage.amazon.s3.secret-key}")
    private String secretKey;

    @Value("${storage.amazon.s3.access-key}")
    private String accessKey;

    @Value("${storage.amazon.s3.region}")
    private String region;

    @Value("${storage.amazon.s3.endpoint}")
    private String endpoint;

    @Value("${storage.amazon.s3.max-retries}")
    private Integer maxRetries;

}
