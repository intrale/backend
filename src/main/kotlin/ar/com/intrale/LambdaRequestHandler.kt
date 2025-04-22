package ar.com.intrale

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.google.gson.Gson

import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.slf4j.Logger
import java.lang.NullPointerException
import kotlin.getValue
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

abstract class LambdaRequestHandler  : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // The request limit most be assigned on Api Gateway
    /*@OptIn(ExperimentalEncodingApi::class)
    override fun handleRequest(requestEvent: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent  = APIGatewayProxyResponseEvent().apply {
        handle(appModule, requestEvent, context)
    }*/


    // The request limit most be assigned on Api Gateway
    @OptIn(ExperimentalEncodingApi::class)
    fun handle(appModule: DI.Module, requestEvent: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent  = APIGatewayProxyResponseEvent().apply {
        try {

            val di = DI {
                import(appModule)
            }

            val logger: Logger by di.instance()

            if (requestEvent != null) {
                var httpMehtod = requestEvent.httpMethod

                if (httpMehtod == "OPTIONS") {
                    val map = mutableMapOf<String, String>()
                    map["Access-Control-Allow-Origin"] = "*"
                    map["Access-Control-Allow-Methods"] = "GET, OPTIONS, HEAD, PUT, POST"
                    map["Access-Control-Allow-Headers"] =
                        "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename"
                    headers = map

                    statusCode = 200
                }

                if (httpMehtod == "POST") {
                    var functionName = requestEvent.pathParameters["function"]
                    val businessName = requestEvent.pathParameters["business"]

                    logger.info("Function name is $functionName")
                    logger.info("Business name is $businessName")

                    var functionResponse : Response

                    if (businessName == null) {
                        logger.info("Business name is null")
                        functionResponse = RequestValidationException("No business defined on path")
                    } else {
                        val config by di.instance<Config>(tag = "config")
                        if (!config.businesses.contains(businessName)){
                            logger.info("Business not avaiable with name $businessName")
                            functionResponse = ExceptionResponse("Business not avaiable with name $businessName")
                        } else {
                            if (functionName == null) {
                                logger.info("No function defined on headers")
                                functionResponse = RequestValidationException("No function defined on path")
                            } else {
                                try {
                                    logger.info("Injecting Function $functionName")
                                    val function by di.instance<Function>(tag = functionName)
                                    runBlocking {
                                        var requestBody:String = ""
                                        try {
                                            requestBody = String(Base64.Default.decode(requestEvent.body));
                                            logger.info("Request body is $requestBody")
                                            functionResponse = function.execute(businessName, functionName, requestEvent.headers, requestBody)
                                        } catch (e: NullPointerException){
                                            logger.info("NullPointerException is thrown")
                                            if (e.message.toString().contains("\"textBody\" is null")){
                                                logger.info("Request body not found")
                                                functionResponse = RequestValidationException("Request body not found")
                                            } else {
                                                logger.info(e.message)
                                                functionResponse = ExceptionResponse(e.message.toString())
                                            }
                                        }

                                        body = Gson().toJson(functionResponse)
                                        logger.info("Returning body is $body")
                                        statusCode = functionResponse.statusCode?.value
                                    }
                                } catch (e: DI.NotFoundException) {
                                    logger.info("No function with name $functionName found")
                                    functionResponse = ExceptionResponse("No function with name $functionName found")
                                }
                            }
                        }
                    }

                    body = Gson().toJson(functionResponse)
                    logger.info("Finally returning body is $body")
                    statusCode = functionResponse.statusCode?.value

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
            body = "Internal Server Error"
        }
    }
}
