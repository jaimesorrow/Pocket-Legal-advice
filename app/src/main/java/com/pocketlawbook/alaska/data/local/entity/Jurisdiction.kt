package com.pocketlawbook.alaska.data.local.entity

/**
 * The bodies of law this app covers. Alaska's Pocket Lawbook covers Alaska state
 * law and federal law and nothing else, so every piece of verified content is
 * tagged with exactly one of these and the UI always discloses which.
 */
enum class Jurisdiction {
    ALASKA,
    FEDERAL
}
