package gitlet;

import java.io.File;
import java.io.Serializable;

/** Represents a Blob.
 *  @author Brandon Byrne
 */
public class Blob implements Serializable {
    /** makes blob representing
     * F named NAME.
     */
    Blob(File f, String name) {
        _file = f;
        _contents = Utils.readContentsAsString(f);
        _name = name;
        _sha1 = Utils.sha1(_contents + name);
    }
    /** gets the contents that this blobs stores.
     * @return contents of file when blob was made.*/
    public String contents() {
        return _contents;
    }
    /** gets sha1 for the file of this blob.
     * @return sha1 of contents of file.*/
    public String sha1() {
        return _sha1;
    }
    /** gets name of the file this blob represents.
     * @return names of file. */
    public String name() {
        return _name;
    }
    /** the file this blob represents.*/
    private File _file;
    /** name of file this file represents. */
    private String _name;
    /** sha1 code of this file. */
    private String _sha1;
    /** contents of file as a string. */
    private String _contents;

}
