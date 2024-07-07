package org.autojs.autojs.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DrawerState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.aiselp.autojs.codeeditor.EditActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.stardust.app.permission.DrawOverlaysPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.autojs.autojs.Pref
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.external.foreground.ForegroundService
import org.autojs.autojs.timing.TimedTaskScheduler
import org.autojs.autojs.ui.build.ProjectConfigActivity
import org.autojs.autojs.ui.common.ScriptOperations
import org.autojs.autojs.ui.compose.theme.AutoXJsTheme
import org.autojs.autojs.ui.compose.widget.MyIcon
import org.autojs.autojs.ui.compose.widget.SearchBox2
import org.autojs.autojs.ui.explorer.ExplorerViewKt
import org.autojs.autojs.ui.floating.FloatyWindowManger
import org.autojs.autojs.ui.main.bindmachine.BindMachine
import org.autojs.autojs.ui.main.components.DocumentPageMenuButton
import org.autojs.autojs.ui.main.components.LogButton
import org.autojs.autojs.ui.main.drawer.DrawerPage
import org.autojs.autojs.ui.main.scripts.ScriptListFragment
import org.autojs.autojs.ui.main.startup.StartUpFragment
import org.autojs.autojs.ui.main.task.TaskManagerFragmentKt
import org.autojs.autojs.ui.main.web.EditorAppManager
import org.autojs.autojs.ui.util.launchActivity
import org.autojs.autojs.ui.widget.fillMaxSize
import org.autojs.autoxjs.R

data class BottomNavigationItem(val icon: Int, val label: String)

class MainActivity : FragmentActivity() {

