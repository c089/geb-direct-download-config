geb-direct-download-config
==========================

Helper class to globally alter the configuration of the download* methods
provided by Geb.

Usage
-----

The class provides a helper to inject a "default" configuration closure for the
HttpURLConnection that is used by the download* methods. This closure will be
executed before the actual download is started. It will also be executed before
any closure passed as an argument to these functions, so any defaults you set
can be override when neccessary.

To use this you have to adapt your base class to wrap all download* calls:

    class MyGebSpec extends GebSpec {

        def downloadConfigInjector = new DownloadConfigInjector({ HttpURLConnection connection ->
            // connection.set...
        })

        //...

        @Override
        Object methodMissing(String name, Object args) {
            return super.methodMissing(name, downloadConfigInjector.adaptArguments(name, args))
        }

    }


Example: Ignoring SSL certificate problems in tests
---------------------------------------------------

A useful example of this is to ignore self-signed SSL Certificates using the
SelfSignedCertificateHelper provided in this repo:


    class AcceptSelfSignedCertificatesConnectionConfig {
        static final Closure CONFIG = { HttpURLConnection connection ->
            if (connection instanceof HttpsURLConnection) {
                def helper = new SelfSignedCertificateHelper(KEYSTORE_FILE_NAME, KEYSTORE_FILE_PASSWORD)
                helper.configureHttpsURLConnectionSSLFactory(connection as HttpsURLConnection)
            }
        }

        private static final String KEYSTORE_FILE_NAME = '/mykeystore.jks'
        private static final String KEYSTORE_FILE_PASSWORD = 'changeit'
    }
