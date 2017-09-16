package org.apache.commons.vfs2.provider.s3;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

public class AmazonS3FilenameParser extends AbstractFileNameParser {

    private static final AmazonS3FilenameParser INSTANCE = new AmazonS3FilenameParser();

    private AmazonS3FilenameParser() { }

    @Override
    public FileName parseUri(VfsComponentContext context, FileName base, String uri) throws FileSystemException {
        // TODO: Append base name.
        StringBuilder name = new StringBuilder();
        String scheme = UriParser.extractScheme(uri, name);
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);
        FileType fileType = UriParser.normalisePath(name);
        final String bucket = UriParser.extractFirstElement(name);
        return new AmazonS3Filename(scheme, bucket, name.toString(), fileType);
    }

    public static AmazonS3FilenameParser getInstance() {
        return AmazonS3FilenameParser.INSTANCE;
    }

}
