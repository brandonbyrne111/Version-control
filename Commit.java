package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/** Represents the Gitlet version-control system.
 *  @author Brandon Byrne
 */
public class Commit implements Serializable {
    /** creates commit with MSG, PARENT, PARENT2
     *BRANCH, and G.
     */
    Commit(String msg, Commit parent, Commit parent2,
           String branch, GitletControlSystem G) throws IOException {
        if (Utils.plainFilenamesIn(G.stage()).size() == 0
            && Utils.plainFilenamesIn(G.removal()).size() == 0
            && parent != null) {
            GitletException g = new GitletException("No"
                    + " changes added to the commit.");
            System.out.println(g.getMessage());
            return;
        }
        _path = G.path();
        _stage = G.stage();
        if (parent2 != null) {
            _parent2 = parent2.sha1();
        } else {
            parent2 = null;
        }
        _branch = branch;
        _g = G;
        _msg = msg;
        if (parent != null) {
            _parent = parent.sha1();
        } else {
            _parent = null;
        }
        _tracked = new ArrayList<String>();
        _blobs = new ArrayList<String>();
        _fileToSha = new HashMap<String, String>();
        _ShaToFile = new HashMap<String, String>();
        _time = new GregorianCalendar();
        if (parent == null) {
            _time = _begining;
            _sha1 = Utils.sha1(Utils.serialize(this));
            return;
        }
        for (String s : Utils.plainFilenamesIn(_stage)) {
            File f = Utils.join(G.stage(), s);
            Blob b = new Blob(f, s);
            _blobs.add(b.sha1());
            f.delete();
            _fileToSha.put(s, b.sha1());
            _ShaToFile.put(b.sha1(), s);
            G.addBlob(b);
            _tracked.add(s);
        }
        for (String s : parent.tracked()) {
            if (!tracked().contains(s)
                    && !Utils.plainFilenamesIn(g().removal()).contains(s)) {
                _tracked.add(s);
                String sha = parent.fileToSha().get(s);
                _blobs.add(sha);
                _fileToSha.put(s, sha);
                _ShaToFile.put(sha, s);
            }
        }
        _sha1 = Utils.sha1(Utils.serialize(this));
    }
    /** reperesents commit as String for log.
     * @return String*/
    public String toString() {
        Formatter f = new Formatter();
        if (_parent2 == null) {
            String r = "===\n"
                       + "commit " + sha1() + "\n"
                       + "Date: " + String.format("%1$ta %1$tb %1$te"
                       + " %1$tH:%1$tM:%1$tS %1$tY", _time) + " -0800\n"
                       + _msg + "\n\n";
            return r;
        }
        String r = "===\n"
                + "commit " + sha1() + "\n"
                + "Merge: " + parent().subSequence(0, 7) + " "
                + parent2().subSequence(0, 7) + "\n"
                + "Date: " + String.format("%1$ta %1$tb %1$te"
                + " %1$tH:%1$tM:%1$tS %1$tY", _time) + " -0800\n"
                + _msg + "\n\n";
        return r;
    }
    /** returns this commit's message. */
    public String msg() {
        return _msg;
    }
    /** returns hashmap fileToSha. */
    public HashMap<String, String> shaToFile() {
        return _ShaToFile;
    }
    /** returns hashmap fileToSha.
     * @return HashMap */
    public HashMap<String, String> fileToSha() {
        return _fileToSha;
    }
    /** returns list of tracked files.
     * @return ArrayList*/
    public ArrayList<String> tracked() {
        return _tracked;
    }
    /** returns _blobs of this commit.
     * @return ArrayList<String> */
    public ArrayList<String> blobs() {
        return _blobs;
    }
    /** get this branch's gitletcontrolSYstem.
     * @return gitletcontrolSystem*/
    public GitletControlSystem g() {
        return _g;
    }
    /** get sha1 code for this commit.
     * @return String */
    public String sha1() {
        return _sha1;
    }
    /** get branch name for this commit.
     * @return String */
    public String branch() {
        return _branch;
    }
    /** get sha1 of parent commit.
     * @return String*/
    public String parent() {
        return _parent;
    }
    /** get sha1 of parent2 commit.
     * @return String*/
    public String parent2() {
        return _parent2;
    }

    /** time of creation. */
    private GregorianCalendar _time;

    /** message of commit. */
    private String _msg;

    /** parent of commit. */
    private String _parent;

    /** list of sha1 codes of blobs in commit's snapshot. */
    private ArrayList<String> _blobs;

    /** list of names of sha1 of files this commit is tracking. */
    private ArrayList<String> _tracked;
    /** path to .gitlet. */
    private File _path;
    /** the staging area. */
    private File _stage;
    /** a hash map of a name of a file and the sha1 of its
     * contents when this commit was made.
     */
    private HashMap<String, String> _fileToSha;
    /** a haah map from sha1 codes to file names. */
    private HashMap<String, String> _ShaToFile;
    /** returns this commit's gitletControlSystem. */
    private GitletControlSystem _g;
    /** sha1 of this commit. */
    private String _sha1;
    /** branch this commit is on. */
    private String _branch;
    /** second parent.*/
    private String _parent2;
    /** _time is assigned to this
     * gregorian calendar object.
     */
    private final GregorianCalendar _begining =
            new GregorianCalendar(1970, Calendar.JANUARY, 1, 0, 0, 0);
}
