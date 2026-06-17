package com.example.hipenter

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object Login : NavKey
@Serializable data object ComposePost : NavKey
@Serializable data object Profile : NavKey
