package com.tj.sequence.interal;

import com.tj.sequence.SequenceException;

public interface SequenceDao {
	/**
	 * get next sequence range
	 * 
	 * @param name
	 * @return
	 * @throws SequenceException
	 */
	public SequenceRange nextRange(String name) throws SequenceException;
}
