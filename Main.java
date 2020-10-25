package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Brandon Byrne
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        GitletException g1;
        if (args == null || args.length == 0) {
            g1 = new GitletException("Please enter a command.");
            System.out.println(g1.getMessage());
            System.exit(0);
        }
        boolean error = true;
        File cwd = new File(System.getProperty("user.dir"));
        File f = Utils.join(cwd, ".gitlet");
        File f1 = Utils.join(f, "GitletObject");
        if (args[0].equals("init")) {
            error = false;
            if (f.exists()) {
                g1 = new GitletException("Gitlet version-control "
                        + "system already exists in the current directory.");
                System.out.println(g1.getMessage());
            } else {
                new GitletControlSystem();
            }
        }
        if (!f.exists()) {
            g1 = new GitletException("Not in an initialized Gitlet directory.");
            System.out.println(g1.getMessage());
            System.exit(0);
        }
        GitletControlSystem g = Utils.readObject(f1, GitletControlSystem.class);
        if (args[0].equals("add")) {
            g.add(args[1]);
            error = false;
        }
        if (args[0].equals("commit")) {
            if (args[1].equals("")) {
                g1 = new GitletException("Please enter a commit message.");
                System.out.println(g1.getMessage());
                return;
            }
            g.commit(args[1], null);
            error = false;
        }
        if (args[0].equals("rm")) {
            g.remove(args[1]);
            error = false;
        }
        if (args[0].equals("log")) {
            System.out.println(g.log());
            error = false;
        }
        error = helper(args, g, error);
        Utils.writeObject(f1, g);
        if (error) {
            g1 = new GitletException("No command with that name exists.");
            System.out.println(g1.getMessage());
        }
    }
    /** does stuff with ARGS, G and ERROR.
     * @return boolean */
    public static boolean helper(String[] args, GitletControlSystem g,
                                 boolean error) throws IOException {
        if (args[0].equals("checkout")) {
            error = false;
            if (args.length == 2) {
                g.checkoutBranch(args[1]);
            }
            if (args.length == 3) {
                g.checkoutFile(args[2], g.active());
            }
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    GitletException e = new GitletException("I"
                            + "ncorrect operands.");
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
                g.checkoutFile(args[3], args[1]);
            }
        }
        if (args[0].equals("global-log")) {
            error = false;
            System.out.println(g.globalLog());
        }
        if (args[0].equals("find")) {
            error = false;
            String r = g.find(args[1]);
            if (r.equals("")) {
                GitletException g1 = new GitletException("Found no "
                        + "commit with that message.");
                System.out.println(g1.getMessage());
                System.exit(0);
            }
            System.out.println(r);
        }
        if (args[0].equals("branch")) {
            error = false;
            g.branch(args[1]);
        }
        if (args[0].equals("rm-branch")) {
            error = false;
            g.rmBranch(args[1]);
        }
        if (args[0].equals("reset")) {
            error = false;
            g.reset(args[1]);
        }
        if (args[0].equals("status")) {
            error = false;
            System.out.println(g.status());
        }
        if (args[0].equals("merge")) {
            error = false;
            g.merge(args[1]);
        }
        return error;
    }
}
