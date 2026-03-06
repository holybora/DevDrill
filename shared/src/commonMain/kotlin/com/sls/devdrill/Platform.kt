package com.sls.devdrill

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
