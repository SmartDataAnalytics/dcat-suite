package org.aksw.dcat_suite.app.vaadin.view;

import com.vaadin.flow.component.grid.Grid;

public class VaadinGridUtils {
	
	/**
	 * By default, upon showing an item detail vaadin hides all other details.
	 * 
	 * Solution based on: https://vaadin.com/forum/thread/17536985/multiple-grid-lines-detail
	 * 
	 * @param <T>
	 * @param grid
	 */
	public static <T> void allowMultipleVisibleItemDetails(Grid<T> grid) {
		grid.setDetailsVisibleOnClick(false);
		grid.addItemClickListener(ev -> {
			if (ev.getItem() != null) {
				T obj = ev.getItem();
				//if (toShow.hasSomethingToShow()) {
					grid.setDetailsVisible(obj, !grid.isDetailsVisible(obj));
				//}
			}
		});
	}
}
