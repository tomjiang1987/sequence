package com.tj.sequence.interal;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceRange {
	private final Long start;
	private final Long end;
	private final Integer step;
	private final AtomicLong value;
	private volatile boolean isRunOut = false;

	public SequenceRange(Long start, Long end, Integer step) {
		this.start = start;
		this.end = end;
		this.step = step;
		this.value = new AtomicLong(start);
	}

	public Long getNext() {
		for (;;) {
			Long currentValue = value.get();
			Long nextValue = currentValue + step;
			if (nextValue > end) {
				isRunOut = true;
				return -1L;
			}
			
			if (value.compareAndSet(currentValue, nextValue)) {
				return currentValue;
			}
		}
	}

	public boolean isRunOut() {
		return isRunOut;
	}

	public Long getStart() {
		return start;
	}

	public Long getEnd() {
		return end;
	}

}
