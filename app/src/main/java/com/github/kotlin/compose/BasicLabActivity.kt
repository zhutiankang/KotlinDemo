package com.github.kotlin.compose

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.github.kotlin.R
import com.github.kotlin.compose.ui.theme.BasicsCodelabTheme

/**
 * LazyColumn 和 LazyRow 相当于 Android View 中的 RecyclerView。
 * LazyColumn 不会像 RecyclerView 一样回收其子级。它会在您滚动它时发出新的可组合项，并保持高效运行，因为与实例化 Android Views 相比，发出可组合项的成本相对较低。
 */
class BasicLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicsCodelabTheme {
                // A surface container using the 'background' color from the theme
                // 可以重复使用 MyApp 可组合项，您就可以省去 onCreate 回调和预览，从而避免重复编写代码
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun Greeting2(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        // remember 可以起到保护作用，防止状态在重组时被重置
        // remember 函数仅在可组合项包含在组合中时起作用 列表划出去就会重置 展开第 1 项内容，然后滚动到第 20 项内容，再返回到第 1 项内容，您会发现第 1 项内容已恢复为原始尺寸 如果需要，您可以使用 rememberSaveable 保存此数据
        var expanded by remember { mutableStateOf(false) }
        // 想探索不同类型的动画，请尝试为 spring 提供不同的参数，尝试使用不同的规范（tween、repeatable）和不同的函数（animateColorAsState
        val extraPadding by animateDpAsState(
            targetValue = if (expanded) 48.dp else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        Row(modifier = Modifier.padding(24.dp)) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = extraPadding.coerceAtLeast(0.dp)) //确保内边距不会为负数，否则可能会导致应用崩溃
            ) {
                Text(text = "Hello,")
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
            ElevatedButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "show less" else "Show more")
            }
        }
    }
}


// 可组合项
@Composable
private fun MyApp(
    modifier: Modifier = Modifier
) {
    // 您可以使用 rememberSaveable，而不使用 remember。这会保存每个在配置更改（如旋转）和进程终止后保留下来的状态。
    var shouldShowOnBoarding by rememberSaveable { mutableStateOf(true) }
    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        if (shouldShowOnBoarding) {
            onBoardingScreen(onContinueClicked = { shouldShowOnBoarding = false })
        } else {
            Greetings()
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = List(1000) { "$it" }
) {
//    Column(modifier = Modifier.padding(vertical = 4.dp)) {
//        for (name in names) {
//            Greeting2(name = name)
//        }
//    }
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
//            Greeting2(name = name)
            greeting(name = name)
        }
//        names.map {
//            item {
//                Greeting2(name = it)
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun greeting(name: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        CardContent(name)
    }
}

@Composable
fun CardContent(name: String) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Text(text = "Hello,")
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            if (expanded) {
                Text(
                    text = ("Composem ipsum color sit lazy, " +
                            "padding theme elit, sed do bouncy. ").repeat(4),
                )
            }
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(id = R.string.show_less)
                } else {
                    stringResource(R.string.show_more)
                }
            )
        }
    }
}

@Composable
private fun onBoardingScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    //shouldShowOnboarding 使用的是 by 关键字，而不是 =。这是一个属性委托，可让您无需每次都输入 .value。
//    var shouldShowOnBoarding by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to the Basics Codelab!")
        Button(modifier = Modifier.padding(24.dp), onClick = onContinueClicked) {
            Text(text = "Continue")
        }
    }
}

@Preview(showBackground = true, widthDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun DefaultPreview2() {
    BasicsCodelabTheme {
        Greetings()
    }
}

@Preview
@Composable
fun MyAppPreview() {
    BasicsCodelabTheme {
        MyApp(modifier = Modifier.fillMaxSize())
    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun onBoardingPreview() {
    BasicsCodelabTheme {
        // 将 onContinueClicked 分配给空 lambda 表达式就等于“什么也不做”，这非常适合于预览。
        onBoardingScreen(onContinueClicked = {})
    }
}