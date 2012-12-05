package helpers.download

import javax.net.ssl.HttpsURLConnection

class AcceptSelfSignedCertificatesConnectionConfig {
    static final Closure CONFIG = { HttpURLConnection connection ->
        if (connection instanceof HttpsURLConnection) {
            def helper = new SelfSignedCertificateHelper(KEYSTORE_FILE_NAME, KEYSTORE_FILE_PASSWORD)
            helper.configureHttpsURLConnectionSSLFactory(connection as HttpsURLConnection)
        }
    }

    private static final String KEYSTORE_FILE_NAME = '/ofbizssl.jks'
    private static final String KEYSTORE_FILE_PASSWORD = 'changeit'
}
