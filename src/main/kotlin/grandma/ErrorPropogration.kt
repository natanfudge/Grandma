package grandma

//sealed class GenericResult<A, B> {
//    abstract fun <C> map(mapping: (A) -> C): GenericResult<C, B>
//    abstract fun <C> flatMap(mapping: (A) -> GenericResult<C, B>): GenericResult<C, B>
//    abstract fun <C> mapFailure(mapping: (B) -> C): GenericResult<A, C>
//    abstract fun <C> flatMapFailure(mapping: (B) -> GenericResult<A, C>): GenericResult<A, C>
//    abstract fun orElse(other: A): A
//    abstract fun orElse(function: (B) -> A): A
//}
//
//data class GenericSuccess<A, B>(val value: A) : GenericResult<A, B>() {
//    override fun <C> map(mapping: (A) -> C): GenericResult<C, B> = GenericSuccess(mapping(value))
//    override fun <C> flatMap(mapping: (A) -> GenericResult<C, B>): GenericResult<C, B> = mapping(value)
//    override fun <C> mapFailure(mapping: (B) -> C): GenericResult<A, C> = GenericSuccess(value)
//    override fun <C> flatMapFailure(mapping: (B) -> GenericResult<A, C>): GenericResult<A, C> = GenericSuccess(value)
//    override fun orElse(other: A): A = value
//    override fun orElse(function: (B) -> A): A = value
//}
//
//data class GenericError<A, B>(val value: B) : GenericResult<A, B>() {
//    override fun <C> map(mapping: (A) -> C): GenericResult<C, B> = GenericError(value)
//    override fun <C> flatMap(mapping: (A) -> GenericResult<C, B>): GenericResult<C, B> = GenericError(value)
//    override fun <C> mapFailure(mapping: (B) -> C): GenericResult<A, C> = GenericError(mapping(value))
//    override fun <C> flatMapFailure(mapping: (B) -> GenericResult<A, C>): GenericResult<A, C> = mapping(value)
//    override fun orElse(other: A): A = other
//    override fun orElse(function: (B) -> A): A = function(value)
//}

sealed class StringResult<A> {
    abstract fun <C> map(mapping: (A) -> C): StringResult<C>
    abstract fun <C> flatMap(mapping: (A) -> StringResult<C>): StringResult<C>
//    abstract fun <C> mapFailure(mapping: (String) -> C): GenericResult<A, C>
//    abstract fun <C> flatMapFailure(mapping: (String) -> GenericResult<A, C>): GenericResult<A, C>
    abstract fun orElse(other: A): A
    abstract fun orElse(function: (String) -> A): A
}

data class StringSuccess<A>(val value: A) : StringResult<A>() {
    override fun <C> map(mapping: (A) -> C): StringResult<C> = StringSuccess(mapping(value))
    override fun <C> flatMap(mapping: (A) -> StringResult<C>): StringResult<C> = mapping(value)
//    override fun <C> mapFailure(mapping: (String) -> C): GenericResult<A, C> = GenericSuccess(value)
//    override fun <C> flatMapFailure(mapping: (String) -> GenericResult<A, C>): GenericResult<A, C> = GenericSuccess(value)
    override fun orElse(other: A): A = value
    override fun orElse(function: (String) -> A): A = value
}

data class StringError<A>(val value: String) : StringResult<A>() {
    override fun <C> map(mapping: (A) -> C): StringResult<C> = StringError(value)
    override fun <C> flatMap(mapping: (A) -> StringResult<C>): StringResult<C> = StringError(value)
//    override fun <C> mapFailure(mapping: (B) -> C): GenericResult<A, C> = GenericError(mapping(value))
//    override fun <C> flatMapFailure(mapping: (B) -> GenericResult<A, C>): GenericResult<A, C> = mapping(value)
    override fun orElse(other: A): A = other
    override fun orElse(function: (String) -> A): A = function(value)
}

