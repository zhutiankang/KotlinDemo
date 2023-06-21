package base.coroutine

import java.util.Timer
import java.util.TimerTask


/**
 * FlowTime
 *
 * @author tiankang
 * @description:
 * Android 倒计时一般实现方式：

handler+postDelayed() 方式
Timer + TimerTask + handler 方式
ScheduledExecutorService + handler 方式  scheduleAtFixedRate周期性，不用等任务完成  scheduleWithFixedDelay周期性，等待任务完成
RxJava 方式
CountDownTimer 方式
有了协程和Flow,我们可以借助Flow这个工具,更加优雅地实现这个需求功能
 1. while 循环 for循环
 2. flow emit
 3. repeat
4. delay,协程作用域随时开启lifecycleScope.launchWhenResumed  参考GitHubActivity
 * @date :2023/6/21 14:07
 */

//flow 定时任务
//fun countDown
/**
 * 开始
 */
//fun startTimer() {
// if (timer == null) {
//  timer = Timer()
// }
// if (timerTask == null) {
//  timerTask = object : TimerTask() {
//   override fun run() {
////    val message = Message()
////    message.what = 2
////    handler.sendMessage(message)
//   }
//  }
// }
// if (timer != null && timerTask != null) {
//  timer.schedule(timerTask, 0, 2000)
// }
//}
//
///**
// * 暂停定时器
// */
//fun stopTimer() {
// if (timer != null) {
//  timer.cancel()
//  timer = null
// }
// if (timerTask != null) {
//  timerTask.cancel()
//  timerTask = null
// }
//}

//倒计时CountDownTimer
//每过1000毫秒执行一次onTick
//倒计时完成执行onFinish
//CountDownTimer timer = new CountDownTimer(5000, 1000){
// @Override
// public void onTick(long sin) {
//  Toast.makeText(MainActivity.this, "" + sin/1000, Toast.LENGTH_SHORT).show();
// }
//
// @Override
// public void onFinish() {
//  Toast.makeText(MainActivity.this, "倒计时完成", Toast.LENGTH_SHORT).show();
// }
//};
////开始
//timer.start();
////暂停
//if (timer != null) {
// timer.cancel();
// timer = null;
//}