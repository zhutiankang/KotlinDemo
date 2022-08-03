package base

/**
 * EngitGenerics
 *
 * @author tiankang
 * @description: 泛型
 * @date :2022/7/1 14:57
 */
class EightGenerics {

    open class TV {

    }

    class XiaoMiTV1 : TV() {

    }

    class XiaoMiTV2 : TV() {

    }

    class Controller<T> {
        fun turnOn(tv: T) {}
        fun turnOff(tv: T) {}
    }

    fun main() {
        val mi1Controller = Controller<XiaoMiTV1>()
//        mi1Controller.turnOn()
    }

    class Controller2<T : TV> {
        fun turnOn(tv: T) {}
        fun turnOff(tv: T) {}
    }

    fun <T> turnOn(tv: TV) {

    }

    fun <T> turnOff(tv: TV) {}

    fun turnOnAll(mi1: XiaoMiTV1, mi2: XiaoMiTV2) {
        // 泛型实参自动推导
        // ↓
//        turnOn(mi1)
//        turnOn(mi2)
    }

    //编译器会认为MutableList与MutableList之间不存在任何继承关系，它们也无法互相替代，
    //这样就不会出现前面提到的两种问题。这就是泛型的不变性
    //就需要泛型的逆变与协变
    //传入 in，传出 out。或者，我们也可以说：泛型作为参数的时候，用 in，泛型作为返回值的时候，用 out
    //@UnsafeVariance
    //型变的口诀：泛型作为参数，用 in；泛型作为返回值，用 out。在特殊场景下，同时作为参数和返回值的泛型参数，我们可以用 @UnsafeVariance 来解决型变冲突。
    //星投影，就是当我们对泛型的具体类型不感兴趣的时候，直接传入一个“星号”作为泛型的实参
    //直接拿泛型来模拟真实世界的场景，建立类比的关系
    //用万能遥控器，类比泛型；用买遥控器的场景，类比逆变；用点外卖的场景，类比协变、星投影

    //逆变 买遥控器 Controller<TV>是Controller<XiaoMiTV1>的子类  父子关系颠倒
    fun buy(controller: Controller<XiaoMiTV1>) {
        val xiaoMiTV1 = XiaoMiTV1()
        controller.turnOn(xiaoMiTV1)
    }

    fun main2() {
        val controller = Controller<TV>()
        // 传入万能遥控器，报错
//        buy(controller)
        buy2(controller)
    }

    // 使用处型变
    fun buy2(controller: Controller<in XiaoMiTV1>) {
        val xiaoMiTV1 = XiaoMiTV1()
        controller.turnOn(xiaoMiTV1)
    }

    // 声明处型变
    class Controller3<in T> {
        fun turnOn(tv: T) {}
    }

    //协变 送外卖 Restaurant<KFC>可以看作是Restaurant<Food>的子类 父子关系一致
    open class Food {}
    class KFC : Food() {}
    class Restaurant<T> {
//        fun orderFood(): T {
//
//        }
    }

    //一家随便类型的饭店
    fun orderFood(restaurant: Restaurant<Food>) {
        // 从这家饭店，点一份外卖
//        val food = restaurant.orderFood()
    }

    fun main3() {
        val kfc = Restaurant<KFC>()
        //传入的是肯德基 类型不匹配
//        orderFood(kfc)
    }

    //使用处型变
    fun orderFood2(restaurant: Restaurant<out Food>) {
        // 从这家饭店，点一份外卖
//        val food = restaurant.orderFood()
    }
    //声明处型变
    class Restaurant2< out T> {
//        fun orderFood(): T {
//
//        }
    }

    //星投影 用“星号”作为泛型的实参 当我们不关心实参到底是什么的时候
//    fun <T> findRestaurant(): Restaurant<T> {}
//    fun findRestaurant2(): Restaurant<*> {}
    fun main4() {
//        val restaurant = findRestaurant() // 注意这里
//        val food: Any? = restaurant.orderFood() // 返回值可能是：任意类型
    }
    class Restaurant3< out T : Food> {
//        fun orderFood(): T {
//
//        }
    }
    fun main5() {
//        val restaurant = findRestaurant() // 注意这里
//        val food: Food = restaurant.orderFood() // 返回值可能是：任意类型
    }
    //函数传入参数的时候，并不一定就意味着写入，这时候，即使泛型 T 是作为参数类型，我们也仍然要想一些办法来用 out 修饰泛型
    //并没有产生写入的行为 可以通过 @UnsafeVariance 这样的注解，来让编译器忽略这个型变冲突的问题
    //              协变
//                    ↓
    public interface List<out E> : Collection<E> {
        //                                泛型作为返回值
//                                       ↓
        public operator fun get(index: Int): E
        //                                           泛型作为参数
//                                                 ↓
        override fun contains(element: @UnsafeVariance E): Boolean
        //                                        泛型作为参数
//                                              ↓
        public fun indexOf(element: @UnsafeVariance E): Int
    }


    //                           逆变   协变
//                            ↓     ↓
    abstract class BaseSingleton<in P, out T> {
        //①处还有一个 instance 是用泛型 T 修饰的。而它是 var 定义的成员变量
        //为什么可以用协变的泛型 T 呢？其实，这是因为它是 private 的
        //                        ①
        @Volatile//           ↓
        private var instance: T? = null
        //                              参数  返回值
        //                               ↓    ↓
        protected abstract val creator: (P)-> T

        //                    参数 返回值
        //                     ↓   ↓
        fun getInstance(param: P): T =
            instance ?: synchronized(this) {
                instance ?: creator(param).also { instance = it }
            }
    }
}