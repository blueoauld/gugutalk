package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, ChatRoomCustomRepository {

    fun findByMember1IdAndMember2Id(member1Id: Long, member2Id: Long): ChatRoom?

    fun findAllByMember1IdOrMember2Id(member1Id: Long, member2Id: Long): List<ChatRoom>
}