package com.jnape.palatable.lambda.lens;

import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.Functor;

import java.util.function.Function;

import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.lens.functions.View.view;

/**
 * Isomorphism between <code>S &lt;-&gt; A</code> and <code>B &lt;-&gt; T</code>, viewed in the context of a
 * <code>{@link Lens}&lt;S,T,A,B&gt;</code> that can be reversed to produce a
 * <code>{@link Lens}&lt;B,A,T,S&gt;</code>. For any <code>{@link Iso}&lt;S,T,A,B&gt;</code>, the properties
 * of the lens laws hold as if they were applied individually to both the forward and reverse lenses.
 *
 * @param <S> the type of the "larger" value for reading in the forward lens
 * @param <T> the type of the "larger" value for putting in the forward lens
 * @param <A> the type of the "smaller" value that is read in the forward lens
 * @param <B> the type of the "smaller" update value in the forward lens
 * @see Lens
 */
public interface Iso<S, T, A, B> extends Lens<S, T, A, B> {

    /**
     * Flip this iso around, yielding the inverse <code>{@link Iso}&lt;B,A,T,S&gt;</code>
     *
     * @return the reversed iso
     */
    Iso<B, A, T, S> reverse();

    @Override
    default <U> Iso<S, U, A, B> fmap(Function<? super T, ? extends U> fn) {
        return iso(view(this), view(reverse()).fmap(fn));
    }

    @Override
    default <U> Iso<S, U, A, B> pure(U u) {
        return fmap(constantly(u));
    }

    @Override
    default <U> Iso<S, T, A, B> discardR(Applicative<U, Lens<S, ?, A, B>> appB) {
        return iso(view(Lens.super.discardR(appB)), view(reverse()));
    }

    @Override
    default <R> Iso<R, T, A, B> diMapL(Function<? super R, ? extends S> fn) {
        return contraMap(fn);
    }

    @Override
    default <U> Iso<S, U, A, B> diMapR(Function<? super T, ? extends U> fn) {
        return fmap(fn);
    }

    @Override
    default <R, U> Iso<R, U, A, B> diMap(Function<? super R, ? extends S> lFn,
                                         Function<? super T, ? extends U> rFn) {
        return this.<R>diMapL(lFn).diMapR(rFn);
    }

    @Override
    default <R> Iso<R, T, A, B> contraMap(Function<? super R, ? extends S> fn) {
        return mapS(fn);
    }

    @Override
    default <R> Iso<R, T, A, B> mapS(Function<? super R, ? extends S> fn) {
        return iso(view(this).contraMap(fn), view(reverse()));
    }

    @Override
    default <U> Iso<S, U, A, B> mapT(Function<? super T, ? extends U> fn) {
        return fmap(fn);
    }

    @Override
    default <C> Iso<S, T, C, B> mapA(Function<? super A, ? extends C> fn) {
        return iso(view(this).fmap(fn), view(reverse()));
    }

    @Override
    default <Z> Iso<S, T, A, Z> mapB(Function<? super Z, ? extends B> fn) {
        return iso(view(this), view(reverse()).contraMap(fn));
    }

    static <S, T, A, B> Iso<S, T, A, B> iso(Function<? super S, ? extends A> f, Function<? super B, ? extends T> g) {
        return new Iso<S, T, A, B>() {
            @Override
            public Iso<B, A, T, S> reverse() {
                return iso(g, f);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <F extends Functor, FT extends Functor<T, F>, FB extends Functor<B, F>> FT apply(
                    Function<? super A, ? extends FB> fn, S s) {
                return (FT) fn.apply(f.apply(s)).<T>fmap(g);
            }
        };
    }

    static <S, A> Iso.Simple<S, A> simpleIso(Function<? super S, ? extends A> f,
                                             Function<? super A, ? extends S> g) {
        return new Iso.Simple<S, A>() {
            @Override
            public Simple<A, S> reverse() {
                return simpleIso(g, f);
            }

            @Override
            public <F extends Functor, FT extends Functor<S, F>, FB extends Functor<A, F>> FT apply(
                    Function<? super A, ? extends FB> fn, S s) {
                return Iso.<S, S, A, A>iso(f, g).apply(fn, s);
            }
        };
    }

    interface Simple<S, A> extends Iso<S, S, A, A> {

        @Override
        Iso.Simple<A, S> reverse();

    }
}