    companion object {
        @JvmStatic
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    //private val scriptListFragment by lazy { ScriptListFragment() }
    private val startUpFragment by lazy { StartUpFragment() }
    //private val taskManagerFragment by lazy { TaskManagerFragmentKt() }
    private val bindMachine by lazy { BindMachine() }
    private val webViewFragment by lazy { EditorAppManager() }
    private var lastBackPressedTime = 0L
    private var drawerState: DrawerState? = null
    private val viewPager: ViewPager2 by lazy { ViewPager2(this) }
    private var scope: CoroutineScope? = null
    @OptIn(ExperimentalPermissionsApi::class)//声明这段代码需要使用权限相关的试验性 API
    override fun onCreate(savedInstanceState: Bundle?) {//重写Activity的onCreate方法，这是每个Android应用的生命周期的开始
        super.onCreate(savedInstanceState)//调用父类的onCreate方法，传入系统保存的应用状态
        WindowCompat.setDecorFitsSystemWindows(window, false)//允许内容延伸进系统窗口, 例如状态栏
        Log.i("MainActivity", "Pid: ${Process.myPid()}")//在日志中打印出当前App的进程ID
        if (Pref.isForegroundServiceEnabled()) ForegroundService.start(this)//如果前台服务已开启，就开始前台服务
        else ForegroundService.stop(this)//否则，停止前台服务

        if (Pref.isFloatingMenuShown() && !FloatyWindowManger.isCircularMenuShowing()) {//如果设置中开了悬浮菜单显示，且当前悬浮菜单尚未展示
            if (DrawOverlaysPermission.isCanDrawOverlays(this)) FloatyWindowManger.showCircularMenu()//如果已有显示悬浮窗的权限，就显示悬浮菜单
            else Pref.setFloatingMenuShown(false)//如果没有，则取消显示悬浮菜单的设置
        }
        setContent {//设置应用的UI内容
            scope = rememberCoroutineScope()//创建一个协程作用域
            AutoXJsTheme {//应用主题
                Surface(color = MaterialTheme.colors.background) {//设置主背景色
                    val permission = rememberExternalStoragePermissionsState {//记住应用是否有读写外部存储的权限
                        if (it) {//如果有权限
                           // scriptListFragment.explorerView.onRefresh()//刷新脚本列表 ********************************！！！
                        }
                    }
                    LaunchedEffect(key1 = Unit, block = {//当状态改变或者初始调用时，执行请求权限的操作
                        permission.launchMultiplePermissionRequest()//请求权限
                    })
                    MainPage(//设置主界面
                        activity = this,//当前的Activity
                        startUpFragment = startUpFragment,//脚本列表的Fragment
                        bindMachine = bindMachine,//任务管理的Fragment
                        webViewFragment = webViewFragment,//网页视图的Fragment
                        onDrawerState = {//处理侧拉菜单的状态
                            this.drawerState = it
                        },
                        viewPager = viewPager//ViewPager实例，用于在各个页面之间切换
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TimedTaskScheduler.ensureCheckTaskWorks(application)
    }

    //后退的时候的逻辑
    override fun onBackPressed() {
//        if (drawerState?.isOpen == true) {
//            scope?.launch { drawerState?.close() }
//            return
//        }
//        if (viewPager.currentItem == 0 && startUpFragment.onBackPressed()) {
//            return
//        }
        back()
    }

    private fun back() {
        val currentTime = System.currentTimeMillis()
        val interval = currentTime - lastBackPressedTime
        if (interval > 2000) {
            lastBackPressedTime = currentTime
            Toast.makeText(
                this,
                getString(R.string.text_press_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()
        } else super.onBackPressed()
    }
}

@Composable
fun MainPage(
    activity: FragmentActivity,
    startUpFragment: StartUpFragment,
    bindMachine: BindMachine,
    webViewFragment: EditorAppManager,
    onDrawerState: (DrawerState) -> Unit,
    viewPager: ViewPager2
) {
    val context = LocalContext.current // 获取当前的Android上下文
    val scaffoldState = rememberScaffoldState() // 创建并记住应用的脚手架状态，脚手架管理应用的结构及行为
    onDrawerState(scaffoldState.drawerState) // 对抽屉的状态进行操作
    val scope = rememberCoroutineScope() // 创建并记住一个协程作用域，用于执行后台任务

    // 获取并记住底栏项目
    val bottomBarItems = remember {
        getBottomItems(context)
    }
    // 创建并记住一个表示当前页面的状态
    var currentPage by remember {
        mutableStateOf(0)
    }

    SetSystemUI(scaffoldState) //设置系统UI

    // 是一个使用compose的布局结构，用于创建应用的顶栏、底栏、抽屉和内容
    Scaffold(
        modifier = Modifier.fillMaxSize(), // 充满可用空间
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen, //抽屉手势可用性取决于是否处于打开状态
        topBar = {
            TopAppBar(
                modifier = Modifier.wrapContentHeight(),
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text(
                            stringResource(id = R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            //style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            )
        } ,
        bottomBar = {  //设置底部栏
            Surface(elevation = 4.dp, color = MaterialTheme.colors.surface) {
                Column {
                    BottomBar(bottomBarItems, currentPage, onSelectedChange = { currentPage = it })
                    Spacer(
                        modifier = Modifier
                            .windowInsetsBottomHeight(WindowInsets.navigationBars) //添加底部间距
                    )
                }
            }
        },

//        drawerContent = {
//            DrawerPage() //设置抽屉中的内容
//        },

        ) {
        AndroidView( //可以插入自定义的Android视图
            modifier = Modifier.padding(it), //设置视图内边距
            factory = { //创建视图
                viewPager.apply {
                    fillMaxSize()
                    adapter = ViewPager2Adapter( //设置adapter
                        activity,
                        startUpFragment,
                        bindMachine,
                        webViewFragment
                    )
                    isUserInputEnabled = false //禁止用户输入
                    ViewCompat.setNestedScrollingEnabled(this, true) //允许嵌套滑动
                }
            },
            update = { viewPager0 -> //Unit//更新视图  页面是在这里实现先跳转的
                viewPager0.currentItem = currentPage
            }
        )
    }
}

fun showExternalStoragePermissionToast(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.text_please_enable_external_storage),
        Toast.LENGTH_SHORT
    ).show()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberExternalStoragePermissionsState(onPermissionsResult: (allAllow: Boolean) -> Unit) =
    rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        onPermissionsResult = { map ->
            onPermissionsResult(map.all { it.value })
        })

@Composable
private fun SetSystemUI(scaffoldState: ScaffoldState) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons =
        if (MaterialTheme.colors.isLight) {
            scaffoldState.drawerState.isOpen || scaffoldState.drawerState.isAnimationRunning
        } else false

    val navigationUseDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            Color.Transparent,
            darkIcons = navigationUseDarkIcons
        )
    }
}

private fun getBottomItems(context: Context) = mutableStateListOf(
    BottomNavigationItem(
        R.drawable.ic_home,
        context.getString(R.string.text_home)
    ),
    BottomNavigationItem(
        R.drawable.ic_manage,
        context.getString(R.string.text_management)
    ),
    BottomNavigationItem(
        R.drawable.ic_web,
        context.getString(R.string.text_document)
        //context.getString(R.string.text_document)
    )
)

@Composable
fun BottomBar(
    items: List<BottomNavigationItem>,
    currentSelected: Int,
    onSelectedChange: (Int) -> Unit
) {
    BottomNavigation(elevation = 0.dp, backgroundColor = MaterialTheme.colors.background) {
        items.forEachIndexed { index, item ->
            val selected = currentSelected == index
            val color = if (selected) MaterialTheme.colors.primary else Color.Gray
            BottomNavigationItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onSelectedChange(index)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        tint = color
                    )
                },
                label = {
                    Text(text = item.label, color = color)
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    currentPage: Int,
    requestOpenDrawer: () -> Unit,
    onSearch: (String) -> Unit,
    scriptListFragment: ScriptListFragment,
    webViewFragment: EditorAppManager,
) {
    var isSearch by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    TopAppBar(elevation = 0.dp) {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.high,
        ) {
            if (!isSearch) {
                IconButton(onClick = requestOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(id = R.string.text_menu),
                    )
                }

                ProvideTextStyle(value = MaterialTheme.typography.h6) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.app_name)
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, EditActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "editor"
                        )
                    }
                }
                if (currentPage == 0) {
                    IconButton(onClick = { isSearch = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.text_search)
                        )
                    }
                }
            } else {
                IconButton(onClick = {
                    isSearch = false
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.text_exit_search)
                    )
                }

                var keyword by remember {
                    mutableStateOf("")
                }
                SearchBox2(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = stringResource(id = R.string.text_search)) },
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(keyword)
                    })
                )
                if (keyword.isNotEmpty()) {
                    IconButton(onClick = { keyword = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    }
                }
            }
            LogButton()
            when (currentPage) {
                0 -> {
                    var expanded by remember {
                        mutableStateOf(false)
                    }
                    Box() {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.desc_more)
                            )
                        }
                        TopAppBarMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            scriptListFragment = scriptListFragment
                        )
                    }
                }

                1 -> {
                    IconButton(onClick = { AutoJs.getInstance().scriptEngineService.stopAll() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.desc_more)
                        )
                    }
                }

                2 -> {
                    DocumentPageMenuButton { webViewFragment.swipeRefreshWebView.webView }
                }
            }

        }
    }
}

