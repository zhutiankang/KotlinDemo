package base.coroutine

import kotlinx.coroutines.Job

/**
 * FourCycleLife
 *
 * @author tiankang
 * @description: Job协程的句柄 协程生命周期、结构化并发
 * Job作用：1.使用 Job 监测协程的生命周期状态 2.使用 Job 操控协程。
 * @date :2022/12/12 17:37
 */

// launch、async 的时候，我们知道它们两个返回值类型分别是 Job 和 Deferred 不管是 launch 还是 async，它们本质上都会返回一个 Job 对象
public interface Deferred<out T> : Job {
    public suspend fun await(): T
}