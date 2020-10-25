package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** Represents a branch.
 *  @author Brandon Byrne
 */
public class Branch implements Serializable {
    /** creates the master branch for init with
     * NAME, parent FIRST, on PARENT, with G.*/
    Branch(String name, Commit first,
           Branch parent, GitletControlSystem G) throws IOException {
        _g = G;
        _name = name;
        _head = first.sha1();
        if (parent != null) {
            _parentBranch = parent.sha1();
        } else {
            _parentBranch = null;
        }
        _path = Utils.join(G.branches(), name);
        _path.mkdir();
        File c = Utils.join(_path, first.sha1());
        if (!c.exists()) {
            c.createNewFile();
        }
        Utils.writeObject(c, first);
        File branchHolder = Utils.join(_path, "branchMeta");
        branchHolder.createNewFile();
        Utils.writeObject(branchHolder, this);
        _sha1 = Utils.sha1(Utils.serialize(this));
    }
    /** creates new branch called
     * NAME in G. */
    Branch(String name, GitletControlSystem G) throws IOException {
        _g = G;
        _name = name;
        _head = G.active();
        _path = Utils.join(G.branches(), name);
        _path.mkdir();
        _parentBranch = G.activeBranch().sha1();
        File branchHolder = Utils.join(_path, "branchMeta");
        branchHolder.createNewFile();
        Utils.writeObject(branchHolder, this);
        Commit head = G.getCommit(_head, G.activeBranch().name());
        File headF = Utils.join(_path, _head);
        headF.createNewFile();
        Utils.writeObject(headF, head);
        _sha1 = Utils.sha1(Utils.serialize(this));
    }
    /** advance head to HEAD.*/
    public void setHead(String head) {
        _head = head;
    }
    /** gets parent branch.
     * @return string*/
    public String parent() {
        return _parentBranch;
    }
    /** gets path to this branch's folder.
     * @return File */
    public File path() {
        return _path;
    }
    /** gets sha1 of this branch.
     * @return String*/
    public String sha1() {
        return _sha1;
    }

    /** gets name of this branch.
     * @return String */
    public String name() {
        return _name;
    }
    /** gets head of this branch.
     * @return String */
    public String head() {
        return _head;
    }
    /** this branch's sha1.*/
    private String _sha1;
    /** get this branch's gitletcontrolSYstem.
     * @return gitletControlSystem*/
    public GitletControlSystem g() {
        return _g;
    }
    /** holds name of branch. */
    private String _name;

    /** sha 1 of head commit of this branch. */
    private String _head;

    /** sha1 of branch this branch diverged from. */
    private String _parentBranch;

    /** path to this branch folder. */
    private File _path;

    /**returns this branch's GitletControlSystem. */
    private GitletControlSystem _g;

}
