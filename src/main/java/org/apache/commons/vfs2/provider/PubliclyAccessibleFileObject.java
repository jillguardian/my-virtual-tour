package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileObject;

import java.net.URI;
import java.net.URL;

public interface PubliclyAccessibleFileObject extends FileObject {

    /**
     * @return direct link to the file
     */
    URI getPubliclyAccessibleUri();

}
