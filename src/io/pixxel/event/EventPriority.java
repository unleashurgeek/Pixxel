package io.pixxel.event;

public enum EventPriority {
	
	LOW(1),
	NORMAL(2),
	HIGH(3),
	HIGHEST(4);
	
	private final int level;
	
	private EventPriority(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
