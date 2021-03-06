package garbagemule.util.syml;

import java.util.List;

/**
 * The SymlEmitter realizes the {@link SymlVisitor} interface by
 * recursively appending the SYML representations of all the SymlNodes
 * in a tree to a StringBuilder.
 * 
 * <p>The approach requires that a call to the getText() method be made
 * to get the emitted tree. The returned String can then be written
 * directly to a file, which can then be parsed using a {@link SymlLexer}
 * to first generate a {@link TokenStream}, and then a {@link SymlParser}
 * to populate an empty {@link SymlNode} with the data.
 * 
 * <p>The SymlEmitter is utilized by the {@link SymlConfig} class when
 * saving the tree to disk.
 * 
 * @author garbagemule
 * @version 0.1
 */
public class SymlEmitter implements SymlVisitor<Void> {
    private String indent;
    private StringBuilder buffy;
    private String newline;
    
    /**
     * Create a new, fresh SymlEmitter. 
     */
    public SymlEmitter() {
        indent = "";
        buffy = new StringBuilder();
        newline = "\n";
    }
    
    /**
     * Create a new, fresh SymlEmitter and immediately
     * visit the given SymlNode. 
     * @param node the node to visit
     */
    public SymlEmitter(SymlNode node) {
        this();
        node.accept(this);
    }
    
    @Override
    public Void visitNode(SymlNode node) {
        appendComment(node.getComment());
        
        if (!node.getName().equals("")) {
            append(indent + node.getName() + ":");
        
            String oldIndent = indent;
            indent += "    ";
        
            for (SymlNode child : node.getChildren()) {
                child.accept(this);
            }
            indent = oldIndent;
        } else {
            for (SymlNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitLeaf(SymlNode node) {
        appendComment(node.getComment());
        
        Object d = node.get();
        if (d != null) {
            if (d instanceof List<?>) {
                append(indent + node.getName() + ":");
                for (Object o : (List<?>) d) {
                    append(indent + "- " + o);
                }
            } else {
                append(indent + node.getName() + ": " + d);
            }
        } else {
            append(indent + node.getName() + ":");
        }
        return null;
    }
    
    private void appendComment(String comment) {
        // Skip null comments
        if (comment == null) {
            return;
        }
        
        // If the comment is simply a newline, insert it and quit
        // This allows us to have linebreaks between nodes
        if (comment.equals(newline)) {
            append("");
            return;
        }
        
        // Split into multiple lines
        for (String part : comment.split(newline)) {
            // Don't use indents for pure newlines
            append(part.equals("") ? "" : indent + "#" + part);
        }
        
        // If comment ends in newline, remember it, because split() doesn't!
        if (comment.endsWith(newline + newline)) {
            append("");
        }
    }
    
    private void append(String s) {
        buffy.append(s).append(newline);
    }
    
    /**
     * Get the text document generated by the visitor.
     * @return the generated document
     */
    public String getText() {
        return buffy.toString();
    }
}
