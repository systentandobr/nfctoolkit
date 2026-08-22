package com.systentando.nfc.di

import android.content.Context
import android.nfc.NfcAdapter
import androidx.room.Room
import com.systentando.nfc.data.local.FormStateRepository
import com.systentando.nfc.data.local.OnboardingRepository
import com.systentando.nfc.data.local.TagDao
import com.systentando.nfc.data.local.TagDatabase
import com.systentando.nfc.data.local.TagRepository
import com.systentando.nfc.nfc.NFCManager
import com.systentando.nfc.nfc.NFCTagReader
import com.systentando.nfc.nfc.NFCTagWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNfcAdapter(@ApplicationContext context: Context): NfcAdapter? {
        return NfcAdapter.getDefaultAdapter(context)
    }

    @Provides
    @Singleton
    fun provideNfcManager(
        @ApplicationContext context: Context,
        nfcAdapter: NfcAdapter?
    ): NFCManager {
        return NFCManager(context, nfcAdapter)
    }

    @Provides
    @Singleton
    fun provideTagReader(): NFCTagReader = NFCTagReader()

    @Provides
    @Singleton
    fun provideTagWriter(): NFCTagWriter = NFCTagWriter()

    @Provides
    @Singleton
    fun provideTagDatabase(@ApplicationContext context: Context): TagDatabase {
        return Room.databaseBuilder(
            context,
            TagDatabase::class.java,
            "nfc_toolkit.db"
        ).build()
    }

    @Provides
    fun provideTagDao(database: TagDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideTagRepository(dao: TagDao): TagRepository = TagRepository(dao)

    @Provides
    @Singleton
    fun provideFormStateRepository(@ApplicationContext context: Context): FormStateRepository {
        return FormStateRepository(context)
    }

    @Provides
    @Singleton
    fun provideOnboardingRepository(@ApplicationContext context: Context): OnboardingRepository {
        return OnboardingRepository(context)
    }
}
