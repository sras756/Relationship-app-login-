package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserProfileRepository

class DatingApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var userProfileRepository: UserProfileRepository
        private set

    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        chatRepository = ChatRepository(database.chatDao(), this)
    }

    companion object {
        lateinit var instance: DatingApp
            private set
    }
}
