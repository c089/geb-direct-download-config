package helpers.download

import spock.lang.Specification
import spock.lang.Unroll
import org.codehaus.groovy.runtime.ComposedClosure

@Unroll
class DownloadConfigInjectorSpec extends Specification {
    public static final Map MAP = [:]
    public static final String STRING = 'http://example.com/'
    public static final Closure CLOSURE = {}
    public static final String INJECT = 'inject'
    public static final String COMPOSE = 'compose'
    public static final String DO_NOT_INJECT_OR_COMPOSE = 'neither'

    void 'it should inject the default config closure when no closure given'() {
        given:
        def injector = new DownloadConfigInjector(CLOSURE)

        when:
        def adaptedArgs = injector.adaptArguments('downloadBytes', [] as Object[])

        then:
        adaptedArgs == [CLOSURE] as Object[]
    }

    void 'it should compose the default with the provided closure when appropriate'() {
        given:
        def defaultClosure = {->}
        def userConfigClosure = {->}
        def injector = new DownloadConfigInjector(defaultClosure)

        when:
        def adaptedArgs = injector.adaptArguments('downloadBytes', [userConfigClosure] as Object[])

        then: 'the given closure has been replaced by a new one'
        adaptedArgs.length == 1
        adaptedArgs[0] instanceof Closure
        adaptedArgs[0] != defaultClosure
        adaptedArgs[0] != userConfigClosure
    }

    void 'it should not try to inject anything when calling the download method'() {
        given:
        def injector = new DownloadConfigInjector(CLOSURE)

        when:
        def adaptedArgs = injector.adaptArguments('download', [] as Object[])

        then: 'no closure is inject'
        adaptedArgs.length == 0
    }

    void 'it should compose or inject configuration closures where appropiate'() {
        given:
        def injector = new DownloadConfigInjector(CLOSURE)

        expect:
        injector.shouldInject(methodName, args) == (injectOrCompose == INJECT)
        injector.shouldCompose(methodName, args) == (injectOrCompose == COMPOSE)

        where:
        methodName      | args              | injectOrCompose
        'download'      | [MAP]             | DO_NOT_INJECT_OR_COMPOSE
        'download'      | [STRING]          | DO_NOT_INJECT_OR_COMPOSE
        'download'      | [null]            | DO_NOT_INJECT_OR_COMPOSE

        'downloadBytes' | [MAP, CLOSURE]    | COMPOSE
        'downloadBytes' | [MAP, null]       | INJECT
        'downloadBytes' | [MAP]             | INJECT

        'downloadBytes' | [CLOSURE]         | COMPOSE
        'downloadBytes' | [null]            | INJECT
        'downloadBytes' | []                | INJECT

        'downloadBytes' | [STRING, CLOSURE] | COMPOSE
        'downloadBytes' | [STRING, null]    | INJECT
        'downloadBytes' | [STRING]          | INJECT
    }

    void 'it should compose the closures so that the default config is called before the method config closure'() {
        given: 'a default and a userConfig closure'
        def defaultClosure = Mock(Closure)
        def userConfigClosure = Mock(Closure)

        when: 'we call the composed closure'
        def injector = new DownloadConfigInjector(defaultClosure)
        injector.compose(userConfigClosure).call(STRING)

        then: 'the default closure is the first in the composed closure'
        1 * defaultClosure.call(STRING)

        then: 'the user config closure is called last'
        1 * userConfigClosure.call(STRING)
    }

    void 'the arguments are passed to both composed closures'() {
        given:
        def spy = Mock(Closure)
        def defaultClosure = { con -> spy(con) }
        def userClosure = { con -> spy(con) }
        def arg = 'theArgumentPassedToTheComposedClosure'

        when:
        def composed = new DownloadConfigInjector(defaultClosure).compose(userClosure)
        composed.call(arg)

        then:
        2 * spy.call(arg)
    }

}
