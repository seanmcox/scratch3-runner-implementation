/**
 * 
 */
package com.shtick.utils.scratch3.runner.standard.blocks.util;

import java.util.Iterator;

import com.shtick.utils.scratch3.runner.core.elements.List;

/**
 * @author Sean
 *
 */
public class AllBadList implements List {
	@Override
	public Iterator<Object> iterator() {
		throw new UnsupportedOperationException("Called iterator when not expected.");
	}
	
	@Override
	public void setItem(Object item, int index) {
		throw new UnsupportedOperationException("Called setItem when not expected.");
	}
	
	@Override
	public String getListName() {
		throw new UnsupportedOperationException("Called getListName when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getContents()
	 */
	@Override
	public Object[] getContents() {
		throw new UnsupportedOperationException("Called getContents when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItem(int)
	 */
	@Override
	public Object getItem(int index) {
		throw new UnsupportedOperationException("Called getItem when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteItem(int)
	 */
	@Override
	public void deleteItem(int index) {
		throw new UnsupportedOperationException("Called deleteItem when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteAll()
	 */
	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Called deleteAll when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#addItem(java.lang.Object, int)
	 */
	@Override
	public void addItem(Object item, int index) {
		throw new UnsupportedOperationException("Called addItem when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#addItem(java.lang.Object)
	 */
	@Override
	public void addItem(Object item) {
		throw new UnsupportedOperationException("Called addItem when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItemCount()
	 */
	@Override
	public int getItemCount() {
		throw new UnsupportedOperationException("Called getItemCount when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object item) {
		throw new UnsupportedOperationException("Called contains when not expected.");
	}
}
