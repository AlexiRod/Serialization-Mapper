package org.hse.rodionov208.classes;

import ru.hse.homework4.Exported;

@Exported
public class TreeNode {
    TreeNode left;
    TreeNode right;

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getRight() {
        return right;
    }

    public TreeNode() {}

    public TreeNode(TreeNode left, TreeNode right) {
        this.left = left;
        this.right = right;
    }
}
