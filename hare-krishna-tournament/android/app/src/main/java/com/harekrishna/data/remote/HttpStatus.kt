package com.harekrishna.data.remote

// Wire status codes the app actually distinguishes on. Not exhaustive — add
// entries as new branches in mapError appear.
enum class HttpStatus(val code: Int) {
    BAD_REQUEST  (400),
    UNAUTHORIZED (401),
    FORBIDDEN    (403),
    NOT_FOUND    (404);
}
