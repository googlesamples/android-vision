package com.example.test.api.response

import retrofit2.Response
import java.io.IOException

class ApiResponse<T> {
    //    {"body":"identify","isSuccess":false,"message":"MemberIsExits","code":"807","response":807,"exception":null}
    var code: Int
    var body: T? = null
    var errorMessage: String? = ""
    var isSuccess: Boolean = false
    var message: String = ""
    var response: Int = -1
    var exception: String = ""

    constructor(error: Throwable) {
        code = 500
        errorMessage = error.message
    }

    constructor(response: Response<T>) {
        code = response.code()
        if (response.isSuccessful) {
            body = response.body()
            errorMessage = null
        } else {
            var message: String? = null
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody()!!.string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (message == null || message.trim { it <= ' ' }.length == 0) {
                message = response.message()
            }
            errorMessage = message
            body = null
        }
    }
}