package com.example.nexusbank.core.network.util

import okhttp3.CertificatePinner

/**
 * SSL certificate pinning for production security.
 * Pins SHA-256 hashes of the server's certificate public key.
 *
 * In production, replace placeholder pins with actual certificate hashes.
 * Generate pins with: openssl s_client -connect api.nexusbank.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
 */
object SSLPinningConfig {

    fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            // Primary pin (current certificate)
            .add(
                "api.nexusbank.com",
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" // TODO: Replace with real pin
            )
            // Backup pin (next certificate for rotation)
            .add(
                "api.nexusbank.com",
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=" // TODO: Replace with real pin
            )
            .build()
    }
}
