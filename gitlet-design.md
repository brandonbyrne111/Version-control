# Gitlet Design Document

**Name**: Brandon Byrne

## Classes and Data Structures
**GITLET CONTROL SYSTEM:**
This class will hold general information regarding the overall
state of gitlet.

FIELDS

1. A reference to the most current commit on the master branch.
2. A folder of files staged for committing.
3. A list of all branches.
4. A folder of files staged for removal.
5. A list of all branches.

**BRANCH:** 
A branch of the Gitlet control system.

FIELDS

1. Name of Branch.
2. The branch that this branch diverges from.
3. The head of this branch.

**COMMITS:**
This class will represent a node in the tree of commits and
will contain all the information related to a particular commit.

FIELDS
1. Reference to parent commit node.
2. Time of commit creation.
3. The node's SHA-1 hash code.
4. Commit's message.
5. List of SHA-1 codes of the blobs the commit took a snapshot
of when the commit was made.
6. list of names of files this commit has blobs for.


**BLOBS:**
This class will record the state of an added file when a commit 
was made.

FIELDS
1. Name of file it holds.
2. The contents of the file represented as a string.
3. The blob's file's SHA-1 code.


## Algorithms
****GITLET CONTROL SYSTEM:****
1. GitletControlSystem():
Creates a folder in the current working directory called gitlet. In
that folder will be a folder called branches, one called stage, one
called removal, and one called blobs. The master branch will be made
and have one commit with no blobs.
2. add(String args): creates a blob to represent the state
of the file and adds it to the staging folder.

3. rm(String name): adds file named name to the folder that holds 
the files to be removed.
4. log(Commit head): Calls head's toString  method and if
head has a parent recursively calls log on head's parent.
5. global-log(): loops through list of branches and calls log 
on the head of each one.
6. find(String message): loops through each branch and loops
through commits in each branch checking for every commit with
the message MESSAGE and adds their hash code to a list that's returned.
7. status

###Commit
1. Commit(String message, Commit parent): calls the commit constructor passing in
the message, and the previous commit in the branch. The program will
loop through staging area and make a list of the names and sha1 codes
of the blobs in the folder. Then the list of file names the parent 
commit has blobs for will be looped through. In that loop the list of 
names of blobs in the staging areas will be looped through
 and if the file name doesn't appear then the corresponding
sha1 code will be passed onto the child commit (the sha1 code of the
blob a commit has for a file name will have the same index number that 
the file name has in the list). If the name is in the removal stage
area nothing is done, if it's found in the add stage area then that blob
and file name are passed onto the child commit and the blob is removed
from the staging area and copied into the file that stores blobs.

****


## Persistence
1. When files are added the program will copy the file into the staging
area.
2. When the gitlet control system is instantiated it'll create a folder
 for the branches, staging area, and all blobs referenced by commits.
3. Whenever a commit is instantiated a new folder is made with the 
 name of the folder being the SHA-1 hashcode of the commit in the 
in a right branch folder in the branches folder in the 
gitlet folder which will hold the serialization of a commit.
4. When a new branch is made a new folder with the name 
 of the folder being the name 
 inputted by the user in the branches folder in 
 gitlet direcotry is created
with the first commit inside. Inside of the new branch folder
in branches is a file that the branch object will be serialized
into so that the branch object can persist.
5. When a commit is made the files in the staging area will be 
made into blobs and a file will hold the serialized blobs with the
name of the file being the sha1 code of the contents of file the 
blob represents.
6. When a merge is made and the algorithm has determined 
what is the fate of each file based off the rules stated in the 
spec a new folder will be made in the master branch for the 
new commit and the appropriate files in the appropriate state 
will be copied into the folder.
7. 



