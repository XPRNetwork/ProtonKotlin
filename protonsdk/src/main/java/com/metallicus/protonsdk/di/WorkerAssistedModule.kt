package com.metallicus.protonsdk.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@Module(includes = [AssistedInject_WorkerAssistedModule::class])
@AssistedModule
interface WorkerAssistedModule