@Composable
fun TopAppBarMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset.Zero,
    scriptListFragment: ScriptListFragment
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, offset = offset) {
        val context = LocalContext.current
        NewDirectory(context, scriptListFragment, onDismissRequest)//这是右边的+号的
        NewFile(context, scriptListFragment, onDismissRequest)
        ImportFile(context, scriptListFragment, onDismissRequest)
        NewProject(context, scriptListFragment, onDismissRequest)
        DropdownMenuItem(onClick = { /*TODO*/ }) {
            MyIcon(
                painter = painterResource(id = R.drawable.ic_timed_task),
                contentDescription = stringResource(id = R.string.text_switch_timed_task_scheduler)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.text_switch_timed_task_scheduler))
        }
    }
}

// 使用@OptIn注解标记这个Composable函数使用了实验性的权限API
@OptIn(ExperimentalPermissionsApi::class)
// 定义一个Composable函数NewDirectory，用于创建新目录
@Composable
private fun NewDirectory(
    context: Context, // Android的Context对象，用于访问应用的资源和组件
    scriptListFragment: ScriptListFragment, // 一个Fragment实例，用于操作脚本列表
    onDismissRequest: () -> Unit // 一个无参数无返回值的lambda表达式，用作函数参数，通常在需要关闭或取消当前操作时调用
) {
    // rememberExternalStoragePermissionsState函数用于记住外部存储权限的状态
    val permission = rememberExternalStoragePermissionsState {
        // lambda表达式，根据权限状态(true为已授权)执行对应的操作
        if (it) // 如果已经获取到了外部存储的权限
            getScriptOperations(
                context,
                scriptListFragment.explorerView // 使用scriptListFragment引用中的explorerView作为参数
            ).newDirectory() // 调用newDirectory函数创建新目录
        else // 如果没有获得外部存储的权限
            showExternalStoragePermissionToast(context) // 显示一个Toast提示需要外部存储权限
    }
    // DropdownMenuItem是一个下拉菜单项，在用户点击时执行lambda表达式中的操作
    DropdownMenuItem(onClick = {
        onDismissRequest() // 调用onDismissRequest来处理点击事件的逻辑，比如关闭当前弹出的窗口或对话框
        permission.launchMultiplePermissionRequest() // 请求外部存储权限
    }) {
        MyIcon(
            painter = painterResource(id = R.drawable.ic_floating_action_menu_dir), // 使用painterResource加载图片资源
            contentDescription = null // 图标的内容描述，这里为null
        )
        Spacer(modifier = Modifier.width(8.dp)) // 使用Spacer插入8.dp宽度的空间
        Text(text = stringResource(id = R.string.text_directory)) // 显示文本，文本资源来自R.string.text_directory
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NewFile(
    context: Context,
    scriptListFragment: ScriptListFragment,
    onDismissRequest: () -> Unit
) {
    val permission = rememberExternalStoragePermissionsState {
        if (it) getScriptOperations(
            context,
            scriptListFragment.explorerView
        ).newFile()
        else showExternalStoragePermissionToast(context)
    }
    DropdownMenuItem(onClick = {
        onDismissRequest()
        permission.launchMultiplePermissionRequest()
    }) {
        MyIcon(
            painter = painterResource(id = R.drawable.ic_floating_action_menu_file),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(id = R.string.text_file))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ImportFile(
    context: Context,
    scriptListFragment: ScriptListFragment,
    onDismissRequest: () -> Unit
) {
    val permission = rememberExternalStoragePermissionsState {
        if (it) getScriptOperations(
            context,
            scriptListFragment.explorerView
        ).importFile()
        else showExternalStoragePermissionToast(context)
    }
    DropdownMenuItem(onClick = {
        onDismissRequest()
        permission.launchMultiplePermissionRequest()
    }) {
        MyIcon(
            painter = painterResource(id = R.drawable.ic_floating_action_menu_open),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(id = R.string.text_import))
    }
}

@Composable
private fun NewProject(
    context: Context,
    scriptListFragment: ScriptListFragment,
    onDismissRequest: () -> Unit
) {
    DropdownMenuItem(onClick = {
        onDismissRequest()
        context.launchActivity<ProjectConfigActivity> {
            putExtra(
                ProjectConfigActivity.EXTRA_PARENT_DIRECTORY,
                scriptListFragment.explorerView.currentPage?.path
            )
            putExtra(ProjectConfigActivity.EXTRA_NEW_PROJECT, true)
        }
    }) {
        MyIcon(
            painter = painterResource(id = R.drawable.ic_project2),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(id = R.string.text_project))
    }
}

private fun getScriptOperations(
    context: Context,
    explorerView: ExplorerViewKt
): ScriptOperations {
    return ScriptOperations(
        context,
        explorerView,
        explorerView.currentPage
    )
}