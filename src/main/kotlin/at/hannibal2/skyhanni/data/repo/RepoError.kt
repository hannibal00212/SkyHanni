package at.hannibal2.skyhanni.data.repo

class RepoError : Error {
    constructor(errorMessage: String) : super(errorMessage)
    constructor(errorMessage: String, cause: Throwable) : super(errorMessage, cause)
}
