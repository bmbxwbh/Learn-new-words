
package com.wordflow.di

import com.wordflow.data.network.DictionaryApi
import com.wordflow.data.network.DictionaryService
import com.wordflow.data.repository.ImportRepository
import com.wordflow.data.repository.WordRepository
import com.wordflow.domain.import.ProcessOcrResultUseCase
import com.wordflow.domain.study.GetNextWordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDictionaryApi(retrofit: Retrofit): DictionaryApi {
        return retrofit.create(DictionaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDictionaryService(api: DictionaryApi): DictionaryService {
        return DictionaryService(api)
    }

    // Provide Use Cases
    @Provides
    @Singleton
    fun provideGetNextWordUseCase(wordRepository: WordRepository): GetNextWordUseCase {
        return GetNextWordUseCase(wordRepository)
    }

    @Provides
    @Singleton
    fun provideProcessOcrResultUseCase(
        importRepository: ImportRepository,
        dictionaryService: DictionaryService
    ): ProcessOcrResultUseCase {
        return ProcessOcrResultUseCase(importRepository, dictionaryService)
    }
}

