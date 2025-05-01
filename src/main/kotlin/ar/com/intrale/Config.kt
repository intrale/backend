package ar.com.intrale

open class Config(
    val businesses: Set<String>,
    val region: String,
    val awsCognitoUserPoolId: String,
    val awsCognitoClientId: String)
