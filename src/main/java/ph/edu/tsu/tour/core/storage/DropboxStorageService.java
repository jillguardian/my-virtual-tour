package ph.edu.tsu.tour.core.storage;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxUploader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxClientV2Base;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;

public class DropboxStorageService implements StreamingStorageService<URI, URI> {

    private static Logger logger = LoggerFactory.getLogger(DropboxStorageService.class);

    /**
     * Client (for user endpoints).
     */
    private DbxClientV2Base dropboxClient;

    public DropboxStorageService(String accessToken, String clientIdentifier) {
        this.dropboxClient = new DbxClientV2(createRequestConfig(clientIdentifier, 3, null), accessToken);
    }

    public DropboxStorageService(String accessToken, String clientIdentifier, int maxRetries, Locale locale) {
        this.dropboxClient = new DbxClientV2(createRequestConfig(clientIdentifier, maxRetries, locale), accessToken);
    }

    protected DbxRequestConfig createRequestConfig(String clientIdentifier, int maxRetries, Locale locale) {
        DbxRequestConfig.Builder builder = DbxRequestConfig.newBuilder(clientIdentifier);
        if (maxRetries >= 0) {
            builder.withAutoRetryEnabled(maxRetries);
        } else {
            builder.withAutoRetryDisabled();
        }
        builder.withUserLocaleFrom(locale);

        return builder.build();
    }

    @Override
    public void write(URI uri, byte[] bytes) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        inputStream = new BufferedInputStream(inputStream);
        DbxUploader uploader = dropboxClient.files().upload(uri.toASCIIString());
        uploader.uploadAndFinish(inputStream);
    }

    @Override
    public void delete(URI uri) throws Exception {
        dropboxClient.files().delete(uri.toASCIIString());
    }

    @Override
    public URI getStream(URI uri) throws Exception {
        String publicUrl;
        Optional<SharedLinkMetadata> existingSharedLink = dropboxClient.sharing().listSharedLinksBuilder()
                .withPath(uri.toASCIIString())
                .withDirectOnly(true)
                .start()
                .getLinks().stream()
                .filter(metadata ->
                        metadata.getLinkPermissions().getRequestedVisibility() == RequestedVisibility.PUBLIC)
                .findFirst();
        if (existingSharedLink.isPresent()) {
            publicUrl = existingSharedLink.get().getUrl();
        } else {
            SharedLinkMetadata sharedLink = dropboxClient.sharing()
                    .createSharedLinkWithSettings(uri.toASCIIString(), SharedLinkSettings.newBuilder()
                            .withRequestedVisibility(RequestedVisibility.PUBLIC)
                            .build());
            publicUrl = sharedLink.getUrl();
        }

        return new URIBuilder(publicUrl)
                .removeQuery()
                .addParameter("dl", "1") // Direct link
                .build();
    }
}
