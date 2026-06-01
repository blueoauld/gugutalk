package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatMessageMedia
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageMediaRepository : JpaRepository<ChatMessageMedia, Long>