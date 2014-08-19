package com.tj.sequence.interal.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tj.sequence.SequenceException;
import com.tj.sequence.interal.SequenceDao;
import com.tj.sequence.interal.SequenceRange;

public class SequenceInstance {
	private final Lock lock = new ReentrantLock();
	private String name;
	private SequenceDao sequenceDao;
	private volatile SequenceRange currentRange;

	public SequenceInstance(String name,SequenceDao sequenceDao) throws SequenceException {
		this.name = name;
		this.sequenceDao = sequenceDao;
	}

	public Long getNext() throws SequenceException {
		if (currentRange == null) {
			lock.lock();
			try {
				if (currentRange == null) {
					currentRange = sequenceDao.nextRange(name);
				}
			} finally {
				lock.unlock();
			}
		}

		long value = currentRange.getNext();

		if (value == -1) {
			lock.lock();
			try {
				for (;;) {
					if (currentRange.isRunOut()) {
						currentRange = sequenceDao.nextRange(name);
					}

					value = currentRange.getNext();
					if (value == -1) {
						continue;
					}
					break;
				}
			} finally {
				lock.unlock();
			}
		}

		if (value < 0) {
			throw new SequenceException("Sequence value overflow, value = " + value);
		}

		return value;
	}

}
