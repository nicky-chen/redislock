package com.nicky.redislock;


public interface SeckillInterface {
	@CacheLock(lockedPrefix="TEST_PREFIX")
	void secKill(String arg1,@LockedObject Long arg2);
}
