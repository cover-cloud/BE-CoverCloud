package com.covercloud.cover.global.security

//
//object SecurityUtil {
//    fun currentUserId(): Long{
////        val authentication = SecurityContextHolder.getContext().authentication?:
//        throw IllegalStateException("User not authenticated")
//
//        val principal = authentication.principal
//
//        return when(principal){
//            is Long -> principal
//            is Int -> principal.toLong()
//            is String -> principal.toLongOrNull() ?: throw IllegalArgumentException("User not authenticated")
//            else -> throw IllegalArgumentException("Unsupported principal type: ${principal::class}")
//        }
//    }
//}