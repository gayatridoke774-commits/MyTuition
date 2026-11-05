package com.example.mytuition.core.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.mytuition.core.data.local.AppDatabase
import com.example.mytuition.core.data.local.TokenManager
import com.example.mytuition.core.data.network.PocketBaseApi
import com.example.mytuition.core.data.network.PocketBaseClient
import com.example.mytuition.core.data.repository.*
import com.example.mytuition.core.domain.repository.AuthRepository
import com.example.mytuition.core.domain.repository.FeeRepository
import com.example.mytuition.core.domain.repository.HomeworkRepository
import com.example.mytuition.core.domain.repository.LibraryRepository
import com.example.mytuition.core.domain.repository.SubjectRepository
import com.example.mytuition.core.domain.repository.TeacherRepository
import com.example.mytuition.core.domain.usecase.*

private val Context.dataStore by preferencesDataStore(name = "mytuition_prefs")

object AppContainer {

    private lateinit var appContext: Context
    lateinit var tokenManager: TokenManager
        private set
    private lateinit var database: AppDatabase
    lateinit var pocketBaseApi: PocketBaseApi
        private set

    lateinit var authRepository: AuthRepository
        private set
    lateinit var homeworkRepository: HomeworkRepository
        private set
    lateinit var subjectRepository: SubjectRepository
        private set
    lateinit var libraryRepository: LibraryRepository
        private set
    lateinit var homeRepository: PocketBaseHomeRepository
        private set
    lateinit var feeRepository: FeeRepository
        private set
    lateinit var profileRepository: ProfileRepository
        private set
    lateinit var teacherRepository: TeacherRepository
        private set

    lateinit var getHomeworkListUseCase: GetHomeworkListUseCase
        private set
    lateinit var getHomeworkDetailUseCase: GetHomeworkDetailUseCase
        private set
    lateinit var markHomeworkCompleteUseCase: MarkHomeworkCompleteUseCase
        private set
    lateinit var getSubjectsUseCase: GetSubjectsUseCase
        private set
    lateinit var getSubjectDetailUseCase: GetSubjectDetailUseCase
        private set
    lateinit var getLibraryResourcesUseCase: GetLibraryResourcesUseCase
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        tokenManager = TokenManager(appContext.dataStore)
        database = AppDatabase.getInstance(appContext)
        pocketBaseApi = PocketBaseClient.create(tokenManager)

        authRepository = PocketBaseAuthRepository(pocketBaseApi, tokenManager)
        homeworkRepository = PocketBaseHomeworkRepository(pocketBaseApi, tokenManager, database)
        subjectRepository = PocketBaseSubjectRepository(pocketBaseApi, database)
        libraryRepository = PocketBaseLibraryRepository(pocketBaseApi, database)
        homeRepository = PocketBaseHomeRepository(pocketBaseApi, tokenManager, database)
        feeRepository = PocketBaseFeeRepository(pocketBaseApi)
        profileRepository = PocketBaseProfileRepository(pocketBaseApi, tokenManager)
        teacherRepository = PocketBaseTeacherRepository(pocketBaseApi)

        getHomeworkListUseCase = GetHomeworkListUseCase(homeworkRepository)
        getHomeworkDetailUseCase = GetHomeworkDetailUseCase(homeworkRepository)
        markHomeworkCompleteUseCase = MarkHomeworkCompleteUseCase(homeworkRepository)
        getSubjectsUseCase = GetSubjectsUseCase(subjectRepository)
        getSubjectDetailUseCase = GetSubjectDetailUseCase(subjectRepository)
        getLibraryResourcesUseCase = GetLibraryResourcesUseCase(libraryRepository)
    }
}
