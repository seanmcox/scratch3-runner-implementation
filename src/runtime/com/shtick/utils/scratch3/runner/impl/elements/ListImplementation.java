/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.shtick.utils.scratch3.runner.core.elements.List;
import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class ListImplementation implements List{
	private String listName;
	private java.util.List<Object> contents;
	private ScratchRuntimeImplementation runtime;
	
	/**
	 * @param listName
	 * @param contents
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param visible
	 * @param runtime 
	 */
	public ListImplementation(String listName, java.util.List<Object> contents, ScratchRuntimeImplementation runtime) {
		super();
		this.listName = listName;
		this.contents = new ArrayList<>(contents);
		this.runtime = runtime;		
	}
	
	private ListImplementation(ListImplementation listImplementation) {
		synchronized(listImplementation) {
			this.listName = listImplementation.listName;
			this.contents = new ArrayList<>();
			synchronized(listImplementation.contents) {
				for(Object content:listImplementation.contents)
					this.contents.add(content);
			}
		}
	}

	@Override
	public String getListName() {
		return listName;
	}

	@Override
	public Object[] getContents() {
		synchronized(contents) {
			return contents.toArray();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Object> iterator() {
		return Collections.unmodifiableList(contents).iterator();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItem(int)
	 */
	@Override
	public Object getItem(int index) {
		return contents.get(index-1);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteItem(int)
	 */
	@Override
	public void deleteItem(int index) {
		synchronized(contents) {
			contents.remove(index-1);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteAll()
	 */
	@Override
	public void deleteAll() {
		synchronized(contents) {
			contents.clear();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#setItem(java.lang.Object, int)
	 */
	@Override
	public void setItem(Object item, int index) {
		synchronized(contents) {
			contents.set(index-1, item);
		}		
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#addItem(java.lang.Object, int)
	 */
	@Override
	public void addItem(Object item, int index) {
		synchronized(contents) {
			contents.add(index-1, item);
		}
	}

	@Override
	public synchronized void addItem(Object item) {
		synchronized(contents) {
			contents.add(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItemCount()
	 */
	@Override
	public int getItemCount() {
		return contents.size();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object item) {
		return contents.contains(item);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ListImplementation(this);
	}
}
