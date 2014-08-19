package com.tj.sequence;

/**
 * sequence interface
 * 
 * @author Tom jiang
 * 
 */
public interface Sequence {

	/**
	 * get next sequence value
	 * 
	 * @return next sequence value
	 * @throws SequenceException
	 */
	public Long nextValue(String name) throws SequenceException;
}
