package org.aksw.dcat_suite.app.vaadin.view;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.vaadin.flow.data.provider.hierarchy.TreeData;

// Source: https://vaadin.com/forum/thread/18481340/refresh-a-treegrid-while-preserving-the-collapse-expand-state
public class TreeDataSynchronizer {

    public static <T> void sync(TreeData<T> from, TreeData<T> to) {
        Queue<T> queue = new LinkedList<>();
        queue.add(null); // root node

        while (! queue.isEmpty()) {
            T currentItem = queue.poll();
            List<T> expectedChildren = from.getChildren(currentItem);
            update(to, currentItem, expectedChildren);
            queue.addAll(expectedChildren);
        }
    }

    private static <T> void update(TreeData<T> td, T parent, List<T> listOfExpectedChildren) {
        Queue<T> expectedChildren = new LinkedList<>(listOfExpectedChildren);
        Queue<T> currentChildren = new LinkedList<>(td.getChildren(parent));

        while (!expectedChildren.isEmpty()) {
            T expectedChild = expectedChildren.poll();
            T currentChild = currentChildren.poll();
            while (!Objects.equals(currentChild, expectedChild)) {
                if (currentChild == null) {
                    td.addItem(parent, expectedChild);
                    break;
                }
                td.removeItem(currentChild);
                currentChild = currentChildren.poll();
            }
        }

        if (expectedChildren.isEmpty()) {
            currentChildren.forEach(td::removeItem);
        }

    }
}