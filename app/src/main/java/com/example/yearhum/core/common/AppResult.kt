package com.example.yearhum.core.common

/** Errors are values: repositories return this instead of throwing into ViewModels. */
sealed interface AppError {
    data object Offline : AppError

    data object NotFound : AppError

    data object RateLimited : AppError

    data class Server(val code: Int) : AppError

    data class Unknown(val message: String?) : AppError
}

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}
