package com.example.mytuition.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytuition.core.designsystem.MyTuitionColors
import com.example.mytuition.core.designsystem.components.BottomNavItem
import com.example.mytuition.core.designsystem.components.FloatingBottomNav
import com.example.mytuition.core.di.AppContainer
import com.example.mytuition.feature.calendar.CalendarScreen
import com.example.mytuition.feature.library.LibraryScreen
import com.example.mytuition.feature.profile.ProfileScreen
import com.example.mytuition.feature.subjects.SubjectsScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToClassDetail: (String) -> Unit = {},
    onNavigateToHomeworkDetail: (String) -> Unit = {},
    onNavigateToSubjectDetail: (String) -> Unit = {}
) {
    var activeIndex by remember { mutableIntStateOf(0) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            AppContainer.homeRepository,
            AppContainer.authRepository
        )
    )

    val navItems = remember {
        listOf(
            BottomNavItem("Home", Icons.Rounded.Home),
            BottomNavItem("Calendar", Icons.Rounded.CalendarToday),
            BottomNavItem("Resources", Icons.Rounded.MenuBook),
            BottomNavItem("Notices", Icons.Rounded.Notifications),
            BottomNavItem("Profile", Icons.Rounded.Person)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyTuitionColors.ScreenBackground)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeIndex) {
                0 -> HomeScreen(
                    onLogout = onLogout,
                    onNavigateToClassDetail = onNavigateToClassDetail,
                    onNavigateToHomeworkDetail = onNavigateToHomeworkDetail,
                    onNavigateToSubjectDetail = onNavigateToSubjectDetail,
                    viewModel = homeViewModel
                )
                1 -> CalendarScreen()
                2 -> LibraryScreen()
                3 -> AnnouncementsScreen(viewModel = homeViewModel)
                4 -> ProfileScreen(onLogout = onLogout)
            }
        }

        FloatingBottomNav(
            items = navItems,
            activeIndex = activeIndex,
            onItemSelect = { activeIndex = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}