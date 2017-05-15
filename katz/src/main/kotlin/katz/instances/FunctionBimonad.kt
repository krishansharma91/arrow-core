package katz

// We don't we want an inherited class to avoid equivalence issues, so a simple HK wrapper will do
data class Function0<out A>(val f: () -> A) : HK<Function0.F, A> {

    class F private constructor()

    companion object : Bimonad<Function0.F>, GlobalInstance<Bimonad<Function0.F>>() {

        override fun <A, B> flatMap(fa: HK<Function0.F, A>, f: (A) -> HK<Function0.F, B>): HK<Function0.F, B> =
                f(fa.ev().invoke())

        override fun <A, B> coflatMap(fa: HK<Function0.F, A>, f: (HK<Function0.F, A>) -> B): HK<Function0.F, B> =
                Function0 { f(fa) }

        override fun <A> pure(a: A): HK<Function0.F, A> =
                Function0 { a }

        override fun <A> extract(fa: HK<Function0.F, A>): A =
                fa.ev().invoke()

        override fun <A, B> tailRecM(a: A, f: (A) -> HK<F, Either<A, B>>): HK<F, B> =
            f(a).ev().invoke().let { either ->
                when (either) {
                    is Either.Left -> tailRecM(either.a, f)
                    is Either.Right -> Function0<B> { either.b }
                }
            }
    }
}

fun <A> HK<Function0.F, A>.ev() = (this as Function0<A>).f