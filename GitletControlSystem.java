package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Represents the Gitlet version-control system.
 *  @author Brandon Byrne
 */
public class GitletControlSystem implements Serializable {
    /** creates a GitletControlSystem. */
    GitletControlSystem() throws IOException {
        File cwd = new File(System.getProperty("user.dir"));
        _path = Utils.join(cwd, ".gitlet");
        if (_path.exists()) {
            GitletException g = new GitletException("A Gitlet version-control"
                    + " system already exists in the current directory");
            System.out.println(g.getMessage());
            return;
        }
        _path.mkdir();
        File stage = Utils.join(_path, "stage");
        stage.mkdir();
        _stage = stage;
        File blobs = Utils.join(_path, "blobs");
        blobs.mkdir();
        _blobs = blobs;
        File removal = Utils.join(_path, "removal");
        removal.mkdir();
        _removal = removal;
        File branches = Utils.join(_path, "branches");
        branches.mkdir();
        _branches = branches;
        File commits = Utils.join(_path, "commits");
        commits.mkdir();
        _commits = commits;
        Commit c = new Commit("initial commit", null, null, "master", this);
        File cFile = Utils.join(_commits, c.msg() + c.sha1());
        cFile.createNewFile();
        Utils.writeObject(cFile, c);
        Branch b = new Branch("master", c, null, this);
        _master = b;
        _activeBranch = b;
        _active = b.head();
        _staged = new ArrayList<String>();
        File g = Utils.join(_path, "GitletObject");
        g.createNewFile();
        _branchNames = new ArrayList<>();
        _branchNames.add("master");
        Utils.writeObject(g, this);
    }
    /** adds a FILE to staging area. */
    public void add(String file) throws IOException {
        File f = new File(file);
        if (!f.exists()) {
            GitletException g = new GitletException("F"
                    + "ile does not exist.");
            System.out.println(g.getMessage());
            return;
        }
        File inRemoval = Utils.join(removal(), file);
        if (inRemoval.exists()) {
            inRemoval.delete();
            return;
        }
        String sha1f = Utils.sha1(Utils.readContentsAsString(f) + file);
        File newfile = Utils.join(stage(), file);
        Commit head = getCommit(active(), activeBranch().name());
        for (String sha1 : head.blobs()) {
            if (sha1.equals(sha1f) && file.equals(head.shaToFile().get(sha1))) {
                if (newfile.exists()) {
                    newfile.delete();
                }
                return;
            }
        }
        if (!newfile.exists()) {
            newfile.createNewFile();
        }
        Utils.writeContents(newfile, Utils.readContentsAsString(f));
        _staged.add(sha1f);
        _tracked.add(file);
    }
    /** creates a new commit
     * with MSG and PARENT2. */
    public void commit(String msg, Commit parent2) throws IOException {
        if (Utils.plainFilenamesIn(_stage).size() == 0
            && Utils.plainFilenamesIn(removal()).size() == 0) {
            GitletException g = new GitletException("No"
                    + " changes added to the commit.");
            System.out.println(g.getMessage());
            return;
        }
        Commit parent = getCommit(active(), activeBranch().name());
        Commit c = new Commit(msg, parent,
                parent2, activeBranch().name(), this);
        File f = Utils.join(branches(), activeBranch().name());
        File f1 = Utils.join(f, c.sha1());
        f1.createNewFile();
        Utils.writeObject(f1, c);
        _active = c.sha1();
        File cFile = Utils.join(commits(), msg + c.sha1());
        cFile.createNewFile();
        Utils.writeObject(cFile, c);
        tracked().clear();
        File r;
        for (String s : Utils.plainFilenamesIn(removal())) {
            r = Utils.join(removal(), s);
            r.delete();
        }
        activeBranch().setHead(_active);
        File meta = Utils.join(f, "branchMeta");
        Utils.writeObject(meta, activeBranch());
        File f2 = Utils.join(path(), "GitletObject");
        Utils.writeObject(f2, this);
    }
    /** creates new branch named NAME. */
    public void branch(String name) throws IOException {
        for (String s : _branchNames) {
            if (s.equals(name)) {
                GitletException g1 =  new GitletException("A "
                        + "branch with that name already exists.");
                System.out.println(g1.getMessage());
                return;
            }
        }
        new Branch(name, this);
        _branchNames.add(name);
    }
    /** removes a branch named NAME. */
    public void rmBranch(String name) {
        if (name.equals(activeBranch().name())) {
            GitletException g1 = new GitletException("Cannot "
                    + "remove the current branch.");
            System.out.println(g1.getMessage());
            return;
        }
        File f = Utils.join(branches(), name);
        if (!f.exists()) {
            GitletException g2 = new GitletException("A "
                    + "branch with that name does not exist.");
            System.out.println(g2.getMessage());
            return;
        }
        File f1;
        for (String s : Utils.plainFilenamesIn(f)) {
            f1 = Utils.join(f, s);
            f1.delete();
        }
        f.delete();
        _branchNames.remove(name);
        File g = Utils.join(path(), "GitletObject");
        Utils.writeObject(g, this);
    }
    /** stages FILE for removal. */
    public void remove(String file) throws IOException {
        boolean error = true;
        File f = Utils.join(stage(), file);
        if (f.exists()) {
            error = false;
            f.delete();
            tracked().remove(file);
        }
        File f1 = new File(file);
        Commit head = getCommit(active(), activeBranch().name());
        if (head.tracked().contains(file)) {
            error = false;
            File r = Utils.join(removal(), file);
            if (!r.exists()) {
                r.createNewFile();
            }
            if (f1.exists()) {
                f1.delete();
            }
        }
        if (error) {
            GitletException g = new GitletException("No"
                    + " reason to remove the file.");
            System.out.println(g.getMessage());
        }
    }
    /**returns log of active commit's history. */
    public String log() {
        String r = "";
        Commit c = getCommit(active(), activeBranch().name());
        while (c != null) {
            r = r + c.toString();
            c = getCommit(c.parent());
        }
        return r;
    }
    /** returns lof of BRANCH's commit history.
     * @return String */
    public String log(String branch) {
        String r = "";
        File branchFolder = Utils.join(branches(), branch);
        for (String commit : Utils.plainFilenamesIn(branchFolder)) {
            Commit c = getCommit(commit, branch);
            r = r + c.toString();
        }
        return r;
    }
    /** returns log of every commit made.
     * @return String*/
    public String globalLog() {
        String r = "";
        for (String file : Utils.plainFilenamesIn(commits())) {
            File f = Utils.join(commits(), file);
            Commit c = Utils.readObject(f, Commit.class);
            r = r + c.toString();
        }
        return r;
    }
    /** finds all commits with message MSG.
     * @return String */
    public String find(String msg) {
        String r = "";
        for (String s : Utils.plainFilenamesIn(commits())) {
            if (s.contains(msg)) {
                File f = Utils.join(commits(), s);
                Commit c = Utils.readObject(f, Commit.class);
                r = r + c.sha1() + "\n";
            }
        }
        return r;
    }
    /** checkout FILE from commit with hash COMMIT.*/
    public void checkoutFile(String file, String commit) {
        Commit head = getCommit(commit);
        if (head == null) {
            GitletException g = new GitletException("No"
                    + " commit with that id exists.");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        if (!head.fileToSha().containsKey(file)) {
            GitletException g = new GitletException("File"
                    + " does not exist in that commit.");
            System.out.println(g.getMessage());
            return;
        }
        String sha1Blob = head.fileToSha().get(file);
        Blob blob = getBlobInBlobs(sha1Blob);
        File cwd = new File(System.getProperty("user.dir"));
        File replace = Utils.join(cwd, file);
        Utils.writeContents(replace, blob.contents());
    }
    /** checkout BRANCH. */
    public void checkoutBranch(String branch) {
        File cwd = new File(System.getProperty("user.dir"));
        File branchFolder = Utils.join(branches(), branch);
        Commit oldHead = getCommit(active(), activeBranch().name());
        if (!branchFolder.exists()) {
            GitletException g = new GitletException("No "
                    + "such branch exists.");
            System.out.println(g.getMessage());
            return;
        }
        if (activeBranch().name().equals(branch)) {
            GitletException g = new GitletException("No "
                    + "need to checkout the current branch.");
            System.out.println(g.getMessage());
            return;
        }
        List<String> stage = Utils.plainFilenamesIn(stage());
        for (String s : Utils.plainFilenamesIn(cwd)) {
            if (!oldHead.tracked().contains(s)
                && !stage.contains(s)) {
                GitletException g = new GitletException("There is an"
                        + " untracked file in the way; "
                        + "delete it or add and commit it first.");
                System.out.println(g.getMessage());
                return;
            }
        }
        File branchMeta = Utils.join(branchFolder, "branchMeta");
        Branch b = Utils.readObject(branchMeta, Branch.class);
        Commit head = getCommit(b.head(), branch);
        for (String s : head.tracked()) {
            checkoutFile(s, head.sha1());
        }
        for (String s : oldHead.shaToFile().values()) {
            if (!head.fileToSha().containsKey(s)) {
                File f = Utils.join(cwd, s);
                f.delete();
            }
        }
        tracked().clear();
        _activeBranch = b;
        _active = b.head();
        for (File f : stage().listFiles()) {
            f.delete();
        }
    }
    /** resets to commit with HASH.*/
    public void reset(String hash) {
        Commit head = null;
        Commit oldHead = getCommit(active(), activeBranch().name());
        for (String s : Utils.plainFilenamesIn(commits())) {
            if (s.contains(hash)) {
                File f = Utils.join(commits(), s);
                head = Utils.readObject(f, Commit.class);
                break;
            }
        }
        if (head == null) {
            GitletException g1 = new GitletException("No"
                    + " commit with that id exists.");
            System.out.println(g1.getMessage());
            System.exit(0);
        }
        File cwd = new File(System.getProperty("user.dir"));
        for (String s : Utils.plainFilenamesIn(cwd)) {
            if (!oldHead.tracked().contains(s)
                    && head.tracked().contains(s)) {
                GitletException g = new GitletException("There is an "
                        + "untracked file in the way;"
                        + " delete it or add and commit it first.");
                System.out.println(g.getMessage());
                return;
            }
            if (!head.tracked().contains(s)) {
                File f = Utils.join(cwd, s);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        for (String s : head.tracked()) {
            checkoutFile(s, head.sha1());
        }
        for (String s : oldHead.shaToFile().values()) {
            if (!head.fileToSha().containsKey(s)
                    && !head.tracked().contains(s)) {
                File f = Utils.join(cwd, s);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        tracked().clear();
        _activeBranch = getBranch(head.branch());
        _active = head.sha1();
        for (File f : stage().listFiles()) {
            f.delete();
        }
        _activeBranch.setHead(head.sha1());
        File f1 = Utils.join(branches(), activeBranch().name());
        File meta = Utils.join(f1, "branchMeta");
        Utils.writeObject(meta, activeBranch());
    }
    /** returns status of this control system. */
    public String status() {
        Commit head = getCommit(active(), activeBranch().name());
        File cwd = new File(System.getProperty("user.dir"));
        String x = System.lineSeparator();
        String r = "=== Branches ===\n";
        String[] order = new String[_branchNames.size()];
        arrange(_branchNames, order);
        for (String s : order) {
            if (s.equals(activeBranch().name())) {
                s = "*" + s;
            }
            r = r + s + "\n";
        }
        r = r + "\n=== Staged Files ===\n";
        order = new String[tracked().size()];
        List<String> stage = Utils.plainFilenamesIn(stage());
        arrange(stage, order);
        for (int i = 0; i < order.length; i++) {
            r = r + order[i] + "\n";
        }
        r = r + "\n=== Removed Files ===";
        List<String> removed = Utils.plainFilenamesIn(removal());
        order = new String[removed.size()];
        arrange(removed, order);
        for (int i = 0; i < order.length; i++) {
            r = r + "\n" + order[i];
        }
        r = r + "\n\n=== Modifications Not Staged For Commit ===\n";
        ArrayList<String> modded = helper();
        order = new String[modded.size()];
        arrange(modded, order);
        for (int i = 0; i < order.length; i++) {
            File current = Utils.join(cwd, order[i]);
            if (!current.exists()) {
                r = r + order[i] + " (deleted)\n";
            } else {
                r = r + order[i] + " (modified)\n";
            }
        }
        r = r + "\n=== Untracked Files ===";
        List<String> stage1 = Utils.plainFilenamesIn(stage());
        for (String s : Utils.plainFilenamesIn(cwd)) {
            if (!head.tracked().contains(s) && !stage1.contains(s)) {
                r = r + "\n" + s;
            }
        }
        return r + "\n";
    }
    /** gets called to in a loop
     * to make a list of files
     * that have untracked modifications.
     * @return ArrayList*/
    public ArrayList<String> helper() {
        File cwd = new File(System.getProperty("user.dir"));
        ArrayList<String> shaOfStaged = new ArrayList<>();
        ArrayList<String> modded = new ArrayList<>();
        for (String s : Utils.plainFilenamesIn(stage())) {
            File staged = Utils.join(cwd, s);
            File fileOnStage = Utils.join(stage(), s);
            String x = Utils.readContentsAsString(fileOnStage) + s;
            String sha = Utils.sha1(x);
            if (!staged.exists()) {
                modded.add(s);
            } else {
                String y = Utils.readContentsAsString(staged) + s;
                String cwdVersion = Utils.sha1(y);
                if (!cwdVersion.equals(sha)) {
                    modded.add(s);
                }
            }
            shaOfStaged.add(sha);
        }
        Commit head = getCommit(active(), activeBranch().name());
        for (String s : head.blobs()) {
            Blob blob = getBlobInBlobs(s);
            File f = Utils.join(cwd, blob.name());
            File remove = Utils.join(removal(), blob.name());
            File stageVersion = Utils.join(stage(), blob.name());
            if (!f.exists() && !remove.exists()) {
                modded.add(blob.name());
            }
            if (!stageVersion.exists() && f.exists()
                    && !Utils.readContentsAsString(f).equals(blob.contents())) {
                modded.add(blob.name());
            }
        }
        return modded;
    }
    /** orders the list of strings in INPUT and puts
     * it into ORDER.
     */
    public void arrange(List<String> input, String[] order) {
        for (String s : input) {
            if (order[0] == null) {
                order[0] = s;
            } else {
                for (int i = 0; i < order.length; i++) {
                    if (s.compareTo(order[i]) < 0) {
                        insert(s, order, i);
                    }
                    if (order[i + 1] == null) {
                        order[i + 1] = s;
                        break;
                    }
                }
            }
        }
    }
    /**inserts String S into array A at position K. */
    public void insert(String s, String[] A, int k) {
        String current = s;
        String next = A[k];
        for (int i = k; i < A.length; i++) {
            if (current == null) {
                break;
            }
            A[i] = current;
            current = next;
            next = A[i + 1];
        }
    }
    /** merges BRANCHNAME with active branch.*/
    public void merge(String branchName) throws IOException {
        mergeErrors(branchName);
        File cwd = new File(System.getProperty("user.dir"));
        Branch b = getBranch(branchName);
        Commit branchHead = getCommit(b.head(), branchName);
        Commit split = ancestor(b.head());
        Commit head = getCommit(active(), activeBranch().name());
        if (split.sha1().equals(branchHead.sha1())) {
            GitletException g = new GitletException("Given branch "
                    + "is an ancestor of the current branch.");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        if (split.sha1().equals(active())) {
            GitletException g = new GitletException("Current "
                    + "branch fast-forwarded.");
            checkoutBranch(branchName);
            System.out.println(g.getMessage());
            System.exit(0);
        }
        boolean conf = false;
        for (String s : head.blobs()) {
            conf = mergeHelper(s, split, branchHead, conf);
        }
        for (String s : branchHead.blobs()) {
            Blob blob = getBlobInBlobs(s);
            String fileName = branchHead.shaToFile().get(s);
            if (!split.tracked().contains(fileName)
                && !head.tracked().contains(fileName)) {
                checkoutFile(fileName, branchHead.sha1());
                add(fileName);
            }
            boolean splitHasFile = split.tracked().contains(blob.name());
            boolean splitHasSameContent = split.blobs().contains(s);
            boolean headHasFile = head.tracked().contains(blob.name());
            if (splitHasFile && !splitHasSameContent && !headHasFile) {
                String contents = "";
                if (headHasFile) {
                    String x = head.fileToSha().get(blob.name());
                    Blob headBlob = getBlobInBlobs(x);
                    contents = headBlob.contents();
                }
                String conflict = "<<<<<<< HEAD\n" + contents
                        + "=======\n" + blob.contents() + "\n>>>>>>>";
                File f1 = Utils.join(cwd, blob.name());
                Utils.writeContents(f1, conflict);
                add(blob.name());
                if (!conf) {
                    conf = true;
                    System.out.println("Encountered a merge conflict.");
                }
            }
        }
        commit("Merged " + branchName
                + " into " + activeBranch().name() + ".", branchHead);
    }
    /** merge helper S, SPLIT is the
     * splitpoint, the head  of the
     * given branch is BRANCHHEAD.
     * CONF is true if a conflict
     * was already encountered.
     * @return boolean*/
    public boolean mergeHelper(String s,
                               Commit split, Commit branchHead, boolean conf)
                            throws IOException {
        File cwd = new File(System.getProperty("user.dir"));
        Blob blob = getBlobInBlobs(s);
        if (split.blobs().contains(s)
                && !branchHead.blobs().contains(s)
                && branchHead.tracked().contains(blob.name())) {
            checkoutFile(blob.name(), branchHead.sha1());
            add(blob.name());
        }
        if (split.blobs().contains(s)
                && !branchHead.tracked().contains(blob.name())) {
            File f = Utils.join(cwd, blob.name());
            if (f.exists()) {
                f.delete();
            }
            remove(blob.name());
        }
        boolean splitHasFile = split.tracked().contains(blob.name());
        boolean splitHasSameContent = split.blobs().contains(s);
        boolean branchHasFile = branchHead.tracked().contains(blob.name());
        boolean branchHasSameContent = branchHead.blobs().contains(s);
        String x = branchHead.fileToSha().get(blob.name());
        if ((splitHasFile && !splitHasSameContent && !branchHasSameContent
                && branchHasFile
                && !split.blobs().contains(x))
                || (!splitHasFile
                && branchHasFile && !branchHasSameContent)
                || (splitHasFile
                && !splitHasSameContent && !branchHasFile)) {
            String content = "";
            if (branchHasFile) {
                String y = branchHead.fileToSha().get(blob.name());
                Blob branchBlob = getBlobInBlobs(y);
                content = branchBlob.contents();
            }
            String conflict = "<<<<<<< HEAD\n" + blob.contents()
                    + "=======\n" + content + ">>>>>>>\n";
            File f1 = Utils.join(cwd, blob.name());
            Utils.writeContents(f1, conflict);
            add(blob.name());
            if (!conf) {
                conf = true;
                System.out.println("Encountered a merge conflict.");
            }
        }
        return conf;
    }
    /** looks for merge errors.
     * BRANCHNAME is given branch in merge.*/
    public void mergeErrors(String branchName) {
        File cwd = new File(System.getProperty("user.dir"));
        if (Utils.plainFilenamesIn(stage()).size() > 0
                || Utils.plainFilenamesIn(removal()).size() > 0) {
            GitletException g = new GitletException("You "
                    + "have uncommitted changes.");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        if (branchName.equals(activeBranch().name())) {
            GitletException g = new GitletException("Cannot"
                    + " merge a branch with itself. ");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        Commit c = getCommit(active(), activeBranch().name());
        List<String> stage = Utils.plainFilenamesIn(stage());
        List<String> removed = Utils.plainFilenamesIn(removal());
        for (String s : Utils.plainFilenamesIn(cwd)) {
            if (!c.tracked().contains(s) && !stage.contains(s)
                && !removed.contains(s)) {
                GitletException g = new GitletException("There"
                        + " is an untracked file in"
                        + " the way; delete it,"
                        + " or add and commit it first.");
                System.out.println(g.getMessage());
                System.exit(0);
            }
        }
    }
    /** finds common ancestor between NAME
     *  and the current one.
     *  @return Commit*/
    public Commit ancestor(String name) {
        Commit c = getCommit(name);
        Commit aHead = getCommit(active(), activeBranch().name());
        ArrayList<String> ancestorsBranch = ancestorDistance(c);
        ArrayList<Commit> front = new ArrayList<>();
        front.add(aHead);
        while (aHead != null) {
            for (int i = 0; i < front.size(); i++) {
                if (ancestorsBranch.contains(front.get(i).sha1())) {
                    return front.get(i);
                }
            }
            int a = front.size();
            for (int i = 0; i < a; i++) {
                Commit holder = front.get(i);
                front.remove(front.get(i));
                front.add(i, getCommit(holder.parent()));
                if (holder.parent2() != null) {
                    front.add(getCommit(holder.parent2()));
                }
            }
        }
        return null;
    }

    /** returns an arraylist of ancestors of START.
     * @return ArrayList*/
    public ArrayList<String> ancestorDistance(Commit start) {
        ArrayList<String> distance = new ArrayList<String>();
        distance.add(start.sha1());
        while (start != null) {
            Commit parent = getCommit(start.parent());
            Commit parent2 = getCommit(start.parent2());
            if (parent != null) {
                distance.add(parent.sha1());
            }
            if (parent2 != null) {
                distance.add(parent2.sha1());
            }
            if (parent2 != null) {
                ArrayList<String> merge = ancestorDistance(parent2);
                for (int i = 0; i < merge.size(); i++) {
                    distance.add(merge.get(i));
                }
            }
            start = parent;
        }
        return distance;
    }
    /** gets BLOB from blobs folder.
     * @return blob*/
    public Blob getBlobInBlobs(String blob) {
        for (String s : Utils.plainFilenamesIn(blobs())) {
            if (blob.contains(s)) {
                File f = Utils.join(blobs(), s);
                return Utils.readObject(f, Blob.class);
            }
        }
        return null;
    }
    /** adds BLOB to blobs directory. */
    public void addBlob(Blob blob) throws IOException {
        File f = Utils.join(blobs(), blob.sha1());
        f.createNewFile();
        Utils.writeObject(f, blob);
    }
    /** loads BRANCH.
     * @return Branch*/
    public Branch getBranch(String branch) {
        File branchFolder = Utils.join(branches(), branch);
        if (!branchFolder.exists()) {
            GitletException g = new GitletException("A "
                    + "branch with that name does not exist.");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        File branchMeta = Utils.join(branchFolder, "branchMeta");
        return Utils.readObject(branchMeta, Branch.class);
    }
    /** load commit in BRANCH folder for SHA1.
     * @return Commit*/
    public Commit getCommit(String sha1, String branch) {
        if (sha1 == null) {
            return null;
        }
        File f = Utils.join(branches(), branch);
        File f1 = Utils.join(f, sha1);
        if (!f1.exists()) {
            GitletException g = new GitletException("No"
                    + " commit with that id exists.");
            System.out.println(g.getMessage());
            System.exit(0);
        }
        return Utils.readObject(f1, Commit.class);
    }
    /** loads commit with hash SHA1.
     * @return Commit */
    public Commit getCommit(String sha1) {
        Commit r = null;
        if (sha1 == null) {
            return null;
        }
        for (String s : Utils.plainFilenamesIn(commits())) {
            if (s.contains(sha1) || s.equals(sha1)) {
                File f = Utils.join(commits(), s);
                r = Utils.readObject(f, Commit.class);
                break;
            }
        }
        return r;
    }
    /**return path to .gitlet. */
    public File path() {
        return _path;
    }
    /** return list of file names in stage. */
    public ArrayList<String> tracked() {
        return _tracked;
    }
    /** returns file that holds blobs. */
    public File blobs() {
        return _blobs;
    }
    /** returns the staging directory. */
    public File stage() {
        return _stage;
    }
    /** returns the removal staging directory. */
    public File removal() {
        return _removal;
    }
    /** returns branches folder. */
    public File branches() {
        return _branches;
    }
    /** returns branches folder. */
    public Branch activeBranch() {
        return _activeBranch;
    }
    /** returns branches folder. */
    public Branch master() {
        return _master;
    }
    /** returns sha1 of head commit. */
    public String active() {
        return _active;
    }
    /** returns list of branch names. */
    public ArrayList<String> branchNames() {
        return _branchNames;
    }
    /** returns folder of commits. */
    public File commits() {
        return _commits;
    }
    /** staging area. */
    private File _stage;
    /** a sha1 of the active head. */
    private String _active;
    /** the path to this gitlet directory. */
    private File _path;
    /** list of sha1 codes of the files in staging area. */
    private ArrayList<String> _staged;
    /** directory of files staged for removal. */
    private File _removal;
    /** branches directory. */
    private File _branches;
    /** active branch. */
    private Branch _activeBranch;
    /** master branch. */
    private Branch _master;
    /** file that holds blobs. */
    private File _blobs;
    /** list of name of files in stage. */
    private ArrayList<String> _tracked = new ArrayList<>();
    /** list of names of branches. */
    private ArrayList<String> _branchNames;
    /** folder that holds each commit ever made. */
    private File _commits;
}
