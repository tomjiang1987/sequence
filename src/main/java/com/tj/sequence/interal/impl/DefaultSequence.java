package com.tj.sequence.interal.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;

import com.tj.sequence.Sequence;
import com.tj.sequence.SequenceException;
import com.tj.sequence.interal.SequenceDao;

public class DefaultSequence implements Sequence {
	private final Lock lock = new ReentrantLock();
	private SequenceDao sequenceDao;
	private final Map<String, SequenceInstance> instances = new ConcurrentHashMap<String, SequenceInstance>();

	@Override
	public Long nextValue(String name) throws SequenceException {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("sequence name can't be null!");
		}

		SequenceInstance instance = instances.get(name);
		if (instance == null) {
			lock.lock();
			try {
				instance = instances.get(name);
				if (instance == null) {
					instance = new SequenceInstance(name, sequenceDao);
					instances.put(name, instance);
				}
			} finally {
				lock.unlock();
			}
		}

		return instance.getNext();
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

}
