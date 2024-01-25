package base.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * EventManage
 * stateFlow与sharedFlow 都是热流
 * StateFlow 如果你需要在多个组件之间共享整个状态，使用 StateFlow。订阅的同时，就会把当前的状态发送给你，回调给你，自来水
 * SharedFlow 如果你更关心事件的传播，每个订阅者只关心在其订阅之后发生的事件，那么使用 SharedFlow 更合适。 订阅之后，等有新消息的时候，才会回调给你
 *
 * @author tiankang
 * @description: 数据通信
 * @date :2024/1/24 17:44
 */
object EventManager {

    private val bluetoothFlow = MutableSharedFlow<String>()
    fun sendBluetoothEvent(message:String) {
        CoroutineScope(Dispatchers.Main).launch{
            bluetoothFlow.emit(message)
        }
    }
    fun getBluetoothFlow():SharedFlow<String> = bluetoothFlow

    // 需要给初始状态
    private val networkFlow = MutableStateFlow("")
    fun sendNetworkEvent(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            networkFlow.emit(message)
        }
    }
    fun getNetworkFlow():StateFlow<String> = networkFlow

    // 需要给初始状态
    private val bluetoothFlow2 = MutableStateFlow("")
    fun sendBluetoothEvent2(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            bluetoothFlow2.emit(message)
        }
    }
    fun getBluetoothFlow2():StateFlow<String> = bluetoothFlow2
}