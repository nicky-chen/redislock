# redislock
redis乐观锁与悲观锁demo
乐观锁:基于jedis的watch tansacation的exec 
悲观锁:基于动态代理实现 + 注解 实现 注：悲观锁不建议使用setnx + expire 两步操作，
会发生死锁，建议一步操作，或者校验过期时间的getset操作
