package com.google.android.gms.samples.vision.face.facetracker.api.api.interf

import com.example.test.api.response.ApiResponse
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignData
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFaceLink {

    @Headers(
        "OrgId: 0B193586CD103FA7",
        "ConsoleName: LabConsole1",
        "CameraName: Camera19"
    )
    @POST("/api/Face/SignIn")
    fun sign(@Header("SignType") signType:String, @Body faceImg: RequestBody):Observable<SignData>

    // TODO: 做到此 API增加
}