package com.sullivan.signearadmin.ui_login.di

import com.sullivan.common.ui_common.navigator.LoginNavigator
import com.sullivan.signearadmin.ui_login.navigator.LoginNavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class NavigatorModule {
    @Binds
    abstract fun provideLoginNavigator(
        navigator: LoginNavigatorImpl
    ): LoginNavigator
}