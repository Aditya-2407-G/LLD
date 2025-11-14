/*file system is used for organizing, storing and managing files and directories on storage devices */


/*requirements - 
 * 
 * heirarchical structure -> file directories and subdirectories -> tree structure
 * file attribures -> metadata (size, creation date name)
 * directory management -> directories contain files and other directories
 * file operations -> CRUD
 * handle file types -> csv, java, mp4
 */


 /*
  * key components -
  1) File System Node -> TrieNode -> base entity in file system 
  2) File Class -> represent individual files, store file specific metadata and content
  3) directory class -> manage collections of files and subdirectories
  4) File System Manager -> central manager for file system operations
  */

  /* composite pattern for hierarchical structure */

  //how trie works-> root node represents the root directory
  // each subsequent level represents subsequent paths and become a child node 
  // directory nodes can have multiple childrens 
  // file nodes are always leaf nodes
  // each node contains metadata about the file -> metadata like size, creation date, type


  //implementation -> 

  /*file system node */

  public abstract class FileSystemNode {
    private String name;
    private Date createdAt;
    private Date modifiedAt;
    private Map<String, FileSystemNode> children; // for directory nodes

    public FileSystemNode(String name) {
        this.name = name;
        this.createdAt = new Date();
        this.modifiedAt = new Date();
        this.children = new HashMap<>();
    

    /// add child node 
    
    public void addChild(FileSystemNode child) {
        children.put(child.getName(), child);
        this.modifiedAt = new Date();
    }
    public bool hasChild(String name) {
        return children.containsKey(name);
    }   
    public FileSystemNode getChild(String name) {
        return children.get(name);
    }

    public boolean removeChild(String name) {
        if (children.containsKey(name)) {
            children.remove(name);
            this.modifiedAt = new Date();
            return true;
        }
        return false;
    }
  }
}

  // file concrete class 

  public class File extends FileSystemNode {

    private String content;
    private String fileType; // e.g., txt, jpg, mp4

    public File(String name, String fileType) {
        super(name);
        this.fileType = fileType;
        this.content = "";
    }

    private Stirng extractFileType(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return "unknown";
    }

    public void setContent(String content) {
        this.content = content;
        this.modifiedAt = new Date();

    }


    public String getContent() {
        return content;
    }

    public String getFileType() {
        return fileType;
    }

    public void display(int depth) {

        // we are doing this to indent based on depth in the tree
        // eg if depth is 2, we want to indent by 4 spaces
        String indent = " ".repeat(depth * 2);
        System.out.println(indent + "- " + getName() + " (File, Type: " + fileType + ")");
    }
  }

  // directory concrete class

  public class Directory extends FileSystemNode {

    public Directory(String name) {
        super(name);
    }

    public void display(int depth) {
        String indent = " ".repeat(depth * 2);
        System.out.println(indent + "+ " + getName() + " (Directory)");
        
        // go into all of the children and display them as well 
        for (FileSystemNode child : children.values()) {
            child.display(depth + 1);
        }
    }
  }

  // file system manager


  public class FileSystem {
    private FileSystemNode root;

    public FileSystem() {
        this.root = new Directory("/");
    }

    public boolean isValidFilePath(String path) {
        return path != null && path.startsWith("/");
    }


    public boolean createPath(Stirng path) {
        if (!isValidFilePath(path)) {
            return false;
        }

        String[] parts = path.split("/");
        FileSystemNode current = root;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (!current.hasChild(part)) {
                // if we are at the last part, create a file, else create a directory
                FileSystemNode newNode;
                if (i == parts.length - 1) {
                    newNode = new File(part, "txt"); // default to txt file
                } else {
                    newNode = new Directory(part);
                }
                current.addChild(newNode);
            }
            current = current.getChild(part);
        }
        return true;
    }

    public String getParentPath(String path) {

        int lastSlashIndex = path.lastIndexOf('/');

        if(lastSlashIndex <= 0) {
            return "/";
        }

        return path.substring(0, lastSlashIndex);
    }

    public String deletePath(String path) {
        if (!isValidFilePath(path) || path.equals("/")) {
            return false;
        }

        String parentPath = getParentPath(path);
        String nodeName = path.substring(path.lastIndexOf('/') + 1);

        FileSystemNode parentNode = navigateToNode(parentPath);
        if (parentNode != null && parentNode.removeChild(nodeName)) {
            return true;
        }
        return false;
    }

    public FileSystemNode getNode(String path) {
        if (!isValidFilePath(path)) {
            return null;
        }

        return navigateToNode(path);
    }

    public navigateToNode(String path) {
        String[] parts = path.split("/");
        FileSystemNode current = root;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (!current.hasChild(part)) {
                return null;
            }
            current = current.getChild(part);
        }
        return current;
    }

    public bool setFileContent(String path, String content) {
        FileSystemNode node = getNode(path);
        if (node instanceof File) {
            ((File) node).setContent(content);
            return true;
        }
        return false;
    }

    public String getFileContent(String path) {
        FileSystemNode node = getNode(path);
        if (node instanceof File) {
            return ((File) node).getContent();
        }
        return null;
    }
  }