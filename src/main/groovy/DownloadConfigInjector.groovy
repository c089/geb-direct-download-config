package helpers.download

class DownloadConfigInjector {

    Closure defaultConfigClosure

    private static final List<String> GEB_DOWNLOAD_SUPPORT_METHODS = [
        'download', 'downloadBytes', 'downloadContent', 'downloadStream', 'downloadText'
    ].asImmutable()

    DownloadConfigInjector(Closure defaultConfigClosure) {
        this.defaultConfigClosure = defaultConfigClosure
    }

    Object[] adaptArguments(String name, Object[] args) {
        Object[] adaptedArgs = args

        if (shouldInject(name, args)) {
            adaptedArgs = injectDdefaultConfigClosure(args)
        }

        else if (shouldCompose(name, args)) {
            adaptedArgs = replaceCustomConfigClosureWithComposedClosureOfDefaultAndCustom(args)
        }

        return adaptedArgs
    }

    private boolean shouldInject(String name, args) {
        // 'download' itself is the only method which does not have the config closure parameter
        return itIsADownloadMethod(name) && itCanBeConfiguredWithAClosure(name) && itDoesNotAlreadyHaveAClosure(args)
    }

    private boolean shouldCompose(String name, args) {
        return itCanBeConfiguredWithAClosure(name) && oneOfTheArgumentsIsAClosure(args)
    }

    private boolean itDoesNotAlreadyHaveAClosure(args) {
        return !oneOfTheArgumentsIsAClosure(args)
    }

    private boolean oneOfTheArgumentsIsAClosure(args) {
        return args.any { it instanceof Closure }
    }

    private boolean itCanBeConfiguredWithAClosure(String name) {
        itIsOneOfTheDownloadMethodsWhichDoNotTakeAClosure(name) && itIsADownloadMethod(name)
    }

    private boolean itIsOneOfTheDownloadMethodsWhichDoNotTakeAClosure(String name) {
        // the 'download' methods itself is the only method which does not have the config closure parameter
        return name != 'download'
    }

    private boolean itIsADownloadMethod(String name) {
        return GEB_DOWNLOAD_SUPPORT_METHODS.contains(name)
    }

    private Object[] injectDdefaultConfigClosure(Object[] args) {
        return Arrays.asList(args) + defaultConfigClosure
    }

    private Object[] replaceCustomConfigClosureWithComposedClosureOfDefaultAndCustom(Object[] args) {
        List argsList = Arrays.asList(args)
        Object closure = argsList.find { it instanceof Closure }
        int indexOfClosure = argsList.indexOf(closure)
        args[indexOfClosure] = compose(closure)
        return args
    }

    private Closure compose(Closure override) {
        return { args ->
            defaultConfigClosure.call(args)
            override.call(args)
        }
    }
}
