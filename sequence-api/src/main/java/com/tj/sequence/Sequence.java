package com.tj.sequence;

/**
 * sequence interface
 * 
 * @author Tom jiang
 * 
 */
public interface Sequence {

	/**
	 * get next value of sequence(@param name).
	 * 
	 * @return next sequence value
	 * @throws SequenceException
	 */
	public Long nextValue(String name) throws SequenceException;
}
