package org.apache.commons.vfs2.provider.dropbox;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

/**
 * What VFS expects from a typical cloud storage client.
 */
public interface DropboxClientWrapper {

    /**
     * Checks if the entry in the given path is a file.
     *
     * @param path path of the entry to check
     * @return {@literal true} if file
     */
    boolean isFile(String path);

    /**
     * Checks if the entry in the given path is a folder.
     *
     * @param path path of the entry to check
     * @return {@literal true} if folder
     */
    boolean isFolder(String path);

    /**
     * Get the size of the file represented by the given path.
     *
     * @param path path of the file to check
     * @return file size; {@literal -1} if unknown
     */
    long getSize(String path);

    /**
     * Gets the last time the file was modified.
     *
     * @param path the file to check
     * @return the last date of modification; {@code null} if unknown
     */
    Date getLastModifiedDate(String path);

    /**
     * Renames an entry.
     *
     * @param oldPath the current path
     * @param newPath the new path
     * @return {@literal true} if rename operation was successful
     */
    boolean rename(String oldPath, String newPath);

    /**
     * Creates a folder
     *
     * @param path path of the folder to create
     * @return {@literal true} if folder was successfully created
     */
    boolean createFolder(String path);

    /**
     * Lists the names of the entries under a folder identified by the given path.
     *
     * @param path path of the folder whose contents is to be listed
     * @return the relative names of the entries under this folder; empty if entry is without children
     */
    Collection<String> listChildren(String path);

    /**
     * Returns an input stream of a file specified by the given path.
     *
     * @param path the file to download
     * @return the input stream of the downloaded file; {@code null} if unsupported
     */
    InputStream getInputStream(String path);

    /**
     * Returns an output stream which could be used to write new data. Ideal for upload operations.
     *
     * @param path the path of the entry
     * @return the output stream where data shall be written; {@code null} if unsupported
     */
    OutputStream getOutputStream(String path);

    /**
     * Deletes the entry identified by the given path.
     *
     * @param path the path of the entry to delete
     * @return {@literal true} if delete operation was successfully deleted
     */
    boolean delete(String path);

    /**
     * Returns a URL used for streaming the content of a specified file (also known as direct links).
     *
     * @param path the path of the file
     * @return link to the file's content stream
     */
    URL getDirectUrl(String path);

}
