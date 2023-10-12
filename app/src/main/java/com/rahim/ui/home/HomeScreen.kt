package com.rahim.ui.home

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rahim.R
import com.rahim.data.alarm.AlarmManagement
import com.rahim.data.modle.Rotin.Routine
import com.rahim.ui.dialog.DialogAddRoutine
import com.rahim.ui.dialog.ErrorDialog
import com.rahim.ui.theme.Purple
import com.rahim.ui.theme.PurpleGrey
import com.rahim.ui.theme.YadinoTheme
import com.rahim.utils.base.view.ItemRoutine
import com.rahim.utils.base.view.ProcessRoutineAdded
import com.rahim.utils.base.view.ShowStatusBar
import com.rahim.utils.base.view.ShowToastShort
import com.rahim.utils.base.view.TopBarCenterAlign
import com.rahim.utils.resours.Resource

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onClickAdd: Boolean,
    isOpenDialog: (Boolean) -> Unit,
) {
    ShowStatusBar(true)
    val context = LocalContext.current
    val alarmManagement = AlarmManagement()
    val currentYer = viewModel.getCurrentTime()[0]
    val currentMonth = viewModel.getCurrentTime()[1]
    val currentDay = viewModel.getCurrentTime()[2]
    val searchItems = ArrayList<Routine>()

    val routineDeleteDialog = rememberSaveable { mutableStateOf<Routine?>(null) }
    val routineUpdateDialog = rememberSaveable { mutableStateOf<Routine?>(null) }
    val routineForAdd = rememberSaveable { mutableStateOf<Routine?>(null) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var clickSearch by rememberSaveable { mutableStateOf(false) }

    viewModel.getCurrentRoutines()
    val routines by viewModel.flowRoutines
        .collectAsStateWithLifecycle(initialValue = Resource.Success(emptyList()))

    val idAlarms by viewModel.idAlarms.collectAsStateWithLifecycle()
    val addRoutine by viewModel.addRoutine.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBarCenterAlign(
                modifier, stringResource(id = R.string.hello_friend), onClickSearch = {
                    clickSearch = !clickSearch
                }
            )
        }, containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (routines) {
            is Resource.Loading -> {}
            is Resource.Success -> {
                routines.data?.let {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = if (it.isEmpty()) Arrangement.Center else Arrangement.Top,
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        ShowSearchBar(it, clickSearch, searchText = searchText) { search ->
                            searchText = search
                            if (search.isNotEmpty()) {
                                searchItems.addAll(it.filter {
                                    it.name == searchText
                                })
                            } else {
                                searchItems.clear()
                            }
                        }
                        if (it.isEmpty()) {
                            EmptyHome()
                        } else {
                            if (searchItems.isEmpty() && searchText.isNotEmpty()) {
                                EmptyHome(
                                    Modifier.padding(top = 70.dp),
                                    messageEmpty = R.string.search_empty_routine
                                )
                            } else {
                                ItemsHome(currentYer, currentMonth, currentDay,
                                    if (searchText.isEmpty()) it else searchItems,
                                    { checkedRoutine ->
                                        viewModel.updateRoutine(checkedRoutine)
                                        alarmManagement.setAlarm(
                                            context,
                                            checkedRoutine,
                                            if (idAlarms == null) checkedRoutine.id?.toLong() else checkedRoutine.idAlarm
                                        )
                                    },
                                    { routineUpdate ->
                                        if (routineUpdate.isSample)
                                            viewModel.showSampleRoutine(true)

                                        routineUpdateDialog.value = routineUpdate
                                    },
                                    { deleteRoutine ->
                                        if (deleteRoutine.isSample)
                                            viewModel.showSampleRoutine(true)

                                        routineDeleteDialog.value = deleteRoutine
                                    })
                            }
                        }
                    }
                }
            }

            is Resource.Error -> {

            }
        }

    }
    routineDeleteDialog.value?.let { routineFromDialog ->
        ErrorDialog(
            isOpen = routineDeleteDialog.value != null,
            isClickOk = {
                if (it) {
                    routineDeleteDialog.value?.let {
                        viewModel.deleteRoutine(it)
                        alarmManagement.cancelAlarm(
                            context,
                            if (it.idAlarm == null) it.id?.toLong() else it.idAlarm
                        )
                    }
                }
                routineDeleteDialog.value = null
            },
            message = stringResource(id = R.string.can_you_delete),
            okMessage = stringResource(
                id = R.string.ok
            )
        )
    }
    DialogAddRoutine(
        isOpen = onClickAdd || routineUpdateDialog.value != null,
        isShowDay = false,
        dayChecked = "",
        openDialog = {
            isOpenDialog(it)
            routineUpdateDialog.value = null
            routineForAdd.value = null
        },
        routineUpdate = routineUpdateDialog.value,
        routine = { routine ->
            if (routineUpdateDialog.value != null) {
                viewModel.updateRoutine(routine)
                alarmManagement.setAlarm(
                    context,
                    routine,
                    if (idAlarms == null) routine.id?.toLong() else routine.idAlarm
                )
                routineUpdateDialog.value = null
            } else {
                routineForAdd.value = routine
                viewModel.addRoutine(alarmManagement.setAlarm(context, routine, idAlarms))
            }
        },
        currentNumberDay = currentDay,
        currentNumberMonth = currentMonth,
        currentNumberYer = currentYer
    )
    if (routineForAdd.value != null)
        ProcessRoutineAdded(addRoutine, context) {
            if (!it)
                isOpenDialog(false)
            routineForAdd.value = null
        }
}

@Composable
fun ShowSearchBar(
    routines: List<Routine>,
    clickSearch: Boolean,
    searchText: String,
    searchValueText: (String) -> Unit
) {
    if (routines.isNotEmpty()) {
        AnimatedVisibility(visible = clickSearch) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.search_hint)) },
                    value = searchText,
                    onValueChange = { searchValueText(it) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = PurpleGrey,
                        focusedIndicatorColor = Purple,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyHome(
    modifier: Modifier = Modifier,
    @StringRes messageEmpty: Int = R.string.not_work_for_day
) {
    Image(
        modifier = modifier
            .sizeIn(minHeight = 320.dp)
            .fillMaxWidth(),
        painter = painterResource(id = R.drawable.empty_list_home),
        contentDescription = "empty list home"
    )
    Text(
        text = stringResource(id = messageEmpty),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ItemsHome(
    currentDay: Int,
    currentMonth: Int,
    currentYer: Int,
    routines: List<Routine>,
    checkedRoutine: (Routine) -> Unit,
    updateRoutine: (Routine) -> Unit,
    deleteRoutine: (Routine) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 28.dp, vertical = 25.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "$currentDay/$currentMonth/$currentYer", fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(id = R.string.list_work_day), fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(top = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        items(items = routines, itemContent = {
            ItemRoutine(routine = it, onChecked = {
                checkedRoutine(it)
            }, openDialogDelete = {
                deleteRoutine(it)
            }, openDialogEdit = {
                updateRoutine(it)
            })
        })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, device = Devices.PIXEL_4)
@Composable
fun HomeScreenPreview() {
    YadinoTheme {
        val viewModel = hiltViewModel<HomeViewModel>()
//        HomeScreen(viewModel = viewModel)
    }
}